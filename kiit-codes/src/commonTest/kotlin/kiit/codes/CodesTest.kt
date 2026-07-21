package kiit.codes

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

// =================================================================================================
// CodesTest — the built-in registry
// =================================================================================================

class CodesTest {
    @Test
    fun successHasCorrectValues() {
        assertEquals("SUCCESS", Codes.SUCCESS.name)
        assertEquals("kiit", Codes.SUCCESS.origin)
        assertEquals("Success", Codes.SUCCESS.message)
        assertTrue(Codes.SUCCESS.success)
    }

    @Test
    fun deniedHasCorrectValues() {
        assertEquals("DENIED", Codes.DENIED.name)
        assertEquals("kiit", Codes.DENIED.origin)
        assertFalse(Codes.DENIED.success)
    }

    @Test
    fun skippedAndDiscardedHaveDistinctNamesAndSuccessTrue() {
        assertTrue(Codes.SKIPPED.success)
        assertTrue(Codes.DISCARDED.success)
        assertTrue(Codes.SKIPPED.name != Codes.DISCARDED.name)
    }

    @Test
    fun informationCodesHaveSuccessTrue() {
        assertTrue(Codes.HELP.success)
        assertTrue(Codes.ABOUT.success)
        assertTrue(Codes.VERSION.success)
        assertTrue(Codes.EXIT.success)
    }

    @Test
    fun everyBuiltInCodeHasKiitOrigin() {
        assertTrue(Codes.all.all { it.origin == "kiit" })
    }
}

// =================================================================================================
// CodesToHttpTest / CompositeLookupTest — CodeLookup implementations
// =================================================================================================

class CodesToHttpTest {
    private val http = CodesToHttp()

    // -------------------------------------------------------------------------
    // toCode — category defaults
    // -------------------------------------------------------------------------

    @Test fun categoryDefaultSucceeded() {
        assertEquals(200, http.toCode(Codes.SUCCESS))
        assertEquals(200, http.toCode(Codes.UPDATED))
    }

    @Test fun categoryDefaultPending() {
        assertEquals(202, http.toCode(Codes.PENDING))
        assertEquals(202, http.toCode(Codes.QUEUED))
    }

    @Test fun categoryDefaultFiltered() {
        assertEquals(200, http.toCode(Codes.SKIPPED))
        assertEquals(200, http.toCode(Codes.DISCARDED))
    }

    @Test fun categoryDefaultInformation() {
        assertEquals(200, http.toCode(Codes.ABOUT))
    }

    @Test fun categoryDefaultDenied() {
        assertEquals(401, http.toCode(Codes.DENIED))
        assertEquals(401, http.toCode(Codes.UNAUTHENTICATED))
    }

    @Test fun categoryDefaultInvalid() {
        assertEquals(400, http.toCode(Codes.BAD_REQUEST))
        assertEquals(400, http.toCode(Codes.INVALID))
    }

    @Test fun categoryDefaultErrored() {
        assertEquals(500, http.toCode(Codes.ERRORED))
    }

    @Test fun categoryDefaultUnserved() {
        assertEquals(503, http.toCode(Codes.UNREACHABLE))
        assertEquals(503, http.toCode(Codes.UNDER_MAINTENANCE))
    }

    // -------------------------------------------------------------------------
    // toCode — per-code overrides
    // -------------------------------------------------------------------------

    @Test fun overrideCreated() {
        assertEquals(201, http.toCode(Codes.CREATED))
    }

    @Test fun overrideHandled() {
        assertEquals(204, http.toCode(Codes.HANDLED))
    }

    @Test fun overrideNotFound() {
        assertEquals(404, http.toCode(Codes.NOT_FOUND))
    }

    @Test fun overrideForbidden() {
        assertEquals(403, http.toCode(Codes.FORBIDDEN))
    }

    @Test fun overrideConflict() {
        assertEquals(409, http.toCode(Codes.CONFLICT))
    }

    @Test fun overrideTimeout() {
        assertEquals(408, http.toCode(Codes.TIMEOUT))
    }

    @Test fun overrideRateLimited() {
        assertEquals(429, http.toCode(Codes.RATE_LIMITED))
    }

    @Test fun overrideUnexpected() {
        assertEquals(500, http.toCode(Codes.UNEXPECTED))
    }

    /**
     * A custom, unregistered status still resolves via its category's default rather than a
     * guessed/literal fallback.
     */
    @Test
    fun toCodeFallsBackToCategoryDefaultForCustomStatus() {
        val custom = Failed.Errored("CUSTOM", "Custom error")
        assertEquals(500, http.toCode(custom)) // Errored's category default
    }

    // -------------------------------------------------------------------------
    // toStatus — reverse lookup, derived from toCode
    // -------------------------------------------------------------------------

    @Test
    fun toStatusFindsRegisteredStatusForUniqueHttpCode() {
        val status = http.toStatus(201)
        assertNotNull(status)
        assertEquals(Codes.CREATED.name, status.name)
    }

    @Test
    fun toStatusReturnsNullForUnrecognizedHttpCode() {
        // No guessed range fallback — an unrecognized code is honestly null, caller decides the default.
        assertNull(http.toStatus(999))
    }

    @Test
    fun toStatusRoundTripsForOverriddenCode() {
        val status = http.toStatus(404)
        assertNotNull(status)
        assertEquals(Codes.NOT_FOUND.name, status.name)
    }
}

class CompositeLookupTest {
    private val customCode = Failed.Errored("PAYMENT_DECLINED", "Payment declined")
    private val lookup = CompositeLookup(base = CodesToHttp(), extensions = mapOf(customCode to 402))

    @Test
    fun extensionTakesPrecedenceForToCode() {
        assertEquals(402, lookup.toCode(customCode))
    }

    @Test
    fun extensionSupportsReverseLookup() {
        val status = lookup.toStatus(402)
        assertNotNull(status)
        assertSame(customCode, status)
    }

    @Test
    fun fallsBackToBaseForRegisteredCodes() {
        assertEquals(401, lookup.toCode(Codes.DENIED))
        assertSame(Codes.CREATED, lookup.toStatus(201))
    }

    @Test
    fun fallsBackToBaseNullWhenNeitherKnows() {
        assertNull(lookup.toStatus(999))
    }
}
