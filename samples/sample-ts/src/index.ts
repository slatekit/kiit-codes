/**
 * Living documentation of kiit-codes from real TypeScript, type-checked (`npm run typecheck`)
 * against the actual generated `.d.ts`, not a hand-written stub. Mirrors the scenarios in
 * kiit-codes/src/jvmTest/java/kiit/codes/JavaInteropTest.java and samples/sample-java (everything
 * nests under a single `kiit` namespace import).
 */
import { kiit, kotlin } from "@kiit/codes"

const { Passed, Failed, Err, Checked, CodesToHttp, RestrictedError, collect, codesAll, codesStatusFor } = kiit.codes
const { KtList } = kotlin.collections

function check(condition: boolean, label: string): void {
  if (!condition) {
    throw new Error(`FAILED: ${label}`)
  }
  console.log(`ok: ${label}`)
}

// Companion constant access: Passed.Succeeded.SUCCESS, Failed.Restricted.DENIED.
const ok = Passed.Succeeded.SUCCESS
const denied = Failed.Restricted.DENIED
check(ok.name === "SUCCESS", "Passed.Succeeded.SUCCESS.name")
check(denied.name === "DENIED", "Failed.Restricted.DENIED.name")

// Err.of: optional trailing `ex` argument omitted.
const err = Err.of("email is required")
check(err.message === "email is required", "Err.of(message).message")

// CodesToHttp: no-arg constructor.
const http = new CodesToHttp()
check(http.toCode(ok) === 200, "CodesToHttp.toCode(SUCCESS) === 200")
check(http.toCode(denied) === 401, "CodesToHttp.toCode(DENIED) === 401")

// Checked.success / Checked.failure: errors is a KtList<Err>, not a native array, so it needs
// KtList.fromJsArray(...) to construct.
const validEmail = Checked.success()
check(validEmail.isValid, "Checked.success().isValid")

const invalidEmail = Checked.failure(Failed.Invalid.BAD_REQUEST, KtList.fromJsArray([err]))
check(!invalidEmail.isValid, "Checked.failure(...).isValid === false")

// collect: the vararg-in-Kotlin overload exports as a plain native JS Array parameter, call it
// with an array.
const combined = collect([validEmail, invalidEmail])
check(!combined.isValid, "collect([...]).isValid === false")
check(combined.errors.asJsReadonlyArrayView().length === 1, "collect([...]).errors.length === 1")

// codesAll / codesStatusFor: top-level proxy functions for the Codes object, since plain Kotlin
// `object`s can't get clean static member access in Kotlin/JS.
const all = codesAll().asJsReadonlyArrayView()
check(all.length > 0, "codesAll().length > 0")
const found = codesStatusFor("kiit", "SUCCESS")
check(found != null, "codesStatusFor('kiit', 'SUCCESS') found")

// RestrictedError: construct/throw/catch, optional trailing errors/cause arguments omitted.
try {
  throw new RestrictedError(Failed.Restricted.UNAUTHENTICATED)
} catch (e) {
  check(e instanceof RestrictedError, "caught e instanceof RestrictedError")
  if (e instanceof RestrictedError) {
    check(e.status.name === "UNAUTHENTICATED", "RestrictedError.status.name")
  }
}

// TypeScript has no compiler-enforced exhaustiveness over Kotlin sealed hierarchies, the
// generated .d.ts emits independent `class` declarations, never a union type. instanceof
// narrowing plus an assertNever fallback is the best available idiom instead, and it only
// catches gaps if AnyPassed below is kept in sync by hand.
//
// Types below use the fully-qualified `kiit.codes.Passed.Succeeded` path, not the destructured
// `Passed` value above, since destructuring only survives on the value side, not the type side.
type AnyPassed =
  | kiit.codes.Passed.Succeeded
  | kiit.codes.Passed.Pending
  | kiit.codes.Passed.Excluded
  | kiit.codes.Passed.Information

function assertNever(x: never): never {
  throw new Error("unhandled Passed case: " + JSON.stringify(x))
}

function describe(p: AnyPassed): string {
  if (p instanceof Passed.Succeeded) return `Succeeded: ${p.name}`
  if (p instanceof Passed.Pending) return `Pending: ${p.name}`
  if (p instanceof Passed.Excluded) return `Excluded: ${p.name}`
  if (p instanceof Passed.Information) return `Information: ${p.name}`
  return assertNever(p)
}

check(describe(ok) === "Succeeded: SUCCESS", "describe(Succeeded.SUCCESS)")

console.log("All sample-ts checks passed.")
