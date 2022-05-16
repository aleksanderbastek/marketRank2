import org.springframework.web.client.RestTemplate
import java.io.File
import java.util.*

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

fun getSpread(ticker_id: String): SpreadOfMarket {
    val orderBook = RestTemplate().getForObject("https://public.kanga.exchange/api/market/orderbook/$ticker_id", OrderBook::class.java)
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

fun getSpreadsOfAllMarkets(): SortedSpreadOfMarket {
    val markets: List<String> = getTickerIds()
    val spreadsOfAllMarkets: List<SpreadOfMarket> =  markets.map { getSpread(it) }

    val lessOrEqual2: List<SpreadOfMarket> = spreadsOfAllMarkets.filter { it.spread != null &&  it.spread <= 2.0}.sortedBy { it.ticker_id }
    val moreThan2: List<SpreadOfMarket> = spreadsOfAllMarkets.filter { it.spread != null &&  it.spread > 2.0}.sortedBy { it.ticker_id }
    val useless: List<SpreadOfMarket> = spreadsOfAllMarkets.filter { it.spread == null}.sortedBy { it.ticker_id }

    val sortedSpreadOfMarket: SortedSpreadOfMarket = SortedSpreadOfMarket(lessOrEqual2, moreThan2, useless)

    return sortedSpreadOfMarket
}

fun getTickerIds(): List<String> {
    val markets = RestTemplate().getForObject("https://public.kanga.exchange/api/market/pairs", Array<Market>::class.java).toList();
    return markets
        ?.map { item -> item.ticker_id }
}

fun writeMarketsToFile(sortedSpreadOfMarket: SortedSpreadOfMarket) {

    // create File
    val dateFormat = DateTimeFormatter.ofPattern("yyyy_MM_dd'T'HH_mm_ss'Z'")
    val zonedDate: String = ZonedDateTime.now(ZoneId.of("UTC+0")).format(dateFormat)

    val fileName: String = "report_spread_${zonedDate}.txt"
    var fileObject = File(fileName)

    val isNewFileCreated :Boolean = fileObject.createNewFile()
    if(isNewFileCreated){
        println("$fileName is created successfully.")
    } else{
        println("$fileName already exists.")
    }

    File(fileName).appendText("Spread <= 2 \n")
    File(fileName).appendText("Nazwa rynku  Spread[%] \n")
    sortedSpreadOfMarket.lessOrEqual2.forEach {
        File(fileName).appendText("${it.ticker_id} ${it.spread}% \n")
    }

    File(fileName).appendText("\nSpread >= 2 \n")
    File(fileName).appendText("Nazwa rynku  Spread[%] \n")
    sortedSpreadOfMarket.moreThan2.forEach {
        File(fileName).appendText("${it.ticker_id} ${it.spread}% \n")
    }

    File(fileName).appendText("\nRynki bez płynności \n")
    File(fileName).appendText("Nazwa rynku  Spread[%] \n")
    sortedSpreadOfMarket.useless.forEach {
        File(fileName).appendText("${it.ticker_id} -\n")
    }
}

fun main(args: Array<String>) {
    val sortedSpreadOfMarket: SortedSpreadOfMarket = getSpreadsOfAllMarkets()
    writeMarketsToFile(sortedSpreadOfMarket)

    //val dateFormat = DateTimeFormatter.ofPattern("yyyy_MM_dd'T'HH:mm:ss'Z'")
    //val zonedDate: String = ZonedDateTime.now(ZoneId.of("UTC+0")).format(dateFormat)

    //println("report_$zonedDate")
}

data class Market(val ticker_id: String)
data class OrderBook(val bids: List<List<Double>>, val asks: List<List<Double>>)
data class SpreadOfMarket(val ticker_id: String, val spread: Double?)
data class SortedSpreadOfMarket(val lessOrEqual2: List<SpreadOfMarket>, val moreThan2: List<SpreadOfMarket>,val useless: List<SpreadOfMarket>,)