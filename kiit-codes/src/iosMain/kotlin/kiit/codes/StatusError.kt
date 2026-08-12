package kiit.codes

/**
 * Swift-idiomatic aliases for the [StatusException] subclasses.
 *
 * Swift error types are conventionally named `XxxError` (e.g. `URLError`, `CocoaError`).
 * `@ObjCName` prevents the auto-generated `KiitCodesXxxError` prefix in the ObjC header, giving
 * Swift consumers the clean names below.
 *
 * Swift usage — construction and property access work directly:
 * ```swift
 * let e = RestrictedError(status: Failed.Restricted.companion.UNAUTHENTICATED, errors: [], cause: nil)
 * print(e.status.name)  // "UNAUTHENTICATED"
 * ```
 *
 * Manually `throw`-ing a constructed instance does **not** work — confirmed by actually running
 * it, not just compiling it: `throw restrictedError as! Error` compiles (the `Error` conformance
 * is a dynamic cast the type-checker can't verify), but crashes at runtime with "Could not cast
 * value of type 'KiitCodesRestrictedError' ... to 'Swift.Error'". Kotlin exception types do not
 * bridge to Swift's `Error` protocol when manually thrown this way, SKIE or no SKIE. The SKIE
 * plugin *is* applied to this module, and its real throws-bridging feature — a Kotlin function
 * annotated `@Throws(SomeException::class)` automatically becomes a native Swift `throws`
 * function at its call site, with no manual casting — genuinely works, but needs such a function
 * to call. **kiit-codes doesn't have one yet** — there is currently no way to idiomatically
 * `throw`/`catch` one of these error types from Swift. See `samples/sample-swift` for the
 * verified-working subset (construction, property access, `onEnum(of:)` exhaustive matching).
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
