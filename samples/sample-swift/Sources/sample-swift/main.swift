// Living documentation of kiit-codes from real Swift, compiled and run against the actual
// KiitCodes.framework (Kotlin/Native) — SKIE compiles its generated Swift wrapper code (onEnum,
// the __Sealed enums, etc.) directly into the framework's own swiftmodule, so `import KiitCodes`
// is all that's needed. Mirrors the scenarios in
// kiit-codes/src/jvmTest/java/kiit/codes/JavaInteropTest.java, samples/sample-java, and
// samples/sample-ts, adapted to what SKIE actually generates (confirmed by inspecting its real
// output, not assumed from docs).
import Foundation
import KiitCodes

func check(_ condition: Bool, _ label: String) {
    guard condition else {
        fatalError("FAILED: \(label)")
    }
    print("ok: \(label)")
}

// Companion constant access — unlike @JvmField (Java) and @JsStatic (JS/TS), plain Kotlin/Native
// interop has NO flattening mechanism: companion members always go through `.companion`, with no
// equivalent annotation to make them directly static. Confirmed via the real ObjC header — every
// leaf type exposes `class var companion: XxxCompanion { get }`, not the members directly.
let ok = Passed.Succeeded.companion.SUCCESS
let denied = Failed.Restricted.companion.DENIED
check(ok.name == "SUCCESS", "Passed.Succeeded.companion.SUCCESS.name")
check(denied.name == "DENIED", "Failed.Restricted.companion.DENIED.name")

// Err — companion factory functions, same `.companion` pattern.
let err = Err.companion.of(msg: "email is required", ex: nil)
check(err.msg == "email is required", "Err.companion.of(msg:ex:).msg")

// CodesToHttp — Kotlin's default-valued constructor does NOT get an automatic zero-arg
// convenience initializer preserved for Swift (confirmed: `init()` compiles but is marked
// `unavailable` in the header) — same category of gap `@JvmOverloads` fixes for Java. Callers
// must pass the full argument explicitly.
let http = CodesToHttp(overrides: [:])
check(http.toCode(status: ok) == 200, "CodesToHttp.toCode(SUCCESS) == 200")
check(http.toCode(status: denied) == 401, "CodesToHttp.toCode(DENIED) == 401")

// Checked.success / Checked.failure — SKIE bridges Swift arrays directly, no manual
// KotlinArray/NSArray wrapping needed (nicer than the JS side, which needs KtList.fromJsArray).
let validEmail = Checked.companion.success(status: ok)
check(validEmail.isValid, "Checked.companion.success(status:).isValid")

let invalidEmail = Checked.companion.failure(status: Failed.Invalid.companion.BAD_REQUEST, errors: [err])
check(!invalidEmail.isValid, "Checked.companion.failure(...).isValid == false")

// collect — SKIE-generated Swift wrapper takes a native Swift array.
let combined = collect(checks: [validEmail, invalidEmail])
check(!combined.isValid, "collect(checks:).isValid == false")
check(combined.errors.count == 1, "collect(checks:).errors.count == 1")

// codesAll / codesStatusFor — SKIE-generated top-level wrapper functions for the Codes object
// (plain Kotlin `object`s don't get clean static member access even with SKIE — same fundamental
// gap as JS; the codesAll()/codesStatusFor() top-level proxies from Codes.kt are what make these
// reachable at all, and SKIE further wraps them into plain Swift functions with native types).
let all = codesAll()
check(all.count > 0, "codesAll().count > 0")
let found = codesStatusFor(origin: "kiit", name: "SUCCESS")
check(found != nil, "codesStatusFor(origin:name:) found")

// RestrictedError — construction and property access work fine directly.
//
// Manually `throw`-ing one, though, does NOT work — confirmed by actually running it, not just
// compiling it. `throw RestrictedError(...) as! Error` compiles (the compiler treats the Error
// conformance as a dynamic cast it can't verify statically), but crashes at runtime:
//   "Could not cast value of type 'KiitCodesRestrictedError' ... to 'Swift.Error'"
// So Kotlin exception types do NOT bridge to Swift's `Error` protocol at all when manually
// constructed and thrown from Swift — despite StatusError.kt's own KDoc showing exactly this
// `catch let e as RestrictedError` pattern as its "without SKIE" example. That example doesn't
// actually work, with or without SKIE. The real SKIE feature this KDoc is describing —
// `@Throws`-annotated Kotlin functions bridging to native Swift `throws` — needs a Kotlin
// function marked `@Throws(...)` to call; kiit-codes doesn't have one anywhere yet (confirmed:
// zero `@Throws` usages in the whole framework header). This is a genuine doc/reality gap, not
// just an optional-nicety gap SKIE closes — the manual pattern the doc offers as a fallback is
// broken today.
let restrictedError = RestrictedError(status: Failed.Restricted.companion.UNAUTHENTICATED, errors: [], cause: nil)
check(restrictedError.status.name == "UNAUTHENTICATED", "RestrictedError(status:).status.name")

// Exhaustiveness — the actual point of adding SKIE. Confirmed empirically (not from docs, which
// don't cover multi-level sealed hierarchies) that SKIE generates ONE Swift enum PER Kotlin
// sealed type, not a single flattened enum for the whole Status -> Passed/Failed -> leaf-type
// hierarchy. So getting from a `Status` down to a concrete leaf type (e.g. Succeeded vs Pending)
// takes TWO nested `onEnum(of:)` calls / switches — each individually exhaustive and
// compiler-enforced, not one flat 8-way match. This is still a real, meaningful upgrade over
// plain Kotlin/Native interop (which gives zero exhaustiveness) and over TypeScript (which also
// gives zero and needs a hand-maintained union) — but it's nested, not flat.
func describe(_ status: Status) -> String {
    switch onEnum(of: status) {
    case .passed(let passed):
        switch onEnum(of: passed) {
        case .succeeded(let s): return "Succeeded: \(s.name)"
        case .pending(let p): return "Pending: \(p.name)"
        case .excluded(let e): return "Excluded: \(e.name)"
        case .information(let i): return "Information: \(i.name)"
        }
    case .failed(let failed):
        switch onEnum(of: failed) {
        case .restricted(let r): return "Restricted: \(r.name)"
        case .invalid(let i): return "Invalid: \(i.name)"
        case .rejected(let r): return "Rejected: \(r.name)"
        case .unserved(let u): return "Unserved: \(u.name)"
        }
    }
    // No `default:` needed at either level — both switches are exhaustive over their own
    // sealed type. Try commenting out any single case above: it's a compile error, not a
    // runtime surprise, at both levels independently.
}

check(describe(ok) == "Succeeded: SUCCESS", "describe(Succeeded.SUCCESS)")
check(describe(denied) == "Restricted: DENIED", "describe(Restricted.DENIED)")

print("All sample-swift checks passed.")
