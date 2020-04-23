package xyz.mperminov.parser

import org.junit.Assert.assertTrue
import org.junit.Test
import java.net.URI

class HrefStringParserTest {

    private val testStringNormal =
        "\"<a href=\"https://www.metal-archives.com/bands/Herrschaft/79364\">Herrschaft</a>\""
    private val testStringWithoutQuotes =
        "<a href=\"https://www.metal-archives.com/bands/Herrschaft/79364\">Herrschaft</a>"
    private val testStringWithoutLink = "<a href=>Herrschaft</a>"
    private val testStringOnlyAnchor = "<a>Herrschaft</a>"
    private val resultHrefInside = "Herrschaft"
    private val resultHrefLink = Link(URI("https://www.metal-archives.com/bands/Herrschaft/79364"))
    private val emptyString = ""
    private val randomString = "ferg345gg35g"

    //region Text inside href tag tests
    @Test
    fun testStringHrefInside_Normal() {
        assertTrue(
            HrefStringParser(
                RegexFactory().regex<String>(),
                RegexFactory().regex<Link>()
            ).hrefText(testStringNormal) == resultHrefInside
        )
    }

    @Test
    fun testStringHrefInside_WithoutQoutes() {
        assertTrue(
            HrefStringParser(
                RegexFactory().regex<String>(),
                RegexFactory().regex<Link>()
            ).hrefText(testStringWithoutQuotes) == resultHrefInside
        )
    }

    @Test
    fun testStringHrefInside_WithoutLink() {
        assertTrue(
            HrefStringParser(
                RegexFactory().regex<String>(),
                RegexFactory().regex<Link>()
            ).hrefText(testStringWithoutLink) == resultHrefInside
        )
    }

    @Test
    fun testStringHrefInside_OnlyAnchor() {
        assertTrue(
            HrefStringParser(
                RegexFactory().regex<String>(),
                RegexFactory().regex<Link>()
            ).hrefText(testStringOnlyAnchor) == resultHrefInside
        )
    }

    @Test
    fun testStringHrefInside_Empty() {
        assertTrue(
            HrefStringParser(
                RegexFactory().regex<String>(),
                RegexFactory().regex<Link>()
            ).hrefText(emptyString) == ""
        )
    }

    @Test
    fun testStringHrefInside_Random() {
        assertTrue(
            HrefStringParser(
                RegexFactory().regex<String>(),
                RegexFactory().regex<Link>()
            ).hrefText(randomString) == ""
        )
    }

    //endregion
//
//    //region Links of href tag tests
    @Test
    fun testLinkHref_Normal() {
        assertTrue(
            HrefStringParser(
                RegexFactory().regex<String>(),
                RegexFactory().regex<Link>()
            ).link(testStringNormal) == resultHrefLink
        )
    }

    @Test
    fun testLinksHref_WithoutQoutes() {
        assertTrue(
            HrefStringParser(
                RegexFactory().regex<String>(),
                RegexFactory().regex<Link>()
            ).link(testStringWithoutQuotes) == resultHrefLink
        )
    }

    @Test
    fun testLinksHref_WithoutLink() {
        assertTrue(
            HrefStringParser(
                RegexFactory().regex<String>(),
                RegexFactory().regex<Link>()
            ).link(testStringWithoutLink) == Link.EMPTY_LINK
        )
    }

    @Test
    fun testLinksHref_Empty() {
        assertTrue(
            HrefStringParser(
                RegexFactory().regex<String>(),
                RegexFactory().regex<Link>()
            ).link(emptyString) == Link.EMPTY_LINK
        )
    }

    @Test
    fun testLinksHref_Random() {
        assertTrue(
            HrefStringParser(
                RegexFactory().regex<String>(),
                RegexFactory().regex<Link>()
            ).link(randomString) == Link.EMPTY_LINK
        )
    }
}