package kiit.codes

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotSame
import kotlin.test.assertSame
import kotlin.test.assertTrue

// =================================================================================================
// StatusTest — Passed/Failed subtypes, copy helpers, ofStatus companion function
// =================================================================================================

class StatusTest {
    // -------------------------------------------------------------------------
    // success flag — Passed subtypes (hoisted onto Passed itself; see Passed.success)
    // -------------------------------------------------------------------------

    @Test fun succeededHasSuccessTrue() {
        assertTrue(Passed.Succeeded("S", "S").success)
    }

    @Test fun pendingHasSuccessTrue() {
        assertTrue(Passed.Pending("P", "P").success)
    }

    @Test fun filteredHasSuccessTrue() {
        assertTrue(Passed.Filtered("F", "F").success)
    }

    @Test fun informationHasSuccessTrue() {
        assertTrue(Passed.Information("I", "I").success)
    }

    // -------------------------------------------------------------------------
    // success flag — Failed subtypes (hoisted onto Failed itself; see Failed.success)
    // -------------------------------------------------------------------------

    @Test fun deniedHasSuccessFalse() {
        assertFalse(Failed.Denied("D", "D").success)
    }

    @Test fun invalidHasSuccessFalse() {
        assertFalse(Failed.Invalid("I", "I").success)
    }

    @Test fun erroredHasSuccessFalse() {
        assertFalse(Failed.Errored("E", "E").success)
    }

    @Test fun unservedHasSuccessFalse() {
        assertFalse(Failed.Unserved("U", "U").success)
    }

    // -------------------------------------------------------------------------
    // origin — defaults to "custom" for direct construction, overridable at the call site
    // -------------------------------------------------------------------------

    @Test fun originDefaultsToCustom() {
        assertEquals("custom", Passed.Succeeded("S", "S").origin)
        assertEquals("custom", Failed.Denied("D", "D").origin)
    }

    @Test fun originIsOverridable() {
        assertEquals("kiit", Passed.Succeeded("S", "S", origin = "kiit").origin)
    }

    // -------------------------------------------------------------------------
    // group — the category discriminant, exhaustive over all 8 subtypes
    // -------------------------------------------------------------------------

    @Test
    fun groupReturnsCorrectStringForAllSubtypes() {
        assertEquals("Succeeded", Passed.Succeeded("S", "S").group)
        assertEquals("Pending", Passed.Pending("P", "P").group)
        assertEquals("Filtered", Passed.Filtered("F", "F").group)
        assertEquals("Information", Passed.Information("N", "N").group)
        assertEquals("Denied", Failed.Denied("D", "D").group)
        assertEquals("Invalid", Failed.Invalid("I", "I").group)
        assertEquals("Errored", Failed.Errored("E", "E").group)
        assertEquals("Unserved", Failed.Unserved("U", "U").group)
    }

    // -------------------------------------------------------------------------
    // copyMessage — updates message, preserves name, origin, and group
    // -------------------------------------------------------------------------

    @Test
    fun copyMessageOnSucceeded() {
        val s = Passed.Succeeded("SUCCESS", "Success", origin = "kiit")
        val copy = s.copyMessage("Custom")
        assertEquals("Custom", copy.message)
        assertEquals(s.name, copy.name)
        assertEquals(s.origin, copy.origin)
        assertTrue(copy.success)
    }

    @Test
    fun copyMessageOnPending() {
        val s = Passed.Pending("PENDING", "Pending", origin = "kiit")
        val copy = s.copyMessage("Custom")
        assertEquals("Custom", copy.message)
        assertEquals(s.origin, copy.origin)
    }

    @Test
    fun copyMessageOnFiltered() {
        val s = Passed.Filtered("SKIPPED", "Skipped", origin = "kiit")
        val copy = s.copyMessage("Custom")
        assertEquals("Custom", copy.message)
    }

    @Test
    fun copyMessageOnInformation() {
        val s = Passed.Information("HELP", "Help", origin = "kiit")
        val copy = s.copyMessage("Custom")
        assertEquals("Custom", copy.message)
        assertTrue(copy.success)
    }

    @Test
    fun copyMessageOnDenied() {
        val s = Failed.Denied("DENIED", "Denied", origin = "kiit")
        val copy = s.copyMessage("Custom")
        assertEquals("Custom", copy.message)
        assertFalse(copy.success)
    }

    @Test
    fun copyMessageOnInvalid() {
        val s = Failed.Invalid("INVALID", "Invalid", origin = "kiit")
        val copy = s.copyMessage("Custom")
        assertEquals("Custom", copy.message)
    }

    @Test
    fun copyMessageOnErrored() {
        val s = Failed.Errored("ERRORED", "Errored", origin = "kiit")
        val copy = s.copyMessage("Custom")
        assertEquals("Custom", copy.message)
    }

    @Test
    fun copyMessageOnUnserved() {
        val s = Failed.Unserved("UNEXPECTED", "Unexpected", origin = "kiit")
        val copy = s.copyMessage("Custom")
        assertEquals("Custom", copy.message)
        assertFalse(copy.success)
    }

    // -------------------------------------------------------------------------
    // copyAll — updates both message and origin, preserves name and group
    // -------------------------------------------------------------------------

    @Test
    fun copyAllOnSucceeded() {
        val s = Passed.Succeeded("SUCCESS", "Success", origin = "kiit")
        val copy = s.copyAll("Custom", "external")
        assertEquals("Custom", copy.message)
        assertEquals("external", copy.origin)
        assertEquals(s.name, copy.name)
    }

    @Test
    fun copyAllOnDenied() {
        val s = Failed.Denied("DENIED", "Denied", origin = "kiit")
        val copy = s.copyAll("Custom", "external")
        assertEquals("Custom", copy.message)
        assertEquals("external", copy.origin)
        assertEquals(s.name, copy.name)
    }

    @Test
    fun copyAllOnUnserved() {
        val s = Failed.Unserved("TIMEOUT", "Timeout", origin = "kiit")
        val copy = s.copyAll("Custom", "external")
        assertEquals("Custom", copy.message)
        assertEquals("external", copy.origin)
        assertEquals(s.name, copy.name)
    }

    // -------------------------------------------------------------------------
    // ofStatus — selects correct instance based on msg / rawStatus nullability
    // -------------------------------------------------------------------------

    @Test
    fun ofStatusReturnStatusWhenBothNull() {
        val status = Codes.SUCCESS
        assertSame(status, Status.ofStatus(null, null, status))
    }

    @Test
    fun ofStatusReturnsRawStatusWhenMsgIsNull() {
        val raw = Codes.CREATED
        val result = Status.ofStatus(null, raw, Codes.SUCCESS)
        assertSame(raw, result)
    }

    @Test
    fun ofStatusReturnsStatusWithUpdatedMsgWhenRawIsNull() {
        val result = Status.ofStatus("Custom", null, Codes.SUCCESS)
        assertEquals("Custom", result.message)
        assertEquals(Codes.SUCCESS.origin, result.origin)
    }

    @Test
    fun ofStatusReturnsRawWithUpdatedMsgWhenBothProvided() {
        val raw = Codes.CREATED
        val result = Status.ofStatus("Custom", raw, Codes.SUCCESS)
        assertEquals("Custom", result.message)
        assertEquals(raw.origin, result.origin)
        assertNotSame(raw, result)
    }
}
