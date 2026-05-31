using BuyApp.Domain.Common;

namespace BuyApp.Domain.ShoppingLists;

public sealed class ShoppingListItem
{
    private const int MaximumNameLength = 160;

    private ShoppingListItem()
    {
    }

    private ShoppingListItem(
        Guid id,
        Guid shoppingListId,
        string name,
        decimal quantity,
        UnitOfMeasure unitOfMeasure,
        int position)
    {
        Id = id;
        ShoppingListId = shoppingListId;
        Name = ValidateName(name);
        Quantity = ValidateQuantity(quantity);
        UnitOfMeasure = unitOfMeasure;
        Position = position;
        Status = ShoppingListItemStatus.Pending;
    }

    public Guid Id { get; private set; }

    public Guid ShoppingListId { get; private set; }

    public string Name { get; private set; } = string.Empty;

    public decimal Quantity { get; private set; }

    public UnitOfMeasure UnitOfMeasure { get; private set; }

    public ShoppingListItemStatus Status { get; private set; }

    public int Position { get; private set; }

    internal static ShoppingListItem Create(
        Guid shoppingListId,
        string name,
        decimal quantity,
        UnitOfMeasure unitOfMeasure,
        int position,
        Guid? id = null)
    {
        return new ShoppingListItem(
            id ?? Guid.NewGuid(),
            shoppingListId,
            name,
            quantity,
            unitOfMeasure,
            position);
    }

    public void SetStatus(ShoppingListItemStatus status)
    {
        Status = status;
    }

    private static string ValidateName(string name)
    {
        ArgumentException.ThrowIfNullOrWhiteSpace(name);

        var normalizedName = name.Trim();
        if (normalizedName.Length > MaximumNameLength)
        {
            throw new DomainException($"Item names cannot exceed {MaximumNameLength} characters.");
        }

        return normalizedName;
    }

    private static decimal ValidateQuantity(decimal quantity)
    {
        if (quantity <= 0)
        {
            throw new DomainException("Item quantities must be greater than zero.");
        }

        return quantity;
    }
}
