using System.Security.Claims;
using BuyApp.Application.ShoppingLists;
using BuyApp.Domain.ShoppingLists;

namespace BuyApp.Api.Endpoints;

public static class ShoppingListEndpoints
{
    public static IEndpointRouteBuilder MapShoppingListEndpoints(this IEndpointRouteBuilder endpoints)
    {
        var group = endpoints.MapGroup("/api/v1/shopping-lists")
            .RequireAuthorization()
            .WithTags("Shopping lists");

        group.MapGet("/", async (
            ClaimsPrincipal principal,
            IShoppingListService service,
            CancellationToken cancellationToken) =>
        {
            return Results.Ok(await service.GetAllAsync(GetUserId(principal), cancellationToken));
        });

        group.MapGet("/{shoppingListId:guid}", async (
            Guid shoppingListId,
            ClaimsPrincipal principal,
            IShoppingListService service,
            CancellationToken cancellationToken) =>
        {
            var shoppingList = await service.GetByIdAsync(
                GetUserId(principal),
                shoppingListId,
                cancellationToken);

            return shoppingList is null ? Results.NotFound() : Results.Ok(shoppingList);
        });

        group.MapPost("/", async (
            CreateShoppingListCommand command,
            ClaimsPrincipal principal,
            IShoppingListService service,
            CancellationToken cancellationToken) =>
        {
            var shoppingList = await service.CreateAsync(
                GetUserId(principal),
                command,
                cancellationToken);

            return Results.Created($"/api/v1/shopping-lists/{shoppingList.Id}", shoppingList);
        });

        group.MapPost("/{shoppingListId:guid}/items", async (
            Guid shoppingListId,
            AddShoppingListItemCommand command,
            ClaimsPrincipal principal,
            IShoppingListService service,
            CancellationToken cancellationToken) =>
        {
            var shoppingList = await service.AddItemAsync(
                GetUserId(principal),
                shoppingListId,
                command,
                cancellationToken);

            return shoppingList is null ? Results.NotFound() : Results.Ok(shoppingList);
        });

        group.MapPatch("/{shoppingListId:guid}/items/{itemId:guid}/status", async (
            Guid shoppingListId,
            Guid itemId,
            SetShoppingListItemStatusRequest request,
            ClaimsPrincipal principal,
            IShoppingListService service,
            CancellationToken cancellationToken) =>
        {
            var shoppingList = await service.SetItemStatusAsync(
                GetUserId(principal),
                shoppingListId,
                itemId,
                request.Status,
                cancellationToken);

            return shoppingList is null ? Results.NotFound() : Results.Ok(shoppingList);
        });

        return endpoints;
    }

    private static Guid GetUserId(ClaimsPrincipal principal)
    {
        var userId = principal.FindFirstValue(ClaimTypes.NameIdentifier);
        return Guid.TryParse(userId, out var parsedUserId)
            ? parsedUserId
            : throw new InvalidOperationException("Authenticated requests require a valid user identifier.");
    }

    private sealed record SetShoppingListItemStatusRequest(ShoppingListItemStatus Status);
}
