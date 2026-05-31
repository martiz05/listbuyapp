package com.martiz05.buyapp

import com.martiz05.buyapp.data.AuthService
import com.martiz05.buyapp.data.ShoppingListItemStatus
import com.martiz05.buyapp.data.ShoppingListRepository
import com.martiz05.buyapp.domain.ShoppingListModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BuyAppController(
    private val authService: AuthService,
    private val shoppingListRepository: ShoppingListRepository,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val mutableState = MutableStateFlow(BuyAppUiState())

    val state = mutableState.asStateFlow()

    init {
        scope.launch {
            shoppingListRepository.shoppingLists.collect { lists ->
                mutableState.update { current -> current.copy(shoppingLists = lists) }
            }
        }
    }

    fun login(email: String, password: String) {
        runAuthAction {
            authService.login(email, password)
        }
    }

    fun register(email: String, password: String) {
        runAuthAction {
            authService.register(email, password)
        }
    }

    fun createList(name: String) {
        scope.launch {
            runLocalAction {
                shoppingListRepository.createList(name)
            }
        }
    }

    fun addItem(shoppingListId: String, name: String, quantity: Double) {
        scope.launch {
            runLocalAction {
                shoppingListRepository.addItem(shoppingListId, name, quantity)
            }
        }
    }

    fun toggleItem(shoppingListId: String, itemId: String, currentStatus: String) {
        val nextStatus = if (currentStatus == ShoppingListItemStatus.SELECTED) {
            ShoppingListItemStatus.PENDING
        } else {
            ShoppingListItemStatus.SELECTED
        }

        scope.launch {
            runLocalAction {
                shoppingListRepository.setItemStatus(shoppingListId, itemId, nextStatus)
            }
        }
    }

    fun synchronize() {
        syncInBackground()
    }

    fun clearError() {
        mutableState.update { current -> current.copy(errorMessage = null) }
    }

    private fun runAuthAction(action: suspend () -> Unit) {
        scope.launch {
            mutableState.update { current -> current.copy(isBusy = true, errorMessage = null) }
            try {
                action()
                mutableState.update { current -> current.copy(isAuthenticated = true, isBusy = false) }
                syncInBackground()
            } catch (exception: Exception) {
                mutableState.update { current ->
                    current.copy(
                        isBusy = false,
                        errorMessage = exception.message ?: "No fue posible iniciar sesión.",
                    )
                }
            }
        }
    }

    private suspend fun runLocalAction(action: suspend () -> Unit) {
        try {
            action()
            syncInBackground()
        } catch (exception: IllegalArgumentException) {
            mutableState.update { current -> current.copy(errorMessage = exception.message) }
        }
    }

    private fun syncInBackground() {
        scope.launch {
            mutableState.update { current -> current.copy(isSynchronizing = true) }
            try {
                shoppingListRepository.synchronize()
                mutableState.update { current ->
                    current.copy(isSynchronizing = false, isOffline = false)
                }
            } catch (_: Exception) {
                mutableState.update { current ->
                    current.copy(isSynchronizing = false, isOffline = true)
                }
            }
        }
    }
}

data class BuyAppUiState(
    val isAuthenticated: Boolean = false,
    val isBusy: Boolean = false,
    val isSynchronizing: Boolean = false,
    val isOffline: Boolean = false,
    val errorMessage: String? = null,
    val shoppingLists: List<ShoppingListModel> = emptyList(),
)
