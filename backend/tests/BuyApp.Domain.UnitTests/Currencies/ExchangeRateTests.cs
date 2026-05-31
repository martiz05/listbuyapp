using BuyApp.Domain.Common;
using BuyApp.Domain.Currencies;

namespace BuyApp.Domain.UnitTests.Currencies;

public sealed class ExchangeRateTests
{
    [Fact]
    public void ConvertToBase_UsesHistoricalForeignToBaseRate()
    {
        var rate = new ExchangeRate(
            new CurrencyCode("NIO"),
            new CurrencyCode("USD"),
            36.6243m,
            new DateOnly(2026, 5, 31));

        var converted = rate.ConvertToBase(new Money(10m, new CurrencyCode("USD")));

        Assert.Equal(366.243m, converted.Amount);
        Assert.Equal(new CurrencyCode("NIO"), converted.Currency);
    }

    [Fact]
    public void Constructor_RejectsNonPositiveRate()
    {
        Assert.Throws<DomainException>(() =>
            new ExchangeRate(
                new CurrencyCode("NIO"),
                new CurrencyCode("USD"),
                0m,
                new DateOnly(2026, 5, 31)));
    }

    [Fact]
    public void ConvertToBase_RejectsUnexpectedCurrency()
    {
        var rate = new ExchangeRate(
            new CurrencyCode("NIO"),
            new CurrencyCode("USD"),
            36.6243m,
            new DateOnly(2026, 5, 31));

        Assert.Throws<DomainException>(() =>
            rate.ConvertToBase(new Money(10m, new CurrencyCode("EUR"))));
    }
}
