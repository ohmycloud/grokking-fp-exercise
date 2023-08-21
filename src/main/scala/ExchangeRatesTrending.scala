import cats.effect.IO
import cats.implicits.*
import cats.effect.unsafe.implicits.global
import ExchangeRates.exchangeRatesTableApiCall
import scala.jdk.CollectionConverters.MapHasAsScala

object model {
  opaque type Currency = String
  object Currency {
    def apply(name: String): Currency = name
    extension (currency: Currency) def name: String = currency
  }
}

object ExchangeRatesTrending extends App {
  import model._
  def exchangeRates(currency: String): Map[String, BigDecimal] = {
    exchangeRatesTableApiCall(currency).asScala.view.mapValues(BigDecimal(_)).toMap
  }

  case class RatePair(previousRate: BigDecimal, rate: BigDecimal)

  // Checking if rates are trending
  def trending(rates: List[BigDecimal]): Boolean = {
    rates.size > 1 &&
    rates.zip(rates.drop(1))
      .forall(ratePair => ratePair match { // ratePair is a tuple
        case (previousRate, rate) => rate > previousRate // use pattern match to deconstruct tuple
      })
  }

  val usdExchangeTables = List(
    Map(Currency("EUR") -> BigDecimal(0.88)),
    Map(Currency("EUR") -> BigDecimal(0.89), Currency("JPY") -> BigDecimal(114.62)),
    Map(Currency("JPY") -> BigDecimal(114))
  )

  def extractSingleCurrencyRate(currency: Currency)(table: Map[Currency, BigDecimal]): Option[BigDecimal] = {
    table.get(currency)
  }

  def exchangeTable(from: Currency): IO[Map[Currency, BigDecimal]] = {
    IO.delay(exchangeRates(from.name)).map(table =>
      table.map(kv => // kv is a tuple of (String, BigDecimal)
        kv match {
          // use pattern match to deconstruct tuple
          case (currencyName, rate) => (Currency(currencyName), rate)
        }
      )
    )
  }

  def retry[A](action: IO[A], maxRetries: Int): IO[A] = {
    List.range(0, maxRetries)
      .map(_ => action)
      .foldLeft(action)((program, retryAction) =>
        program.orElse(retryAction)
      )
  }

  def lastRates(from: Currency, to: Currency): IO[List[BigDecimal]] = {
    for {
      table1 <- retry(exchangeTable(from), 10)
      table2 <- retry(exchangeTable(from), 10)
      table3 <- retry(exchangeTable(from), 10)
      lastTables = List(table1, table2, table3)
    } yield lastTables.flatMap(extractSingleCurrencyRate(to))
  }

  // The top-down approach is about starting with a signature and implementing it.
  // The bottom-up approach is looking for small problems and starting with them.
  def exchangeIfTrending(amount: BigDecimal,
                         from: Currency,
                         to: Currency): IO[Option[BigDecimal]] = {
    lastRates(from, to).map(rates =>
      if (trending(rates)) Some(amount * rates.last) else None
    )
  }

  def exchangeIfTrendingRecursive(amount: BigDecimal,
                         from: Currency,
                         to: Currency): IO[Option[BigDecimal]] = {
    for {
      rates <- lastRates(from, to)
      result <- if (trending(rates)) IO.pure(Some(amount * rates.last))
                else exchangeIfTrending(amount, from, to)
    } yield result
  }
}
