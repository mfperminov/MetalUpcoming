package xyz.mperminov.parser

import org.junit.Assert.assertTrue
import org.junit.Test


class HrefStringParserTest {

    val testStringNormal =
        "\"<a href=\"https://www.metal-archives.com/bands/Herrschaft/79364\">Herrschaft</a>\""
    val testStringWithoutQuotes =
        "<a href=\"https://www.metal-archives.com/bands/Herrschaft/79364\">Herrschaft</a>"
    val testStringWithoutLink = "<a href=>Herrschaft</a>"
    val testStringOnlyAnchor = "<a>Herrschaft</a>"
    val resultHrefInside = "Herrschaft"
    val resultHrefLink = "https://www.metal-archives.com/bands/Herrschaft/79364"
    val emptyString = ""
    val randomString = "ferg345gg35g"


    //region Text inside href tag tests
    @Test
    fun testStringHrefInside_Normal() {
        assertTrue(HrefStringParser().getTextInsideHrefTag(testStringNormal) == resultHrefInside)
    }

    @Test
    fun testStringHrefInside_WithoutQoutes() {
        assertTrue(HrefStringParser().getTextInsideHrefTag(testStringWithoutQuotes) == resultHrefInside)
    }

    @Test
    fun testStringHrefInside_WithoutLink() {
        assertTrue(HrefStringParser().getTextInsideHrefTag(testStringWithoutLink) == resultHrefInside)
    }

    @Test
    fun testStringHrefInside_OnlyAnchor() {
        assertTrue(HrefStringParser().getTextInsideHrefTag(testStringOnlyAnchor) == resultHrefInside)
    }

    @Test
    fun testStringHrefInside_Empty() {
        assertTrue(HrefStringParser().getTextInsideHrefTag(emptyString) == PARSE_ERROR)
    }

    @Test
    fun testStringHrefInside_Random() {
        assertTrue(HrefStringParser().getTextInsideHrefTag(randomString) == PARSE_ERROR)
    }
    //endregion

    //region Links of href tag tests
    @Test
    fun testLinkHref_Normal() {
        assertTrue(HrefStringParser().getLinkFromHrefTag(testStringNormal) == resultHrefLink)
    }

    @Test
    fun testLinksHref_WithoutQoutes() {
        assertTrue(HrefStringParser().getLinkFromHrefTag(testStringWithoutQuotes) == resultHrefLink)
    }

    @Test
    fun testLinksHref_WithoutLink() {
        assertTrue(HrefStringParser().getLinkFromHrefTag(testStringWithoutLink) == PARSE_ERROR)
    }

    @Test
    fun testLinksHref_Empty() {
        assertTrue(HrefStringParser().getLinkFromHrefTag(emptyString) == PARSE_ERROR)
    }

    @Test
    fun testLinksHref_Random() {
        assertTrue(HrefStringParser().getLinkFromHrefTag(randomString) == PARSE_ERROR)
    }
}