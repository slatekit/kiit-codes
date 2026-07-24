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
 */
object Codes {
    // ---- Succeeded ----
    val SUCCESS = Passed.Succeeded("SUCCESS", "Success", origin = StatusConstants.KIIT)
    val CREATED = Passed.Succeeded("CREATED", "Created", origin = StatusConstants.KIIT)
    val UPDATED = Passed.Succeeded("UPDATED", "Updated", origin = StatusConstants.KIIT)
    val FETCHED = Passed.Succeeded("FETCHED", "Fetched", origin = StatusConstants.KIIT)
    val PATCHED = Passed.Succeeded("PATCHED", "Patched", origin = StatusConstants.KIIT)
    val DELETED = Passed.Succeeded("DELETED", "Deleted", origin = StatusConstants.KIIT)
    // e.g. a silent OK, similar to HTTP 204
    val HANDLED = Passed.Succeeded("HANDLED", "Handled", origin = StatusConstants.KIIT)

    // ---- Pending ----
    val PENDING = Passed.Pending("PENDING", "Pending", origin = StatusConstants.KIIT)
    val QUEUED = Passed.Pending("QUEUED", "Queued", origin = StatusConstants.KIIT)
    val CONFIRM = Passed.Pending("CONFIRM", "Confirm", origin = StatusConstants.KIIT)

    // ---- Filtered ----
    // not processed at all
    val SKIPPED = Passed.Filtered("SKIPPED", "Skipped", origin = StatusConstants.KIIT)
    // processed, result thrown away
    val DISCARDED = Passed.Filtered("DISCARDED", "Discarded", origin = StatusConstants.KIIT)

    // ---- Information ----
    val HELP = Passed.Information("HELP", "Help", origin = StatusConstants.KIIT)
    val ABOUT = Passed.Information("ABOUT", "About", origin = StatusConstants.KIIT)
    val VERSION = Passed.Information("VERSION", "Version", origin = StatusConstants.KIIT)
    val EXIT = Passed.Information("EXIT", "Exiting", origin = StatusConstants.KIIT)

    // ---- Denied — security / access-control ----
    val DENIED = Failed.Denied("DENIED", "Denied", origin = StatusConstants.KIIT)
    val UNAUTHENTICATED = Failed.Denied("UNAUTHENTICATED", "Unauthenticated", origin = StatusConstants.KIIT)
    val UNAUTHORIZED = Failed.Denied("UNAUTHORIZED", "Unauthorized", origin = StatusConstants.KIIT)

    // ---- Invalid — bad input ----
    val BAD_REQUEST = Failed.Invalid("BAD_REQUEST", "Bad request", origin = StatusConstants.KIIT) // e.g. malformed JSON
    // e.g. well-formed but invalid values
    val INVALID = Failed.Invalid("INVALID", "Invalid", origin = StatusConstants.KIIT)
    // e.g. resource/endpoint not found
    val NOT_FOUND = Failed.Invalid("NOT_FOUND", "Not found", origin = StatusConstants.KIIT)

    // ---- Errored — known, expected business-rule failure ----
    // e.g. domain model not found
    val MISSING = Failed.Errored("MISSING", "Missing item", origin = StatusConstants.KIIT)
    val FORBIDDEN = Failed.Errored("FORBIDDEN", "Forbidden", origin = StatusConstants.KIIT)
    val CONFLICT = Failed.Errored("CONFLICT", "Conflict", origin = StatusConstants.KIIT)
    val DEPRECATED = Failed.Errored("DEPRECATED", "Deprecated", origin = StatusConstants.KIIT)
    val ERRORED = Failed.Errored("ERRORED", "Errored", origin = StatusConstants.KIIT) // general purpose use

    // ---- Unserved — valid & permitted, can't be serviced right now ----
    val UNIMPLEMENTED = Failed.Unserved("UNIMPLEMENTED", "Not implemented", origin = StatusConstants.KIIT)
    val UNSUPPORTED = Failed.Unserved("UNSUPPORTED", "Not supported", origin = StatusConstants.KIIT)
    val TIMEOUT = Failed.Unserved("TIMEOUT", "Timeout", origin = StatusConstants.KIIT)
    val RATE_LIMITED = Failed.Unserved("RATE_LIMITED", "Rate limited", origin = StatusConstants.KIIT)
    // e.g. dependency down
    val UNREACHABLE = Failed.Unserved("UNREACHABLE", "Unreachable", origin = StatusConstants.KIIT)
    val UNDER_MAINTENANCE = Failed.Unserved("UNDER_MAINTENANCE", "Under maintenance", origin = StatusConstants.KIIT)
    // unhandled/uncaught path
    val UNEXPECTED = Failed.Unserved("UNEXPECTED", "Unexpected", origin = StatusConstants.KIIT)

    /** All built-in codes. Used for reverse lookups — see [CodesToHttp], [CompositeLookup]. */
    val all: List<Status> =
        listOf(
            SUCCESS, CREATED, UPDATED, FETCHED, PATCHED, DELETED, HANDLED,
            PENDING, QUEUED, CONFIRM,
            SKIPPED, DISCARDED,
            HELP, ABOUT, VERSION, EXIT,
            DENIED, UNAUTHENTICATED, UNAUTHORIZED,
            BAD_REQUEST, INVALID, NOT_FOUND,
            MISSING, FORBIDDEN, CONFLICT, DEPRECATED, ERRORED,
            UNIMPLEMENTED, UNSUPPORTED, TIMEOUT, RATE_LIMITED, UNREACHABLE, UNDER_MAINTENANCE, UNEXPECTED,
        )
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

    /** Converts a target protocol [code] to a matching [Status], or null if there is no match. */
    fun toStatus(code: Int): Status?
}

/**
 * Default [CodeLookup] implementation mapping [Status] to HTTP status codes.
 *
 * Category -> HTTP default:
 *   Succeeded / Filtered / Information -> 200      Pending -> 202
 *   Denied -> 401      Invalid -> 400      Errored -> 500      Unserved -> 503
 *
 * Individual codes can differ from their category's default via [overrides] (e.g. CREATED -> 201,
 * NOT_FOUND -> 404). [toStatus] is derived from [toCode] rather than a separately maintained
 * reverse table, so the two directions can never drift out of sync with each other.
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
            is Failed.Denied -> 401
            is Failed.Invalid -> 400
            is Failed.Errored -> 500
            is Failed.Unserved -> 503
        }
    }

    /**
     * Reverse lookup, derived from [toCode] over the built-in [Codes.all] registry. Note this
     * only finds statuses registered in [Codes] — a caller's own custom [Status] instances that
     * were never added to that registry won't be found here even if they'd resolve to [code]
     * via [toCode]. Use [CompositeLookup] if you need custom statuses to also be reverse-lookupable.
     */
    override fun toStatus(code: Int): Status? = Codes.all.firstOrNull { toCode(it) == code }

    companion object {
        val DEFAULT_OVERRIDES: Map<Status, Int> =
            mapOf(
                Codes.CREATED to 201,
                Codes.HANDLED to 204,
                Codes.CONFIRM to 200,
                Codes.NOT_FOUND to 404,
                Codes.MISSING to 400,
                Codes.FORBIDDEN to 403,
                Codes.CONFLICT to 409,
                Codes.DEPRECATED to 426,
                Codes.UNIMPLEMENTED to 501,
                Codes.UNSUPPORTED to 501,
                Codes.TIMEOUT to 408,
                Codes.RATE_LIMITED to 429,
                Codes.UNEXPECTED to 500,
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
 * val MY_DOMAIN_CODE = Failed.Errored("PAYMENT_DECLINED", "Payment declined")
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
