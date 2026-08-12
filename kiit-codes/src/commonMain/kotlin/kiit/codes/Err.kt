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
@file:OptIn(ExperimentalJsExport::class, ExperimentalJsStatic::class)

package kiit.codes

import kotlin.js.ExperimentalJsExport
import kotlin.js.ExperimentalJsStatic
import kotlin.js.JsExport
import kotlin.js.JsName
import kotlin.js.JsStatic
import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmStatic

/**
 * Capability interface for any type that carries a list of [Err]. Implemented by [Checked] so a
 * validation-style result can be worked with generically alongside other error-carrying types.
 */
@JsExport
interface HasErrors {
    val errors: List<Err>
}

/**
 * Err is an error representation for use with `Result`/`Outcome`-style types and can be
 * created from
 * 1. simple strings
 * 2. exceptions
 * 3. field with name/value
 * 4. list of strings or Errs
 */
@JsExport
sealed class Err {
    abstract val msg: String
    abstract val cause: Throwable?
    abstract val ref: Any?

    /**
     * Different implementations for Error.
     *
     * Default Error implementation to represent an error with message and optional throwable
     */
    data class ErrorInfo
        @JvmOverloads
        constructor(
            override val msg: String,
            override val cause: Throwable? = null,
            override val ref: Any? = null,
        ) : Err()

    /**
     * Error implementation to represent an error on a specific field
     * @param field: Name of the field causing the error e.g. "email"
     * @param value: Value of the field causing the error e.g. "some_invalid_value"
     */
    data class ErrorField
        @JvmOverloads
        constructor(
            val field: String,
            val value: String,
            override val msg: String,
            override val cause: Throwable? = null,
            override val ref: Any? = null,
        ) : Err()

    /**
     * Error implementation to store list of errors
     * @param errors: List of all the errors
     */
    data class ErrorList
        @JvmOverloads
        constructor(
            val errors: List<Err>,
            override val msg: String,
            override val cause: Throwable? = null,
            override val ref: Any? = null,
        ) : Err()

    /**
     * Provides easy ways to build the Err type from various sources such as strings, exceptions, field errors
     */
    companion object {
        @JvmStatic
        @JsStatic
        @JvmOverloads
        fun of(msg: String, ex: Throwable? = null): Err {
            return ErrorInfo(msg, ex)
        }

        // @JsName disambiguates from the of(msg, ex) overload above — JS has no function
        // overloading, and @JsStatic requires a stable, unmangled name, so two same-named
        // companion members can't both be reachable from JS without one being renamed.
        @JvmStatic
        @JsStatic
        @JsName("ofStatus")
        fun of(status: Status): Err {
            return ErrorInfo(status.message)
        }

        @JvmStatic
        @JsStatic
        @JvmOverloads
        fun on(field: String, value: String, msg: String, ex: Throwable? = null): Err {
            return ErrorField(field, value, msg, ex)
        }

        /**
         * Builds an [Err] for a [field] with a specific per-occurrence [msg], without a [value].
         * Use this instead of [on] when the value itself could be sensitive (e.g. a password or
         * token) and shouldn't be carried on the error.
         *
         * `@JsName` disambiguates from the `on(field, value, msg, ex)` overload above — same
         * reason as `of`/`ofStatus`.
         */
        @JvmStatic
        @JsStatic
        @JvmOverloads
        @JsName("onField")
        fun on(field: String, msg: String, ex: Throwable? = null): Err {
            return ErrorField(field, "", msg, ex)
        }

        @JvmStatic
        @JsStatic
        fun ex(ex: Throwable): Err {
            return ErrorInfo(ex.message ?: "", ex)
        }

        @JvmStatic
        @JsStatic
        fun obj(err: Any): Err {
            return ErrorInfo(err.toString(), null, err)
        }

        @JvmStatic
        @JsStatic
        fun list(errors: List<String>, msg: String?): ErrorList {
            return ErrorList(errors.map { ErrorInfo(it) }, msg ?: "Error occurred")
        }

        @JvmStatic
        @JsStatic
        fun build(error: Any?): Err {
            return when (error) {
                null -> of(Unserved.UNEXPECTED.message)
                is Err -> error
                is String -> of(error)
                is Exception -> ex(error)
                else -> obj(error)
            }
        }
    }
}
