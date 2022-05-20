package org.marketRank2Models

data class Market(val ticker_id: String)
data class OrderBook(val bids: List<List<Double>>, val asks: List<List<Double>>)
data class SpreadOfMarket(val ticker_id: String, val spread: Double)
data class SortedSpreadOfMarket(val lessOrEqual2: List<SpreadOfMarket>, val moreThan2: List<SpreadOfMarket>,val useless: List<SpreadOfMarket>)
