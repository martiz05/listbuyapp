using BuyApp.Domain.ShoppingLists;
using BuyApp.Infrastructure.Identity;
using Microsoft.AspNetCore.Identity;
using Microsoft.AspNetCore.Identity.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore;

namespace BuyApp.Infrastructure.Persistence;

public sealed class BuyAppDbContext(DbContextOptions<BuyAppDbContext> options)
    : IdentityDbContext<ApplicationUser, IdentityRole<Guid>, Guid>(options)
{
    public DbSet<ShoppingList> ShoppingLists => Set<ShoppingList>();

    protected override void OnModelCreating(ModelBuilder builder)
    {
        base.OnModelCreating(builder);
        builder.ApplyConfigurationsFromAssembly(typeof(BuyAppDbContext).Assembly);
    }
}
