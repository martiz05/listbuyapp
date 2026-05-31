using BuyApp.Domain.Common;

namespace BuyApp.Domain.Currencies;

public sealed class ExchangeRate
{
    public ExchangeRate(
        CurrencyCode baseCurrency,
        CurrencyCode foreignCurrency,
        decimal foreignToBaseRate,
        DateOnly effectiveOn)
    {
        if (baseCurrency == foreignCurrency)
        {
            throw new DomainException("An exchange rate requires two different currencies.");
        }

        if (foreignToBaseRate <= 0)
        {
            throw new DomainException("An exchange rate must be greater than zero.");
        }

        BaseCurrency = baseCurrency;
        ForeignCurrency = foreignCurrency;
        ForeignToBaseRate = foreignToBaseRate;
        EffectiveOn = effectiveOn;
    }

    public CurrencyCode BaseCurrency { get; }

    public CurrencyCode ForeignCurrency { get; }

    public decimal ForeignToBaseRate { get; }

    public DateOnly EffectiveOn { get; }

    public Money ConvertToBase(Money amount)
    {
        if (amount.Currency != ForeignCurrency)
        {
            throw new DomainException(
                $"Expected an amount in {ForeignCurrency}, but received {amount.Currency}.");
        }

        return new Money(amount.Amount * ForeignToBaseRate, BaseCurrency);
    }
}
