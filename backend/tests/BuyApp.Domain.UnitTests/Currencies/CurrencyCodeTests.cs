using BuyApp.Domain.Common;
using BuyApp.Domain.Currencies;

namespace BuyApp.Domain.UnitTests.Currencies;

public sealed class CurrencyCodeTests
{
    [Theory]
    [InlineData("nio", "NIO")]
    [InlineData(" USD ", "USD")]
    public void Constructor_NormalizesValidIsoCode(string input, string expected)
    {
        var currency = new CurrencyCode(input);

        Assert.Equal(expected, currency.Value);
    }

    [Theory]
    [InlineData("US")]
    [InlineData("EURO")]
    [InlineData("U1D")]
    public void Constructor_RejectsInvalidIsoCode(string input)
    {
        Assert.Throws<DomainException>(() => new CurrencyCode(input));
    }
}
