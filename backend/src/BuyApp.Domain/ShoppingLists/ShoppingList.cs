using BuyApp.Domain.Common;

namespace BuyApp.Domain.ShoppingLists;

public sealed class ShoppingList
{
    private const int MaximumNameLength = 120;
    private readonly List<ShoppingListItem> _items = [];

    private ShoppingList()
    {
    }

    private ShoppingList(Guid id, Guid userId, string name, DateTimeOffset createdAtUtc)
    {
        if (userId == Guid.Empty)
        {
            throw new DomainException("A shopping list requires an owner.");
        }

        Id = id;
        UserId = userId;
        Name = ValidateName(name);
        CreatedAtUtc = createdAtUtc;
    }

    public Guid Id { get; private set; }

    public Guid UserId { get; private set; }

    public string Name { get; private set; } = string.Empty;

    public DateTimeOffset CreatedAtUtc { get; private set; }

    public DateTimeOffset? ArchivedAtUtc { get; private set; }

    public IReadOnlyCollection<ShoppingListItem> Items => _items.AsReadOnly();

    public static ShoppingList Create(
        Guid userId,
        string name,
        DateTimeOffset createdAtUtc,
        Guid? id = null)
    {
        return new ShoppingList(id ?? Guid.NewGuid(), userId, name, createdAtUtc);
    }

    public ShoppingListItem AddItem(
        string name,
        decimal quantity,
        UnitOfMeasure unitOfMeasure,
        Guid? id = null)
    {
        EnsureActive();

        var item = ShoppingListItem.Create(Id, name, quantity, unitOfMeasure, _items.Count, id);
        _items.Add(item);

        return item;
    }

    public void SetItemStatus(Guid itemId, ShoppingListItemStatus status)
    {
        EnsureActive();

        var item = _items.SingleOrDefault(candidate => candidate.Id == itemId);
        if (item is null)
        {
            throw new DomainException("The shopping list item was not found.");
        }

        item.SetStatus(status);
    }

    public void Archive(DateTimeOffset archivedAtUtc)
    {
        ArchivedAtUtc ??= archivedAtUtc;
    }

    private void EnsureActive()
    {
        if (ArchivedAtUtc is not null)
        {
            throw new DomainException("Archived shopping lists cannot be modified.");
        }
    }

    private static string ValidateName(string name)
    {
        ArgumentException.ThrowIfNullOrWhiteSpace(name);

        var normalizedName = name.Trim();
        if (normalizedName.Length > MaximumNameLength)
        {
            throw new DomainException($"Shopping list names cannot exceed {MaximumNameLength} characters.");
        }

        return normalizedName;
    }
}
