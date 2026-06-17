package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.CategoryEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    categories: List<CategoryEntity>,
    onBack: () -> Unit,
    onAddCategory: (String, String, String) -> Unit,
    onEditCategory: (CategoryEntity) -> Unit,
    onDeleteCategory: (CategoryEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<CategoryEntity?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("分类管理") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { 
                    editingCategory = null
                    showDialog = true 
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, "Add Category")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(innerPadding).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { category ->
                CategoryItem(
                    category = category,
                    onClick = {
                        editingCategory = category
                        showDialog = true
                    },
                    onDelete = { onDeleteCategory(category) }
                )
            }
        }

        if (showDialog) {
            CategoryDialog(
                category = editingCategory,
                onDismiss = { showDialog = false },
                onSave = { name, iconName, type ->
                    if (editingCategory == null) {
                        onAddCategory(name, iconName, type)
                    } else {
                        onEditCategory(editingCategory!!.copy(name = name, iconName = iconName, type = type))
                    }
                    showDialog = false
                }
            )
        }
    }
}

@Composable
fun CategoryItem(
    category: CategoryEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getIconForName(category.iconName),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = category.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    text = if (category.type == "INCOME") "收入" else "支出",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (category.type == "INCOME") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

val availableIcons = listOf(
    "Star" to Icons.Default.Star,
    "Home" to Icons.Default.Home,
    "ShoppingCart" to Icons.Default.ShoppingCart,
    "Face" to Icons.Default.Face,
    "Favorite" to Icons.Default.Favorite,
    "Settings" to Icons.Default.Settings,
    "Build" to Icons.Default.Build,
    "Phone" to Icons.Default.Phone
)

fun getIconForName(name: String): ImageVector {
    return availableIcons.find { it.first == name }?.second ?: Icons.Default.Star
}

@Composable
fun CategoryDialog(
    category: CategoryEntity?,
    onDismiss: () -> Unit,
    onSave: (String, String, String) -> Unit
) {
    var name by remember { mutableStateOf(category?.name ?: "") }
    var selectedIcon by remember { mutableStateOf(category?.iconName ?: availableIcons.first().first) }
    var type by remember { mutableStateOf(category?.type ?: "EXPENSE") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (category == null) "添加分类" else "编辑分类") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("名称") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    FilterChip(
                        selected = type == "EXPENSE",
                        onClick = { type = "EXPENSE" },
                        label = { Text("支出") }
                    )
                    FilterChip(
                        selected = type == "INCOME",
                        onClick = { type = "INCOME" },
                        label = { Text("收入") }
                    )
                }

                Text("图标:")
                LazyColumn(
                    modifier = Modifier.height(150.dp)
                ) {
                    items(availableIcons.chunked(3)) { rowIcons ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            rowIcons.forEach { (iconName, imageVector) ->
                                IconButton(
                                    onClick = { selectedIcon = iconName },
                                    modifier = Modifier.background(
                                        if (selectedIcon == iconName) MaterialTheme.colorScheme.primaryContainer else androidx.compose.ui.graphics.Color.Transparent,
                                        CircleShape
                                    )
                                ) {
                                    Icon(
                                        imageVector = imageVector,
                                        contentDescription = iconName,
                                        tint = if (selectedIcon == iconName) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(name, selectedIcon, type)
                    }
                }
            ) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}
