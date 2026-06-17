package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountingDao {
    // --- Messages ---
    @Query("SELECT * FROM messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<ChatMessage>>

    @Insert
    suspend fun insertMessage(message: ChatMessage)

    // --- Transactions ---
    @Query("SELECT * FROM transactions ORDER BY date DESC, timestamp DESC")
    fun getAllTransactions(): Flow<List<DbTransaction>>

    // Optional: for AI context (e.g. recent 100)
    @Query("SELECT * FROM transactions ORDER BY date DESC, timestamp DESC LIMIT 100")
    suspend fun getRecentTransactions(): List<DbTransaction>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: DbTransaction)

    // --- Budgets ---
    @Query("SELECT * FROM budgets")
    fun getAllBudgets(): Flow<List<BudgetEntity>>

    @Query("SELECT * FROM budgets")
    suspend fun getBudgetsList(): List<BudgetEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: BudgetEntity)

    // --- Categories ---
    @Query("SELECT * FROM categories")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity)

    @androidx.room.Update
    suspend fun updateCategory(category: CategoryEntity)

    @androidx.room.Delete
    suspend fun deleteCategory(category: CategoryEntity)
    
    @Query("SELECT count(*) FROM categories")
    suspend fun getCategoryCount(): Int
}
