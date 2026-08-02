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

    @Test fun excludedHasSuccessTrue() {
        assertTrue(Passed.Excluded("F", "F").success)
    }

    @Test fun informationHasSuccessTrue() {
        assertTrue(Passed.Information("I", "I").success)
    }

    // -------------------------------------------------------------------------
    // success flag — Failed subtypes (hoisted onto Failed itself; see Failed.success)
    // -------------------------------------------------------------------------

    @Test fun restrictedHasSuccessFalse() {
        assertFalse(Failed.Restricted("R", "R").success)
    }

    @Test fun invalidHasSuccessFalse() {
        assertFalse(Failed.Invalid("I", "I").success)
    }

    @Test fun rejectedHasSuccessFalse() {
        assertFalse(Failed.Rejected("E", "E").success)
    }

    @Test fun unservedHasSuccessFalse() {
        assertFalse(Failed.Unserved("U", "U").success)
    }

    // -------------------------------------------------------------------------
    // origin — defaults to "custom" for direct construction, overridable at the call site
    // -------------------------------------------------------------------------

    @Test fun originDefaultsToCustom() {
        assertEquals(StatusConstants.CUSTOM, Passed.Succeeded("S", "S").origin)
        assertEquals(StatusConstants.CUSTOM, Failed.Restricted("R", "R").origin)
    }

    @Test fun originIsOverridable() {
        assertEquals(StatusConstants.KIIT, Passed.Succeeded("S", "S", origin = StatusConstants.KIIT).origin)
    }

    // -------------------------------------------------------------------------
    // group — the category discriminant, exhaustive over all 8 subtypes
    // -------------------------------------------------------------------------

    @Test
    fun groupReturnsCorrectStringForAllSubtypes() {
        assertEquals("Succeeded", Passed.Succeeded("S", "S").group)
        assertEquals("Pending", Passed.Pending("P", "P").group)
        assertEquals("Excluded", Passed.Excluded("F", "F").group)
        assertEquals("Information", Passed.Information("N", "N").group)
        assertEquals("Restricted", Failed.Restricted("R", "R").group)
        assertEquals("Invalid", Failed.Invalid("I", "I").group)
        assertEquals("Rejected", Failed.Rejected("E", "E").group)
        assertEquals("Unserved", Failed.Unserved("U", "U").group)
    }

    // -------------------------------------------------------------------------
    // id — "$origin.$name", derived, usable as a map/lookup key
    // -------------------------------------------------------------------------

    @Test
    fun idIsOriginDotName() {
        val s = Failed.Restricted("RESTRICTED", "Restricted", origin = StatusConstants.KIIT)
        assertEquals("kiit.RESTRICTED", s.id)
    }

    @Test
    fun idReflectsUpdatedOriginAfterCopyAll() {
        val s = Passed.Succeeded("SUCCESS", "Success", origin = StatusConstants.KIIT)
        val copy = s.copyAll("Custom", "external")
        assertEquals("external.SUCCESS", copy.id)
    }

    // -------------------------------------------------------------------------
    // isNeutral — true only for Excluded/Information, false for every other subtype
    // -------------------------------------------------------------------------

    @Test
    fun isNeutralIsTrueForExcludedAndInformation() {
        assertTrue(Passed.Excluded("F", "F").isNeutral)
        assertTrue(Passed.Information("I", "I").isNeutral)
    }

    @Test
    fun isNeutralIsFalseForSucceededAndPending() {
        assertFalse(Passed.Succeeded("S", "S").isNeutral)
        assertFalse(Passed.Pending("P", "P").isNeutral)
    }

    @Test
    fun isNeutralIsFalseForEveryFailedSubtype() {
        assertFalse(Failed.Restricted("R", "R").isNeutral)
        assertFalse(Failed.Invalid("I", "I").isNeutral)
        assertFalse(Failed.Rejected("E", "E").isNeutral)
        assertFalse(Failed.Unserved("U", "U").isNeutral)
    }

    // -------------------------------------------------------------------------
    // copyAll — updates both message and origin, preserves name and group
    // -------------------------------------------------------------------------

    @Test
    fun copyAllOnSucceeded() {
        val s = Passed.Succeeded("SUCCESS", "Success", origin = StatusConstants.KIIT)
        val copy = s.copyAll("Custom", "external")
        assertEquals("Custom", copy.message)
        assertEquals("external", copy.origin)
        assertEquals(s.name, copy.name)
    }

    @Test
    fun copyAllOnRestricted() {
        val s = Failed.Restricted("RESTRICTED", "Restricted", origin = StatusConstants.KIIT)
        val copy = s.copyAll("Custom", "external")
        assertEquals("Custom", copy.message)
        assertEquals("external", copy.origin)
        assertEquals(s.name, copy.name)
    }

    @Test
    fun copyAllOnUnserved() {
        val s = Failed.Unserved("TIMEOUT", "Timeout", origin = StatusConstants.KIIT)
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
        val status = Succeeded.SUCCESS
        assertSame(status, Status.ofStatus(null, null, status))
    }

    @Test
    fun ofStatusReturnsRawStatusWhenMsgIsNull() {
        val raw = Succeeded.CREATED
        val result = Status.ofStatus(null, raw, Succeeded.SUCCESS)
        assertSame(raw, result)
    }

    @Test
    fun ofStatusReturnsStatusWithUpdatedMsgWhenRawIsNull() {
        val result = Status.ofStatus("Custom", null, Succeeded.SUCCESS)
        assertEquals("Custom", result.message)
        assertEquals(Succeeded.SUCCESS.origin, result.origin)
    }

    @Test
    fun ofStatusReturnsRawWithUpdatedMsgWhenBothProvided() {
        val raw = Succeeded.CREATED
        val result = Status.ofStatus("Custom", raw, Succeeded.SUCCESS)
        assertEquals("Custom", result.message)
        assertEquals(raw.origin, result.origin)
        assertNotSame(raw, result)
    }

    // -------------------------------------------------------------------------
    // Typealiases (Restricted, Invalid, ...) are fully transparent — the same type as their
    // Failed.X/Passed.X target, not a copy. A `when` mixing aliased and fully-qualified branches
    // must still be treated as exhaustive by the compiler; this is a compile-time check as much
    // as a runtime one — it wouldn't build if the aliases introduced a distinct type.
    // -------------------------------------------------------------------------

    @Test
    fun aliasedAndFullyQualifiedBranchesAreExhaustiveInTheSameWhen() {
        val status: Status = Restricted.DENIED
        val label =
            when (status) {
                is Restricted -> "restricted"
                is Failed -> "failed"
                is Passed -> "passed"
            }
        assertEquals("restricted", label)
    }

    @Test
    fun aliasedConstantIsSameInstanceAsFullyQualifiedConstant() {
        assertSame(Failed.Restricted.DENIED, Restricted.DENIED)
    }
}
