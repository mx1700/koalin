package me.mx1700.koalin.core

interface Request {

    val headers: Map<String, String>

    val url: String

    val href: String

    val origin: String

    val scheme: String

    val method: String

    val path:String

    val query: Map<String, String>

    val queryString: String

    //TODO: 是否应该是 cookie 对象?
    val cookies: Map<String, String>

    val host: String

    val hostname: String

    val fresh: Boolean

    val stale: Boolean
        get() = !fresh

    val idempotent: Boolean

    val charset: String?

    val secure: Boolean

    val ips: Iterable<String>

    val contentType: String

}