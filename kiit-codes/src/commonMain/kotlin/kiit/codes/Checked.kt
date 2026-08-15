/**
 *  <kiit_header>
 * url: www.kiit.dev
 * git: www.github.com/slatekit/kiit
 * org: www.codehelix.co
 * author: Kishore Reddy
 * copyright: 2016 CodeHelix Solutions Inc.
 * license: refer to website and/or github
 * about: A Kotlin Tool-Kit for Server + Android
 *  </kiit_header>
 */
@file:JvmName("Checks")
@file:OptIn(ExperimentalJsExport::class, ExperimentalJsStatic::class)

package kiit.codes

import kotlin.js.ExperimentalJsExport
import kotlin.js.ExperimentalJsStatic
import kotlin.js.JsExport
import kotlin.js.JsName
import kotlin.js.JsStatic
import kotlin.jvm.JvmName
import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmStatic

/**
 * Non-monadic result of a validation-style check that reports every problem found rather than
 * short-circuiting on the first one.
 *
 * 1. A form with three invalid fields should report all three, not just the first. Deliberately
 *    has no `map`/`flatMap`. Compose multiple checks with [collect] instead of chaining.
 * 2. Construction is only through [success]/[failure] so [status] and [errors] can never be out
 *    of sync: a passing [Checked] always has an empty [errors] list, a failing one always has
 *    at least one entry in it.
 * 3. Implements [HasErrors]. Deliberately skips an equivalent "has status" interface: [status]
 *    is typed [Status], not [Failed], since one [Checked] instance can represent either outcome.
 */
@JsExport
class Checked private constructor(val status: Status, override val errors: List<Err>) : HasErrors {
    val isValid: Boolean get() = errors.isEmpty()

    companion object {
        /** A passing check with no errors. */
        @JvmStatic
        @JsStatic
        @JvmOverloads
        fun success(status: Passed = Succeeded.SUCCESS): Checked = Checked(status, emptyList())

        /** A failing check with one or more [errors]. */
        @JvmStatic
        @JsStatic
        fun failure(status: Failed, errors: List<Err>): Checked {
            require(errors.isNotEmpty()) { "failure requires at least one Err" }
            return Checked(status, errors)
        }
    }
}

/**
 * [collect] over varargs instead of a [List]: the JS/TS-friendlier overload. A Kotlin `vararg`
 * exports as a plain native JS `Array` parameter (`collect([a, b, c])`), not the opaque
 * `kotlin.collections.KtList` wrapper the [List] overload below uses. It's a real `Array<Checked>`
 * parameter, not a JS rest/spread parameter, even though `vararg` is the Kotlin-side spelling.
 */
@JsExport
fun collect(vararg checks: Checked): Checked = collect(checks.toList())

/**
 * 1. Collects multiple [checks] into one: passes only if every one of them passed, otherwise
 *    fails with [Failed.Invalid.INVALID_VALUE] and every error from every failing entry pooled
 *    together, in the order the checks were given.
 * 2. `@JsName` disambiguates this from the `vararg` overload above. JS has no function overloading,
 *    so two top-level `@JsExport` functions named `collect` can't both keep that exact name.
 */
@JsExport
@JsName("collectList")
fun collect(checks: List<Checked>): Checked {
    val errors = checks.flatMap { it.errors }
    return if (errors.isEmpty()) Checked.success() else Checked.failure(Invalid.INVALID_VALUE, errors)
}
