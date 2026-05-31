using BuyApp.Domain.ShoppingLists;
using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Metadata.Builders;

namespace BuyApp.Infrastructure.Persistence.Configurations;

public sealed class ShoppingListItemConfiguration : IEntityTypeConfiguration<ShoppingListItem>
{
    public void Configure(EntityTypeBuilder<ShoppingListItem> builder)
    {
        builder.ToTable("shopping_list_items");
        builder.HasKey(item => item.Id);
        builder.Property(item => item.Id)
            .ValueGeneratedNever();

        builder.Property(item => item.Name)
            .HasMaxLength(160)
            .IsRequired();

        builder.Property(item => item.Quantity)
            .HasPrecision(18, 4);

        builder.Property(item => item.UnitOfMeasure)
            .HasConversion<string>()
            .HasMaxLength(32);

        builder.Property(item => item.Status)
            .HasConversion<string>()
            .HasMaxLength(32);

        builder.HasIndex(item => new { item.ShoppingListId, item.Position })
            .IsUnique();
    }
}
