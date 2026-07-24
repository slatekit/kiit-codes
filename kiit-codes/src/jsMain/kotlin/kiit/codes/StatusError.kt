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
 * import { DeniedError, Codes } from '@kiit/codes'
 *
 * throw new DeniedError(Codes.UNAUTHORIZED)
 *
 * try { ... } catch (e) {
 *     if (e instanceof DeniedError) { console.log(e.status.name) }
 * }
 * ```
 */
@JsExport
@JsName("DeniedError")
class DeniedError(
    status: Failed.Denied,
    errors: List<Err> = emptyList(),
    cause: Throwable? = null,
) : StatusException.DeniedException(status, errors, cause)

@JsExport
@JsName("InvalidError")
class InvalidError(
    status: Failed.Invalid,
    errors: List<Err> = emptyList(),
    cause: Throwable? = null,
) : StatusException.InvalidException(status, errors, cause)

@JsExport
@JsName("ErroredError")
class ErroredError(
    status: Failed.Errored,
    errors: List<Err> = emptyList(),
    cause: Throwable? = null,
) : StatusException.ErroredException(status, errors, cause)

@JsExport
@JsName("UnservedError")
class UnservedError(
    status: Failed.Unserved,
    errors: List<Err> = emptyList(),
    cause: Throwable? = null,
) : StatusException.UnservedException(status, errors, cause)
