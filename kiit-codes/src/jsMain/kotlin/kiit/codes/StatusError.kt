package kiit.codes

/**
 * JS/TypeScript-idiomatic aliases for the [StatusException] subclasses.
 *
 * JavaScript error types are conventionally named `XxxError`. Each subclass is annotated with
 * `@JsExport` so it appears in the generated `.d.ts`, while [StatusException] remains internal
 * to the Kotlin bundle and is not exported.
 *
 * TypeScript usage:
 * ```ts
 * import { RestrictedError, Codes } from '@kiit/codes'
 *
 * throw new RestrictedError(Codes.UNAUTHORIZED)
 *
 * try { ... } catch (e) {
 *     if (e instanceof RestrictedError) { console.log(e.status.name) }
 * }
 * ```
 */
@JsExport
@JsName("RestrictedError")
class RestrictedError(
    status: Failed.Restricted,
    errors: List<Err> = emptyList(),
    cause: Throwable? = null,
) : StatusException.RestrictedException(status, errors, cause)

@JsExport
@JsName("InvalidError")
class InvalidError(
    status: Failed.Invalid,
    errors: List<Err> = emptyList(),
    cause: Throwable? = null,
) : StatusException.InvalidException(status, errors, cause)

@JsExport
@JsName("RejectedError")
class RejectedError(
    status: Failed.Rejected,
    errors: List<Err> = emptyList(),
    cause: Throwable? = null,
) : StatusException.RejectedException(status, errors, cause)

@JsExport
@JsName("UnservedError")
class UnservedError(
    status: Failed.Unserved,
    errors: List<Err> = emptyList(),
    cause: Throwable? = null,
) : StatusException.UnservedException(status, errors, cause)
