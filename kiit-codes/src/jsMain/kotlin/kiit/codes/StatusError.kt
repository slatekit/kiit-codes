@file:OptIn(ExperimentalJsExport::class)

package kiit.codes

import kotlin.js.ExperimentalJsExport

/**
 * JS/TypeScript-idiomatic aliases for the [StatusException] subclasses.
 *
 * JavaScript error types are conventionally named `XxxError`. Each subclass is annotated with
 * `@JsExport` so it appears in the generated `.d.ts`, while [StatusException] remains internal
 * to the Kotlin bundle and is not exported.
 *
 * TypeScript usage — everything nests under the `kiit.codes` namespace in the generated `.d.ts`,
 * so a single `kiit` import covers the whole library:
 * ```ts
 * import { kiit } from '@kiit/codes'
 *
 * throw new kiit.codes.RestrictedError(kiit.codes.Failed.Restricted.UNAUTHENTICATED)
 *
 * try { ... } catch (e) {
 *     if (e instanceof kiit.codes.RestrictedError) { console.log(e.status.name) }
 * }
 * ```
 *
 * Each subclass declares its own `status`/`errors` properties (renamed to those exact names for
 * JS via `@JsName`, distinct Kotlin-side names to avoid colliding with the inherited, non-open
 * [StatusException.status]/[StatusException.errors]) rather than relying on the inherited ones as
 * they stand — [StatusException] isn't exported, so its members would otherwise be completely
 * invisible in the generated `.d.ts` for these subtypes (confirmed empirically: even an `override`
 * with a narrower type is silently dropped from the `.d.ts` by the exporter, which assumes
 * overridden members are already described on the — here, invisible — supertype).
 */
@JsExport
@JsName("RestrictedError")
class RestrictedError(
    status: Failed.Restricted,
    errors: List<Err> = emptyList(),
    cause: Throwable? = null,
) : StatusException.RestrictedException(status, errors, cause) {
    @JsName("status")
    val restrictedStatus: Failed.Restricted = status

    @JsName("errors")
    val exportedErrors: List<Err> get() = checked.errors
}

@JsExport
@JsName("InvalidError")
class InvalidError(
    status: Failed.Invalid,
    errors: List<Err> = emptyList(),
    cause: Throwable? = null,
) : StatusException.InvalidException(status, errors, cause) {
    @JsName("status")
    val invalidStatus: Failed.Invalid = status

    @JsName("errors")
    val exportedErrors: List<Err> get() = checked.errors
}

@JsExport
@JsName("RejectedError")
class RejectedError(
    status: Failed.Rejected,
    errors: List<Err> = emptyList(),
    cause: Throwable? = null,
) : StatusException.RejectedException(status, errors, cause) {
    @JsName("status")
    val rejectedStatus: Failed.Rejected = status

    @JsName("errors")
    val exportedErrors: List<Err> get() = checked.errors
}

@JsExport
@JsName("UnservedError")
class UnservedError(
    status: Failed.Unserved,
    errors: List<Err> = emptyList(),
    cause: Throwable? = null,
) : StatusException.UnservedException(status, errors, cause) {
    @JsName("status")
    val unservedStatus: Failed.Unserved = status

    @JsName("errors")
    val exportedErrors: List<Err> get() = checked.errors
}
