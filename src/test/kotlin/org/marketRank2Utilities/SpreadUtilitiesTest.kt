package org.marketRank2Utilities

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import java.math.BigDecimal
import java.math.RoundingMode

internal class SpreadUtilitiesTest {
    val spreadUtilities: SpreadUtilities = SpreadUtilities()
    @Test
    fun countSpread() {
        val expected = 7.64
        val result = BigDecimal(spreadUtilities.countSpread("EUR_PLN", listOf(4.2610), listOf(4.5997)).spread).setScale(2, RoundingMode.HALF_EVEN)
        println(result)
        assertEquals(expected, result.toDouble())
    }

    @Test
    fun getOrderBook() {
        val expected = 200
        val ticker_id = "BTC_EUR"
        assertEquals(200, spreadUtilities.getOrderBook(ticker_id).statusCodeValue)
    }

    @Test
    fun getPairs() {
        val expected = 200
        assertEquals(200, spreadUtilities.getPairs().statusCodeValue)
    }

}