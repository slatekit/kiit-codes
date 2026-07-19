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
 * Using these is optional — they're provided as sensible defaults and for kiit-result builder
 * methods. Custom domain codes can be created by constructing any [Passed] or [Failed] subtype
 * directly; only the four categories under each are fixed/closed (see [Status]).
 *
 * Numeric code ranges (conceptual grouping only — see the NOTE on [Status.code]):
 *   200000-200999 Succeeded     201000-201999 Pending
 *   202000-202999 Filtered      203000-203999 Information
 *   400000-400999 Denied        401000-401999 Invalid
 *   402000-402999 Errored       403000-403999 Unserved
 *
 * Uniqueness of every code in this registry is enforced at object-init time (see the `init`
 * block below) — a duplicate code will fail loudly the first time [Codes] is touched, rather
 * than silently producing a wrong HTTP mapping. So is every code falling inside its own
 * category's documented range above.
 */
object Codes {
    // ---- Succeeded (200000-200999) ----
    val SUCCESS = Passed.Succeeded("SUCCESS", 200001, "Success")
    val CREATED = Passed.Succeeded("CREATED", 200002, "Created")
    val UPDATED = Passed.Succeeded("UPDATED", 200003, "Updated")
    val FETCHED = Passed.Succeeded("FETCHED", 200004, "Fetched")
    val PATCHED = Passed.Succeeded("PATCHED", 200005, "Patched")
    val DELETED = Passed.Succeeded("DELETED", 200006, "Deleted")
    val HANDLED = Passed.Succeeded("HANDLED", 200007, "Handled") // e.g. a silent OK, similar to HTTP 204

    // ---- Pending (201000-201999) ----
    val PENDING = Passed.Pending("PENDING", 201001, "Pending")
    val QUEUED = Passed.Pending("QUEUED", 201002, "Queued")
    val CONFIRM = Passed.Pending("CONFIRM", 201003, "Confirm")

    // ---- Filtered (202000-202999) ----
    val SKIPPED = Passed.Filtered("SKIPPED", 202001, "Skipped") // not processed at all
    val DISCARDED = Passed.Filtered("DISCARDED", 202002, "Discarded") // processed, result thrown away

    // ---- Information (203000-203999) ----
    val HELP = Passed.Information("HELP", 203001, "Help")
    val ABOUT = Passed.Information("ABOUT", 203002, "About")
    val VERSION = Passed.Information("VERSION", 203003, "Version")
    val EXIT = Passed.Information("EXIT", 203004, "Exiting")

    // ---- Denied (400000-400999) — security / access-control ----
    val DENIED = Failed.Denied("DENIED", 400001, "Denied")
    val UNAUTHENTICATED = Failed.Denied("UNAUTHENTICATED", 400002, "Unauthenticated")
    val UNAUTHORIZED = Failed.Denied("UNAUTHORIZED", 400003, "Unauthorized")

    // ---- Invalid (401000-401999) — bad input ----
    val BAD_REQUEST = Failed.Invalid("BAD_REQUEST", 401001, "Bad request") // e.g. malformed JSON
    val INVALID = Failed.Invalid("INVALID", 401002, "Invalid") // e.g. well-formed but invalid values
    val NOT_FOUND = Failed.Invalid("NOT_FOUND", 401003, "Not found") // e.g. resource/endpoint not found

    // ---- Errored (402000-402999) — known, expected business-rule failure ----
    val MISSING = Failed.Errored("MISSING", 402001, "Missing item") // e.g. domain model not found
    val FORBIDDEN = Failed.Errored("FORBIDDEN", 402002, "Forbidden")
    val CONFLICT = Failed.Errored("CONFLICT", 402003, "Conflict")
    val DEPRECATED = Failed.Errored("DEPRECATED", 402004, "Deprecated")
    val ERRORED = Failed.Errored("ERRORED", 402005, "Errored") // general purpose use

    // ---- Unserved (403000-403999) — valid & permitted, can't be serviced right now ----
    val UNIMPLEMENTED = Failed.Unserved("UNIMPLEMENTED", 403001, "Not implemented")
    val UNSUPPORTED = Failed.Unserved("UNSUPPORTED", 403002, "Not supported")
    val TIMEOUT = Failed.Unserved("TIMEOUT", 403003, "Timeout")
    val RATE_LIMITED = Failed.Unserved("RATE_LIMITED", 403004, "Rate limited")
    val UNREACHABLE = Failed.Unserved("UNREACHABLE", 403005, "Unreachable") // e.g. dependency down
    val UNDER_MAINTENANCE = Failed.Unserved("UNDER_MAINTENANCE", 403006, "Under maintenance")
    val UNEXPECTED = Failed.Unserved("UNEXPECTED", 403007, "Unexpected") // unhandled/uncaught path

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

    private val byCode: Map<Int, Status> = all.associateBy { it.code }

    init {
        check(byCode.size == all.size) {
            val dupes = all.groupBy { it.code }.filterValues { it.size > 1 }.keys
            "Duplicate Status codes detected in Codes registry: $dupes"
        }
        val outOfRange = all.filterNot { it.code in categoryRange(it) }
        check(outOfRange.isEmpty()) {
            val offenders = outOfRange.map { "${it.name}=${it.code}" }
            "Status codes outside their documented category range: $offenders"
        }
    }

    /** The documented numeric range for [status]'s category — see the range table above. */
    private fun categoryRange(status: Status): IntRange =
        when (status) {
            is Passed.Succeeded -> 200000..200999
            is Passed.Pending -> 201000..201999
            is Passed.Filtered -> 202000..202999
            is Passed.Information -> 203000..203999
            is Failed.Denied -> 400000..400999
            is Failed.Invalid -> 401000..401999
            is Failed.Errored -> 402000..402999
            is Failed.Unserved -> 403000..403999
        }

    /** Looks up a built-in [Status] by its internal registry code (e.g. 400001). Null if unknown. */
    fun statusForCode(code: Int): Status? = byCode[code]
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
    private val overrides: Map<Int, Int> = DEFAULT_OVERRIDES,
) : CodeLookup {
    override fun toCode(status: Status): Int {
        overrides[status.code]?.let { return it }
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
        val DEFAULT_OVERRIDES: Map<Int, Int> =
            mapOf(
                Codes.CREATED.code to 201,
                Codes.HANDLED.code to 204,
                Codes.CONFIRM.code to 200,
                Codes.NOT_FOUND.code to 404,
                Codes.MISSING.code to 400,
                Codes.FORBIDDEN.code to 403,
                Codes.CONFLICT.code to 409,
                Codes.DEPRECATED.code to 426,
                Codes.UNIMPLEMENTED.code to 501,
                Codes.UNSUPPORTED.code to 501,
                Codes.TIMEOUT.code to 408,
                Codes.RATE_LIMITED.code to 429,
                Codes.UNEXPECTED.code to 500,
            )
    }
}

/**
 * Composes a [base] [CodeLookup] with client-supplied [extensions], without modifying or
 * subclassing the base implementation (composition over inheritance). [extensions] take
 * precedence over [base] for both directions.
 *
 * [extensions] is keyed by the actual [Status] instance (not just its numeric code) so that
 * [toStatus] can be answered correctly for custom statuses that aren't part of the [Codes.all]
 * registry — a plain `Map<Int, Int>` of code-to-code can't support that, since it never holds
 * a reference to the actual custom Status object to return.
 *
 * ```kotlin
 * val MY_DOMAIN_CODE = Failed.Errored("PAYMENT_DECLINED", 700123, "Payment declined")
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
