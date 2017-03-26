package me.mx1700.koalin.servlet

import me.mx1700.koalin.TimeUtil
import me.mx1700.koalin.core.Request as CoreRequest

class Request(
        private val ctx: Context
) : me.mx1700.koalin.core.Request {

    val req = ctx.req

    override val headers: Map<String, String> by lazy {
        req.headerNames.asSequence().map { it to req.getHeader(it) }.toMap()
    }
    override val url: String = req.requestURL.toString()

    override val href: String by lazy {
        if (queryString.isNullOrEmpty())
            url
        else
            "$url?$queryString"
    }

    override val origin: String = "${req.scheme}://${req.remoteHost}"

    override val scheme: String = req.scheme

    override val method: String = req.method

    override val path: String = req.pathInfo

    override val query: Map<String, String> by lazy {
        req.parameterNames.asSequence().map { it to req.getParameter(it) }.toMap()
    }

    override val queryString: String? = req.queryString

    override val cookies: Map<String, String> by lazy {
        req.cookies?.map { it.name to it.value }?.toMap() ?: emptyMap()
    }

    override val host: String by lazy {
        if (ctx.app.proxy && headers["X-Forwarded-Host"] != null)
            headers["X-Forwarded-Host"]!!.split("\\s*,\\s*".toRegex())[0]
        else
            "${this.hostname}:${req.serverPort}"
    }

    override val hostname by lazy {
        if (ctx.app.proxy && headers["X-Forwarded-Host"] != null) {
            this.host.split(':')[0]
        } else {
            req.remoteHost
        }
    }
    override val fresh: Boolean
        get() {
            if (method != "GET" && method != "HEAD") {
                return false
            }
            val s = ctx.res.status
            val req = ctx.request
            val res = ctx.response

            // 2xx or 304 as per rfc2616 14.26
            if ((s in 200..299) || 304 == s) {
                var etagMatches = true;
                var notModified = true;

                val modifiedSince = req.headers["if-modified-since"]
                val noneMatch = req.headers["if-none-match"]
                val lastModified = res.headers["last-modified"]
                val etag = res.headers["etag"]
                val cc = req.headers["cache-control"]

                // unconditional request
                if (modifiedSince == null && noneMatch == null) return false

                // check for no-cache cache request directive
                if (cc != null && cc.indexOf("no-cache") != -1) return false

                // if-none-match
                if (noneMatch != null) {
                    etagMatches = noneMatch.split(" *, *".toRegex())
                            .any { it == "*" || it == etag || it == "w/" + etag }
                }

                // if-modified-since
                if (modifiedSince != null && lastModified != null) {
                    notModified = TimeUtil.stringToDate(lastModified) <= TimeUtil.stringToDate(modifiedSince);
                }

                return etagMatches && notModified
            }

            return false
        }
    override val idempotent: Boolean by lazy {
        method in listOf("GET", "HEAD", "PUT", "DELETE", "OPTIONS", "TRACE")
    }

    override val charset: String? = req.characterEncoding

    /**
     * 请求数据长度
     */
    override val length: Long? = req.contentLengthLong

    /**
     * 协议   TODO:有歧义，servlet.protocol 返回的 http/1.1
     */
    override val protocol: String by lazy {
        when {
            req.isSecure -> "https"
            !ctx.app.proxy -> "http"
            headers["X-Forwarded-Proto"] != null ->
                headers["X-Forwarded-Proto"]!!.split("\\s*,\\s*".toRegex())[0]
            else -> "http"
        }
    }

    override val secure: Boolean by lazy {
        protocol == "https"
    }

    override val ips: Iterable<String> by lazy {
        if (ctx.app.proxy && headers["X-Forwarded-For"] != null)
            headers["X-Forwarded-For"]!!.split("\\s*,\\s*".toRegex())
        else
            emptyList()
    }

    override val contentType: String? = req.contentType
}
