package kiit.codes

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

// =================================================================================================
// ErrTest — the Err builder companion
// =================================================================================================

class ErrTest {
    @Test
    fun ofMessageBuildsErrorInfo() {
        val err = Err.of("bad thing")
        assertIs<Err.ErrorInfo>(err)
        assertEquals("bad thing", err.msg)
        assertNull(err.cause)
    }

    @Test
    fun ofMessageWithThrowableCarriesCause() {
        val root = IllegalStateException("root")
        val err = Err.of("bad thing", root)
        assertSame(root, err.cause)
    }

    @Test
    fun ofStatusUsesStatusMessage() {
        val err = Err.of(Codes.UNAUTHORIZED)
        assertIs<Err.ErrorInfo>(err)
        assertEquals(Codes.UNAUTHORIZED.message, err.msg)
    }

    @Test
    fun ofStatusWithMessageUsesGivenMessageNotStatusMessage() {
        val err = Err.of(Codes.INVALID, "email must contain @")
        assertIs<Err.ErrorInfo>(err)
        assertEquals("email must contain @", err.msg)
        assertSame(Codes.INVALID, err.ref)
    }

    @Test
    fun onFieldBuildsErrorField() {
        val err = Err.on("email", "not-an-email", "invalid email") as Err.ErrorField
        assertEquals("email", err.field)
        assertEquals("not-an-email", err.value)
        assertEquals("invalid email", err.msg)
    }

    @Test
    fun exBuildsErrorInfoFromThrowableMessage() {
        val root = IllegalStateException("boom")
        val err = Err.ex(root)
        assertEquals("boom", err.msg)
        assertSame(root, err.cause)
    }

    @Test
    fun objBuildsErrorInfoWithRef() {
        val payload = mapOf("k" to "v")
        val err = Err.obj(payload)
        assertEquals(payload.toString(), err.msg)
        assertSame(payload, err.ref)
    }

    @Test
    fun listBuildsErrorList() {
        val err = Err.list(listOf("one", "two"), "multiple errors")
        assertEquals("multiple errors", err.msg)
        assertEquals(2, err.errors.size)
        assertTrue(err.errors.all { it is Err.ErrorInfo })
    }

    @Test
    fun listUsesDefaultMessageWhenNull() {
        val err = Err.list(listOf("one"), null)
        assertEquals("Error occurred", err.msg)
    }

    @Test
    fun buildReturnsSameErrInstance() {
        val original = Err.of("already an err")
        assertSame(original, Err.build(original))
    }

    @Test
    fun buildWrapsStringAsErrorInfo() {
        val err = Err.build("plain string")
        assertIs<Err.ErrorInfo>(err)
        assertEquals("plain string", err.msg)
    }

    @Test
    fun buildWrapsExceptionViaEx() {
        val root = RuntimeException("failure")
        val err = Err.build(root)
        assertEquals("failure", err.msg)
        assertSame(root, err.cause)
    }

    @Test
    fun buildWrapsOtherObjectsViaObj() {
        val payload = 42
        val err = Err.build(payload)
        assertEquals("42", err.msg)
        assertEquals(payload, err.ref)
    }

    @Test
    fun buildFallsBackToUnexpectedMessageForNull() {
        val err = Err.build(null)
        assertEquals(Codes.UNEXPECTED.message, err.msg)
    }
}
