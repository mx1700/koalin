package mx.mx1700.koalin.servlet

import me.mx1700.koalin.TimeUtil
import me.mx1700.koalin.core.Cookie
import me.mx1700.koalin.core.ResponseCookies
import me.mx1700.koalin.core.ResponseHeader
import java.net.URLConnection
import java.util.*
import me.mx1700.koalin.core.Response as CoreResponse

class Response(
        private val ctx: Context
): CoreResponse {

    private val res = ctx.res

    override val headers = Headers()

    override var status: Int
        get() = res.status
        set(value) { res.status = value }

    override val cookies = Cookies()

    override var body: Any? = null

    override var contentType: String?
        get() = res.contentType
        set(value) {
            if (value == null || value.indexOf("/") >= 0) {
                res.contentType = value
            } else {
                val fileNameMap = URLConnection.getFileNameMap()
                res.contentType = fileNameMap.getContentTypeFor(value)
            }
        }

    override var lastModified: Date?
        get() {
            val last = headers["Last-Modified"]
            return if (last != null) TimeUtil.stringToDate(last) else null
        }
        set(value) {
            headers["Last-Modified"] = if (value != null) TimeUtil.dateToString(value) else null
        }

    override var etag: String?
        get() = headers["ETag"]
        set(value) {
            if (value == null) {
                headers.remove("Etag")
                return
            }
            if (!"^(W/)?\".*".toRegex().matches(value)) {
                headers["Etag"] = "\"$value\""
            } else {
                headers["Etag"] = value
            }
        }

    override var character: String?
        get() = res.characterEncoding
        set(value) { res.characterEncoding = value }

    override fun redirect(url: String) {
        res.sendRedirect(url)
    }

    override fun attachment(fileName: String) {
        this.contentType = fileName
        headers["Content-Disposition"] = "attachment; filename=\"$fileName\""
    }

    inner class Headers: ResponseHeader {
        override fun get(name: String): String? = res.getHeader(name)

        override fun set(name: String, value: String?) = res.setHeader(name, value)

        override fun remove(name: String) = set(name, null)

        override fun contains(name: String): Boolean = res.containsHeader(name)

        override val length: Int
            get() = res.headerNames.count()

        override fun iterator(): Iterator<Pair<String, String>> {
            return res.headerNames.map { it to res.getHeader(it) }.iterator()
        }
    }

    inner class Cookies: ResponseCookies {

        private val cookiesMap = HashMap<String, Cookie>()

        override operator fun get(name: String): Cookie? = cookiesMap[name]

        override operator fun set(name: String, value: String?) {
            this.set(name, value, "/")
        }

        override fun set(cookie: Cookie) {
            set(cookie.name, cookie.value, cookie.path, cookie.domain, cookie.secure, cookie.httpOnly, cookie.maxAge)
        }

        override fun set(name: String, value: String?,
                path: String?, domain: String?,
                secure: Boolean, httpOnly: Boolean,
                maxAge: Int?) {

            val cookie = Cookie(name, value, path, domain, secure, httpOnly, maxAge)
            cookiesMap[name] = cookie

            val sCookie = javax.servlet.http.Cookie(name, value)
            if (path != null) sCookie.path = path
            if (domain != null) sCookie.domain = domain
            sCookie.secure = secure
            sCookie.isHttpOnly = httpOnly
            if (maxAge != null) sCookie.maxAge = maxAge
            res.addCookie(sCookie)
        }

        override fun iterator(): Iterator<Cookie> {
            return cookiesMap.values.iterator()
        }
    }

}
