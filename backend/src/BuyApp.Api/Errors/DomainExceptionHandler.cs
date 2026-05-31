using BuyApp.Domain.Common;
using Microsoft.AspNetCore.Diagnostics;

namespace BuyApp.Api.Errors;

public sealed class DomainExceptionHandler : IExceptionHandler
{
    public async ValueTask<bool> TryHandleAsync(
        HttpContext httpContext,
        Exception exception,
        CancellationToken cancellationToken)
    {
        if (exception is not DomainException)
        {
            return false;
        }

        await Results.Problem(
                statusCode: StatusCodes.Status400BadRequest,
                title: "A business rule was not satisfied.",
                detail: exception.Message)
            .ExecuteAsync(httpContext);

        return true;
    }
}
