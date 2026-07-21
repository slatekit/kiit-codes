package kiit.codes

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame
import kotlin.test.assertTrue

// =================================================================================================
// CheckedTest
// =================================================================================================

class CheckedTest {
    @Test
    fun successDefaultsToCodesSuccess() {
        val checked = Checked.success()
        assertSame(Codes.SUCCESS, checked.status)
        assertTrue(checked.errors.isEmpty())
    }

    @Test
    fun successAcceptsCustomPassedStatus() {
        val checked = Checked.success(Codes.CREATED)
        assertSame(Codes.CREATED, checked.status)
        assertTrue(checked.errors.isEmpty())
    }

    @Test
    fun failureCarriesStatusAndErrors() {
        val errors = listOf(Err.of("bad field"))
        val checked = Checked.failure(Codes.BAD_REQUEST, errors)
        assertSame(Codes.BAD_REQUEST, checked.status)
        assertEquals(errors, checked.errors)
    }

    @Test
    fun failureRequiresAtLeastOneError() {
        assertFailsWith<IllegalArgumentException> {
            Checked.failure(Codes.BAD_REQUEST, emptyList())
        }
    }

    // -------------------------------------------------------------------------
    // combine
    // -------------------------------------------------------------------------

    @Test
    fun combineWithNoChecksSucceeds() {
        val result = combine()
        assertSame(Codes.SUCCESS, result.status)
        assertTrue(result.errors.isEmpty())
    }

    @Test
    fun combineWithAllPassingChecksSucceeds() {
        val result = combine(Checked.success(), Checked.success(Codes.CREATED))
        assertSame(Codes.SUCCESS, result.status)
        assertTrue(result.errors.isEmpty())
    }

    @Test
    fun combineWithOneFailurePoolsItsErrors() {
        val errors = listOf(Err.of("bad field"))
        val result = combine(Checked.success(), Checked.failure(Codes.BAD_REQUEST, errors))
        assertEquals(Codes.INVALID, result.status)
        assertEquals(errors, result.errors)
    }

    @Test
    fun combineWithMultipleFailuresPoolsAllErrorsInOrder() {
        val firstErrors = listOf(Err.of("first"))
        val secondErrors = listOf(Err.of("second"), Err.of("third"))
        val result =
            combine(
                Checked.failure(Codes.BAD_REQUEST, firstErrors),
                Checked.success(),
                Checked.failure(Codes.NOT_FOUND, secondErrors),
            )
        assertEquals(Codes.INVALID, result.status)
        assertEquals(firstErrors + secondErrors, result.errors)
    }
}
