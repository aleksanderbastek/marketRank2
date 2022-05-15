import org.springframework.web.client.RestTemplate

fun getSpread(ticker_id: String): SpreadOfMarket {
    val orderBook = RestTemplate().getForObject("https://public.kanga.exchange/api/market/orderbook/$ticker_id", OrderBook::class.java)
    val bids: List<Double> = orderBook.bids.map {it[0]}
    val asks: List<Double> = orderBook.asks.map {it[0]}
    if (bids.isEmpty() || asks.isEmpty())
        return SpreadOfMarket(ticker_id,null)
    val minBid = bids.maxOf { it }
    val maxAsk = asks.minOf { it }
    val substrBidAsk = maxAsk - minBid
    val sumBidAsk = maxAsk + minBid

    return SpreadOfMarket(ticker_id,(substrBidAsk / (0.5 * sumBidAsk))*100)
}

fun getSpreadsOfAllMarkets(): List<SpreadOfMarket> {
    val markets: List<String> = getTickerIds()
    val spreadsOfAllMarkets: List<SpreadOfMarket> =  markets.map { getSpread(it) }

    return spreadsOfAllMarkets
}

fun getTickerIds(): List<String> {
    val markets = RestTemplate().getForObject("https://public.kanga.exchange/api/market/pairs", Array<Market>::class.java).toList();
    return markets
        ?.map { item -> item.ticker_id }
}

fun main(args: Array<String>) {
    println(getSpreadsOfAllMarkets())

    // Try adding program arguments via Run/Debug configuration.
    // Learn more about running applications: https://www.jetbrains.com/help/idea/running-applications.html.
    println("Program arguments: ${args.joinToString()}")
}

data class Market(val ticker_id: String)
data class OrderBook(val bids: List<List<Double>>, val asks: List<List<Double>>)
data class SpreadOfMarket(val ticker_id: String, val spread: Double?)