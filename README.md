<div align="center">

# kiit-codes

**A Kotlin library for classifying and handling success and failure.**

A small, dependency-free status and error taxonomy for application outcomes — with extensible codes, protocol mappings, validation, typed exceptions, and optional `Result<T, E>` integration.

[![Maven Central](https://img.shields.io/maven-central/v/dev.kiit/kiit-codes?color=blue)](https://central.sonatype.com/artifact/dev.kiit/kiit-codes)
[![Build](https://img.shields.io/github/actions/workflow/status/slatekit/kiit-codes/ci.yml?branch=main)](https://github.com/slatekit/kiit-codes/actions/workflows/ci.yml)
[![License](https://img.shields.io/github/license/slatekit/kiit-codes)](./LICENSE)
[![Kotlin](https://img.shields.io/badge/kotlin-multiplatform-purple.svg)](https://kotlinlang.org)

Part of [Kiit](https://www.kiit.dev) · [Docs](https://www.kiit.dev/codes) · [Blog](#)

</div>

![Kiit Codes overview](./assets/kiit-codes-overview.png)

## Why

Applications need a consistent way to describe **what happened**.

In practice, outcomes are often spread across exceptions, booleans, domain enums, and transport-specific codes such as HTTP statuses. That makes the same outcome harder to classify consistently across APIs, services, jobs, validation, CLIs, logs, and metrics.

**kiit-codes provides one application-level vocabulary for success and failure.**

It separates three concerns:

- **Classification** — a small, fixed taxonomy gives every outcome a consistent meaning.
- **Specificity** — built-in and domain-specific codes describe exactly what happened.
- **Transport** — HTTP, gRPC, and other protocols are mappings at the boundary rather than the application model itself.

The library can be used directly as a `Status`, with validation and collected errors, through typed exceptions, or with the separate `kiit-result` module.

## Start

**Gradle (Kotlin DSL):**

```kotlin
dependencies {
    implementation("dev.kiit:kiit-codes:0.1.2")
}
```

A status can represent an outcome without requiring exceptions or a result type:

```kotlin
import kiit.codes.*

fun authorize(userId: String, requesterId: String): Status =
    if (userId != requesterId) Restricted.UNAUTHORIZED
    else Succeeded.SUCCESS

when (val status = authorize(userId, requesterId)) {
    is Passed -> log.info("ok: ${status.name}")
    is Failed -> log.warn("failed: ${status.name} — ${status.message}")
}
```

Built-in codes expose stable fields suitable for application logic, logging, APIs, and diagnostics:

```json
{
    "id"      : "kiit.CONFLICT",
    "name"    : "CONFLICT",
    "group"   : "Rejected",
    "origin"  : "kiit",
    "success" : false,
    "message" : "The request conflicts with the current state"
}
```

## Taxonomy

Codes uses a three-tier model:

**Status → Group → Code**

`Status` and `Group` are fixed so applications share the same high-level meaning. `Code` is open: Kiit supplies common defaults while applications can add their own domain-specific codes.

![Kiit Codes taxonomy](./assets/kiit-codes-taxonomy.png)

The two statuses are:

- **Passed** — `Succeeded`, `Pending`, `Excluded`, `Information`
- **Failed** — `Restricted`, `Invalid`, `Rejected`, `Unserved`

Each code provides an `id`, `name`, `group`, `origin`, `message`, and `success` flag. Built-in codes use the `kiit` origin and each group has a default code for cases where more precision is unnecessary.

The built-in taxonomy contains common application outcomes such as `SUCCESS`, `CREATED`, `DENIED`, `INVALID_VALUE`, `CONFLICT`, `TIMEOUT`, and `UNEXPECTED`.

## Extensibility

The taxonomy stays consistent while codes remain open to your domain.

![Kiit Codes custom codes](./assets/kiit-codes-custom.png)

Custom codes use the same group types as Kiit's defaults:

```kotlin
import kiit.codes.Failed

val PAYMENT_DECLINED = Failed.Rejected(
    name = "PAYMENT_DECLINED",
    message = "Payment declined",
    origin = "payments"
)
```

`PAYMENT_DECLINED` remains a `Rejected` outcome everywhere in the system while retaining its domain-specific identity. The `origin` keeps custom namespaces distinct from Kiit and from other modules or teams.

## Protocols

Application outcomes should describe **what happened**, independently of how they are transported.

Kiit codes can be mapped to protocol-specific representations at system boundaries.

![Kiit Codes protocol mappings](./assets/kiit-codes-protocols.png)

### HTTP

`CodesToHttp` maps statuses to HTTP codes using group defaults plus specific overrides where needed.

```kotlin
import kiit.codes.*

val http = CodesToHttp()

http.toCode(Succeeded.CREATED) // 201
http.toCode(Invalid.INVALID_VALUE) // 400
http.toStatus(404)?.name // "NOT_FOUND"
```

Reverse conversion is deterministic but can be lossy because multiple application codes may map to the same HTTP status.

### gRPC

`CodesToGrpc` follows the same model and covers all gRPC status codes.

```kotlin
import kiit.codes.*

val grpc = CodesToGrpc()

grpc.toCode(Restricted.DENIED) // 7
grpc.toStatus(9)?.name // "PRECONDITION_FAILED"
```

The mapping abstraction is not limited to HTTP and gRPC. `CodeLookup` and `CompositeLookup` can be used to define or extend mappings for other protocols.

```kotlin
val lookup = CompositeLookup(
    base = CodesToHttp(),
    extensions = mapOf(PAYMENT_DECLINED to 402)
)

lookup.toCode(PAYMENT_DECLINED) // 402
```

## Usage

Codes is designed to work at different application boundaries without requiring one error-handling style.

### Status

Use `Status` when the outcome itself is enough.

```kotlin
import kiit.codes.*

fun authorize(userId: String, requesterId: String): Status =
    if (userId != requesterId) Restricted.UNAUTHORIZED
    else Succeeded.SUCCESS

when (val status = authorize(userId, requesterId)) {
    is Passed -> log.info("ok: ${status.name}")
    is Failed -> log.warn("failed: ${status.name} — ${status.message}")
}
```
![Kiit Codes protocol mappings](./assets/kiit-codes-usage.png)


## Roadmap

kiit-codes is being extracted and polished as a standalone module of Kiit. Current work is focused on the Kotlin release, documentation, examples, and ecosystem integration.

| # | Topic | Description |
|---:|---|---|
| 1 | **Documentation** | Update documentation and examples as needed. |
| 2 | **TypeScript** | Add native TypeScript support with an idiomatic implementation of the same Codes taxonomy and semantics. |
| 3 | **Taxonomy** | Continue taxonomy review based on real-world usage and community feedback. |

See [GitHub Issues](https://github.com/slatekit/kiit-codes/issues) for current work and discussions.

## Learn More

| # | Topic | Description |
|---:|---|---|
| 1 | **Taxonomy** | Explore every built-in group and code, their intended meanings, defaults, and distinctions. [Read the taxonomy docs](https://www.kiit.dev/docs/codes/taxonomy). |
| 2 | **Extensibility** | Learn how extensibility works and how to create domain-specific codes while keeping the shared taxonomy intact. [Read the extensibility docs](https://www.kiit.dev/docs/codes/extensibility). |
| 3 | **Protocols** | See the complete HTTP and gRPC mappings and learn how to create custom mappings. [Read the protocol docs](https://www.kiit.dev/docs/codes/protocols). |
| 4 | **Validation** | Learn how `Err`, `Checked`, and `collect` model validation and accumulate multiple errors. [Read the validation docs](https://www.kiit.dev/docs/codes/validation). |
| 5 | **Exceptions** | See the typed exception hierarchy and patterns for integrating Codes with exception-based boundaries. [Read the exception docs](https://www.kiit.dev/docs/codes/exceptions). |
| 6 | **Result** | Learn how the separate `kiit-result` module builds `Result<T, E>` handling on top of the same Codes taxonomy. [Read the Result docs](https://www.kiit.dev/docs/codes/result). |
| 7 | **FAQ** | Answers to common questions about the taxonomy, design choices, alternatives, adoption, AI considerations, and project maturity. [Read the FAQ](https://www.kiit.dev/docs/codes/faq). |
| 8 | **Design** | Read more about the reasoning behind fixed groups, extensible codes, protocol independence, and where Codes fits relative to domain errors. [Read the design docs](https://www.kiit.dev/docs/codes/design). |

## Requirements

- Kotlin Multiplatform
- JVM, Android, JS (IR), iOS (arm64, simulator arm64, x64)
- No external runtime dependencies

## Contributing

Contributions and design feedback are welcome. See [BUILD.md](./BUILD.md) for build, test, and publish instructions.

## License

[Apache License 2.0](./LICENSE)

---

<div align="center">

**kiit-codes** is one module of [Kiit](https://www.kiit.dev) — a lightweight, modular Kotlin framework for building server applications, APIs, CLIs, and jobs.

**Adopt one module at a time.**

</div>
