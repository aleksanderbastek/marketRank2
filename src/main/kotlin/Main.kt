import org.marketRank2Utilities.SpreadUtilities
import org.marketRank2Models.SortedSpreadOfMarket as SortedSpreadOfMarket
fun main(args: Array<String>) {
    val spreadUtilities: SpreadUtilities = SpreadUtilities()
    println("Wait for fetching data")
    val sortedSpreadOfMarket: SortedSpreadOfMarket = spreadUtilities.getSpreadsOfAllMarkets()
    println("Wait for writing data into a file")
    spreadUtilities.writeMarketsToFile(sortedSpreadOfMarket)
}

data class SortedSpreadOfMarket(val lessOrEqual2: List<SortedSpreadOfMarket>, val moreThan2: List<SortedSpreadOfMarket>, val useless: List<SortedSpreadOfMarket>)