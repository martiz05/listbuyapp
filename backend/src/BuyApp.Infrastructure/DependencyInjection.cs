using BuyApp.Application.ShoppingLists;
using BuyApp.Infrastructure.Identity;
using BuyApp.Infrastructure.Persistence;
using BuyApp.Infrastructure.ShoppingLists;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.DependencyInjection;

namespace BuyApp.Infrastructure;

public static class DependencyInjection
{
    public static IServiceCollection AddInfrastructure(
        this IServiceCollection services,
        IConfiguration configuration)
    {
        var connectionString = configuration.GetConnectionString("BuyAppDatabase");
        if (string.IsNullOrWhiteSpace(connectionString))
        {
            throw new InvalidOperationException(
                "Connection string 'BuyAppDatabase' must be configured outside source control.");
        }

        services.AddDbContext<BuyAppDbContext>(options => options.UseNpgsql(connectionString));

        services
            .AddIdentityApiEndpoints<ApplicationUser>()
            .AddEntityFrameworkStores<BuyAppDbContext>();

        services.AddAuthorization();
        services.AddScoped<IShoppingListService, ShoppingListService>();

        return services;
    }
}
