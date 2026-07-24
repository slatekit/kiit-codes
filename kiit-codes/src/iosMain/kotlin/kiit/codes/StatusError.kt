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
 * } catch let e as RestrictedError {
 *     print(e.status.name)  // e.g. "UNAUTHORIZED"
 * }
 * ```
 *
 * With the SKIE plugin, functions annotated with `@Throws(...)` are automatically bridged to
 * idiomatic Swift `throws` with no manual NSError casting.
 */
@OptIn(kotlin.experimental.ExperimentalObjCName::class)
@ObjCName("RestrictedError", swiftName = "RestrictedError")
class RestrictedError(
    status: Failed.Restricted,
    errors: List<Err> = emptyList(),
    cause: Throwable? = null,
) : StatusException.RestrictedException(status, errors, cause)

@OptIn(kotlin.experimental.ExperimentalObjCName::class)
@ObjCName("InvalidError", swiftName = "InvalidError")
class InvalidError(
    status: Failed.Invalid,
    errors: List<Err> = emptyList(),
    cause: Throwable? = null,
) : StatusException.InvalidException(status, errors, cause)

@OptIn(kotlin.experimental.ExperimentalObjCName::class)
@ObjCName("RejectedError", swiftName = "RejectedError")
class RejectedError(
    status: Failed.Rejected,
    errors: List<Err> = emptyList(),
    cause: Throwable? = null,
) : StatusException.RejectedException(status, errors, cause)

@OptIn(kotlin.experimental.ExperimentalObjCName::class)
@ObjCName("UnservedError", swiftName = "UnservedError")
class UnservedError(
    status: Failed.Unserved,
    errors: List<Err> = emptyList(),
    cause: Throwable? = null,
) : StatusException.UnservedException(status, errors, cause)
