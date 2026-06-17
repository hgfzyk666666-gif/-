package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.api.Candidate
import com.example.api.Content
import com.example.api.GenerateContentRequest
import com.example.api.GenerationConfig
import com.example.api.Part
import com.example.api.RetrofitClient
import com.example.data.AccountingRepository
import com.example.data.AppDatabase
import com.example.data.BudgetEntity
import com.example.data.CategoryEntity
import com.example.data.ChatMessage
import com.example.data.DbTransaction
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Serializable
data class AgentResponse(
    val reply: String,
    val addTransactions: List<TransactionData>? = null,
    val setBudgets: List<BudgetData>? = null
)

@Serializable
data class TransactionData(
    val date: String, // YYYY-MM-DD
    val type: String, // INCOME / EXPENSE
    val category: String,
    val amount: Double,
    val note: String
)

@Serializable
data class BudgetData(
    val category: String,
    val amount: Double
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val repository = AccountingRepository(db.dao())

    val messages: StateFlow<List<ChatMessage>> = repository.allMessages.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val transactions: StateFlow<List<DbTransaction>> = repository.allTransactions.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val budgets: StateFlow<List<BudgetEntity>> = repository.allBudgets.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    
    val categories: StateFlow<List<CategoryEntity>> = repository.allCategories.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val totalExpense = transactions.map { list ->
        list.filter { it.type == "EXPENSE" }.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalIncome = transactions.map { list ->
        list.filter { it.type == "INCOME" }.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)
    
    val jsonParser = Json { ignoreUnknownKeys = true }

    init {
        viewModelScope.launch {
            repository.initializeDefaultCategories()
            
            val list = repository.allMessages.first()
            if (list.isEmpty()) {
                val greeting = "你好！我是果凡记账助手 \uD83D\uDCB0\n你可以直接告诉我收支情况，例如：\n  • \"午饭花了28块\"\n  • \"收到工资12000\"\n  • \"查看本月报表\"\n  • \"设置餐饮预算500元\""
                repository.insertMessage(ChatMessage(text = greeting, isFromUser = false))
            }
        }
    }

    val isLoading = kotlinx.coroutines.flow.MutableStateFlow(false)

    fun addCategory(name: String, iconName: String, type: String) {
        viewModelScope.launch {
            repository.insertCategory(CategoryEntity(name = name, iconName = iconName, type = type))
        }
    }

    fun updateCategory(category: CategoryEntity) {
        viewModelScope.launch {
            repository.updateCategory(category)
        }
    }

    fun deleteCategory(category: CategoryEntity) {
        viewModelScope.launch {
            repository.deleteCategory(category)
        }
    }

    fun addManualTransaction(amount: Double, category: String, type: String, note: String, date: String) {
        viewModelScope.launch {
            repository.insertTransaction(
                DbTransaction(
                    date = date,
                    type = type,
                    category = category,
                    amount = amount,
                    note = note
                )
            )
            // also output a greeting message to verify it
            val typeStr = if(type == "EXPENSE") "支出" else "收入"
            val responseText = "已成功记录一笔$typeStr：$category ￥$amount"
            repository.insertMessage(ChatMessage(text = responseText, isFromUser = false))
        }
    }

    fun sendMessage(text: String) {
        viewModelScope.launch {
            val userMsg = ChatMessage(text = text, isFromUser = true)
            repository.insertMessage(userMsg)
            isLoading.value = true
            
            val reply = fetchGeminiResponse(text)
            repository.insertMessage(ChatMessage(text = reply, isFromUser = false))
            isLoading.value = false
        }
    }

    private suspend fun fetchGeminiResponse(userText: String): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return "请在系统设置中配置有效的 GEMINI_API_KEY"
        }

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val currentDate = dateFormat.format(Date())

        val recentTransactions = repository.getRecentTransactionsList()
        val currentBudgets = repository.getBudgetsList()

        val systemPrompt = """
            你是用户的智能记账助手。
            当前日期：$currentDate
            当前预算列表：${currentBudgets.joinToString { "${it.category}:${it.amount}" }}
            最近30天账单数据（用于计算平均值和异常分析）：
            ${recentTransactions.joinToString("\n") { "${it.date} ${it.type} ${it.category} ￥${it.amount} (${it.note})" }}
            
            你的任务是：
            1. 识别用户的意图：记录收支、设置预算、或者查询报表。
            2. 如果用户输入模糊，请主动补充合理默认值（如未说日期默认今天），并在回复中告知用户。遇到无法识别的内容，礼貌询问确认。
            3. 回复要求简洁清晰，避免冗长。报表和统计数据保留两位小数。金额一律以 ￥ 符号标注。
            4. 预算提醒：如果新增的支出导致某个分类（或总支出）接近预算（>80%）或超出预算，请在回复中主动提醒！
            5. 异常支出提醒：如果新增的支出金额比该分类在最近记录中的平均水平高出30%以上，主动在回复中提醒。
            6. 消费规律与省钱建议：如果用户要求分析或查看报表，帮用户发现消费规律（如周末比工作日高），并提供简洁实用的省钱建议。
            
            你必须且只能返回纯正的 JSON 格式数据（MIME type: application/json），不要将JSON包装在Markdown(```json ...)中，严格遵循以下结构：
            {
              "reply": "给用户的自然语言回复，用于聊天界面显示。",
              "addTransactions": [
                {"date": "YYYY-MM-DD", "type": "INCOME或EXPENSE", "category": "餐饮/交通/等", "amount": 28.0, "note": "午饭"}
              ],
              "setBudgets": [
                {"category": "餐饮或TOTAL", "amount": 500.0}
              ]
            }
            如果不需新增账单，则省略addTransactions。如果不需设置预算，则省略setBudgets。
            如果用户询问报表，请在reply中总结本月结余、各分类支出、建议等。
        """.trimIndent()

        val request = GenerateContentRequest(
            systemInstruction = Content(parts = listOf(Part(systemPrompt))),
            contents = listOf(Content(parts = listOf(Part(userText)))),
            generationConfig = GenerationConfig(responseMimeType = "application/json", temperature = 0.2f)
        )

        return try {
            val response = RetrofitClient.service.generateContent(apiKey, request)
            var textRes = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: return "抱歉，无法生成回复。"
            
            // Clean markdown just in case
            if (textRes.startsWith("```json")) {
                textRes = textRes.removePrefix("```json").removeSuffix("```").trim()
            } else if (textRes.startsWith("```")) {
                textRes = textRes.removePrefix("```").removeSuffix("```").trim()
            }

            val parsed = jsonParser.decodeFromString<AgentResponse>(textRes)

            // Save to DB
            parsed.addTransactions?.forEach { tr ->
                repository.insertTransaction(
                    DbTransaction(
                        date = tr.date,
                        type = tr.type,
                        category = tr.category,
                        amount = tr.amount,
                        note = tr.note
                    )
                )
            }
            parsed.setBudgets?.forEach { bd ->
                repository.insertBudget(BudgetEntity(category = bd.category, amount = bd.amount))
            }

            parsed.reply
        } catch (e: Exception) {
            "请求出错：${e.message}"
        }
    }
}
