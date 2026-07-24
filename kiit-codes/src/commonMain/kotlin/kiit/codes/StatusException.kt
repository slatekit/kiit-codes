package kiit.codes

/**
 * Sealed exception hierarchy carrying a [Checked] instead of a plain message string.
 *
 * Throw one of the typed subclasses ([DeniedException], [InvalidException], [ErroredException],
 * [UnservedException]) when a [Failed] status needs to cross a call boundary that can only
 * communicate via exceptions (e.g. from a service layer into a framework that catches
 * [Exception]). [StatusException] itself is sealed and cannot be thrown or caught as a
 * standalone concrete type — catch it to narrow exhaustively over all four subclasses, or catch
 * one subclass directly to only handle that category:
 *
 * ```kotlin
 * throw StatusException.DeniedException(Codes.UNAUTHORIZED)
 *
 * try {
 *     // ...
 * } catch (e: StatusException) {
 *     when (e) {
 *         is StatusException.DeniedException    -> // handle auth failure
 *         is StatusException.InvalidException   -> // handle bad input
 *         is StatusException.ErroredException   -> // handle known business-rule failure
 *         is StatusException.UnservedException  -> // handle capacity / timeout / unimplemented / unexpected
 *     }
 * }
 *
 * // narrow catch, no `when` needed
 * catch (e: StatusException.DeniedException) { /* ... */ }
 * ```
 *
 * Use [Failed.toException] to convert a bare [Failed] status into the matching subclass without
 * writing the `when` yourself.
 *
 * Each subclass is `open`, so a one-line domain-specific subclass remains possible, e.g.
 * `class RegistrationException(status: Failed.Denied, ...) : StatusException.DeniedException(...)`.
 *
 * On iOS, prefer the `DeniedError`/`InvalidError`/`ErroredError`/`UnservedError` subclasses
 * defined in `iosMain` — each carries an `@ObjCName` annotation so Swift consumers see an
 * idiomatic `XxxError` name. On JS, prefer the equivalents defined in `jsMain`, annotated with
 * `@JsExport` so TypeScript consumers see them in the generated `.d.ts` file.
 */
sealed class StatusException(
    val checked: Checked,
    cause: Throwable? = null,
) : Exception(checked.status.message, cause) {
    val status: Status get() = checked.status
    val errors: List<Err> get() = checked.errors

    /** Thrown for a [Failed.Denied] status — security / access-control failure. */
    open class DeniedException(status: Failed.Denied, errors: List<Err> = emptyList(), cause: Throwable? = null) :
        StatusException(Checked.failure(status, errors.ifEmpty { listOf(Err.of(status)) }), cause)

    /** Thrown for a [Failed.Invalid] status — the request as given cannot be satisfied. */
    open class InvalidException(status: Failed.Invalid, errors: List<Err> = emptyList(), cause: Throwable? = null) :
        StatusException(Checked.failure(status, errors.ifEmpty { listOf(Err.of(status)) }), cause)

    /** Thrown for a [Failed.Errored] status — a known, expected business-rule failure. */
    open class ErroredException(status: Failed.Errored, errors: List<Err> = emptyList(), cause: Throwable? = null) :
        StatusException(Checked.failure(status, errors.ifEmpty { listOf(Err.of(status)) }), cause)

    /** Thrown for a [Failed.Unserved] status — valid & permitted, but can't be serviced right now. */
    open class UnservedException(status: Failed.Unserved, errors: List<Err> = emptyList(), cause: Throwable? = null) :
        StatusException(Checked.failure(status, errors.ifEmpty { listOf(Err.of(status)) }), cause)
}

/**
 * Converts a bare [Failed] status into the matching [StatusException] subclass, so callers don't
 * need to write the `when` themselves.
 *
 * Deliberately has no `else` branch: if [Failed] ever gains a new subtype, this becomes a
 * compile error to fix here, not something a wildcard branch would silently mishandle.
 */
fun Failed.toException(errors: List<Err> = emptyList()): StatusException =
    when (this) {
        is Failed.Denied -> StatusException.DeniedException(this, errors)
        is Failed.Invalid -> StatusException.InvalidException(this, errors)
        is Failed.Errored -> StatusException.ErroredException(this, errors)
        is Failed.Unserved -> StatusException.UnservedException(this, errors)
    }
