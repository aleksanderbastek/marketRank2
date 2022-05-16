import SpreadUtilities.SortedSpreadOfMarket
import SpreadUtilities.getSpreadsOfAllMarkets
import SpreadUtilities.writeMarketsToFile

fun main(args: Array<String>) {
    val sortedSpreadOfMarket: SortedSpreadOfMarket = getSpreadsOfAllMarkets()
    writeMarketsToFile(sortedSpreadOfMarket)
}

