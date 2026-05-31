using BuyApp.Domain.Common;

namespace BuyApp.Domain.Currencies;

public readonly record struct Money
{
    public Money(decimal amount, CurrencyCode currency)
    {
        if (amount < 0)
        {
            throw new DomainException("Money amounts cannot be negative.");
        }

        Amount = amount;
        Currency = currency;
    }

    public decimal Amount { get; }

    public CurrencyCode Currency { get; }
}
