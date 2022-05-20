import org.marketRank2Utilities.SpreadUtilities
import java.math.BigDecimal
import java.math.RoundingMode
import org.marketRank2Models.SortedSpreadOfMarket as SortedSpreadOfMarket
fun main(args: Array<String>) {
    val spreadUtilities: SpreadUtilities = SpreadUtilities()
    println("Wait for fetching data")
    val sortedSpreadOfMarket: SortedSpreadOfMarket = spreadUtilities.getSpreadsOfAllMarkets()
    println("Wait for writing data into a file")
    spreadUtilities.writeMarketsToFile(sortedSpreadOfMarket)
}