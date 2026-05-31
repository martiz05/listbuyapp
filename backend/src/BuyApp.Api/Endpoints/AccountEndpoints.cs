using System.Security.Claims;
using BuyApp.Infrastructure.Identity;

namespace BuyApp.Api.Endpoints;

public static class AccountEndpoints
{
    public static IEndpointRouteBuilder MapAccountEndpoints(this IEndpointRouteBuilder endpoints)
    {
        endpoints.MapGroup("/api/v1/auth")
            .WithTags("Authentication")
            .MapIdentityApi<ApplicationUser>();

        endpoints.MapGet("/api/v1/account/me", (ClaimsPrincipal principal) =>
            {
                var response = new CurrentUserResponse(
                    principal.FindFirstValue(ClaimTypes.NameIdentifier)!,
                    principal.FindFirstValue(ClaimTypes.Email));

                return Results.Ok(response);
            })
            .RequireAuthorization()
            .WithTags("Account");

        return endpoints;
    }

    private sealed record CurrentUserResponse(string Id, string? Email);
}
