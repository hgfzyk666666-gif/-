package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val text: String,
    val isFromUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "transactions")
data class DbTransaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String, // "YYYY-MM-DD"格式
    val type: String, // "INCOME" or "EXPENSE"
    val category: String,
    val amount: Double,
    val note: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey val category: String, // "TOTAL" represents overall budget
    val amount: Double
)

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val iconName: String,
    val type: String // "INCOME" or "EXPENSE"
)

