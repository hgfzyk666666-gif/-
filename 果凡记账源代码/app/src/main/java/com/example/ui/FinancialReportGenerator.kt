package com.example.ui

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.DbTransaction
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.TextAlignment
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun exportReport(context: Context, transactions: List<DbTransaction>) {
    try {
        val pdfDir = File(context.cacheDir, "reports")
        if (!pdfDir.exists()) {
            pdfDir.mkdirs()
        }
        val fileName = "财务报表_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA).format(Date())}.pdf"
        val pdfFile = File(pdfDir, fileName)

        val writer = PdfWriter(pdfFile.absolutePath)
        val pdf = PdfDocument(writer)
        val document = Document(pdf)

        // Title
        val title = Paragraph("果凡记账 - 财务报表")
            .setTextAlignment(TextAlignment.CENTER)
            .setFontSize(20f)
            .setBold()
        document.add(title)

        val dateStr = "生成时间: " + SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA).format(Date())
        document.add(Paragraph(dateStr).setTextAlignment(TextAlignment.RIGHT))

        document.add(Paragraph("\n"))

        // Total
        val totalIncome = transactions.filter { it.type == "INCOME" }.sumOf { it.amount }
        val totalExpense = transactions.filter { it.type == "EXPENSE" }.sumOf { it.amount }
        
        val summaryTable = Table(floatArrayOf(1f, 1f, 1f)).useAllAvailableWidth()
        summaryTable.addHeaderCell("总收入")
        summaryTable.addHeaderCell("总支出")
        summaryTable.addHeaderCell("净结余")
        summaryTable.addCell(String.format("¥%.2f", totalIncome))
        summaryTable.addCell(String.format("¥%.2f", totalExpense))
        summaryTable.addCell(String.format("¥%.2f", totalIncome - totalExpense))
        document.add(summaryTable)

        document.add(Paragraph("\n"))

        // Expense by Category
        document.add(Paragraph("支出明细").setBold().setFontSize(16f))
        val expenseTable = Table(floatArrayOf(2f, 2f, 1f)).useAllAvailableWidth()
        expenseTable.addHeaderCell("分类")
        expenseTable.addHeaderCell("金额")
        expenseTable.addHeaderCell("笔数")
        
        val expenseGrouping = transactions.filter { it.type == "EXPENSE" }.groupBy { it.category }
        expenseGrouping.entries.sortedByDescending { it.value.sumOf { t -> t.amount } }.forEach { (category, txs) ->
            expenseTable.addCell(category)
            expenseTable.addCell(String.format("¥%.2f", txs.sumOf { it.amount }))
            expenseTable.addCell(txs.size.toString())
        }
        document.add(expenseTable)

        document.add(Paragraph("\n"))
        
        // Income by Category
        document.add(Paragraph("收入明细").setBold().setFontSize(16f))
        val incomeTable = Table(floatArrayOf(2f, 2f, 1f)).useAllAvailableWidth()
        incomeTable.addHeaderCell("分类")
        incomeTable.addHeaderCell("金额")
        incomeTable.addHeaderCell("笔数")
        
        val incomeGrouping = transactions.filter { it.type == "INCOME" }.groupBy { it.category }
        incomeGrouping.entries.sortedByDescending { it.value.sumOf { t -> t.amount } }.forEach { (category, txs) ->
            incomeTable.addCell(category)
            incomeTable.addCell(String.format("¥%.2f", txs.sumOf { it.amount }))
            incomeTable.addCell(txs.size.toString())
        }
        document.add(incomeTable)

        document.add(Paragraph("\n"))

        // Daily Records
        document.add(Paragraph("流水记录").setBold().setFontSize(16f))
        val recordsTable = Table(floatArrayOf(2f, 1f, 1f, 3f)).useAllAvailableWidth()
        recordsTable.addHeaderCell("日期")
        recordsTable.addHeaderCell("分类")
        recordsTable.addHeaderCell("金额")
        recordsTable.addHeaderCell("备注")
        transactions.sortedByDescending { it.timestamp }.forEach { tx ->
            recordsTable.addCell(tx.date)
            recordsTable.addCell(tx.category)
            val prefix = if(tx.type == "INCOME") "+" else "-"
            recordsTable.addCell(prefix + String.format("¥%.2f", tx.amount))
            recordsTable.addCell(tx.note)
        }
        document.add(recordsTable)

        document.close()

        sharePdf(context, pdfFile)

    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "报表生成失败", Toast.LENGTH_SHORT).show()
    }
}

private fun sharePdf(context: Context, pdfFile: File) {
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", pdfFile)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "application/pdf"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "分享财务报表"))
}
