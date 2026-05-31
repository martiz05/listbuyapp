using BuyApp.Domain.ShoppingLists;

namespace BuyApp.Application.ShoppingLists;

public interface IShoppingListService
{
    Task<IReadOnlyCollection<ShoppingListSummary>> GetAllAsync(
        Guid userId,
        CancellationToken cancellationToken);

    Task<ShoppingListDetails?> GetByIdAsync(
        Guid userId,
        Guid shoppingListId,
        CancellationToken cancellationToken);

    Task<ShoppingListDetails> CreateAsync(
        Guid userId,
        CreateShoppingListCommand command,
        CancellationToken cancellationToken);

    Task<ShoppingListDetails?> AddItemAsync(
        Guid userId,
        Guid shoppingListId,
        AddShoppingListItemCommand command,
        CancellationToken cancellationToken);

    Task<ShoppingListDetails?> SetItemStatusAsync(
        Guid userId,
        Guid shoppingListId,
        Guid itemId,
        ShoppingListItemStatus status,
        CancellationToken cancellationToken);
}

public sealed record CreateShoppingListCommand(string Name);

public sealed record AddShoppingListItemCommand(
    string Name,
    decimal Quantity,
    UnitOfMeasure UnitOfMeasure);

public sealed record ShoppingListSummary(
    Guid Id,
    string Name,
    DateTimeOffset CreatedAtUtc,
    int ItemCount,
    int SelectedItemCount);

public sealed record ShoppingListDetails(
    Guid Id,
    string Name,
    DateTimeOffset CreatedAtUtc,
    DateTimeOffset? ArchivedAtUtc,
    IReadOnlyCollection<ShoppingListItemDetails> Items);

public sealed record ShoppingListItemDetails(
    Guid Id,
    string Name,
    decimal Quantity,
    UnitOfMeasure UnitOfMeasure,
    ShoppingListItemStatus Status,
    int Position);
