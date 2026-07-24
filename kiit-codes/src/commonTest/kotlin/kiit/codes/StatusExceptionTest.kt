package kiit.codes

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame
import kotlin.test.fail

// =================================================================================================
// StatusExceptionTest
// =================================================================================================

class StatusExceptionTest {
    @Test
    fun deniedExceptionExposesItsStatus() {
        val ex = StatusException.DeniedException(Codes.UNAUTHORIZED)
        assertSame(Codes.UNAUTHORIZED, ex.status)
    }

    @Test
    fun invalidExceptionExposesItsStatus() {
        val ex = StatusException.InvalidException(Codes.BAD_REQUEST)
        assertSame(Codes.BAD_REQUEST, ex.status)
    }

    @Test
    fun erroredExceptionExposesItsStatus() {
        val ex = StatusException.ErroredException(Codes.CONFLICT)
        assertSame(Codes.CONFLICT, ex.status)
    }

    @Test
    fun unservedExceptionExposesItsStatus() {
        val ex = StatusException.UnservedException(Codes.TIMEOUT)
        assertSame(Codes.TIMEOUT, ex.status)
    }

    @Test
    fun defaultErrorsWrapTheStatusSingly() {
        val ex = StatusException.DeniedException(Codes.UNAUTHORIZED)
        assertEquals(1, ex.errors.size)
        assertEquals(Codes.UNAUTHORIZED.message, ex.errors.single().msg)
    }

    @Test
    fun explicitErrorsAreStoredInsteadOfTheDefault() {
        val errors = listOf(Err.of("bad field"))
        val ex = StatusException.InvalidException(Codes.BAD_REQUEST, errors)
        assertEquals(errors, ex.errors)
    }

    @Test
    fun causePropagatesToThrowableCause() {
        val root = IllegalStateException("root")
        val ex = StatusException.ErroredException(Codes.CONFLICT, cause = root)
        assertSame(root, ex.cause)
    }

    @Test
    fun messageComesFromCheckedStatus() {
        val ex = StatusException.UnservedException(Codes.UNREACHABLE)
        assertEquals(ex.checked.status.message, ex.message)
    }

    @Test
    fun catchingAsSealedBaseNarrowsExhaustively() {
        val caught: StatusException =
            try {
                throw StatusException.InvalidException(Codes.BAD_REQUEST)
            } catch (e: StatusException) {
                e
            }
        // Exhaustive `when` with no `else` — fails to compile if a subtype is missing.
        val label =
            when (caught) {
                is StatusException.DeniedException -> "denied"
                is StatusException.InvalidException -> "invalid"
                is StatusException.ErroredException -> "errored"
                is StatusException.UnservedException -> "unserved"
            }
        assertEquals("invalid", label)
    }

    @Test
    fun narrowCatchDoesNotCatchMismatchedSubtype() {
        assertFailsWith<StatusException.InvalidException> {
            try {
                throw StatusException.InvalidException(Codes.BAD_REQUEST)
            } catch (e: StatusException.DeniedException) {
                fail("DeniedException catch block should not run for an InvalidException")
            }
        }
    }

    // -------------------------------------------------------------------------
    // Failed.toException
    // -------------------------------------------------------------------------

    @Test
    fun toExceptionOnDeniedProducesDeniedException() {
        val ex = Codes.UNAUTHORIZED.toException()
        assertEquals(StatusException.DeniedException::class, ex::class)
    }

    @Test
    fun toExceptionOnInvalidProducesInvalidException() {
        val ex = Codes.BAD_REQUEST.toException()
        assertEquals(StatusException.InvalidException::class, ex::class)
    }

    @Test
    fun toExceptionOnErroredProducesErroredException() {
        val ex = Codes.CONFLICT.toException()
        assertEquals(StatusException.ErroredException::class, ex::class)
    }

    @Test
    fun toExceptionOnUnservedProducesUnservedException() {
        val ex = Codes.TIMEOUT.toException()
        assertEquals(StatusException.UnservedException::class, ex::class)
    }

    @Test
    fun toExceptionPropagatesExplicitErrors() {
        val errors = listOf(Err.of("bad field"))
        val ex = Codes.BAD_REQUEST.toException(errors)
        assertEquals(errors, ex.errors)
    }
}
