using BuyApp.Domain.Common;

namespace BuyApp.Domain.Currencies;

public readonly record struct CurrencyCode
{
    private const int IsoCodeLength = 3;

    public CurrencyCode(string value)
    {
        ArgumentException.ThrowIfNullOrWhiteSpace(value);

        var normalizedValue = value.Trim().ToUpperInvariant();
        if (normalizedValue.Length != IsoCodeLength ||
            normalizedValue.Any(character => character is < 'A' or > 'Z'))
        {
            throw new DomainException("Currency codes must contain exactly three ASCII letters.");
        }

        Value = normalizedValue;
    }

    public string Value { get; }

    public override string ToString() => Value;
}
