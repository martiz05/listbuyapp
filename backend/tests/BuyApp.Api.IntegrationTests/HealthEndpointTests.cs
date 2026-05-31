using Microsoft.AspNetCore.Hosting;
using Microsoft.AspNetCore.Mvc.Testing;

namespace BuyApp.Api.IntegrationTests;

public sealed class HealthEndpointTests : IClassFixture<HealthEndpointTests.BuyAppApiFactory>
{
    private readonly HttpClient _client;

    public HealthEndpointTests(BuyAppApiFactory factory)
    {
        _client = factory.CreateClient();
    }

    [Fact]
    public async Task GetHealth_ReturnsOk()
    {
        var response = await _client.GetAsync("/health");

        response.EnsureSuccessStatusCode();
    }

    public sealed class BuyAppApiFactory : WebApplicationFactory<Program>
    {
        protected override void ConfigureWebHost(IWebHostBuilder builder)
        {
            builder.UseSetting(
                "ConnectionStrings:BuyAppDatabase",
                "Host=localhost;Database=buyapp_tests;Username=test;Password=test");
        }
    }
}
