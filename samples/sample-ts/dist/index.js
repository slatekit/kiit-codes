"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
/**
 * Living documentation of kiit-codes from real TypeScript, type-checked (`npm run typecheck`)
 * against the actual generated `.d.ts` at ../../kiit-codes/build/dist/js/productionLibrary — not
 * a hand-written stub. Mirrors the scenarios in
 * kiit-codes/src/jvmTest/java/kiit/codes/JavaInteropTest.java and samples/sample-java, adapted to
 * the real, confirmed `.d.ts` shape (everything nests under a single `kiit` namespace import).
 */
const codes_1 = require("@kiit/codes");
const { Passed, Failed, Err, Checked, CodesToHttp, RestrictedError, collect, codesAll, codesStatusFor } = codes_1.kiit.codes;
const { KtList } = codes_1.kotlin.collections;
function check(condition, label) {
    if (!condition) {
        throw new Error(`FAILED: ${label}`);
    }
    console.log(`ok: ${label}`);
}
// Companion constant access — Passed.Succeeded.SUCCESS, Failed.Restricted.DENIED.
const ok = Passed.Succeeded.SUCCESS;
const denied = Failed.Restricted.DENIED;
check(ok.name === "SUCCESS", "Passed.Succeeded.SUCCESS.name");
check(denied.name === "DENIED", "Failed.Restricted.DENIED.name");
// Err.of — optional trailing `ex` argument omitted.
const err = Err.of("email is required");
check(err.msg === "email is required", "Err.of(msg).msg");
// CodesToHttp — no-arg constructor.
const http = new CodesToHttp();
check(http.toCode(ok) === 200, "CodesToHttp.toCode(SUCCESS) === 200");
check(http.toCode(denied) === 401, "CodesToHttp.toCode(DENIED) === 401");
// Checked.success / Checked.failure — errors is a KtList<Err>, not a native array, so it needs
// KtList.fromJsArray(...) to construct (see Codes.kt's KDoc / README for why this stays a KtList
// rather than a native array: it's a shared commonMain signature, not JS-only).
const validEmail = Checked.success();
check(validEmail.isValid, "Checked.success().isValid");
const invalidEmail = Checked.failure(Failed.Invalid.BAD_REQUEST, KtList.fromJsArray([err]));
check(!invalidEmail.isValid, "Checked.failure(...).isValid === false");
// collect — the vararg-in-Kotlin overload exports as a plain native JS Array parameter (not a
// rest/spread parameter, despite the Kotlin-side `vararg` spelling) — call it with an array.
const combined = collect([validEmail, invalidEmail]);
check(!combined.isValid, "collect([...]).isValid === false");
check(combined.errors.asJsReadonlyArrayView().length === 1, "collect([...]).errors.length === 1");
// codesAll / codesStatusFor — top-level proxy functions for the Codes object (plain Kotlin
// `object`s can't get clean static member access in Kotlin/JS — see Codes.kt's KDoc).
const all = codesAll().asJsReadonlyArrayView();
check(all.length > 0, "codesAll().length > 0");
const found = codesStatusFor("kiit", "SUCCESS");
check(found != null, "codesStatusFor('kiit', 'SUCCESS') found");
// RestrictedError — construct/throw/catch, optional trailing errors/cause arguments omitted.
try {
    throw new RestrictedError(Failed.Restricted.UNAUTHENTICATED);
}
catch (e) {
    check(e instanceof RestrictedError, "caught e instanceof RestrictedError");
    if (e instanceof RestrictedError) {
        check(e.status.name === "UNAUTHENTICATED", "RestrictedError.status.name");
    }
}
function assertNever(x) {
    throw new Error("unhandled Passed case: " + JSON.stringify(x));
}
function describe(p) {
    if (p instanceof Passed.Succeeded)
        return `Succeeded: ${p.name}`;
    if (p instanceof Passed.Pending)
        return `Pending: ${p.name}`;
    if (p instanceof Passed.Excluded)
        return `Excluded: ${p.name}`;
    if (p instanceof Passed.Information)
        return `Information: ${p.name}`;
    return assertNever(p);
}
check(describe(ok) === "Succeeded: SUCCESS", "describe(Succeeded.SUCCESS)");
console.log("All sample-ts checks passed.");
