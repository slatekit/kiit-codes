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
package kiit.codes

/**
 * Non-monadic result of a validation-style check that reports every problem found rather than
 * short-circuiting on the first one — a form/request with three invalid fields should report all
 * three, not just the first. Deliberately has no `map`/`flatMap`; compose multiple checks with
 * [combine], not by chaining.
 *
 * Construction is only through [success]/[failure] so [status] and [errors] can never drift out
 * of sync with each other: a passing [Checked] always has an empty [errors] list, a failing one
 * always has at least one entry.
 */
class Checked private constructor(val status: Status, val errors: List<Err>) {
    companion object {
        /** A passing check with no errors. */
        fun success(status: Passed = Codes.SUCCESS): Checked = Checked(status, emptyList())

        /** A failing check with one or more [errors]. */
        fun failure(status: Failed, errors: List<Err>): Checked {
            require(errors.isNotEmpty()) { "failure requires at least one Err" }
            return Checked(status, errors)
        }
    }
}

/**
 * Combines multiple [checks] into one: passes only if every one of them passed, otherwise fails
 * with [Codes.INVALID] and every error from every failing entry pooled together, in order.
 */
fun combine(vararg checks: Checked): Checked {
    val errors = checks.flatMap { it.errors }
    return if (errors.isEmpty()) Checked.success() else Checked.failure(Codes.INVALID, errors)
}
