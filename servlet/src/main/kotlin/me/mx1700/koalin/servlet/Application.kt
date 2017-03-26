package me.mx1700.koalin.servlet

import me.mx1700.koalin.core.Context
import java.io.InputStream
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import me.mx1700.koalin.core.Application as CoreApplication

class Application(): me.mx1700.koalin.core.Application() {

    val servlet: HttpServlet = Servlet()

    override fun respond(ctx: Context) {
        val context = ctx as me.mx1700.koalin.servlet.Context
        val res = context.res
        val body = ctx.response.body

        var close = false
        try {
            if (res.isCommitted) return
            if (body == null) return

            when (body) {
                is CharSequence -> res.writer.print(body)
                is ByteArray -> res.outputStream.write(body)
                is InputStream -> {
                    val b = ByteArray(512)
                    close = true
                    body.use {
                        while (true) {
                            val n = body.read(b)
                            if (n == -1) break
                            res.outputStream.write(b, 0, n)
                        }
                    }
                }
                else -> throw IllegalArgumentException("不支持的 body 类型: " + body.javaClass.name)
            }
        } finally {
            if (!close && body != null && body is InputStream) {
                //保证流最后关闭
                body.use { }
            }
        }
    }

    inner class Servlet: HttpServlet() {
        override fun service(req: HttpServletRequest, resp: HttpServletResponse) {
            val ctx = me.mx1700.koalin.servlet.Context(this@Application, req, resp)
            callback(ctx)
        }
    }
}
