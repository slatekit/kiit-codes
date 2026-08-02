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
        assertSame(Succeeded.SUCCESS, checked.status)
        assertTrue(checked.errors.isEmpty())
    }

    @Test
    fun successAcceptsCustomPassedStatus() {
        val checked = Checked.success(Succeeded.CREATED)
        assertSame(Succeeded.CREATED, checked.status)
        assertTrue(checked.errors.isEmpty())
    }

    @Test
    fun failureCarriesStatusAndErrors() {
        val errors = listOf(Err.of("bad field"))
        val checked = Checked.failure(Invalid.BAD_REQUEST, errors)
        assertSame(Invalid.BAD_REQUEST, checked.status)
        assertEquals(errors, checked.errors)
    }

    @Test
    fun failureRequiresAtLeastOneError() {
        assertFailsWith<IllegalArgumentException> {
            Checked.failure(Invalid.BAD_REQUEST, emptyList())
        }
    }

    @Test
    fun isValidIsTrueForSuccessFalseForFailure() {
        assertTrue(Checked.success().isValid)
        assertTrue(!Checked.failure(Invalid.BAD_REQUEST, listOf(Err.of("bad field"))).isValid)
    }

    @Test
    fun implementsHasErrors() {
        val checked = Checked.failure(Invalid.BAD_REQUEST, listOf(Err.of("bad field")))
        val hasErrors: HasErrors = checked
        assertEquals(checked.errors, hasErrors.errors)
    }

    // -------------------------------------------------------------------------
    // collect
    // -------------------------------------------------------------------------

    @Test
    fun collectWithNoChecksSucceeds() {
        val result = collect()
        assertSame(Succeeded.SUCCESS, result.status)
        assertTrue(result.errors.isEmpty())
    }

    @Test
    fun collectWithAllPassingChecksSucceeds() {
        val result = collect(Checked.success(), Checked.success(Succeeded.CREATED))
        assertSame(Succeeded.SUCCESS, result.status)
        assertTrue(result.errors.isEmpty())
    }

    @Test
    fun collectWithOneFailurePoolsItsErrors() {
        val errors = listOf(Err.of("bad field"))
        val result = collect(Checked.success(), Checked.failure(Invalid.BAD_REQUEST, errors))
        assertEquals(Invalid.INVALID_VALUE, result.status)
        assertEquals(errors, result.errors)
    }

    @Test
    fun collectWithMultipleFailuresPoolsAllErrorsInOrder() {
        val firstErrors = listOf(Err.of("first"))
        val secondErrors = listOf(Err.of("second"), Err.of("third"))
        val result =
            collect(
                Checked.failure(Invalid.BAD_REQUEST, firstErrors),
                Checked.success(),
                Checked.failure(Invalid.NOT_FOUND, secondErrors),
            )
        assertEquals(Invalid.INVALID_VALUE, result.status)
        assertEquals(firstErrors + secondErrors, result.errors)
    }

    @Test
    fun collectOverListBehavesIdenticallyToVararg() {
        val firstErrors = listOf(Err.of("first"))
        val secondErrors = listOf(Err.of("second"), Err.of("third"))
        val checks =
            listOf(
                Checked.failure(Invalid.BAD_REQUEST, firstErrors),
                Checked.success(),
                Checked.failure(Invalid.NOT_FOUND, secondErrors),
            )
        val fromList = collect(checks)
        val fromVararg = collect(*checks.toTypedArray())
        assertEquals(fromVararg.status, fromList.status)
        assertEquals(fromVararg.errors, fromList.errors)
    }
}
