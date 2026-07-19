package sample

import kiit.codes.Codes
import kiit.codes.Failed
import kiit.codes.Status
import kiit.codes.StatusException

data class User(val id: String, val email: String)

/**
 * A tiny service that returns a [Status] for every outcome instead of throwing for
 * expected failures — [StatusException] is reserved for crossing a call boundary
 * that can only communicate via exceptions (see [SampleApp]).
 */
class UserService {
    private val users = mutableMapOf<String, User>()

    fun create(id: String, email: String): Status {
        if (email.isBlank()) return Codes.BAD_REQUEST
        if (users.containsKey(id)) return Codes.CONFLICT
        users[id] = User(id, email)
        return Codes.CREATED
    }

    fun fetch(id: String): User? = users[id]

    fun authorize(id: String, requesterId: String): Status =
        when {
            !users.containsKey(id) -> Codes.NOT_FOUND
            id != requesterId -> Codes.UNAUTHORIZED
            else -> Codes.SUCCESS
        }

    /** Throws [StatusException] instead of returning [Status] — for callers that need an exception. */
    fun requireAuthorized(id: String, requesterId: String) {
        val status = authorize(id, requesterId)
        if (status is Failed) throw StatusException(status)
    }
}
