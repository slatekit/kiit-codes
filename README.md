<div align="center">

# kiit.codes

**A status/error** taxonomy conceptually similar to Http Status Codes/gRPC codes for Kotlin — plus a complete, exception-based way to actually use it.

[![Maven Central](https://img.shields.io/maven-central/v/dev.kiit/kiit-codes?color=blue)](https://central.sonatype.com/artifact/dev.kiit/kiit-codes)
[![Build](https://img.shields.io/github/actions/workflow/status/slatekit/kiit-codes/build.yml?branch=main)](https://github.com/slatekit/kiit-codes/actions)
[![License](https://img.shields.io/github/license/slatekit/kiit-codes)](./LICENSE)
[![Kotlin](https://img.shields.io/badge/kotlin-multiplatform-purple.svg)](https://kotlinlang.org)

Part of the [Kiit](https://www.kiit.dev) framework · [kiit.dev/codes](https://www.kiit.dev/codes) · [Blog post](#) · [Video walkthrough](#)

</div>

---

## 📚 Table of Contents

- [ℹ️ About](#ℹ️-about)
- [🧩 The problem](#-the-problem)
- [💡 The idea](#-the-idea)
- [🚀 Quick start](#-quick-start)
- [🧠 Core concepts](#-core-concepts)
- [📖 Built-in codes](#-built-in-codes)
- [🌐 HTTP conversion](#-http-conversion)
- [🧾 Err & Checked](#-err--checked)
- [⚠️ Exceptions](#️-exceptions)
- [🛠️ Use cases](#️-use-cases)
- [✅ When to use this](#-when-to-use-this-and-when-not-to)
- [📦 Requirements](#-requirements)
- [🗺️ Roadmap](#️-roadmap)
- [🤝 Contributing](#-contributing)
- [📄 License](#-license)

---

## ℹ️ About

**kiit.codes** is a platform-agnostic set of status and error code types for Kotlin Multiplatform. It describes the outcome of any operation — a service call, a background job step, an API request, a CLI command — using a consistent, structured shape instead of raw exceptions or ad-hoc booleans.

It's really two things working together, not one:

1. **A closed taxonomy.** Every outcome is a `Status`, a stable `name`, a `group`, an `origin`, a constant `message`, and a `success` flag, grouped into a small, fixed set of categories (`Succeeded`, `Pending`, `Filtered`, `Information`, `Denied`, `Invalid`, `Errored`, `Unserved`) that's consistent across every layer and every target — JVM, Android, JS/TypeScript, and iOS.
2. **A complete, exception-based way to use it.** `Checked` captures the actual detail behind a failure, and a sealed `StatusException` family lets you throw and catch that detail without ever losing structure, no `Result` type, no functional-programming buy-in required.

It's a small, dependency-free library — you can adopt it on its own, independent of the rest of [Kiit](https://www.kiit.dev). A separate module, `kiit-results`, builds a `Result<T, E>` type on top of this same taxonomy for anyone who prefers explicit return values over exceptions, but it isn't required to get real value out of this package alone.

```json
{
    "name"   : "TOKEN_EXPIRED",
    "group"  : "Denied",
    "origin" : "kiit",
    "success": false,
    "message": "Session token expired"
}
```

## 🧩 The problem

Most codebases end up with three incompatible ways of describing "what happened": exceptions (expensive, unstructured, and easy to over- or under-catch), raw booleans (`success: Boolean` — no room to say *why*), and ad-hoc HTTP status codes borrowed as a stand-in for domain meaning even outside an HTTP context.

None of these compose well. A background job doesn't have an HTTP status. A CLI command's "help was printed" isn't a failure, but it also isn't the same kind of success as "the record was created." And a huge amount of exception handling is boilerplate, the same custom exception class, rewritten per domain, mostly just to get dispatch and a place to stash a couple of fields.

## 💡 The idea

**kiit.codes is a closed taxonomy of outcomes, layered on top of open, extensible codes, with a real, exception-based way to act on both.**

The eight categories (`Passed = Succeeded | Pending | Filtered | Information`, `Failed = Denied | Invalid | Errored | Unserved`) are fixed by design — every consumer branches on the same shape. Individual codes *within* a category are yours to extend: construct a `Passed.*` or `Failed.*` subtype directly for any domain-specific outcome, tagged with your own `origin`, and it still slots into the same taxonomy for logging, aggregation, and HTTP conversion.

On top of that, `Checked` and a sealed `StatusException` give you a structured, compiler-checked replacement for most custom exception classes, not a taxonomy waiting for a second library to become useful.

## 🚀 Quick start

**Gradle (Kotlin DSL):**

```kotlin
dependencies {
    implementation("dev.kiit:kiit-codes:0.1.2")
}
```

**Return a status when the outcome itself is all the detail you need:**

```kotlin
fun authorize(userId: String, requesterId: String): Status =
    if (userId != requesterId) Codes.UNAUTHORIZED else Codes.SUCCESS
```

**Branch on the outcome, exhaustively:**

```kotlin
when (val status = authorize(userId, requesterId)) {
    is Passed -> log.info("ok: ${status.name}")
    is Failed -> log.warn("failed: ${status.name} — ${status.message}")
}
```

**Return `Checked` when a failure needs to carry *why*, not just *that*:**

```kotlin
fun createUser(email: String): Checked =
    if (users.containsKey(email)) {
        Checked.failure(Codes.CONFLICT, listOf(Err.on("email", email, "already registered")))
    } else {
        // ... create the user ...
        Checked.success(Codes.CREATED)
    }

val result = createUser(email)
if (!result.isValid) {
    log.warn("failed: ${result.status.name} — ${result.errors}")
}
```

**Report more than one problem at once with `collect`:**

```kotlin
fun checkEmail(email: String): Checked =
    if (email.contains("@")) Checked.success()
    else Checked.failure(Codes.INVALID, listOf(Err.on("email", email, "must contain @")))

fun checkPhone(phone: String): Checked =
    if (phone.length >= 10) Checked.success()
    else Checked.failure(Codes.INVALID, listOf(Err.on("phone", phone, "too short")))

val result = collect(checkEmail(email), checkPhone(phone))
if (!result.isValid) {
    // result.errors has every problem found, not just the first
}
```

**Throw with structure, catch with structure:**

```kotlin
throw StatusException.DeniedException(Codes.UNAUTHORIZED)

try {
    // ...
} catch (e: StatusException.DeniedException) {
    // handled without ever touching a when block
}
```

**Convert to HTTP when you need a real status code:**

```kotlin
val http = CodesToHttp()
http.toCode(Codes.CREATED)   // 201
http.toCode(Codes.DENIED)    // 401
```

See [`samples/sample1`](./samples/sample1) for a runnable end-to-end example.

## 🧠 Core concepts

```
Status = Passed    | Failed
Passed = Succeeded | Pending | Filtered | Information
Failed = Denied    | Invalid | Errored  | Unserved
```

```mermaid
graph TD
    classDef statusNode        fill:#3b82f6,stroke:#1d4ed8,color:#ffffff,font-weight:bold
    classDef passedNode        fill:#86efac,stroke:#16a34a,color:#14532d,font-weight:bold
    classDef succeededNode     fill:#22c55e,stroke:#15803d,color:#ffffff,font-weight:bold
    classDef pendingNode       fill:#fde047,stroke:#ca8a04,color:#713f12,font-weight:bold
    classDef filteredNode      fill:#9ca3af,stroke:#6b7280,color:#ffffff,font-weight:bold
    classDef informationNode   fill:#38bdf8,stroke:#0284c7,color:#0c4a6e,font-weight:bold
    classDef failedNode        fill:#fca5a5,stroke:#f87171,color:#7f1d1d,font-weight:bold
    classDef deniedNode        fill:#111827,stroke:#000000,color:#ffffff,font-weight:bold
    classDef invalidNode       fill:#f97316,stroke:#c2410c,color:#ffffff,font-weight:bold
    classDef erroredNode       fill:#dc2626,stroke:#b91c1c,color:#ffffff,font-weight:bold
    classDef unservedNode      fill:#7f1d1d,stroke:#450a0a,color:#ffffff,font-weight:bold

    Status["Status<br/>name / group / origin / message / success"]:::statusNode

    Passed["Passed<br/>success: true"]:::passedNode
    Failed["Failed<br/>success: false"]:::failedNode

    Succeeded["Succeeded<br/>group: Succeeded"]:::succeededNode
    Pending["Pending<br/>group: Pending"]:::pendingNode
    Filtered["Filtered<br/>group: Filtered"]:::filteredNode
    Information["Information<br/>group: Information"]:::informationNode

    Denied["Denied<br/>group: Denied"]:::deniedNode
    Invalid["Invalid<br/>group: Invalid"]:::invalidNode
    Errored["Errored<br/>group: Errored"]:::erroredNode
    Unserved["Unserved<br/>group: Unserved"]:::unservedNode

    Status --> Passed
    Status --> Failed
    Passed --> Succeeded
    Passed --> Pending
    Passed --> Filtered
    Passed --> Information
    Failed --> Denied
    Failed --> Invalid
    Failed --> Errored
    Failed --> Unserved
```

| Term | What it is |
|---|---|
| **Status** | The outcome of an operation — `name`, `group`, `origin`, `message`, `success`. Sealed: `Passed` or `Failed`. |
| **Passed** | `Succeeded` (primary purpose completed), `Pending` (accepted, not yet done), `Filtered` (excluded — not processed, or processed and discarded), `Information` (metadata output, e.g. `HELP`). |
| **Failed** | `Denied` (security/access-control), `Invalid` (bad input), `Errored` (known business-rule failure), `Unserved` (valid & permitted, but can't be handled right now). |
| **origin** | Where a code came from — `"kiit"` for built-ins, your own module or team name for custom codes. Uniqueness is enforced over `(origin, name)`, not globally, so two teams can both have a code named `CONFLICT` without colliding. |
| **Codes** | The built-in registry of common `Status` instances — optional, and duplicate-checked at init time. |
| **CodeLookup** | Bidirectional conversion between a `Status` and a target protocol's code (`toCode`/`toStatus`), direction-explicit so the two code spaces can't be confused. |
| **Err** | A single piece of instance-level detail behind a failure, a field, a value, a cause — the thing `Status` deliberately doesn't carry on its own. |
| **Checked** | A `Status` plus zero or more `Err`, for reporting every problem found at once instead of stopping at the first one. `collect` combines several `Checked` into one. |
| **StatusException** | Sealed, carries a `Checked` across a call boundary that can only communicate via exceptions. One subtype per `Failed` category — `DeniedException`, `InvalidException`, `ErroredException`, `UnservedException`. |

## 📖 Built-in codes

The `Codes` object provides a standard registry — using it is optional, and you can construct any `Passed`/`Failed` subtype directly for domain-specific outcomes.

| Category | Examples |
|---|---|
| Succeeded | `SUCCESS`, `CREATED`, `UPDATED`, `FETCHED`, `DELETED`, `HANDLED` |
| Pending | `PENDING`, `QUEUED`, `CONFIRM` |
| Filtered | `SKIPPED` (not processed), `DISCARDED` (processed, result thrown away) |
| Information | `HELP`, `ABOUT`, `VERSION`, `EXIT` |
| Denied | `DENIED`, `UNAUTHENTICATED`, `UNAUTHORIZED` |
| Invalid | `BAD_REQUEST`, `INVALID`, `NOT_FOUND` |
| Errored | `MISSING`, `FORBIDDEN`, `CONFLICT`, `DEPRECATED`, `ERRORED` |
| Unserved | `UNIMPLEMENTED`, `UNSUPPORTED`, `TIMEOUT`, `RATE_LIMITED`, `UNREACHABLE`, `UNDER_MAINTENANCE`, `UNEXPECTED` |

Every built-in code's `origin` is `"kiit"`. Custom codes should supply their own, a module or team name, rather than relying on a default, so uniqueness only has to hold within your own `origin`, not globally:

```kotlin
val PAYMENT_DECLINED = Failed.Errored("PAYMENT_DECLINED", "Payment declined", origin = "payments")
```

Uniqueness over `(origin, name)` is enforced at object-init time, a collision fails loudly the first time `Codes` is touched, rather than silently producing a wrong lookup later.

## 🌐 HTTP conversion

`CodesToHttp` maps `Status` to HTTP status codes: a compiler-exhaustive category default (`Succeeded` → 200, `Denied` → 401, etc.), layered with a small overrides table, keyed by `Status` instance, for the handful of codes that differ (`CREATED` → 201, `NOT_FOUND` → 404). `toStatus` is derived from `toCode`, so the two directions can never drift apart.

```kotlin
val http = CodesToHttp()
http.toStatus(404)?.name   // "NOT_FOUND"
http.toStatus(999)         // null — unrecognized code, no guessed fallback
```

`CompositeLookup` composes a base lookup with your own extensions, also keyed by `Status` instance so custom, unregistered statuses are reverse-lookupable too:

```kotlin
val lookup = CompositeLookup(base = CodesToHttp(), extensions = mapOf(PAYMENT_DECLINED to 402))
lookup.toCode(PAYMENT_DECLINED) // 402
```

## 🧾 Err & Checked

`Status` describes the *kind* of outcome, not the specific value behind it. `Err` and `Checked` are what fill that in.

- **`Err`** carries one piece of real, per-occurrence detail — a field name, a bad value, an underlying `cause`.
- **`Checked`** pairs a `Status` with zero or more `Err`. Its constructor is private, only reachable through `success()`/`failure()`, so a passing `Checked` can never carry errors and a failing one can never be empty.
- **`collect`** combines several `Checked` into one, pooling every error from every failing entry, not just the first.

```kotlin
fun validateEmail(email: String): Checked =
    if (email.contains("@")) Checked.success()
    else Checked.failure(Codes.INVALID, listOf(Err.of(Codes.INVALID, "must contain @")))

fun validatePhone(phone: String): Checked =
    if (phone.length >= 10) Checked.success()
    else Checked.failure(Codes.INVALID, listOf(Err.of(Codes.INVALID, "too short")))

val result = collect(validateEmail(email), validatePhone(phone))
if (!result.isValid) {
    // result.errors has every problem found, not just the first
}
```

`Checked` is deliberately non-monadic, no `map`/`flatMap`. Accumulate with `collect` before you have a value, don't chain afterward.

## ⚠️ Exceptions

`StatusException` is sealed, one subtype per `Failed` category, each carrying a `Checked` so no structure is lost crossing an exception-only boundary:

```kotlin
throw StatusException.DeniedException(Codes.UNAUTHORIZED)

try {
    // ...
} catch (e: StatusException) {
    when (e) {
        is StatusException.DeniedException    -> // handle auth failure
        is StatusException.InvalidException   -> // handle bad input
        is StatusException.ErroredException   -> // handle known business-rule failure
        is StatusException.UnservedException  -> // handle capacity / timeout / unimplemented / unexpected
    }
}
```

Or catch narrowly, by class, without ever touching a `when` block:

```kotlin
catch (e: StatusException.DeniedException) { /* handled */ }
```

**This replaces most of what a custom exception class used to do.** A hand-rolled `RegistrationException` was usually doing three jobs at once, routing by class, carrying custom fields, and grouping a family of related failures. `Status`, `Checked`, and the sealed exception family already do all three, generally better, since dispatch and grouping are compiler-checked instead of left to a class hierarchy you maintain by hand.

**Before:**

```kotlin
class RegistrationException(
    val field: String,
    val reason: String,
) : Exception("$field: $reason")
```

**After:**

```kotlin
throw StatusException.InvalidException(Codes.INVALID, listOf(Err.of(Codes.INVALID, "email: already taken")))
```

If a named class is still useful for framework or crash-tooling reasons that dispatch on exception type specifically, each subtype is `open`, so it's a one-line addition, not a whole class with its own fields and catch logic:

```kotlin
class RegistrationException(status: Failed.Denied, errors: List<Err> = emptyList()) :
    StatusException.DeniedException(status, errors)
```

**Converting a bare `Failed` you already have in hand:**

```kotlin
fun Failed.toException(errors: List<Err> = emptyList()): StatusException =
    when (this) {
        is Failed.Denied     -> StatusException.DeniedException(this, errors)
        is Failed.Invalid    -> StatusException.InvalidException(this, errors)
        is Failed.Errored    -> StatusException.ErroredException(this, errors)
        is Failed.Unserved   -> StatusException.UnservedException(this, errors)
    }
```

## 🛠️ Use cases

1. **Service layers** — return a `Status` instead of throwing for expected failures; reserve exceptions for boundary crossings.
2. **Validation** — `Checked` and `collect` report every problem found at once, not just the first.
3. **API responses** — a consistent, structured error body across every endpoint, convertible to a real HTTP code via `CodesToHttp`.
4. **Existing, exception-based codebases** — `StatusException` supplements exceptions rather than replacing them, adopt it in one service or one endpoint without a rewrite.
5. **Background jobs / CLIs** — `Pending`/`Information` categories that don't map cleanly to HTTP but still need a consistent shape.
6. **Logging & metrics** — `name`, `group`, and `origin` are stable, aggregable, and searchable keys, for humans and for AI tooling reading the codebase.
7. **Cross-platform consumers** — the same taxonomy on JVM, Android, JS/TypeScript, and iOS.

## ✅ When to use this and when not to

**Good fit if:**
1. You want one consistent shape for "what happened" across services, jobs, APIs, and CLIs.
2. You're tired of writing a new custom exception class for every domain, just to get dispatch and a couple of fields.
3. You need to convert internal outcomes to HTTP (or another protocol) without hardcoding numeric ranges.
4. You're building or consuming a Kotlin Multiplatform target (JS/iOS) and want idiomatic error types on each side.

**Probably not necessary if:**
1. Your app is entirely internal, single-platform, and exceptions already communicate everything you need.
2. You want explicit, monadic return values (`Result<T, E>`) rather than throw/catch, in which case see [kiit-results](https://github.com/slatekit/kiit), which builds on this same taxonomy.

## 📦 Requirements

1. Kotlin Multiplatform — JVM, Android, JS (IR), iOS (arm64, simulator arm64, x64)
2. No external runtime dependencies

## 🗺️ Roadmap

- [ ] npm publish pipeline for JS consumers (`@kiit/codes`)
- [ ] SPM / XCFramework pipeline for Swift consumers
- [ ] GitHub Actions workflow for CI + Maven Central publish

Track progress or open a discussion in [Issues](https://github.com/slatekit/kiit-codes/issues).

## 🤝 Contributing

Contributions are welcome — see [BUILD.md](./BUILD.md) for build, test, and publish instructions.

## 📄 License

[Apache License 2.0](./LICENSE)

---

<div align="center">

kiit.codes is one module of **[Kiit](https://www.kiit.dev)** — a lightweight, modular, 100% Kotlin framework for building server apps, APIs, CLIs, and jobs. Adopt one module at a time.

</div>
