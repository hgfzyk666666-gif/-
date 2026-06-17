package com.example

import android.os.Bundle
import android.content.Context
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.shape.CircleShape
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.ChatMessage
import com.example.ui.MainViewModel
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch

import com.example.ui.CategoriesScreen
import com.example.ui.ChartsScreen

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                var errorMessage by remember { mutableStateOf<String?>(null) }
                val context = LocalContext.current

                LaunchedEffect(Unit) {
                    val prefs = context.getSharedPreferences("crash_prefs", Context.MODE_PRIVATE)
                    errorMessage = prefs.getString("crash_log", null)
                }

                Thread.setDefaultUncaughtExceptionHandler { _, e ->
                    val prefs = context.getSharedPreferences("crash_prefs", Context.MODE_PRIVATE)
                    prefs.edit().putString("crash_log", e.stackTraceToString()).commit()
                }

                if (errorMessage != null) {
                    Scaffold {
                        LazyColumn(modifier = Modifier.fillMaxSize().padding(it).padding(16.dp)) {
                            item {
                                Text("CRASH LOG:\n$errorMessage", color = MaterialTheme.colorScheme.error)
                            }
                            item {
                                Button(onClick = {
                                    context.getSharedPreferences("crash_prefs", Context.MODE_PRIVATE).edit().clear().apply()
                                    errorMessage = null
                                }) { Text("Clear Log") }
                            }
                        }
                    }
                    return@MyApplicationTheme
                }

                val viewModel: MainViewModel = viewModel()

                val messages by viewModel.messages.collectAsStateWithLifecycle()
                val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
                val categories by viewModel.categories.collectAsStateWithLifecycle()
                
                var currentScreen by remember { mutableStateOf("chat") }
                var showManualEntry by remember { mutableStateOf(false) }

                if (currentScreen == "categories") {
                    CategoriesScreen(
                        categories = categories,
                        onBack = { currentScreen = "chat" },
                        onAddCategory = viewModel::addCategory,
                        onEditCategory = viewModel::updateCategory,
                        onDeleteCategory = viewModel::deleteCategory
                    )
                } else {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        containerColor = MaterialTheme.colorScheme.background,
                        topBar = {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp, vertical = 16.dp)
                                    .statusBarsPadding(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "果凡记账",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = SimpleDateFormat("MM月dd日", Locale.CHINA).format(Date()),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        val totalIncome by viewModel.totalIncome.collectAsStateWithLifecycle()
                                        val totalExpense by viewModel.totalExpense.collectAsStateWithLifecycle()
                                        Text(
                                            text = "收入 ¥%.2f | 支出 ¥%.2f".format(totalIncome, totalExpense),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Row {
                                    if (currentScreen == "charts") {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .background(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer)
                                                .clickable { 
                                                    com.example.ui.exportReport(context, viewModel.transactions.value)
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = androidx.compose.material.icons.Icons.Default.Share,
                                                contentDescription = "Export Report",
                                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer)
                                            .clickable { currentScreen = "categories" },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Settings,
                                            contentDescription = "Settings",
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }
                            }
                        },
                        bottomBar = {
                            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                                NavigationBarItem(
                                    selected = currentScreen == "chat",
                                    onClick = { currentScreen = "chat" },
                                    icon = { Icon(Icons.Default.List, contentDescription = "记账") },
                                    label = { Text("记账") }
                                )
                                NavigationBarItem(
                                    selected = currentScreen == "charts",
                                    onClick = { currentScreen = "charts" },
                                    icon = { Icon(Icons.Default.PieChart, contentDescription = "图表") },
                                    label = { Text("图表") }
                                )
                            }
                        },
                        floatingActionButton = {
                            if (currentScreen == "chat") {
                                FloatingActionButton(
                                    onClick = { showManualEntry = true },
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    shape = CircleShape
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Add Transaction", tint = MaterialTheme.colorScheme.onPrimary)
                                }
                            }
                        }
                    ) { innerPadding ->
                        if (currentScreen == "chat") {
                            ChatScreen(
                                messages = messages,
                                isLoading = isLoading,
                                onSendMessage = { text -> viewModel.sendMessage(text) },
                                modifier = Modifier.padding(innerPadding)
                            )
                        } else if (currentScreen == "charts") {
                            ChartsScreen(
                                transactions = viewModel.transactions.collectAsStateWithLifecycle().value,
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                    }
                    
                    if (showManualEntry) {
                        com.example.ui.ManualEntrySheet(
                            onDismissRequest = { showManualEntry = false },
                            onSaveTransaction = { amount, category, type, note, date ->
                                viewModel.addManualTransaction(amount, category, type, note, date)
                                showManualEntry = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChatScreen(
    messages: List<ChatMessage>,
    isLoading: Boolean,
    onSendMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Scroll to bottom on new message
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(messages) { msg ->
                ChatBubble(message = msg)
            }
            if (isLoading) {
                item {
                    ChatBubble(
                        message = ChatMessage(text = "思考中...", isFromUser = false),
                        isTyping = true
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .background(androidx.compose.ui.graphics.Color.White, RoundedCornerShape(32.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = inputText,
                onValueChange = { inputText = it },
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_input"),
                placeholder = { Text("告诉我你花了多少钱...", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                    unfocusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                    focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                    unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent
                )
            )
            IconButton(
                onClick = {
                    if (inputText.isNotBlank()) {
                        onSendMessage(inputText)
                        inputText = ""
                    }
                },
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                    .testTag("send_button")
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage, isTyping: Boolean = false) {
    val isUser = message.isFromUser
    val alignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
    val bgColor = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer
    val textColor = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = alignment
    ) {
        if (isTyping) {
            Text(
                text = message.text,
                color = textColor,
                modifier = Modifier
                    .background(
                        color = bgColor,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            )
        } else {
            Text(
                text = message.text,
                color = textColor,
                modifier = Modifier
                    .background(
                        color = bgColor,
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isUser) 16.dp else 4.dp,
                            bottomEnd = if (isUser) 4.dp else 16.dp
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            )
        }
    }
}
