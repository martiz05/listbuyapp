using BuyApp.Domain.Common;
using BuyApp.Domain.ShoppingLists;

namespace BuyApp.Domain.UnitTests.ShoppingLists;

public sealed class ShoppingListTests
{
    [Fact]
    public void AddItem_AddsPendingItemWithDecimalQuantity()
    {
        var shoppingList = ShoppingList.Create(Guid.NewGuid(), "Weekly groceries", DateTimeOffset.UtcNow);

        var item = shoppingList.AddItem("Bananas", 1.75m, UnitOfMeasure.Kilogram);

        Assert.Equal("Bananas", item.Name);
        Assert.Equal(1.75m, item.Quantity);
        Assert.Equal(ShoppingListItemStatus.Pending, item.Status);
    }

    [Fact]
    public void SetItemStatus_SelectsExistingItem()
    {
        var shoppingList = ShoppingList.Create(Guid.NewGuid(), "Weekly groceries", DateTimeOffset.UtcNow);
        var item = shoppingList.AddItem("Milk", 2m, UnitOfMeasure.Liter);

        shoppingList.SetItemStatus(item.Id, ShoppingListItemStatus.Selected);

        Assert.Equal(ShoppingListItemStatus.Selected, item.Status);
    }

    [Fact]
    public void AddItem_RejectsNonPositiveQuantity()
    {
        var shoppingList = ShoppingList.Create(Guid.NewGuid(), "Weekly groceries", DateTimeOffset.UtcNow);

        Assert.Throws<DomainException>(() =>
            shoppingList.AddItem("Milk", 0m, UnitOfMeasure.Liter));
    }

    [Fact]
    public void AddItem_RejectsArchivedList()
    {
        var shoppingList = ShoppingList.Create(Guid.NewGuid(), "Weekly groceries", DateTimeOffset.UtcNow);
        shoppingList.Archive(DateTimeOffset.UtcNow);

        Assert.Throws<DomainException>(() =>
            shoppingList.AddItem("Milk", 1m, UnitOfMeasure.Liter));
    }
}
