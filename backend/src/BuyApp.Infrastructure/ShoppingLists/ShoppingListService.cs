using BuyApp.Application.ShoppingLists;
using BuyApp.Domain.ShoppingLists;
using BuyApp.Infrastructure.Persistence;
using Microsoft.EntityFrameworkCore;

namespace BuyApp.Infrastructure.ShoppingLists;

public sealed class ShoppingListService(BuyAppDbContext dbContext) : IShoppingListService
{
    public async Task<IReadOnlyCollection<ShoppingListSummary>> GetAllAsync(
        Guid userId,
        CancellationToken cancellationToken)
    {
        return await dbContext.ShoppingLists
            .AsNoTracking()
            .Where(shoppingList => shoppingList.UserId == userId && shoppingList.ArchivedAtUtc == null)
            .OrderByDescending(shoppingList => shoppingList.CreatedAtUtc)
            .Select(shoppingList => new ShoppingListSummary(
                shoppingList.Id,
                shoppingList.Name,
                shoppingList.CreatedAtUtc,
                shoppingList.Items.Count,
                shoppingList.Items.Count(item => item.Status == ShoppingListItemStatus.Selected)))
            .ToListAsync(cancellationToken);
    }

    public async Task<ShoppingListDetails?> GetByIdAsync(
        Guid userId,
        Guid shoppingListId,
        CancellationToken cancellationToken)
    {
        var shoppingList = await FindOwnedListAsync(userId, shoppingListId, cancellationToken);
        return shoppingList is null ? null : MapDetails(shoppingList);
    }

    public async Task<ShoppingListDetails> CreateAsync(
        Guid userId,
        CreateShoppingListCommand command,
        CancellationToken cancellationToken)
    {
        if (command.Id is not null)
        {
            var existingList = await FindOwnedListAsync(userId, command.Id.Value, cancellationToken);
            if (existingList is not null)
            {
                return MapDetails(existingList);
            }
        }

        var shoppingList = ShoppingList.Create(userId, command.Name, DateTimeOffset.UtcNow, command.Id);

        dbContext.ShoppingLists.Add(shoppingList);
        await dbContext.SaveChangesAsync(cancellationToken);

        return MapDetails(shoppingList);
    }

    public async Task<ShoppingListDetails?> AddItemAsync(
        Guid userId,
        Guid shoppingListId,
        AddShoppingListItemCommand command,
        CancellationToken cancellationToken)
    {
        var shoppingList = await FindOwnedListAsync(userId, shoppingListId, cancellationToken);
        if (shoppingList is null)
        {
            return null;
        }

        if (command.Id is not null && shoppingList.Items.Any(item => item.Id == command.Id))
        {
            return MapDetails(shoppingList);
        }

        var item = shoppingList.AddItem(
            command.Name,
            command.Quantity,
            command.UnitOfMeasure,
            command.Id);
        dbContext.Add(item);
        await dbContext.SaveChangesAsync(cancellationToken);

        return MapDetails(shoppingList);
    }

    public async Task<ShoppingListDetails?> SetItemStatusAsync(
        Guid userId,
        Guid shoppingListId,
        Guid itemId,
        ShoppingListItemStatus status,
        CancellationToken cancellationToken)
    {
        var shoppingList = await FindOwnedListAsync(userId, shoppingListId, cancellationToken);
        if (shoppingList is null)
        {
            return null;
        }

        shoppingList.SetItemStatus(itemId, status);
        await dbContext.SaveChangesAsync(cancellationToken);

        return MapDetails(shoppingList);
    }

    private async Task<ShoppingList?> FindOwnedListAsync(
        Guid userId,
        Guid shoppingListId,
        CancellationToken cancellationToken)
    {
        return await dbContext.ShoppingLists
            .Include(shoppingList => shoppingList.Items)
            .SingleOrDefaultAsync(
                shoppingList => shoppingList.Id == shoppingListId && shoppingList.UserId == userId,
                cancellationToken);
    }

    private static ShoppingListDetails MapDetails(ShoppingList shoppingList)
    {
        var items = shoppingList.Items
            .OrderBy(item => item.Position)
            .Select(item => new ShoppingListItemDetails(
                item.Id,
                item.Name,
                item.Quantity,
                item.UnitOfMeasure,
                item.Status,
                item.Position))
            .ToList();

        return new ShoppingListDetails(
            shoppingList.Id,
            shoppingList.Name,
            shoppingList.CreatedAtUtc,
            shoppingList.ArchivedAtUtc,
            items);
    }
}
