package com.example.ui

import android.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.DbTransaction
import com.github.mikephil.charting.charts.HorizontalBarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.PercentFormatter
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ChartsScreen(
    transactions: List<DbTransaction>,
    modifier: Modifier = Modifier
) {
    var periodType by remember { mutableStateOf<PeriodType>(PeriodType.MONTH) }
    var periodOffset by remember { mutableStateOf(0) } // 0 = current, -1 = prev, etc.

    val calendar = Calendar.getInstance()
    // adjust calendar based on type and offset
    when (periodType) {
        PeriodType.WEEK -> calendar.add(Calendar.WEEK_OF_YEAR, periodOffset)
        PeriodType.MONTH -> calendar.add(Calendar.MONTH, periodOffset)
        PeriodType.YEAR -> calendar.add(Calendar.YEAR, periodOffset)
    }

    val dateRangeText = when (periodType) {
        PeriodType.WEEK -> {
            val format = SimpleDateFormat("MM.dd", Locale.CHINA)
            val calStart = calendar.clone() as Calendar
            calStart.set(Calendar.DAY_OF_WEEK, calStart.firstDayOfWeek)
            val calEnd = calendar.clone() as Calendar
            calEnd.set(Calendar.DAY_OF_WEEK, calEnd.firstDayOfWeek + 6)
            "第${calendar.get(Calendar.WEEK_OF_YEAR)}周 (${format.format(calStart.time)}-${format.format(calEnd.time)})"
        }
        PeriodType.MONTH -> SimpleDateFormat("yyyy年MM月", Locale.CHINA).format(calendar.time)
        PeriodType.YEAR -> SimpleDateFormat("yyyy年", Locale.CHINA).format(calendar.time)
    }

    val filteredTransactions = transactions.filter { t ->
        val tFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        try {
            val tDate = tFormat.parse(t.date)
            if (tDate != null) {
                val tCal = Calendar.getInstance().apply { time = tDate }
                when (periodType) {
                    PeriodType.WEEK -> tCal.get(Calendar.YEAR) == calendar.get(Calendar.YEAR) && tCal.get(Calendar.WEEK_OF_YEAR) == calendar.get(Calendar.WEEK_OF_YEAR)
                    PeriodType.MONTH -> tCal.get(Calendar.YEAR) == calendar.get(Calendar.YEAR) && tCal.get(Calendar.MONTH) == calendar.get(Calendar.MONTH)
                    PeriodType.YEAR -> tCal.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)
                }
            } else false
        } catch (e: Exception) { false }
    }

    val totalIncome = filteredTransactions.filter { it.type == "INCOME" }.sumOf { it.amount }
    val totalExpense = filteredTransactions.filter { it.type == "EXPENSE" }.sumOf { it.amount }
    val balance = totalIncome - totalExpense

    LazyColumn(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        item {
            // Tab Row
            TabRow(
                selectedTabIndex = periodType.ordinal,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Tab(selected = periodType == PeriodType.WEEK, onClick = { periodType = PeriodType.WEEK; periodOffset = 0 }, text = { Text("周") })
                Tab(selected = periodType == PeriodType.MONTH, onClick = { periodType = PeriodType.MONTH; periodOffset = 0 }, text = { Text("月") })
                Tab(selected = periodType == PeriodType.YEAR, onClick = { periodType = PeriodType.YEAR; periodOffset = 0 }, text = { Text("年") })
            }
        }
        item {
            // Period Selector
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { periodOffset-- }) { Icon(Icons.Default.ChevronLeft, "Previous") }
                Text(text = dateRangeText, fontWeight = FontWeight.Bold)
                IconButton(onClick = { periodOffset++ }, enabled = periodOffset < 0) { Icon(Icons.Default.ChevronRight, "Next") }
            }
        }
        item {
            // Cards
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    OverviewItem("总收入", totalIncome)
                    OverviewItem("总支出", totalExpense)
                    OverviewItem("结余", balance)
                }
            }
        }
        item {
            Text("支出分类占比", modifier = Modifier.padding(16.dp), fontWeight = FontWeight.Bold)
            Box(modifier = Modifier.fillMaxWidth().height(300.dp)) {
                ExpensePieChart(filteredTransactions, totalExpense)
            }
        }
        item {
            Text("收支趋势", modifier = Modifier.padding(16.dp), fontWeight = FontWeight.Bold)
            Box(modifier = Modifier.fillMaxWidth().height(300.dp)) {
                TrendLineChart(filteredTransactions, periodType, calendar)
            }
        }
        item {
            Text("支出排行", modifier = Modifier.padding(16.dp), fontWeight = FontWeight.Bold)
            Box(modifier = Modifier.fillMaxWidth().height(400.dp)) {
                ExpenseBarChart(filteredTransactions)
            }
        }
    }
}

enum class PeriodType { WEEK, MONTH, YEAR }

@Composable
fun OverviewItem(title: String, amount: Double) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(title, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
        Text("¥%.2f".format(amount), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
    }
}

private val CHART_COLORS = listOf(
    Color.rgb(255, 210, 0),
    Color.rgb(255, 150, 0),
    Color.rgb(255, 100, 0),
    Color.rgb(255, 50, 0),
    Color.rgb(255, 200, 100),
    Color.rgb(200, 200, 0),
    Color.rgb(150, 220, 0),
    Color.rgb(100, 240, 0),
    Color.rgb(50, 255, 0)
)

@Composable
fun ExpensePieChart(transactions: List<DbTransaction>, totalExpense: Double) {
    AndroidView(
        factory = { context ->
            PieChart(context).apply {
                description.isEnabled = false
                isDrawHoleEnabled = true
                setHoleColor(Color.TRANSPARENT)
                centerText = "总支出\n¥%.2f".format(totalExpense)
                setCenterTextSize(16f)
                legend.isWordWrapEnabled = true
                setUsePercentValues(true)
            }
        },
        update = { chart ->
            val expenseByCategory = transactions.filter { it.type == "EXPENSE" }
                .groupBy { it.category }
                .mapValues { it.value.sumOf { t -> t.amount } }
                .entries.sortedByDescending { it.value }

            val entries = expenseByCategory.map { PieEntry(it.value.toFloat(), it.key) }
            val dataSet = PieDataSet(entries, "").apply {
                colors = CHART_COLORS
                valueTextSize = 12f
                valueTextColor = Color.WHITE
                valueFormatter = PercentFormatter(chart)
            }
            chart.data = PieData(dataSet)
            chart.invalidate()
        }
    )
}

@Composable
fun TrendLineChart(transactions: List<DbTransaction>, periodType: PeriodType, targetCal: Calendar) {
    AndroidView(
        factory = { context ->
            LineChart(context).apply {
                description.isEnabled = false
                setTouchEnabled(true)
                isDragEnabled = true
                setScaleEnabled(true)
                setPinchZoom(true)
                xAxis.position = XAxis.XAxisPosition.BOTTOM
                xAxis.setDrawGridLines(false)
                axisRight.isEnabled = false
            }
        },
        update = { chart ->
            // group by day, week, month depends on periodType
            // For simplicity in this demo, let's group by day
            val dates = transactions.map { it.date }.distinct().sorted()
            
            val incomeEntries = ArrayList<Entry>()
            val expenseEntries = ArrayList<Entry>()

            dates.forEachIndexed { index, date ->
                val dayT = transactions.filter { it.date == date }
                incomeEntries.add(Entry(index.toFloat(), dayT.filter { it.type == "INCOME" }.sumOf { it.amount }.toFloat()))
                expenseEntries.add(Entry(index.toFloat(), dayT.filter { it.type == "EXPENSE" }.sumOf { it.amount }.toFloat()))
            }

            val incomeSet = LineDataSet(incomeEntries, "收入").apply {
                color = Color.rgb(255, 210, 0)
                setCircleColor(Color.rgb(255, 210, 0))
                lineWidth = 2f
                circleRadius = 4f
                setDrawValues(false)
            }
            val expenseSet = LineDataSet(expenseEntries, "支出").apply {
                color = Color.RED
                setCircleColor(Color.RED)
                lineWidth = 2f
                circleRadius = 4f
                setDrawValues(false)
            }

            chart.xAxis.valueFormatter = IndexAxisValueFormatter(dates.map { it.substring(5) }) // MM-dd
            chart.xAxis.granularity = 1f
            chart.data = LineData(incomeSet, expenseSet)
            chart.invalidate()
        }
    )
}

@Composable
fun ExpenseBarChart(transactions: List<DbTransaction>) {
    AndroidView(
        factory = { context ->
            HorizontalBarChart(context).apply {
                description.isEnabled = false
                setDrawGridBackground(false)
                xAxis.position = XAxis.XAxisPosition.BOTTOM
                xAxis.setDrawGridLines(false)
                axisLeft.axisMinimum = 0f
                axisRight.isEnabled = false
                legend.isEnabled = false
            }
        },
        update = { chart ->
            val expenseByCategory = transactions.filter { it.type == "EXPENSE" }
                .groupBy { it.category }
                .mapValues { it.value.sumOf { t -> t.amount } }
                .entries.sortedBy { it.value } // Ascending for horizontal bar to show highest at top

            val entries = expenseByCategory.mapIndexed { index, entry -> 
                BarEntry(index.toFloat(), entry.value.toFloat())
            }
            val labels = expenseByCategory.map { it.key }

            val dataSet = BarDataSet(entries, "支出").apply {
                color = Color.rgb(255, 210, 0)
                valueTextSize = 10f
            }

            chart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            chart.xAxis.granularity = 1f
            chart.xAxis.labelCount = labels.size
            chart.data = BarData(dataSet)
            chart.invalidate()
        }
    )
}

