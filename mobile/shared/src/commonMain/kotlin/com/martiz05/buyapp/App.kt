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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.martiz05.buyapp.data.ShoppingListItemStatus
import com.martiz05.buyapp.domain.ShoppingListItemModel
import com.martiz05.buyapp.domain.ShoppingListModel

@Composable
fun App(controller: BuyAppController) {
    val state by controller.state.collectAsState()

    MaterialTheme {
        if (!state.isAuthenticated) {
            AuthContent(
                isBusy = state.isBusy,
                errorMessage = state.errorMessage,
                onLogin = controller::login,
                onRegister = controller::register,
            )
        } else {
            ShoppingListsContent(
                state = state,
                onCreateList = controller::createList,
                onAddItem = controller::addItem,
                onToggleItem = controller::toggleItem,
                onSynchronize = controller::synchronize,
            )
        }
    }
}

@Composable
private fun AuthContent(
    isBusy: Boolean,
    errorMessage: String?,
    onLogin: (String, String) -> Unit,
    onRegister: (String, String) -> Unit,
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("BuyApp", style = MaterialTheme.typography.headlineLarge)
        Text(
            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp),
            text = "Organiza tus compras y conserva tus listas.",
            style = MaterialTheme.typography.bodyLarge,
        )
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo electrónico") },
            singleLine = true,
        )
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
        )
        errorMessage?.let { message ->
            Text(
                modifier = Modifier.padding(top = 12.dp),
                text = message,
                color = MaterialTheme.colorScheme.error,
            )
        }
        Row(
            modifier = Modifier.padding(top = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Button(
                enabled = !isBusy && email.isNotBlank() && password.isNotBlank(),
                onClick = { onLogin(email, password) },
            ) {
                Text("Ingresar")
            }
            TextButton(
                enabled = !isBusy && email.isNotBlank() && password.isNotBlank(),
                onClick = { onRegister(email, password) },
            ) {
                Text("Crear cuenta")
            }
        }
        if (isBusy) {
            CircularProgressIndicator(modifier = Modifier.padding(top = 20.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ShoppingListsContent(
    state: BuyAppUiState,
    onCreateList: (String) -> Unit,
    onAddItem: (String, String, Double) -> Unit,
    onToggleItem: (String, String, String) -> Unit,
    onSynchronize: () -> Unit,
) {
    val activeList = state.shoppingLists.firstOrNull()
    var showCreateListDialog by remember { mutableStateOf(false) }
    var showAddItemDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(activeList?.name ?: "BuyApp") },
                actions = {
                    TextButton(onClick = onSynchronize) {
                        Text(if (state.isSynchronizing) "Sincronizando" else "Sincronizar")
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (activeList == null) {
                        showCreateListDialog = true
                    } else {
                        showAddItemDialog = true
                    }
                },
            ) {
                Text("+")
            }
        },
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
        ) {
            if (state.isOffline) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    text = "Modo sin conexión: tus cambios están guardados en el dispositivo.",
                    color = MaterialTheme.colorScheme.tertiary,
                )
            }
            state.errorMessage?.let { message ->
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                )
            }
            if (activeList == null) {
                EmptyListsContent(onCreateList = { showCreateListDialog = true })
            } else {
                ShoppingListContent(activeList, onToggleItem)
            }
        }
    }

    if (showCreateListDialog) {
        TextInputDialog(
            title = "Nueva lista",
            label = "Nombre",
            confirmText = "Crear",
            onDismiss = { showCreateListDialog = false },
            onConfirm = { name ->
                onCreateList(name)
                showCreateListDialog = false
            },
        )
    }

    if (showAddItemDialog && activeList != null) {
        AddItemDialog(
            onDismiss = { showAddItemDialog = false },
            onConfirm = { name, quantity ->
                onAddItem(activeList.id, name, quantity)
                showAddItemDialog = false
            },
        )
    }
}

@Composable
private fun EmptyListsContent(onCreateList: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("Prepara tu próxima compra", style = MaterialTheme.typography.headlineMedium)
        Text(
            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp),
            text = "Crea una lista y marca los productos a medida que los encuentras.",
            style = MaterialTheme.typography.bodyLarge,
        )
        Button(onClick = onCreateList) {
            Text("Crear lista")
        }
    }
}

@Composable
private fun ShoppingListContent(
    shoppingList: ShoppingListModel,
    onToggleItem: (String, String, String) -> Unit,
) {
    if (shoppingList.items.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text("Usa + para agregar tu primer producto.")
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        items(shoppingList.items, key = ShoppingListItemModel::id) { item ->
            val isSelected = item.status == ShoppingListItemStatus.SELECTED
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = {
                        onToggleItem(item.shoppingListId, item.id, item.status)
                    },
                )
                Column {
                    Text(
                        text = item.name,
                        textDecoration = if (isSelected) TextDecoration.LineThrough else null,
                    )
                    Text(
                        text = "${item.quantity} ${item.unitOfMeasure}",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
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
                Text("Cancelar")
            }
        },
    )
}

@Composable
private fun AddItemDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Double) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("1") }
    val parsedQuantity = quantity.replace(',', '.').toDoubleOrNull()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Agregar producto") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Producto") },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Cantidad") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank() && parsedQuantity != null && parsedQuantity > 0,
                onClick = { onConfirm(name.trim(), parsedQuantity!!) },
            ) {
                Text("Agregar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        },
    )
}
