package com.example.data

import kotlinx.coroutines.flow.Flow

class AccountingRepository(private val dao: AccountingDao) {

    val allMessages: Flow<List<ChatMessage>> = dao.getAllMessages()
    val allTransactions: Flow<List<DbTransaction>> = dao.getAllTransactions()
    val allBudgets: Flow<List<BudgetEntity>> = dao.getAllBudgets()
    val allCategories: Flow<List<CategoryEntity>> = dao.getAllCategories()

    suspend fun insertMessage(message: ChatMessage) {
        dao.insertMessage(message)
    }

    suspend fun insertTransaction(transaction: DbTransaction) {
        dao.insertTransaction(transaction)
    }

    suspend fun insertBudget(budget: BudgetEntity) {
        dao.insertBudget(budget)
    }
    
    suspend fun insertCategory(category: CategoryEntity) {
        dao.insertCategory(category)
    }
    
    suspend fun updateCategory(category: CategoryEntity) {
        dao.updateCategory(category)
    }
    
    suspend fun deleteCategory(category: CategoryEntity) {
        dao.deleteCategory(category)
    }
    
    suspend fun initializeDefaultCategories() {
        if (dao.getCategoryCount() == 0) {
            val defaults = listOf(
                CategoryEntity(name = "餐饮", iconName = "Star", type = "EXPENSE"),
                CategoryEntity(name = "交通", iconName = "Home", type = "EXPENSE"),
                CategoryEntity(name = "购物", iconName = "ShoppingCart", type = "EXPENSE"),
                CategoryEntity(name = "娱乐", iconName = "Face", type = "EXPENSE"),
                CategoryEntity(name = "工资", iconName = "Star", type = "INCOME"),
                CategoryEntity(name = "理财", iconName = "Star", type = "INCOME")
            )
            defaults.forEach { dao.insertCategory(it) }
        }
    }

    suspend fun getRecentTransactionsList(): List<DbTransaction> {
        return dao.getRecentTransactions()
    }

    suspend fun getBudgetsList(): List<BudgetEntity> {
        return dao.getBudgetsList()
    }
}
