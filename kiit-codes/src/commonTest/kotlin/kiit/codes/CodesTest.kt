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
        assertEquals(StatusConstants.KIIT, Codes.SUCCESS.origin)
        assertEquals("Success", Codes.SUCCESS.message)
        assertTrue(Codes.SUCCESS.success)
    }

    @Test
    fun deniedHasCorrectValues() {
        assertEquals("DENIED", Codes.DENIED.name)
        assertEquals(StatusConstants.KIIT, Codes.DENIED.origin)
        assertFalse(Codes.DENIED.success)
    }

    @Test
    fun forbiddenIsDenied() {
        // Access-control outcome, not a business-rule failure — see Codes.kt for the reasoning.
        assertTrue(Codes.FORBIDDEN is Failed.Denied)
    }

    @Test
    fun removedIsInvalid() {
        // A more specific, permanent variant of NOT_FOUND — same category.
        assertTrue(Codes.REMOVED is Failed.Invalid)
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
        assertTrue(Codes.all.all { it.origin == StatusConstants.KIIT })
    }

    @Test
    fun everyBuiltInCodeIsUniqueByOriginAndName() {
        val keys = Codes.all.map { it.origin to it.name }
        assertEquals(keys.size, keys.toSet().size)
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

    @Test fun overrideRemoved() {
        assertEquals(410, http.toCode(Codes.REMOVED))
    }

    @Test fun overrideMissing() {
        assertEquals(404, http.toCode(Codes.MISSING))
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

    // -------------------------------------------------------------------------
    // toStatus — deterministic canonical choice for codes shared by multiple statuses
    // -------------------------------------------------------------------------

    /**
     * Six built-in statuses resolve to 200. Pins the canonical winner so this can't silently
     * change if [Codes.all]'s declaration order ever shifts.
     */
    @Test
    fun toStatus200ResolvesToSuccessNotOtherSharedStatuses() {
        assertSame(Codes.SUCCESS, http.toStatus(200))
    }

    /** NOT_FOUND and MISSING both resolve to 404 as of the MISSING remap; NOT_FOUND wins. */
    @Test
    fun toStatus404ResolvesToNotFoundNotMissing() {
        assertSame(Codes.NOT_FOUND, http.toStatus(404))
    }

    /** ERRORED and UNEXPECTED both resolve to 500; UNEXPECTED wins. */
    @Test
    fun toStatus500ResolvesToUnexpectedNotErrored() {
        assertSame(Codes.UNEXPECTED, http.toStatus(500))
    }

    /** UNIMPLEMENTED and UNSUPPORTED both resolve to 501; UNIMPLEMENTED wins. */
    @Test
    fun toStatus501ResolvesToUnimplementedNotUnsupported() {
        assertSame(Codes.UNIMPLEMENTED, http.toStatus(501))
    }

    /** DENIED, UNAUTHENTICATED, and UNAUTHORIZED all resolve to 401; UNAUTHENTICATED wins. */
    @Test
    fun toStatus401ResolvesToUnauthenticatedNotDeniedOrUnauthorized() {
        assertSame(Codes.UNAUTHENTICATED, http.toStatus(401))
    }

    /**
     * The canonical tie-breaking is still derived from this instance's own [CodesToHttp.toCode],
     * not a fixed table — a custom [overrides] map changes both directions together.
     */
    @Test
    fun toStatusStaysInSyncWithCustomOverridesNotJustDefaults() {
        val custom = CodesToHttp(overrides = mapOf(Codes.TIMEOUT to 504))
        assertSame(Codes.TIMEOUT, custom.toStatus(504))
        assertNull(custom.toStatus(408)) // TIMEOUT no longer resolves to 408 for this instance
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
