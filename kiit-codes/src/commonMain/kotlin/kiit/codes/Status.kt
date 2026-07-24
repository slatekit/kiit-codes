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
 *   Passed  = Succeeded  | Pending | Filtered | Information
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
 * Subtypes: [Succeeded], [Pending], [Filtered], [Information].
 */
sealed class Passed : Status {
    final override val success: Boolean get() = true

    final override val group: String
        get() = when (this) {
            is Succeeded -> "Succeeded"
            is Pending -> "Pending"
            is Filtered -> "Filtered"
            is Information -> "Information"
        }

    /** Operation's primary purpose completed (e.g. a value was created, fetched, updated). */
    data class Succeeded(
        override val name: String,
        override val message: String,
        override val origin: String = StatusConstants.CUSTOM,
    ) : Passed()

    /** Operation accepted but not yet fully processed (e.g. queued, waiting, confirmed). */
    data class Pending(
        override val name: String,
        override val message: String,
        override val origin: String = StatusConstants.CUSTOM,
    ) : Passed()

    /**
     * Item was excluded from the operation's normal output. Covers both:
     *   - not processed at all (e.g. SKIPPED — screened out before any work happened), and
     *   - processed, then its result was deliberately discarded (e.g. DISCARDED).
     * The distinction is carried by [name], not by separate types — see [Codes.SKIPPED]
     * and [Codes.DISCARDED].
     */
    data class Filtered(
        override val name: String,
        override val message: String,
        override val origin: String = StatusConstants.CUSTOM,
    ) : Passed()

    /**
     * Informational / metadata response — no primary operation was performed.
     * E.g. HELP, ABOUT, VERSION output from a CLI command.
     */
    data class Information(
        override val name: String,
        override val message: String,
        override val origin: String = StatusConstants.CUSTOM,
    ) : Passed()

    override fun copyAll(msg: String, origin: String): Status =
        when (this) {
            is Succeeded -> copy(message = msg, origin = origin)
            is Pending -> copy(message = msg, origin = origin)
            is Filtered -> copy(message = msg, origin = origin)
            is Information -> copy(message = msg, origin = origin)
        }
}

/**
 * Parent sealed type for all failure statuses (success = false for every subtype).
 * Subtypes: [Restricted], [Invalid], [Rejected], [Unserved].
 */
sealed class Failed : Status {
    final override val success: Boolean get() = false

    final override val group: String
        get() = when (this) {
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
    ) : Failed()

    /** The request as given cannot be satisfied — malformed input, invalid values, or not found. */
    data class Invalid(
        override val name: String,
        override val message: String,
        override val origin: String = StatusConstants.CUSTOM,
    ) : Failed()

    /** A known, expected business-rule failure — understood and handled by the caller. */
    data class Rejected(
        override val name: String,
        override val message: String,
        override val origin: String = StatusConstants.CUSTOM,
    ) : Failed()

    /**
     * The request is valid and permitted, but cannot be serviced right now for reasons unrelated
     * to what was sent — capacity, timeout, an unimplemented/unsupported capability, planned
     * maintenance, or a genuinely unexpected/unhandled failure (see [Codes.UNEXPECTED]).
     */
    data class Unserved(
        override val name: String,
        override val message: String,
        override val origin: String = StatusConstants.CUSTOM,
    ) : Failed()

    override fun copyAll(msg: String, origin: String): Status =
        when (this) {
            is Restricted -> copy(message = msg, origin = origin)
            is Invalid -> copy(message = msg, origin = origin)
            is Rejected -> copy(message = msg, origin = origin)
            is Unserved -> copy(message = msg, origin = origin)
        }
}
