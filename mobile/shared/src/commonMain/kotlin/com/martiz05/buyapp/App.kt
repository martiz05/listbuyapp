package com.martiz05.buyapp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    var listName by remember { mutableStateOf<String?>(null) }
    var showCreateListDialog by remember { mutableStateOf(false) }
    var showAddItemDialog by remember { mutableStateOf(false) }
    var nextItemId by remember { mutableStateOf(1) }
    val shoppingItems = remember { mutableStateListOf<ShoppingListItemUi>() }

    MaterialTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(listName ?: "BuyApp") },
                )
            },
            floatingActionButton = {
                if (listName != null) {
                    FloatingActionButton(onClick = { showAddItemDialog = true }) {
                        Text("+")
                    }
                }
            },
        ) { contentPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding),
            ) {
                if (listName == null) {
                    StartShoppingList(
                        onCreateList = { showCreateListDialog = true },
                    )
                } else {
                    ShoppingListContent(
                        items = shoppingItems,
                        onToggleItem = { toggledItem ->
                            val itemIndex = shoppingItems.indexOfFirst { item -> item.id == toggledItem.id }
                            shoppingItems[itemIndex] = toggledItem.copy(isSelected = !toggledItem.isSelected)
                        },
                    )
                }
            }
        }

        if (showCreateListDialog) {
            TextInputDialog(
                title = "Create shopping list",
                label = "List name",
                confirmText = "Create",
                onDismiss = { showCreateListDialog = false },
                onConfirm = { name ->
                    listName = name
                    showCreateListDialog = false
                },
            )
        }

        if (showAddItemDialog) {
            TextInputDialog(
                title = "Add product",
                label = "Product name",
                confirmText = "Add",
                onDismiss = { showAddItemDialog = false },
                onConfirm = { name ->
                    shoppingItems.add(ShoppingListItemUi(nextItemId++, name))
                    showAddItemDialog = false
                },
            )
        }
    }
}

@Composable
private fun StartShoppingList(onCreateList: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Plan your next purchase",
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(
            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp),
            text = "Create a shopping list and mark products as you find them.",
            style = MaterialTheme.typography.bodyLarge,
        )
        Button(onClick = onCreateList) {
            Text("Create shopping list")
        }
    }
}

@Composable
private fun ShoppingListContent(
    items: List<ShoppingListItemUi>,
    onToggleItem: (ShoppingListItemUi) -> Unit,
) {
    if (items.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text("Use + to add your first product.")
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        items(items, key = { item -> item.id }) { item ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Checkbox(
                    checked = item.isSelected,
                    onCheckedChange = { onToggleItem(item) },
                )
                Text(
                    text = item.name,
                    textDecoration = if (item.isSelected) TextDecoration.LineThrough else null,
                )
            }
        }
    }
}

@Composable
private fun TextInputDialog(
    title: String,
    label: String,
    confirmText: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var value by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = value,
                onValueChange = { value = it },
                label = { Text(label) },
                singleLine = true,
            )
        },
        confirmButton = {
            TextButton(
                enabled = value.isNotBlank(),
                onClick = { onConfirm(value.trim()) },
            ) {
                Text(confirmText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}

private data class ShoppingListItemUi(
    val id: Int,
    val name: String,
    val isSelected: Boolean = false,
)
