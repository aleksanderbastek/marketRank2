package SpreadUtilities

import org.springframework.web.client.RestTemplate

import java.io.File
import java.util.*
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

// get single spread data for a ticker_id
private fun getSpread(ticker_id: String): SpreadOfMarket {
    val apiUrl: String = "https://public.kanga.exchange/api/market/orderbook/"
    val orderBook = RestTemplate().getForObject("$apiUrl$ticker_id", OrderBook::class.java)
    val bids: List<Double> = orderBook.bids.map {it[0]}
    val asks: List<Double> = orderBook.asks.map {it[0]}

    if (bids.isEmpty() || asks.isEmpty())
        return SpreadOfMarket(ticker_id,null)

    val maxBid = bids.maxOf { it }
    val minAsk = asks.minOf { it }
    val substrBidAsk = minAsk - maxBid
    val sumBidAsk = minAsk + maxBid

    return SpreadOfMarket(ticker_id,(substrBidAsk / (0.5 * sumBidAsk))*100)
}

// get all ticker_ids for all markets in kanga api
private fun getTickerIds(): List<String> {
    val apiUrl: String = "https://public.kanga.exchange/api/market/pairs"
    val markets = RestTemplate().getForObject(apiUrl, Array<Market>::class.java).toList()

    return markets
        ?.map { item -> item.ticker_id }
}

// get the object which include data of all spreads for all markets data divided into three categories: (less or equal 2%, more than 2% and markets without spreads)
fun getSpreadsOfAllMarkets(): SortedSpreadOfMarket {
    val markets: List<String> = getTickerIds()
    val spreadsOfAllMarkets: List<SpreadOfMarket> =  markets.map { getSpread(it) }

    val lessOrEqual2: List<SpreadOfMarket> = spreadsOfAllMarkets.filter { it.spread != null &&  it.spread <= 2.0}.sortedBy { it.ticker_id }
    val moreThan2: List<SpreadOfMarket> = spreadsOfAllMarkets.filter { it.spread != null &&  it.spread > 2.0}.sortedBy { it.ticker_id }
    val useless: List<SpreadOfMarket> = spreadsOfAllMarkets.filter { it.spread == null}.sortedBy { it.ticker_id }

    val sortedSpreadOfMarket: SortedSpreadOfMarket = SortedSpreadOfMarket(lessOrEqual2, moreThan2, useless)

    return sortedSpreadOfMarket
}

// return name of file in report_spread_{dateTime}.txt format
fun getFileName(): String {
    val dateFormat = DateTimeFormatter.ofPattern("yyyy_MM_dd'T'HH_mm_ss'Z'")
    val zonedDate: String = ZonedDateTime.now(ZoneId.of("UTC+0")).format(dateFormat)

    val fileName: String = "report_spread_${zonedDate}.txt"
    return fileName
}

// write table with markets and spreads to txt file

fun writeMarketsToFile(sortedSpreadOfMarket: SortedSpreadOfMarket) {
    val fileName: String = getFileName()
    var spreadTable = File(fileName)

    spreadTable.createNewFile()

    spreadTable.appendText("Spread <= 2 \n")
    spreadTable.appendText("Nazwa rynku  Spread[%] \n")
    sortedSpreadOfMarket.lessOrEqual2.forEach {
        spreadTable.appendText("${it.ticker_id} ${it.spread}% \n")
    }

    spreadTable.appendText("\nSpread >= 2 \n")
    spreadTable.appendText("Nazwa rynku  Spread[%] \n")
    sortedSpreadOfMarket.moreThan2.forEach {
        spreadTable.appendText("${it.ticker_id} ${it.spread}% \n")
    }

    spreadTable.appendText("\nRynki bez płynności \n")
    spreadTable.appendText("Nazwa rynku  Spread[%] \n")
    sortedSpreadOfMarket.useless.forEach {
        spreadTable.appendText("${it.ticker_id} -\n")
    }
}

data class Market(val ticker_id: String)
data class OrderBook(val bids: List<List<Double>>, val asks: List<List<Double>>)
data class SpreadOfMarket(val ticker_id: String, val spread: Double?)
data class SortedSpreadOfMarket(val lessOrEqual2: List<SpreadOfMarket>, val moreThan2: List<SpreadOfMarket>,val useless: List<SpreadOfMarket>,)