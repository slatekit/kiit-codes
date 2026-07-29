package kiit.codes

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
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
        assertEquals("The operation completed successfully.", Codes.SUCCESS.message)
        assertTrue(Codes.SUCCESS.success)
    }

    @Test
    fun deniedHasCorrectValues() {
        assertEquals("DENIED", Codes.DENIED.name)
        assertEquals(StatusConstants.KIIT, Codes.DENIED.origin)
        assertFalse(Codes.DENIED.success)
    }

    @Test
    fun forbiddenIsRestricted() {
        // Access-control outcome, not a business-rule failure — see Codes.kt for the reasoning.
        assertTrue(Codes.FORBIDDEN is Failed.Restricted)
    }

    @Test
    fun expiredIsRejected() {
        // Was valid and timed out — a known business outcome, not malformed input.
        assertTrue(Codes.EXPIRED is Failed.Rejected)
    }

    @Test
    fun goneIsRejected() {
        // Deliberately removed — a known business outcome, not malformed input.
        assertTrue(Codes.GONE is Failed.Rejected)
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

    @Test
    fun everyBuiltInCodeHasAUniqueId() {
        val ids = Codes.all.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
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
        assertEquals(202, http.toCode(Codes.PROCESSING))
        assertEquals(202, http.toCode(Codes.QUEUED))
        assertEquals(202, http.toCode(Codes.ACCEPTED))
    }

    @Test fun categoryDefaultExcluded() {
        assertEquals(200, http.toCode(Codes.OMITTED))
        assertEquals(200, http.toCode(Codes.SKIPPED))
        assertEquals(200, http.toCode(Codes.DISCARDED))
    }

    @Test fun categoryDefaultInformation() {
        assertEquals(200, http.toCode(Codes.ABOUT))
    }

    @Test fun categoryDefaultRestricted() {
        assertEquals(401, http.toCode(Codes.DENIED))
        assertEquals(401, http.toCode(Codes.UNAUTHENTICATED))
    }

    @Test fun categoryDefaultInvalid() {
        assertEquals(400, http.toCode(Codes.BAD_REQUEST))
        assertEquals(400, http.toCode(Codes.INVALID_VALUE))
        assertEquals(400, http.toCode(Codes.OUT_OF_RANGE))
    }

    @Test fun categoryDefaultRejected() {
        assertEquals(500, http.toCode(Codes.RULE_VIOLATION))
        assertEquals(500, http.toCode(Codes.PRECONDITION_FAILED))
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

    @Test fun overrideExpired() {
        assertEquals(410, http.toCode(Codes.EXPIRED))
    }

    @Test fun overrideGone() {
        assertEquals(410, http.toCode(Codes.GONE))
    }

    @Test fun overrideNotExists() {
        assertEquals(404, http.toCode(Codes.NOT_EXISTS))
    }

    @Test fun overrideCancelled() {
        assertEquals(499, http.toCode(Codes.CANCELLED))
    }

    @Test fun overrideRedirected() {
        assertEquals(307, http.toCode(Codes.REDIRECTED))
    }

    @Test fun overridePayloadTooLarge() {
        assertEquals(413, http.toCode(Codes.PAYLOAD_TOO_LARGE))
    }

    @Test fun overrideConflict() {
        assertEquals(409, http.toCode(Codes.CONFLICT))
    }

    @Test fun overrideLocked() {
        assertEquals(423, http.toCode(Codes.LOCKED))
    }

    @Test fun overrideSuspended() {
        assertEquals(403, http.toCode(Codes.SUSPENDED))
    }

    @Test fun overrideInvalidEntity() {
        assertEquals(422, http.toCode(Codes.INVALID_ENTITY))
    }

    @Test fun overrideResourceLimited() {
        assertEquals(429, http.toCode(Codes.RESOURCE_LIMITED))
    }

    @Test fun overrideTimeout() {
        assertEquals(504, http.toCode(Codes.TIMEOUT))
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
        val custom = Failed.Rejected("CUSTOM", "Custom error")
        assertEquals(500, http.toCode(custom)) // Rejected's category default
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
     * Spells out the lossy round trip directly: converting UPDATED forward and back doesn't
     * return UPDATED. toStatus(toCode(x)) == x does not generally hold — see toStatus's doc.
     */
    @Test
    fun httpRoundTripDoesNotPreserveTheOriginalStatus() {
        val original = Codes.UPDATED
        val code = http.toCode(original)
        val restored = http.toStatus(code)

        assertEquals(200, code)
        assertSame(Codes.SUCCESS, restored)
        assertNotEquals<Status?>(original, restored)
    }

    /**
     * Many built-in statuses resolve to 200. Pins the canonical winner so this can't silently
     * change if [Codes.all]'s declaration order ever shifts.
     */
    @Test
    fun toStatus200ResolvesToSuccessNotOtherSharedStatuses() {
        assertSame(Codes.SUCCESS, http.toStatus(200))
    }

    /** NOT_FOUND and NOT_EXISTS both resolve to 404; NOT_FOUND wins. */
    @Test
    fun toStatus404ResolvesToNotFoundNotNotExists() {
        assertSame(Codes.NOT_FOUND, http.toStatus(404))
    }

    /** EXPIRED and GONE both resolve to 410; GONE wins — its name and message are the literal HTTP 410 concept. */
    @Test
    fun toStatus410ResolvesToGoneNotExpired() {
        assertSame(Codes.GONE, http.toStatus(410))
    }

    /** RULE_VIOLATION, PRECONDITION_FAILED, and UNEXPECTED all resolve to 500; UNEXPECTED wins. */
    @Test
    fun toStatus500ResolvesToUnexpectedNotRuleViolationOrPreconditionFailed() {
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

    /** FORBIDDEN and SUSPENDED both resolve to 403; FORBIDDEN wins. */
    @Test
    fun toStatus403ResolvesToForbiddenNotSuspended() {
        assertSame(Codes.FORBIDDEN, http.toStatus(403))
    }

    /**
     * The canonical tie-breaking is still derived from this instance's own [CodesToHttp.toCode],
     * not a fixed table — a custom [overrides] map changes both directions together.
     */
    @Test
    fun toStatusStaysInSyncWithCustomOverridesNotJustDefaults() {
        val custom = CodesToHttp(overrides = mapOf(Codes.TIMEOUT to 599))
        assertSame(Codes.TIMEOUT, custom.toStatus(599))
        assertNull(custom.toStatus(504)) // TIMEOUT no longer resolves to 504 for this instance
    }
}

// =================================================================================================
// CodesToGrpcTest
// =================================================================================================

class CodesToGrpcTest {
    private val grpc = CodesToGrpc()

    @Test fun categoryDefaultPassedIsOk() {
        assertEquals(0, grpc.toCode(Codes.SUCCESS))
        assertEquals(0, grpc.toCode(Codes.PROCESSING))
        assertEquals(0, grpc.toCode(Codes.SKIPPED))
        assertEquals(0, grpc.toCode(Codes.HELP))
    }

    @Test fun categoryDefaultRestrictedIsPermissionDenied() {
        assertEquals(7, grpc.toCode(Codes.DENIED))
        assertEquals(7, grpc.toCode(Codes.UNAUTHORIZED))
        assertEquals(7, grpc.toCode(Codes.FORBIDDEN))
    }

    @Test fun categoryDefaultInvalidIsInvalidArgument() {
        assertEquals(3, grpc.toCode(Codes.BAD_REQUEST))
        assertEquals(3, grpc.toCode(Codes.MISSING_FIELD))
    }

    @Test fun categoryDefaultRejectedIsFailedPrecondition() {
        assertEquals(9, grpc.toCode(Codes.RULE_VIOLATION))
        assertEquals(9, grpc.toCode(Codes.NOT_EXISTS))
        assertEquals(9, grpc.toCode(Codes.EXPIRED))
        assertEquals(9, grpc.toCode(Codes.GONE))
    }

    @Test fun categoryDefaultUnservedIsInternal() {
        assertEquals(13, grpc.toCode(Codes.UNSUPPORTED))
        assertEquals(13, grpc.toCode(Codes.UNDER_MAINTENANCE))
    }

    @Test fun overrideCancelled() {
        assertEquals(1, grpc.toCode(Codes.CANCELLED))
    }

    @Test fun overrideUnauthenticated() {
        assertEquals(16, grpc.toCode(Codes.UNAUTHENTICATED))
    }

    @Test fun overrideNotFound() {
        assertEquals(5, grpc.toCode(Codes.NOT_FOUND))
    }

    @Test fun overrideOutOfRange() {
        assertEquals(11, grpc.toCode(Codes.OUT_OF_RANGE))
    }

    @Test fun overrideConflict() {
        assertEquals(6, grpc.toCode(Codes.CONFLICT))
    }

    @Test fun overrideUnimplemented() {
        assertEquals(12, grpc.toCode(Codes.UNIMPLEMENTED))
    }

    @Test fun overrideUnreachable() {
        assertEquals(14, grpc.toCode(Codes.UNREACHABLE))
    }

    @Test fun overrideTimeout() {
        assertEquals(4, grpc.toCode(Codes.TIMEOUT))
    }

    @Test fun overrideRateLimited() {
        assertEquals(8, grpc.toCode(Codes.RATE_LIMITED))
    }

    @Test fun overrideUnexpected() {
        assertEquals(2, grpc.toCode(Codes.UNEXPECTED))
    }

    @Test fun overrideDataLoss() {
        assertEquals(15, grpc.toCode(Codes.DATA_LOSS))
    }

    /** Not an official gRPC mapping, shares RESOURCE_EXHAUSTED (8) with RATE_LIMITED deliberately. */
    @Test
    fun overridePayloadTooLargeSharesResourceExhausted() {
        assertEquals(8, grpc.toCode(Codes.PAYLOAD_TOO_LARGE))
    }

    /** Genuine sibling of RATE_LIMITED, same axis, shares RESOURCE_EXHAUSTED (8) too. */
    @Test
    fun overrideResourceLimited() {
        assertEquals(8, grpc.toCode(Codes.RESOURCE_LIMITED))
    }

    // -------------------------------------------------------------------------
    // toStatus — deterministic canonical choice for gRPC codes shared by multiple statuses
    // -------------------------------------------------------------------------

    /** Every non-overridden Passed status resolves to 0 (OK); SUCCESS wins. */
    @Test
    fun toStatus0ResolvesToSuccess() {
        assertSame(Codes.SUCCESS, grpc.toStatus(0))
    }

    /** BAD_REQUEST, INVALID_VALUE, MISSING_FIELD, and INVALID_ENTITY all resolve to 3; INVALID_VALUE wins. */
    @Test
    fun toStatus3ResolvesToInvalidValue() {
        assertSame(Codes.INVALID_VALUE, grpc.toStatus(3))
    }

    /** ALREADY_EXISTS — only CONFLICT resolves to 6, no tie to break. */
    @Test
    fun toStatus6ResolvesToConflict() {
        assertSame(Codes.CONFLICT, grpc.toStatus(6))
    }

    /** DENIED, UNAUTHORIZED, and FORBIDDEN all resolve to 7; DENIED wins. */
    @Test
    fun toStatus7ResolvesToDenied() {
        assertSame(Codes.DENIED, grpc.toStatus(7))
    }

    /** RATE_LIMITED, PAYLOAD_TOO_LARGE, and RESOURCE_LIMITED all resolve to 8; RATE_LIMITED wins. */
    @Test
    fun toStatus8ResolvesToRateLimitedNotPayloadTooLargeOrResourceLimited() {
        assertSame(Codes.RATE_LIMITED, grpc.toStatus(8))
    }

    /** RULE_VIOLATION, NOT_EXISTS, PRECONDITION_FAILED, EXPIRED, and GONE all resolve to 9; PRECONDITION_FAILED wins. */
    @Test
    fun toStatus9ResolvesToPreconditionFailed() {
        assertSame(Codes.PRECONDITION_FAILED, grpc.toStatus(9))
    }

    /** UNSUPPORTED, UNDER_MAINTENANCE, and INTERNAL all resolve to 13; INTERNAL wins. */
    @Test
    fun toStatus13ResolvesToInternal() {
        assertSame(Codes.INTERNAL, grpc.toStatus(13))
    }

    /** ABORTED (10) has no dedicated Status and nothing falls through to it by default; null. */
    @Test
    fun toStatus10ReturnsNullSinceAbortedHasNoDedicatedCode() {
        assertNull(grpc.toStatus(10))
    }

    @Test
    fun toStatusReturnsNullForUnrecognizedGrpcCode() {
        assertNull(grpc.toStatus(999))
    }

    @Test
    fun toStatusStaysInSyncWithCustomOverridesNotJustDefaults() {
        val custom = CodesToGrpc(overrides = mapOf(Codes.TIMEOUT to 99))
        assertSame(Codes.TIMEOUT, custom.toStatus(99))
        assertNull(custom.toStatus(4)) // TIMEOUT no longer resolves to 4 for this instance
    }
}

class CompositeLookupTest {
    private val customCode = Failed.Rejected("PAYMENT_DECLINED", "Payment declined")
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
