package sample

import kiit.codes.Failed
import kiit.codes.Invalid
import kiit.codes.Rejected
import kiit.codes.Restricted
import kiit.codes.Status
import kiit.codes.StatusException
import kiit.codes.Succeeded
import kiit.codes.toException

data class User(val id: String, val email: String)

/**
 * A tiny service that returns a [Status] for every outcome instead of throwing for
 * expected failures — [StatusException] is reserved for crossing a call boundary
 * that can only communicate via exceptions (see [SampleApp]).
 */
class UserService {
    private val users = mutableMapOf<String, User>()

    fun create(id: String, email: String): Status {
        if (email.isBlank()) return Invalid.BAD_REQUEST
        if (users.containsKey(id)) return Rejected.CONFLICT
        users[id] = User(id, email)
        return Succeeded.CREATED
    }

    fun fetch(id: String): User? = users[id]

    fun authorize(id: String, requesterId: String): Status =
        when {
            !users.containsKey(id) -> Invalid.NOT_FOUND
            id != requesterId -> Restricted.UNAUTHORIZED
            else -> Succeeded.SUCCESS
        }

    /** Throws [StatusException] instead of returning [Status] — for callers that need an exception. */
    fun requireAuthorized(id: String, requesterId: String) {
        val status = authorize(id, requesterId)
        if (status is Failed) throw status.toException()
    }
}
