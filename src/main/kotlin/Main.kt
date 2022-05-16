import SpreadUtilities.SortedSpreadOfMarket
import SpreadUtilities.getSpreadsOfAllMarkets
import SpreadUtilities.writeMarketsToFile

fun main(args: Array<String>) {
    println("Wait for fetching data")
    val sortedSpreadOfMarket: SortedSpreadOfMarket = getSpreadsOfAllMarkets()
    println("Wait for writing data into a file")
    writeMarketsToFile(sortedSpreadOfMarket)
}

