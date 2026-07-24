package kiit.codes

/**
 * Swift-idiomatic aliases for the [StatusException] subclasses.
 *
 * Swift error types are conventionally named `XxxError` (e.g. `URLError`, `CocoaError`).
 * `@ObjCName` prevents the auto-generated `KiitCodesXxxError` prefix in the ObjC header, giving
 * Swift consumers the clean names below.
 *
 * Swift usage (without SKIE):
 * ```swift
 * do {
 *     try someKotlinApi()
 * } catch let e as DeniedError {
 *     print(e.status.name)  // e.g. "UNAUTHORIZED"
 * }
 * ```
 *
 * With the SKIE plugin, functions annotated with `@Throws(...)` are automatically bridged to
 * idiomatic Swift `throws` with no manual NSError casting.
 */
@OptIn(kotlin.experimental.ExperimentalObjCName::class)
@ObjCName("DeniedError", swiftName = "DeniedError")
class DeniedError(
    status: Failed.Denied,
    errors: List<Err> = emptyList(),
    cause: Throwable? = null,
) : StatusException.DeniedException(status, errors, cause)

@OptIn(kotlin.experimental.ExperimentalObjCName::class)
@ObjCName("InvalidError", swiftName = "InvalidError")
class InvalidError(
    status: Failed.Invalid,
    errors: List<Err> = emptyList(),
    cause: Throwable? = null,
) : StatusException.InvalidException(status, errors, cause)

@OptIn(kotlin.experimental.ExperimentalObjCName::class)
@ObjCName("ErroredError", swiftName = "ErroredError")
class ErroredError(
    status: Failed.Errored,
    errors: List<Err> = emptyList(),
    cause: Throwable? = null,
) : StatusException.ErroredException(status, errors, cause)

@OptIn(kotlin.experimental.ExperimentalObjCName::class)
@ObjCName("UnservedError", swiftName = "UnservedError")
class UnservedError(
    status: Failed.Unserved,
    errors: List<Err> = emptyList(),
    cause: Throwable? = null,
) : StatusException.UnservedException(status, errors, cause)
