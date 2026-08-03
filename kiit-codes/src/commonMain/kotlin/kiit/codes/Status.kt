/**
 *  <kiit_header>
 * url: www.kiit.dev
 * git: www.github.com/slatekit/kiit
 * org: www.codehelix.co
 * author: Kishore Reddy
 * copyright: 2016 CodeHelix Solutions Inc.
 * license: refer to website and/or github
 * about: A Kotlin Tool-Kit for Server + Android
 *  </kiit_header>
 */
package kiit.codes

/** Well-known [Status.origin] values. */
object StatusConstants {
    /** Origin for every built-in [Codes] entry. */
    const val KIIT = "kiit"

    /** Default origin for consumer/custom statuses that don't specify one explicitly. */
    const val CUSTOM = "custom"
}

/**
 * Platform-agnostic status type describing the outcome of any operation — a service call,
 * a background job step, an API request, or a CLI command.
 *
 * Shape (maps directly to JSON / API error responses):
 *   { "id": "kiit.TOKEN_EXPIRED", "name": "TOKEN_EXPIRED", "group": "Restricted", "origin": "kiit",
 *     "message": "Session token expired", "success": false }
 *
 * Hierarchy. Categories are closed/sealed and fixed by design, to enforce a consistent taxonomy
 * across every consumer. Individual codes *within* a category are open — create new domain codes
 * by constructing a [Passed] or [Failed] subtype directly (see [Codes] for the built-in set):
 *
 *   Status  = Passed     | Failed
 *   Passed  = Succeeded  | Pending | Excluded | Information
 *   Failed  = Restricted | Invalid | Rejected | Unserved
 */
sealed interface Status {
    /**
     * Unique domain label, e.g. "TOKEN_EXPIRED", "RATE_LIMITED".
     * SCREAMING_SNAKE_CASE, stable — used as a searchable/aggregable key in logs and metrics.
     */
    val name: String

    /**
     * Origin of this status, e.g. [StatusConstants.KIIT] for every built-in [Codes] entry.
     * Consumer/custom subtypes default to [StatusConstants.CUSTOM] rather than silently inheriting
     * [StatusConstants.KIIT], so a status can never accidentally misrepresent where it came from.
     */
    val origin: String

    /** Stable identity, `"$origin.$name"` — unique across every [Status], usable as a map key. */
    val id: String get() = "$origin.$name"

    /**
     * Human-readable, constant description — never constructed from runtime data. Per-instance /
     * runtime detail (e.g. "field X was invalid because...") belongs on whatever wraps this
     * Status (an error/result type one layer up), not here — that keeps [message] safe to use
     * as an aggregation key across every occurrence of this status.
     */
    val message: String

    /**
     * True for all [Passed] subtypes, false for all [Failed] subtypes. Callers that don't need
     * to narrow the sealed type can branch on this directly instead of pattern matching.
     */
    val success: Boolean

    /** The category discriminant, e.g. "Restricted", "Rejected" — see the hierarchy above. */
    val group: String

    /** Returns a copy of this status with an updated [msg] and [origin], preserving name and group. */
    fun copyAll(msg: String, origin: String): Status

    companion object {
        /**
         * Resolves a status from an optional [msg] override and an optional [rawStatus] override,
         * falling back to [status] when neither is supplied. [rawStatus], if present, is used as
         * the base instead of [status]; [msg], if present, is then applied on top of that base.
         */
        @Suppress("UNCHECKED_CAST")
        fun <T : Status> ofStatus(msg: String?, rawStatus: T?, status: T): T {
            val base = rawStatus ?: status
            return if (msg == null) base else base.copyAll(msg, base.origin) as T
        }
    }
}

/**
 * Parent sealed type for all non-failure statuses (success = true for every subtype).
 * Subtypes: [Succeeded], [Pending], [Excluded], [Information].
 *
 * Each subtype's built-in constants live on its own companion object, not on [Codes] — this keeps
 * IDE autocomplete scoped (typing `Succeeded.` shows only [Succeeded]'s own members). [Codes] is
 * an aggregate/lookup layer over these, not where they're declared. The package-level typealiases
 * below (e.g. `Succeeded` for `Passed.Succeeded`) are the same type, not a copy — they exist purely
 * to avoid writing the `Passed.`/`Failed.` prefix at every call site.
 */
sealed class Passed : Status {
    final override val success: Boolean get() = true

    final override val group: String
        get() =
            when (this) {
                is Succeeded -> "Succeeded"
                is Pending -> "Pending"
                is Excluded -> "Excluded"
                is Information -> "Information"
            }

    /** Operation's primary purpose completed (e.g. a value was created, fetched, updated). */
    data class Succeeded(
        override val name: String,
        override val message: String,
        override val origin: String = StatusConstants.CUSTOM,
    ) : Passed() {
        companion object {
            val SUCCESS = Succeeded("SUCCESS", "The operation completed successfully.", origin = StatusConstants.KIIT)
            val CREATED = Succeeded("CREATED", "A new resource was created.", origin = StatusConstants.KIIT)
            val UPDATED = Succeeded("UPDATED", "The resource was fully updated.", origin = StatusConstants.KIIT)
            val PATCHED = Succeeded("PATCHED", "The resource was partially updated.", origin = StatusConstants.KIIT)
            val FETCHED = Succeeded("FETCHED", "The requested resource was retrieved.", origin = StatusConstants.KIIT)
            val DELETED = Succeeded("DELETED", "The resource was deleted.", origin = StatusConstants.KIIT)
            val HANDLED =
                Succeeded(
                    "HANDLED",
                    "The request was handled successfully, with no content to return.",
                    origin = StatusConstants.KIIT,
                )
            val REFERRED =
                Succeeded(
                    "REFERRED",
                    "The operation completed; see the referenced location for the result.",
                    origin = StatusConstants.KIIT,
                )
            val EXITED = Succeeded("EXITED", "The application exited cleanly.", origin = StatusConstants.KIIT)
        }
    }

    /** Operation accepted but not yet fully processed (e.g. queued, waiting, confirmed). */
    data class Pending(
        override val name: String,
        override val message: String,
        override val origin: String = StatusConstants.CUSTOM,
    ) : Passed() {
        companion object {
            val ACCEPTED = Pending("ACCEPTED", "The request was accepted.", origin = StatusConstants.KIIT)
            val QUEUED =
                Pending(
                    "QUEUED",
                    "The request was accepted and is waiting to be processed.",
                    origin = StatusConstants.KIIT,
                )
            val PROCESSING =
                Pending(
                    "PROCESSING",
                    "The request is actively being processed.",
                    origin = StatusConstants.KIIT,
                )
            val CONFIRM =
                Pending(
                    "CONFIRM",
                    "The request was accepted and is awaiting confirmation.",
                    origin = StatusConstants.KIIT,
                )
            val REDIRECTED =
                Pending(
                    "REDIRECTED",
                    "The operation is being completed at another location for this request only.",
                    origin = StatusConstants.KIIT,
                )
            val SCHEDULED =
                Pending(
                    "SCHEDULED",
                    "The operation is scheduled to run at a future time.",
                    origin = StatusConstants.KIIT,
                )
        }
    }

    /**
     * Item was excluded from the operation's normal output — not processed at all (e.g.
     * SKIPPED), processed then discarded (e.g. DISCARDED), or omitted for any other reason.
     * The distinction is carried by [name], not by separate types — see [Excluded.SKIPPED]
     * and [Excluded.DISCARDED].
     */
    data class Excluded(
        override val name: String,
        override val message: String,
        override val origin: String = StatusConstants.CUSTOM,
    ) : Passed() {
        companion object {
            val OMITTED = Excluded("OMITTED", "The item was excluded from the result.", origin = StatusConstants.KIIT)
            val SKIPPED = Excluded("SKIPPED", "The item was not processed.", origin = StatusConstants.KIIT)
            val DISCARDED =
                Excluded(
                    "DISCARDED",
                    "The item was processed, but excluded for reasons unrelated to its own state.",
                    origin = StatusConstants.KIIT,
                )
            val CANCELLED =
                Excluded(
                    "CANCELLED",
                    "The operation was cancelled by the caller before completion.",
                    origin = StatusConstants.KIIT,
                )
            val DEDUPLICATED =
                Excluded(
                    "DEDUPLICATED",
                    "The item was identified as a duplicate and was not processed again.",
                    origin = StatusConstants.KIIT,
                )
            val DISQUALIFIED =
                Excluded(
                    "DISQUALIFIED",
                    "The item did not meet the required criteria and was excluded.",
                    origin = StatusConstants.KIIT,
                )
        }
    }

    /**
     * Informational / metadata response — no primary operation was performed.
     * E.g. HELP, ABOUT, VERSION output from a CLI command.
     */
    data class Information(
        override val name: String,
        override val message: String,
        override val origin: String = StatusConstants.CUSTOM,
    ) : Passed() {
        companion object {
            val NOTICE = Information("NOTICE", "An informational notice.", origin = StatusConstants.KIIT)
            val ADVISORY =
                Information(
                    "ADVISORY",
                    "An advisory that may require attention or action.",
                    origin = StatusConstants.KIIT,
                )
            val HELP = Information("HELP", "Help information was returned.", origin = StatusConstants.KIIT)
            val ABOUT =
                Information(
                    "ABOUT",
                    "Information about this application was returned.",
                    origin = StatusConstants.KIIT,
                )
            val VERSION = Information("VERSION", "The current version was returned.", origin = StatusConstants.KIIT)
            val MOVED =
                Information(
                    "MOVED",
                    "The resource has permanently moved to a new location.",
                    origin = StatusConstants.KIIT,
                )
        }
    }

    override fun copyAll(msg: String, origin: String): Status =
        when (this) {
            is Succeeded -> copy(message = msg, origin = origin)
            is Pending -> copy(message = msg, origin = origin)
            is Excluded -> copy(message = msg, origin = origin)
            is Information -> copy(message = msg, origin = origin)
        }
}

/**
 * Parent sealed type for all failure statuses (success = false for every subtype).
 * Subtypes: [Restricted], [Invalid], [Rejected], [Unserved].
 *
 * See [Passed]'s doc for why built-in constants live on each subtype's own companion object
 * rather than on [Codes].
 */
sealed class Failed : Status {
    final override val success: Boolean get() = false

    final override val group: String
        get() =
            when (this) {
                is Restricted -> "Restricted"
                is Invalid -> "Invalid"
                is Rejected -> "Rejected"
                is Unserved -> "Unserved"
            }

    /** Security / access-control failure — the caller is not permitted to perform this action. */
    data class Restricted(
        override val name: String,
        override val message: String,
        override val origin: String = StatusConstants.CUSTOM,
    ) : Failed() {
        companion object {
            val DENIED = Restricted("DENIED", "The request was denied.", origin = StatusConstants.KIIT)
            val UNAUTHENTICATED =
                Restricted(
                    "UNAUTHENTICATED",
                    "Valid authentication credentials are required.",
                    origin = StatusConstants.KIIT,
                )
            val UNAUTHORIZED =
                Restricted(
                    "UNAUTHORIZED",
                    "The caller does not have permission to perform this action.",
                    origin = StatusConstants.KIIT,
                )
            val FORBIDDEN =
                Restricted("FORBIDDEN", "Access to this resource is forbidden.", origin = StatusConstants.KIIT)
            val LOCKED =
                Restricted(
                    "LOCKED",
                    "Access is locked; resolve the condition to restore access.",
                    origin = StatusConstants.KIIT,
                )
            val SUSPENDED =
                Restricted("SUSPENDED", "Access has been administratively suspended.", origin = StatusConstants.KIIT)
        }
    }

    /** The request as given cannot be satisfied — malformed input, invalid values, or not found. */
    data class Invalid(
        override val name: String,
        override val message: String,
        override val origin: String = StatusConstants.CUSTOM,
    ) : Failed() {
        companion object {
            val INVALID_VALUE =
                Invalid(
                    "INVALID_VALUE",
                    "The request was well-formed, but contained invalid values.",
                    origin = StatusConstants.KIIT,
                )
            val BAD_REQUEST =
                Invalid(
                    "BAD_REQUEST",
                    "The request was malformed and could not be understood.",
                    origin = StatusConstants.KIIT,
                )
            val NOT_FOUND =
                Invalid("NOT_FOUND", "The requested route or endpoint does not exist.", origin = StatusConstants.KIIT)
            val OUT_OF_RANGE =
                Invalid(
                    "OUT_OF_RANGE",
                    "A value was outside the acceptable range.",
                    origin = StatusConstants.KIIT,
                )
            val PAYLOAD_TOO_LARGE =
                Invalid(
                    "PAYLOAD_TOO_LARGE",
                    "The request payload exceeds the allowed size.",
                    origin = StatusConstants.KIIT,
                )
            val MISSING_FIELD =
                Invalid("MISSING_FIELD", "A required field was not provided.", origin = StatusConstants.KIIT)
        }
    }

    /** A known, expected business-rule failure — understood and handled by the caller. */
    data class Rejected(
        override val name: String,
        override val message: String,
        override val origin: String = StatusConstants.CUSTOM,
    ) : Failed() {
        companion object {
            val RULE_VIOLATION =
                Rejected(
                    "RULE_VIOLATION",
                    "The request was understood but rejected by a business rule.",
                    origin = StatusConstants.KIIT,
                )
            val CONFLICT =
                Rejected(
                    "CONFLICT",
                    "The request conflicts with the current state of the resource.",
                    origin = StatusConstants.KIIT,
                )
            val NOT_EXISTS =
                Rejected(
                    "NOT_EXISTS",
                    "The request was valid, but the referenced item does not exist.",
                    origin = StatusConstants.KIIT,
                )
            val PRECONDITION_FAILED =
                Rejected("PRECONDITION_FAILED", "A required precondition was not met.", origin = StatusConstants.KIIT)
            val EXPIRED =
                Rejected(
                    "EXPIRED",
                    "The referenced item is no longer valid; it has expired.",
                    origin = StatusConstants.KIIT,
                )
            val GONE =
                Rejected(
                    "GONE",
                    "This resource was deliberately removed and is no longer available.",
                    origin = StatusConstants.KIIT,
                )
        }
    }

    /**
     * The request is valid and permitted, but cannot be serviced right now for reasons unrelated
     * to what was sent — capacity, timeout, an unsupported capability, planned
     * maintenance, or a genuinely unexpected/unhandled failure (see [Unserved.UNEXPECTED]).
     */
    data class Unserved(
        override val name: String,
        override val message: String,
        override val origin: String = StatusConstants.CUSTOM,
    ) : Failed() {
        companion object {
            val UNEXPECTED =
                Unserved("UNEXPECTED", "An unexpected, unclassified error occurred.", origin = StatusConstants.KIIT)
            val UNSUPPORTED =
                Unserved("UNSUPPORTED", "This capability is not currently available.", origin = StatusConstants.KIIT)
            val TIMEOUT =
                Unserved(
                    "TIMEOUT",
                    "The operation did not complete within the allotted time.",
                    origin = StatusConstants.KIIT,
                )
            val RATE_LIMITED =
                Unserved("RATE_LIMITED", "Too many requests; try again later.", origin = StatusConstants.KIIT)
            val RESOURCE_LIMITED =
                Unserved("RESOURCE_LIMITED", "A resource limit has been reached.", origin = StatusConstants.KIIT)
            val UNREACHABLE =
                Unserved("UNREACHABLE", "A required dependency could not be reached.", origin = StatusConstants.KIIT)
            val UNDER_MAINTENANCE =
                Unserved(
                    "UNDER_MAINTENANCE",
                    "The service is temporarily under maintenance.",
                    origin = StatusConstants.KIIT,
                )
            val INTERNAL =
                Unserved(
                    "INTERNAL",
                    "An internal system invariant was violated.",
                    origin = StatusConstants.KIIT,
                )
            val DATA_LOSS =
                Unserved("DATA_LOSS", "Unrecoverable data loss or corruption occurred.", origin = StatusConstants.KIIT)
        }
    }

    override fun copyAll(msg: String, origin: String): Status =
        when (this) {
            is Restricted -> copy(message = msg, origin = origin)
            is Invalid -> copy(message = msg, origin = origin)
            is Rejected -> copy(message = msg, origin = origin)
            is Unserved -> copy(message = msg, origin = origin)
        }
}

// Package-level shorthands, fully transparent — e.g. `Restricted` and `Failed.Restricted` are the
// same type, not a copy, so each alias inherits its target's companion members with nothing extra
// needed. These exist purely for brevity at call sites; the real declarations live on the types
// themselves (see [Passed], [Failed]).
typealias Succeeded = Passed.Succeeded
typealias Pending = Passed.Pending
typealias Excluded = Passed.Excluded
typealias Information = Passed.Information

typealias Restricted = Failed.Restricted
typealias Invalid = Failed.Invalid
typealias Rejected = Failed.Rejected
typealias Unserved = Failed.Unserved

/** True only for [Passed.Excluded]/[Passed.Information] — every other subtype is a genuine success or failure. */
val Status.isNeutral: Boolean get() = this is Passed.Excluded || this is Passed.Information
