package com.martiz05.buyapp.data

import com.martiz05.buyapp.data.local.PendingSyncOperationEntity
import com.martiz05.buyapp.data.local.ShoppingListDao
import com.martiz05.buyapp.data.local.ShoppingListEntity
import com.martiz05.buyapp.data.local.ShoppingListItemEntity
import com.martiz05.buyapp.data.local.ShoppingListWithItems
import com.martiz05.buyapp.data.remote.AddShoppingListItemRequest
import com.martiz05.buyapp.data.remote.BuyAppApi
import com.martiz05.buyapp.data.remote.CreateShoppingListRequest
import com.martiz05.buyapp.data.remote.SetShoppingListItemStatusRequest
import com.martiz05.buyapp.data.remote.ShoppingListRemote
import com.martiz05.buyapp.domain.ShoppingListItemModel
import com.martiz05.buyapp.domain.ShoppingListModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class ShoppingListRepository(
    private val dao: ShoppingListDao,
    private val api: BuyAppApi,
    private val json: Json = Json,
) {
    val shoppingLists: Flow<List<ShoppingListModel>> =
        dao.observeActiveLists().map { lists -> lists.map(ShoppingListWithItems::toModel) }

    @OptIn(ExperimentalTime::class, ExperimentalUuidApi::class)
    suspend fun createList(name: String) {
        val trimmedName = name.trim()
        require(trimmedName.isNotEmpty()) { "El nombre de la lista es obligatorio." }

        val request = CreateShoppingListRequest(
            id = Uuid.random().toString(),
            name = trimmedName,
        )
        dao.saveListAndEnqueue(
            shoppingList = ShoppingListEntity(
                id = request.id,
                name = request.name,
                createdAtUtc = Clock.System.now().toString(),
                archivedAtUtc = null,
            ),
            operation = PendingSyncOperationEntity(
                operationType = PendingOperationType.CREATE_LIST,
                payload = json.encodeToString(request),
            ),
        )
    }

    @OptIn(ExperimentalUuidApi::class)
    suspend fun addItem(
        shoppingListId: String,
        name: String,
        quantity: Double,
        unitOfMeasure: String = "unit",
    ) {
        val trimmedName = name.trim()
        require(trimmedName.isNotEmpty()) { "El nombre del producto es obligatorio." }
        require(quantity > 0) { "La cantidad debe ser mayor que cero." }

        val request = AddShoppingListItemRequest(
            id = Uuid.random().toString(),
            name = trimmedName,
            quantity = quantity,
            unitOfMeasure = unitOfMeasure,
        )
        dao.saveItemAndEnqueue(
            item = ShoppingListItemEntity(
                id = request.id,
                shoppingListId = shoppingListId,
                name = request.name,
                quantity = request.quantity.toString(),
                unitOfMeasure = request.unitOfMeasure,
                status = ShoppingListItemStatus.PENDING,
                position = dao.countItems(shoppingListId),
            ),
            operation = PendingSyncOperationEntity(
                operationType = PendingOperationType.ADD_ITEM,
                payload = json.encodeToString(
                    AddItemOperation(
                        shoppingListId = shoppingListId,
                        request = request,
                    ),
                ),
            ),
        )
    }

    suspend fun setItemStatus(
        shoppingListId: String,
        itemId: String,
        status: String,
    ) {
        require(status in ShoppingListItemStatus.entries) { "Estado de producto no válido." }

        val request = SetShoppingListItemStatusRequest(status)
        dao.updateItemStatusAndEnqueue(
            itemId = itemId,
            status = status,
            operation = PendingSyncOperationEntity(
                operationType = PendingOperationType.SET_ITEM_STATUS,
                payload = json.encodeToString(
                    SetItemStatusOperation(
                        shoppingListId = shoppingListId,
                        itemId = itemId,
                        request = request,
                    ),
                ),
            ),
        )
    }

    suspend fun synchronize() {
        syncMutex.withLock {
            dao.getPendingOperations().forEach { operation ->
                when (operation.operationType) {
                    PendingOperationType.CREATE_LIST ->
                        api.createShoppingList(json.decodeFromString(operation.payload))

                    PendingOperationType.ADD_ITEM -> {
                        val payload = json.decodeFromString<AddItemOperation>(operation.payload)
                        api.addShoppingListItem(payload.shoppingListId, payload.request)
                    }

                    PendingOperationType.SET_ITEM_STATUS -> {
                        val payload = json.decodeFromString<SetItemStatusOperation>(operation.payload)
                        api.setShoppingListItemStatus(
                            shoppingListId = payload.shoppingListId,
                            itemId = payload.itemId,
                            request = payload.request,
                        )
                    }

                    else -> error("Operación de sincronización desconocida: ${operation.operationType}")
                }
                dao.deletePendingOperation(operation.id)
            }

            api.getShoppingLists().forEach { remoteList ->
                dao.mergeRemoteList(
                    shoppingList = remoteList.toEntity(),
                    items = remoteList.items.map { item ->
                        ShoppingListItemEntity(
                            id = item.id,
                            shoppingListId = remoteList.id,
                            name = item.name,
                            quantity = item.quantity.toString(),
                            unitOfMeasure = item.unitOfMeasure,
                            status = item.status,
                            position = item.position,
                        )
                    },
                )
            }
        }
    }

    private companion object {
        val syncMutex = Mutex()
    }
}

private object PendingOperationType {
    const val CREATE_LIST = "create-list"
    const val ADD_ITEM = "add-item"
    const val SET_ITEM_STATUS = "set-item-status"
}

object ShoppingListItemStatus {
    const val PENDING = "pending"
    const val SELECTED = "selected"
    const val UNAVAILABLE = "unavailable"

    val entries = setOf(PENDING, SELECTED, UNAVAILABLE)
}

@Serializable
private data class AddItemOperation(
    val shoppingListId: String,
    val request: AddShoppingListItemRequest,
)

@Serializable
private data class SetItemStatusOperation(
    val shoppingListId: String,
    val itemId: String,
    val request: SetShoppingListItemStatusRequest,
)

private fun ShoppingListWithItems.toModel() = ShoppingListModel(
    id = shoppingList.id,
    name = shoppingList.name,
    items = items.map { item ->
        ShoppingListItemModel(
            id = item.id,
            shoppingListId = item.shoppingListId,
            name = item.name,
            quantity = item.quantity,
            unitOfMeasure = item.unitOfMeasure,
            status = item.status,
            position = item.position,
        )
    },
)

private fun ShoppingListRemote.toEntity() = ShoppingListEntity(
    id = id,
    name = name,
    createdAtUtc = createdAtUtc,
    archivedAtUtc = archivedAtUtc,
)
