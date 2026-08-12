package sample

import kiit.codes.CodesToHttp
import kiit.codes.Failed
import kiit.codes.Passed
import kiit.codes.Status
import kiit.codes.StatusException

private val http = CodesToHttp()

fun main() {
    val service = UserService()

    report("create alice", service.create("alice", "alice@example.com"))
    report("create alice again", service.create("alice", "alice@example.com"))
    report("create with blank email", service.create("bob", ""))

    report("authorize alice as alice", service.authorize("alice", "alice"))
    report("authorize alice as bob", service.authorize("alice", "bob"))
    report("authorize unknown user", service.authorize("carol", "carol"))

    try {
        service.requireAuthorized("alice", "bob")
    } catch (e: StatusException) {
        println("caught StatusException: ${e.status.name} — ${e.message}")
    }
}

private fun report(label: String, status: Status) {
    val outcome =
        when (status) {
            is Passed -> "ok"
            is Failed -> "failed"
        }
    println("$label -> ${status.name} ($outcome, http=${http.toCode(status)})")
}
