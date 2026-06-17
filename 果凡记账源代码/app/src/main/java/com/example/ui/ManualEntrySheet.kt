package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items as lazyItems
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class CategoryDef(val name: String, val icon: ImageVector)

val expenseCategories = listOf(
    CategoryDef("餐饮", Icons.Default.Restaurant),
    CategoryDef("购物", Icons.Default.ShoppingCart),
    CategoryDef("日用", Icons.Default.LocalMall),
    CategoryDef("交通", Icons.Default.DirectionsCar),
    CategoryDef("蔬菜", Icons.Default.Eco),
    CategoryDef("水果", Icons.Default.LocalFlorist),
    CategoryDef("零食", Icons.Default.Fastfood),
    CategoryDef("运动", Icons.Default.DirectionsRun),
    CategoryDef("娱乐", Icons.Default.Movie),
    CategoryDef("通讯", Icons.Default.Phone),
    CategoryDef("服饰", Icons.Default.Checkroom),
    CategoryDef("美容", Icons.Default.Face),
    CategoryDef("住房", Icons.Default.Home),
    CategoryDef("居家", Icons.Default.Chair), 
    CategoryDef("孩子", Icons.Default.ChildCare),
    CategoryDef("长辈", Icons.Default.Elderly),
    CategoryDef("社交", Icons.Default.People),
    CategoryDef("旅行", Icons.Default.Flight),
    CategoryDef("烟酒", Icons.Default.SmokingRooms),
    CategoryDef("数码", Icons.Default.Devices),
    CategoryDef("汽车", Icons.Default.DirectionsCar),
    CategoryDef("医疗", Icons.Default.LocalHospital),
    CategoryDef("书籍", Icons.Default.MenuBook),
    CategoryDef("学习", Icons.Default.School),
    CategoryDef("宠物", Icons.Default.Pets),
    CategoryDef("礼金", Icons.Default.CardGiftcard),
    CategoryDef("礼物", Icons.Default.Redeem),
    CategoryDef("办公", Icons.Default.BusinessCenter)
)

val incomeCategories = listOf(
    CategoryDef("工资", Icons.Default.AttachMoney),
    CategoryDef("兼职", Icons.Default.Work),
    CategoryDef("奖金", Icons.Default.MonetizationOn),
    CategoryDef("生意", Icons.Default.Storefront),
    CategoryDef("理财", Icons.Default.TrendingUp),
    CategoryDef("还款", Icons.Default.Payment),
    CategoryDef("礼金", Icons.Default.CardGiftcard),
    CategoryDef("报销", Icons.Default.RequestQuote),
    CategoryDef("其他", Icons.Default.AddBox),
    CategoryDef("转账", Icons.Default.SwapHoriz)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualEntrySheet(
    onDismissRequest: () -> Unit,
    onSaveTransaction: (amount: Double, category: String, type: String, note: String, date: String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxHeight(0.9f)
    ) {
        ManualEntryContent(
            onDismiss = onDismissRequest,
            onSave = onSaveTransaction
        )
    }
}

@Composable
fun ManualEntryContent(
    onDismiss: () -> Unit,
    onSave: (Double, String, String, String, String) -> Unit
) {
    var isExpense by remember { mutableStateOf(true) }
    var amountString by remember { mutableStateOf("0.00") }
    var isEnteringDecimal by remember { mutableStateOf(false) }
    var currentCategory by remember { mutableStateOf(expenseCategories.first()) }
    var note by remember { mutableStateOf("") }
    var isNoteFocused by remember { mutableStateOf(false) }
    val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    
    // reset selection if type changes
    LaunchedEffect(isExpense) {
        if (isExpense) {
            if (!expenseCategories.contains(currentCategory)) {
                currentCategory = expenseCategories.first()
            }
        } else {
            if (!incomeCategories.contains(currentCategory)) {
                currentCategory = incomeCategories.first()
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Top Tabs
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            TabButton("支出", isExpense) { isExpense = true }
            Spacer(modifier = Modifier.width(32.dp))
            TabButton("收入", !isExpense) { isExpense = false }
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        // Categories Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            val list = if (isExpense) expenseCategories else incomeCategories
            items(list) { category ->
                val selected = currentCategory == category
                Column(
                    modifier = Modifier
                        .clickable { currentCategory = category }
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                color = if (selected) MaterialTheme.colorScheme.primary else Color(0xFFF2F2F2),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = category.icon,
                            contentDescription = category.name,
                            tint = if (selected) MaterialTheme.colorScheme.onPrimary else Color.Gray,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = category.name,
                        fontSize = 12.sp,
                        color = if (selected) MaterialTheme.colorScheme.onBackground else Color.Gray
                    )
                }
            }
        }

        // Amount Input Area (Bottom Fixed)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
        ) {
            HorizontalDivider(color = Color(0xFFEEEEEE))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isNoteFocused = false }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = currentCategory.icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(currentCategory.name, fontWeight = FontWeight.SemiBold)
                
                Spacer(modifier = Modifier.weight(1f))
                
                Text(
                    text = "¥ $amountString",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (!isNoteFocused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                )
            }
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                OutlinedTextField(
                    value = note,
                    onValueChange = {},
                    readOnly = true,
                    placeholder = { Text("点击填写备注...") },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .onFocusChanged { if (it.isFocused) isNoteFocused = true },
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = Color(0xFFF9F9F9),
                        focusedContainerColor = Color(0xFFF9F9F9),
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (isNoteFocused) {
                T9Keyboard(
                    onKeyPress = { key ->
                        note += key
                    },
                    onBackspace = {
                        if (note.isNotEmpty()) {
                            note = note.dropLast(1)
                        }
                    },
                    onClear = {
                        note = ""
                    },
                    onDone = {
                        isNoteFocused = false
                    }
                )
            } else {
                // Numeric Keypad
                Numpad(
                    onKeyPress = { key ->
                        when (key) {
                            "." -> {
                                if (!amountString.contains(".")) {
                                    amountString = if (amountString == "0.00" || amountString == "0") "0." else "$amountString."
                                }
                            }
                            "DEL" -> {
                                if (amountString.length > 1) {
                                    amountString = amountString.dropLast(1)
                                    if (amountString.endsWith(".")) amountString = amountString.dropLast(1)
                                } else {
                                    amountString = "0.00"
                                }
                            }
                            "OK" -> {
                                val amount = amountString.toDoubleOrNull() ?: 0.0
                                if (amount > 0) {
                                    onSave(amount, currentCategory.name, if(isExpense) "EXPENSE" else "INCOME", note, currentDate)
                                    onDismiss()
                                }
                            }
                            else -> {
                                if (amountString == "0.00" || amountString == "0") {
                                    amountString = key
                                } else {
                                    val dotIndex = amountString.indexOf(".")
                                    if (dotIndex == -1 || amountString.length - dotIndex <= 2) {
                                        amountString += key
                                    }
                                }
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun TabButton(text: String, selected: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) MaterialTheme.colorScheme.onBackground else Color.Gray,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        if (selected) {
            Box(
                modifier = Modifier
                    .width(32.dp)
                    .height(3.dp)
                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(1.5.dp))
            )
        } else {
            Box(modifier = Modifier.height(3.dp))
        }
    }
}

@Composable
fun Numpad(onKeyPress: (String) -> Unit) {
    val keys = listOf(
        listOf("7", "8", "9", "今天"),
        listOf("4", "5", "6", "DEL"),
        listOf("1", "2", "3", "OK"),
        listOf(".", "0", "00")
    )
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF7F7F7))
            .padding(4.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(3f)) {
                for (i in 0..3) {
                    val rowKeys = keys[i]
                    Row(modifier = Modifier.fillMaxWidth()) {
                        for (j in 0..2) {
                            NumpadButton(text = rowKeys[j], modifier = Modifier.weight(1f)) {
                                onKeyPress(rowKeys[j])
                            }
                        }
                    }
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                NumpadAction(text = "今天", modifier = Modifier) { }
                NumpadAction(text = "DEL", modifier = Modifier) { onKeyPress("DEL") }
                NumpadAction(
                    text = "OK",
                    modifier = Modifier.fillMaxHeight(),
                    bgColor = MaterialTheme.colorScheme.primary,
                    textColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    onKeyPress("OK")
                }
            }
        }
    }
}

@Composable
fun NumpadButton(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .padding(4.dp)
            .height(56.dp)
            .background(Color.White, RoundedCornerShape(8.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, fontSize = 20.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun NumpadAction(
    text: String,
    modifier: Modifier = Modifier,
    bgColor: Color = Color.White,
    textColor: Color = Color.Black,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .padding(4.dp)
            .height(if (text == "OK") 120.dp else 56.dp)
            .background(bgColor, RoundedCornerShape(8.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (text == "DEL") {
            Icon(Icons.Default.Backspace, contentDescription = "Delete", tint = Color.Gray)
        } else {
            Text(text = text, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = textColor)
        }
    }
}

val t9Dictionary = mapOf(
    "1" to listOf("。","，","？","！"),
    "2" to listOf("啊","吧","把","爸","擦","菜","才"),
    "3" to listOf("的","地","得","大","打","到"),
    "4" to listOf("个","给","过","国","高","好"),
    "5" to listOf("就","家","看","可","快"),
    "6" to listOf("没","买","卖","吗","某","明"),
    "7" to listOf("去","钱","请","起","期","上"),
    "8" to listOf("他","她","太","同","天"),
    "9" to listOf("我","有","一","要","也","晚"),
    "24" to listOf("吃","迟","池"),
    "244" to listOf("吃","迟","池"),
    "326" to listOf("饭","烦","翻"),
    "624" to listOf("买","卖"),
    "7468" to listOf("收"),
    "96" to listOf("我","握"),
    "426" to listOf("好","号"),
    "224" to listOf("菜","财","才"),
    "926" to listOf("晚","万","玩"),
    "92" to listOf("亚","压","呀"),
    "546" to listOf("今","进"),
    "8426" to listOf("天","添")
)
val defaultCandidates = listOf("的","我","了","是","饭","买","钱","支出","收入","今天","好")

@Composable
fun T9Keyboard(
    onKeyPress: (String) -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit,
    onDone: () -> Unit
) {
    var pinyinSeq by remember { mutableStateOf("") }
    
    val currentCandidates = if (pinyinSeq.isEmpty()) {
        defaultCandidates
    } else {
        t9Dictionary[pinyinSeq] ?: listOf("...")
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF0F0F0)) // 浅灰色背景
    ) {
        // 候选词栏
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(Color.White)
        ) {
            LazyRow(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (pinyinSeq.isNotEmpty()) {
                    item {
                        Text(
                            text = pinyinSeq,
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                lazyItems(currentCandidates) { candidate ->
                    Text(
                        text = candidate,
                        modifier = Modifier
                            .clickable {
                                if (candidate != "...") {
                                    onKeyPress(candidate)
                                    pinyinSeq = ""
                                }
                            }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        fontSize = 18.sp
                    )
                }
            }
            HorizontalDivider(
                color = MaterialTheme.colorScheme.primary,
                thickness = 1.dp,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }

        val keys = listOf(
            listOf("1" to "。，？", "2" to "ABC", "3" to "DEF"),
            listOf("4" to "GHI", "5" to "JKL", "6" to "MNO"),
            listOf("7" to "PQRS", "8" to "TUV", "9" to "WXYZ"),
            listOf("符号" to "", "0" to "空格", "DEL" to "")
        )

        Column(modifier = Modifier.padding(4.dp)) {
            for (row in keys) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    for (key in row) {
                        T9KeyButton(
                            mainText = key.first,
                            subText = key.second,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                when (key.first) {
                                    "DEL" -> {
                                        if (pinyinSeq.isNotEmpty()) {
                                            pinyinSeq = pinyinSeq.dropLast(1)
                                        } else {
                                            onBackspace()
                                        }
                                    }
                                    "0" -> {
                                        if (pinyinSeq.isEmpty()) onKeyPress(" ")
                                    }
                                    "符号" -> {
                                        onDone()
                                    }
                                    else -> {
                                        pinyinSeq += key.first
                                    }
                                }
                            },
                            onLongClick = {
                                if (key.first == "DEL") {
                                    pinyinSeq = ""
                                    onClear()
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun T9KeyButton(
    mainText: String,
    subText: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .padding(4.dp)
            .height(56.dp)
            .background(Color.White, RoundedCornerShape(8.dp))
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onClick() },
                    onLongPress = { onLongClick() }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        if (mainText == "DEL") {
            Icon(Icons.Default.Backspace, contentDescription = "Delete", tint = Color.Gray)
        } else if (mainText == "符号") {
            Text("完成", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = mainText, fontSize = 20.sp, fontWeight = FontWeight.Medium)
                if (subText.isNotEmpty()) {
                    Text(text = subText, fontSize = 10.sp, color = Color.Gray)
                }
            }
        }
    }
}
