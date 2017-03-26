package me.mx1700.koalin.core

import java.util.*

interface Response {
    val headers: ResponseHeader

    var status: Int

    //var message: String

    val cookies: ResponseCookies

    var body: Any?

    var contentType: String?

    var lastModified: Date?

    var etag: String?

    var character: String?

    fun redirect(url: String)

    fun attachment(fileName: String)


}

interface ResponseHeader: Iterable<Pair<String, String>> {
    operator fun get(name: String): String?
    operator fun set(name: String, value: String?)
    fun remove(name: String)
    fun contains(name: String): Boolean
    val length: Int
}

interface ResponseCookies: Iterable<Cookie> {
    operator fun get(name: String): Cookie?
    fun set(cookie: Cookie)
    operator fun set(name: String, value: String?)
    fun set(name: String, value: String?,
            path: String? = "/", domain: String? = null,
            secure: Boolean = false, httpOnly: Boolean = true,
            maxAge: Int? = null)
}