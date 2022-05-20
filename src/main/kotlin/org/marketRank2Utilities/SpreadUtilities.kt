package org.marketRank2Utilities
import org.springframework.web.client.RestTemplate

import java.io.File
import java.util.*
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

import org.marketRank2Models.*
import org.springframework.http.ResponseEntity

class SpreadUtilities {
    val apiUrl: String = "https://public.kanga.exchange/api/market"
    fun countSpread(ticker_id: String, bids: List<Double>, asks: List<Double>): SpreadOfMarket {
        val maxBid = bids.maxOf { it }
        val minAsk = asks.minOf { it }
        val substrBidAsk = minAsk - maxBid
        val sumBidAsk = minAsk + maxBid

        return SpreadOfMarket(ticker_id,(substrBidAsk / (0.5 * sumBidAsk))*100)
    }

    // api calls
    fun getOrderBook(ticker_id: String): ResponseEntity<OrderBook> {
        val apiUrl: String = "${this.apiUrl}/orderbook/${ticker_id}"
        return RestTemplate().getForEntity(apiUrl, OrderBook::class.java)
    }

    fun getPairs(): ResponseEntity<Array<Market>> {
        val apiUrl: String = "${this.apiUrl}/pairs"
        return RestTemplate().getForEntity(apiUrl, Array<Market>::class.java)
    }

    // get single spread data for a ticker_id
    private fun getSpread(ticker_id: String): SpreadOfMarket {
        val orderBook: OrderBook = getOrderBook(ticker_id).body
        val bids: List<Double> = orderBook.bids.map {it[0]}
        val asks: List<Double> = orderBook.asks.map {it[0]}

        if (bids.isEmpty() || asks.isEmpty())
            return SpreadOfMarket(ticker_id,-1.0)

        return countSpread(ticker_id, bids, asks)
    }

    // get all ticker_ids for all markets in kanga api
    private fun getTickerIds(): List<String> {
        val markets: List<Market> = getPairs().body.toList()
        return markets
            ?.map { item -> item.ticker_id }
    }

    // get the object which include data of all spreads for all markets data divided into three categories: (less or equal 2%, more than 2% and markets without spreads)
    fun getSpreadsOfAllMarkets(): SortedSpreadOfMarket {
        val markets: List<String> = getTickerIds()
        val spreadsOfAllMarkets: List<SpreadOfMarket> =  markets.map { getSpread(it) }

        val lessOrEqual2: List<SpreadOfMarket> = spreadsOfAllMarkets.filter { it.spread != -1.0 &&  it.spread <= 2.0}.sortedBy { it.ticker_id }
        val moreThan2: List<SpreadOfMarket> = spreadsOfAllMarkets.filter { it.spread != -1.0 &&  it.spread > 2.0}.sortedBy { it.ticker_id }
        val useless: List<SpreadOfMarket> = spreadsOfAllMarkets.filter { it.spread == -1.0}.sortedBy { it.ticker_id }

        val sortedSpreadOfMarket: SortedSpreadOfMarket = SortedSpreadOfMarket(lessOrEqual2, moreThan2, useless)

        return sortedSpreadOfMarket
    }

    // return name of file in report_spread_{dateTime}.txt format
    private fun getFileName(): String {
        val dateFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy_MM_dd'T'HH_mm_ss'Z'")
        val zonedDate: String = ZonedDateTime.now(ZoneId.of("UTC+0")).format(dateFormat)

        val fileName: String = "report_spread_${zonedDate}.txt"
        return fileName
    }

    // write table with markets and spreads to txt file

    fun writeMarketsToFile(sortedSpreadOfMarket: SortedSpreadOfMarket) {
        val fileName: String = getFileName()
        var spreadTable = File(fileName)

        val createFile = spreadTable.createNewFile()
        if (!createFile) {
            throw Exception("Script could not create the file!")
        } else {
            print("The File was created successfully")
        }

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
}