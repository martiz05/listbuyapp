using System.Text.Json;
using System.Text.Json.Serialization;
using BuyApp.Api.Endpoints;
using BuyApp.Api.Errors;
using BuyApp.Application;
using BuyApp.Infrastructure;

var builder = WebApplication.CreateBuilder(args);

builder.Services.AddOpenApi();
builder.Services.AddHealthChecks();
builder.Services.AddProblemDetails();
builder.Services.AddExceptionHandler<DomainExceptionHandler>();
builder.Services.ConfigureHttpJsonOptions(options =>
{
    options.SerializerOptions.Converters.Add(new JsonStringEnumConverter(JsonNamingPolicy.CamelCase));
});
builder.Services
    .AddApplication()
    .AddInfrastructure(builder.Configuration);

var app = builder.Build();

if (app.Environment.IsDevelopment())
{
    app.MapOpenApi();
}

app.UseExceptionHandler();
app.MapHealthChecks("/health");
app.MapAccountEndpoints();
app.MapShoppingListEndpoints();

app.Run();

public partial class Program;
