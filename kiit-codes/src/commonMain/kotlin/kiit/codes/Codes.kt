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

/**
 * Built-in registry of standard [Status] codes covering common operation outcomes.
 *
 * Using this is optional — they're provided as sensible defaults and for kiit-result builder
 * methods. Custom domain codes can be created by constructing any [Passed] or [Failed] subtype
 * directly; only the four categories under each are fixed/closed (see [Status]). Every entry in
 * this registry has [Status.origin] == [StatusConstants.KIIT].
 *
 * Uniqueness of every entry's [Status.id] is enforced at object-init time — a collision fails
 * loudly the first time [Codes] is touched, rather than silently producing a wrong lookup later.
 * [Status.id] is scoped to `origin.name`, not `name` alone, so a consumer's own custom codes
 * (with their own [Status.origin]) can never collide with a built-in one.
 */
object Codes {
    // ---- Succeeded ----
    val SUCCESS = Passed.Succeeded("SUCCESS", "The operation completed successfully.", origin = StatusConstants.KIIT)
    val CREATED = Passed.Succeeded("CREATED", "A new resource was created.", origin = StatusConstants.KIIT)
    val UPDATED = Passed.Succeeded("UPDATED", "The resource was fully updated.", origin = StatusConstants.KIIT)
    val PATCHED = Passed.Succeeded("PATCHED", "The resource was partially updated.", origin = StatusConstants.KIIT)
    val FETCHED = Passed.Succeeded("FETCHED", "The requested resource was retrieved.", origin = StatusConstants.KIIT)
    val DELETED = Passed.Succeeded("DELETED", "The resource was deleted.", origin = StatusConstants.KIIT)
    val HANDLED =
        Passed.Succeeded("HANDLED", "The request was handled successfully, with no content to return.", origin = StatusConstants.KIIT)
    val REFERRED =
        Passed.Succeeded("REFERRED", "The operation completed; see the referenced location for the result.", origin = StatusConstants.KIIT)

    // ---- Pending ----
    val ACCEPTED = Passed.Pending("ACCEPTED", "The request was accepted.", origin = StatusConstants.KIIT)
    val QUEUED =
        Passed.Pending("QUEUED", "The request was accepted and is waiting to be processed.", origin = StatusConstants.KIIT)
    val PROCESSING = Passed.Pending("PROCESSING", "The request is actively being processed.", origin = StatusConstants.KIIT)
    val CONFIRM =
        Passed.Pending("CONFIRM", "The request was accepted and is awaiting confirmation.", origin = StatusConstants.KIIT)
    val REDIRECTED =
        Passed.Pending(
            "REDIRECTED",
            "The operation is being completed at another location for this request only.",
            origin = StatusConstants.KIIT,
        )

    // ---- Filtered ----
    val SKIPPED = Passed.Filtered("SKIPPED", "The item was not processed.", origin = StatusConstants.KIIT)
    val DISCARDED =
        Passed.Filtered("DISCARDED", "The item was processed, but its result was intentionally discarded.", origin = StatusConstants.KIIT)
    val CANCELLED =
        Passed.Filtered("CANCELLED", "The operation was cancelled by the caller before completion.", origin = StatusConstants.KIIT)
    val EXCLUDED = Passed.Filtered("EXCLUDED", "The item was excluded from the result.", origin = StatusConstants.KIIT)

    // ---- Information ----
    val HELP = Passed.Information("HELP", "Help information was returned.", origin = StatusConstants.KIIT)
    val ABOUT = Passed.Information("ABOUT", "Information about this application was returned.", origin = StatusConstants.KIIT)
    val VERSION = Passed.Information("VERSION", "The current version was returned.", origin = StatusConstants.KIIT)
    val EXIT = Passed.Information("EXIT", "The application is exiting.", origin = StatusConstants.KIIT)
    val MOVED =
        Passed.Information("MOVED", "The resource has permanently moved to a new location.", origin = StatusConstants.KIIT)
    val NOTICE = Passed.Information("NOTICE", "An informational notice.", origin = StatusConstants.KIIT)

    // ---- Restricted — security / access-control ----
    val RESTRICTED = Failed.Restricted("RESTRICTED", "The request was denied.", origin = StatusConstants.KIIT)
    val UNAUTHENTICATED =
        Failed.Restricted("UNAUTHENTICATED", "Valid authentication credentials are required.", origin = StatusConstants.KIIT)
    val UNAUTHORIZED =
        Failed.Restricted("UNAUTHORIZED", "The caller does not have permission to perform this action.", origin = StatusConstants.KIIT)
    val FORBIDDEN = Failed.Restricted("FORBIDDEN", "Access to this resource is forbidden.", origin = StatusConstants.KIIT)

    // ---- Invalid — bad input ----
    val BAD_REQUEST =
        Failed.Invalid("BAD_REQUEST", "The request was malformed and could not be understood.", origin = StatusConstants.KIIT)
    val INVALID_VALUE =
        Failed.Invalid("INVALID_VALUE", "The request was well-formed, but contained invalid values.", origin = StatusConstants.KIIT)
    val NOT_FOUND =
        Failed.Invalid("NOT_FOUND", "The requested route or endpoint does not exist.", origin = StatusConstants.KIIT)
    val OUT_OF_RANGE = Failed.Invalid("OUT_OF_RANGE", "A value was outside the acceptable range.", origin = StatusConstants.KIIT)
    val PAYLOAD_TOO_LARGE =
        Failed.Invalid("PAYLOAD_TOO_LARGE", "The request payload exceeds the allowed size.", origin = StatusConstants.KIIT)

    // permanently removed, was here before — not part of the current audit pass, unchanged
    val REMOVED = Failed.Invalid("REMOVED", "Removed", origin = StatusConstants.KIIT)

    // ---- Rejected — known, expected business-rule failure ----
    val CONFLICT =
        Failed.Rejected("CONFLICT", "The request conflicts with the current state of the resource.", origin = StatusConstants.KIIT)
    val RULE_VIOLATION =
        Failed.Rejected("RULE_VIOLATION", "The request was understood but rejected by a business rule.", origin = StatusConstants.KIIT)
    val NOT_EXISTS =
        Failed.Rejected("NOT_EXISTS", "The request was valid, but the referenced item does not exist.", origin = StatusConstants.KIIT)
    val PRECONDITION_FAILED =
        Failed.Rejected("PRECONDITION_FAILED", "A required precondition was not met.", origin = StatusConstants.KIIT)

    // ---- Unserved — valid & permitted, can't be serviced right now ----
    val UNIMPLEMENTED =
        Failed.Unserved("UNIMPLEMENTED", "This capability has not been implemented yet.", origin = StatusConstants.KIIT)
    val UNSUPPORTED =
        Failed.Unserved("UNSUPPORTED", "This capability is not supported and will not be implemented.", origin = StatusConstants.KIIT)
    val TIMEOUT = Failed.Unserved("TIMEOUT", "The operation did not complete within the allotted time.", origin = StatusConstants.KIIT)
    val RATE_LIMITED = Failed.Unserved("RATE_LIMITED", "Too many requests; try again later.", origin = StatusConstants.KIIT)
    val UNREACHABLE =
        Failed.Unserved("UNREACHABLE", "A required dependency could not be reached.", origin = StatusConstants.KIIT)
    val UNDER_MAINTENANCE =
        Failed.Unserved("UNDER_MAINTENANCE", "The service is temporarily under maintenance.", origin = StatusConstants.KIIT)
    val UNEXPECTED =
        Failed.Unserved("UNEXPECTED", "An unexpected, unclassified error occurred.", origin = StatusConstants.KIIT)
    val CONCURRENCY_CONFLICT =
        Failed.Unserved(
            "CONCURRENCY_CONFLICT",
            "A concurrent operation conflicted with this one; retrying may succeed.",
            origin = StatusConstants.KIIT,
        )
    val INTERNAL = Failed.Unserved("INTERNAL", "An internal system invariant was violated.", origin = StatusConstants.KIIT)
    val DATA_LOSS =
        Failed.Unserved("DATA_LOSS", "Unrecoverable data loss or corruption occurred.", origin = StatusConstants.KIIT)

    /** All built-in codes. Used for reverse lookups — see [CodesToHttp], [CompositeLookup]. */
    val all: List<Status> =
        listOf(
            SUCCESS, CREATED, UPDATED, PATCHED, FETCHED, DELETED, HANDLED, REFERRED,
            ACCEPTED, QUEUED, PROCESSING, CONFIRM, REDIRECTED,
            SKIPPED, DISCARDED, CANCELLED, EXCLUDED,
            HELP, ABOUT, VERSION, EXIT, MOVED, NOTICE,
            RESTRICTED, UNAUTHENTICATED, UNAUTHORIZED, FORBIDDEN,
            BAD_REQUEST, INVALID_VALUE, NOT_FOUND, OUT_OF_RANGE, PAYLOAD_TOO_LARGE, REMOVED,
            CONFLICT, RULE_VIOLATION, NOT_EXISTS, PRECONDITION_FAILED,
            UNIMPLEMENTED, UNSUPPORTED, TIMEOUT, RATE_LIMITED, UNREACHABLE, UNDER_MAINTENANCE, UNEXPECTED,
            CONCURRENCY_CONFLICT, INTERNAL, DATA_LOSS,
        )

    init {
        val duplicates = all.groupBy { it.id }.filterValues { it.size > 1 }.keys
        check(duplicates.isEmpty()) { "Duplicate Status codes detected in Codes registry: $duplicates" }
    }
}

/**
 * Bidirectional conversion between a [Status] and a target protocol's status code (e.g. HTTP).
 *
 * Implementations should be exhaustive over [Status]'s categories ([Passed]/[Failed] subtypes),
 * typically via a `when` with no `else` branch, so a newly added category is caught at compile
 * time. Individual codes within a category do not need an exhaustive mapping — they can be
 * handled via a small overrides table layered on top of the category default (see [CodesToHttp]).
 */
interface CodeLookup {
    /** Converts a [Status] to the target protocol's code. */
    fun toCode(status: Status): Int

    /**
     * Converts a target protocol [code] to a matching [Status], or null if there is no match.
     * The forward direction is typically many-to-one, so this is inherently lossy — it returns
     * *a* status that resolves to [code], not necessarily the specific one a caller originally
     * had in hand.
     */
    fun toStatus(code: Int): Status?
}

/**
 * Default [CodeLookup] implementation mapping [Status] to HTTP status codes.
 *
 * Category -> HTTP default:
 *   Succeeded / Filtered / Information -> 200      Pending -> 202
 *   Restricted -> 401  Invalid -> 400   Rejected -> 500        Unserved -> 503
 *
 * Individual codes can differ from their category's default via [overrides] (e.g. CREATED -> 201,
 * NOT_FOUND -> 404). [toStatus] is derived from [toCode] and is lossy — see its own doc.
 *
 * Clients needing additional/custom codes should compose with [CompositeLookup] rather than
 * subclassing this type directly — see [CompositeLookup] for why.
 */
open class CodesToHttp(
    private val overrides: Map<Status, Int> = DEFAULT_OVERRIDES,
) : CodeLookup {
    override fun toCode(status: Status): Int {
        overrides[status]?.let { return it }
        return when (status) {
            is Passed.Succeeded -> 200
            is Passed.Pending -> 202
            is Passed.Filtered -> 200
            is Passed.Information -> 200
            is Failed.Restricted -> 401
            is Failed.Invalid -> 400
            is Failed.Rejected -> 500
            is Failed.Unserved -> 503
        }
    }

    /**
     * Reverse lookup, derived from [toCode] so it can't drift out of sync with a custom
     * [overrides] map. Lossy by nature (many statuses can share one code); ties are broken
     * deterministically via [CANONICAL_PREFERENCE] rather than [Codes.all]'s declaration order.
     * Only finds statuses registered in [Codes] — see [CompositeLookup] for custom ones.
     */
    override fun toStatus(code: Int): Status? =
        CANONICAL_PREFERENCE.firstOrNull { toCode(it) == code }
            ?: Codes.all.firstOrNull { toCode(it) == code }

    companion object {
        val DEFAULT_OVERRIDES: Map<Status, Int> =
            mapOf(
                Codes.CREATED to 201,
                Codes.HANDLED to 204,
                Codes.CONFIRM to 200,
                Codes.CANCELLED to 499,
                Codes.REDIRECTED to 307,
                Codes.NOT_FOUND to 404,
                Codes.REMOVED to 410,
                Codes.NOT_EXISTS to 404,
                Codes.FORBIDDEN to 403,
                Codes.CONFLICT to 409,
                Codes.PAYLOAD_TOO_LARGE to 413,
                // share 501 deliberately — HTTP has no separate "unsupported" code
                Codes.UNIMPLEMENTED to 501,
                Codes.UNSUPPORTED to 501,
                // deadline exceeded waiting on something else, not a slow client (408)
                Codes.TIMEOUT to 504,
                Codes.RATE_LIMITED to 429,
                Codes.UNEXPECTED to 500,
            )

        /**
         * One canonical winner per HTTP code that more than one built-in [Status] can resolve
         * to via [toCode] (under [DEFAULT_OVERRIDES] or a category default) — see [toStatus].
         */
        private val CANONICAL_PREFERENCE: List<Status> =
            listOf(
                Codes.SUCCESS, Codes.CREATED, Codes.HANDLED, Codes.PROCESSING,
                Codes.UNAUTHENTICATED, Codes.FORBIDDEN,
                Codes.INVALID_VALUE, Codes.NOT_FOUND, Codes.REMOVED,
                Codes.CONFLICT, Codes.TIMEOUT, Codes.RATE_LIMITED,
                Codes.UNIMPLEMENTED, Codes.UNDER_MAINTENANCE, Codes.UNEXPECTED,
            )
    }
}

/**
 * [CodeLookup] implementation mapping [Status] to gRPC status codes (0-16).
 *
 * Category -> gRPC default: Passed (all) -> 0 (OK)   Restricted -> 7 (PERMISSION_DENIED)
 *   Invalid -> 3 (INVALID_ARGUMENT)   Rejected -> 9 (FAILED_PRECONDITION)   Unserved -> 13 (INTERNAL)
 */
open class CodesToGrpc(
    private val overrides: Map<Status, Int> = DEFAULT_OVERRIDES,
) : CodeLookup {
    override fun toCode(status: Status): Int {
        overrides[status]?.let { return it }
        return when (status) {
            is Passed.Succeeded -> 0
            is Passed.Pending -> 0
            is Passed.Filtered -> 0
            is Passed.Information -> 0
            is Failed.Restricted -> 7
            is Failed.Invalid -> 3
            is Failed.Rejected -> 9
            is Failed.Unserved -> 13
        }
    }

    /**
     * Reverse lookup, derived from [toCode] so it can't drift out of sync with a custom
     * [overrides] map. Ties broken deterministically via [CANONICAL_PREFERENCE], same approach
     * as [CodesToHttp.toStatus].
     */
    override fun toStatus(code: Int): Status? =
        CANONICAL_PREFERENCE.firstOrNull { toCode(it) == code }
            ?: Codes.all.firstOrNull { toCode(it) == code }

    companion object {
        val DEFAULT_OVERRIDES: Map<Status, Int> =
            mapOf(
                Codes.CANCELLED to 1,
                Codes.UNAUTHENTICATED to 16,
                Codes.INVALID_VALUE to 3,
                Codes.NOT_FOUND to 5,
                Codes.OUT_OF_RANGE to 11,
                Codes.RESTRICTED to 7,
                Codes.CONCURRENCY_CONFLICT to 10,
                Codes.PRECONDITION_FAILED to 9,
                Codes.UNIMPLEMENTED to 12,
                Codes.UNREACHABLE to 14,
                Codes.TIMEOUT to 4,
                Codes.RATE_LIMITED to 8,
                Codes.UNEXPECTED to 2,
                Codes.INTERNAL to 13,
                Codes.DATA_LOSS to 15,
                // RESOURCE_EXHAUSTED — widely used real-world convention, not an official mapping
                Codes.PAYLOAD_TOO_LARGE to 8,
            )

        /** One canonical winner per gRPC code with more than one resolving [Status] — see [toStatus]. */
        private val CANONICAL_PREFERENCE: List<Status> =
            listOf(
                Codes.SUCCESS,
                Codes.INVALID_VALUE,
                Codes.RESTRICTED,
                Codes.RATE_LIMITED,
                Codes.PRECONDITION_FAILED,
                Codes.INTERNAL,
            )
    }
}

/**
 * Composes a [base] [CodeLookup] with client-supplied [extensions], without modifying or
 * subclassing the base implementation (composition over inheritance). [extensions] take
 * precedence over [base] for both directions.
 *
 * [extensions] is keyed by the actual [Status] instance so that [toStatus] can be answered
 * correctly for custom statuses that aren't part of the [Codes.all] registry.
 *
 * ```kotlin
 * val MY_DOMAIN_CODE = Failed.Rejected("PAYMENT_DECLINED", "Payment declined")
 * val lookup = CompositeLookup(CodesToHttp(), mapOf(MY_DOMAIN_CODE to 402))
 * ```
 */
class CompositeLookup(
    private val base: CodeLookup,
    private val extensions: Map<Status, Int>,
) : CodeLookup {
    override fun toCode(status: Status): Int = extensions[status] ?: base.toCode(status)

    override fun toStatus(code: Int): Status? {
        val extended = extensions.entries.firstOrNull { it.value == code }?.key
        return extended ?: base.toStatus(code)
    }
}
