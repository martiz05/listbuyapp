using BuyApp.Domain.ShoppingLists;
using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Metadata.Builders;

namespace BuyApp.Infrastructure.Persistence.Configurations;

public sealed class ShoppingListConfiguration : IEntityTypeConfiguration<ShoppingList>
{
    public void Configure(EntityTypeBuilder<ShoppingList> builder)
    {
        builder.ToTable("shopping_lists");
        builder.HasKey(shoppingList => shoppingList.Id);
        builder.Property(shoppingList => shoppingList.Id)
            .ValueGeneratedNever();

        builder.Property(shoppingList => shoppingList.Name)
            .HasMaxLength(120)
            .IsRequired();

        builder.HasIndex(shoppingList => new { shoppingList.UserId, shoppingList.ArchivedAtUtc });

        builder.HasMany(shoppingList => shoppingList.Items)
            .WithOne()
            .HasForeignKey(item => item.ShoppingListId)
            .OnDelete(DeleteBehavior.Cascade);

        builder.Navigation(shoppingList => shoppingList.Items)
            .UsePropertyAccessMode(PropertyAccessMode.Field);
    }
}
