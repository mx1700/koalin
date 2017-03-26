package me.mx1700.koalin.servlet

import me.mx1700.koalin.core.Application
import me.mx1700.koalin.core.Next
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import me.mx1700.koalin.core.Context as CoreContext

class Context(
        override val app: Application,
        val req: HttpServletRequest,
        val res: HttpServletResponse
): me.mx1700.koalin.core.Context {

    override val request: Request = Request(this)
    override val response: Response = Response(this)
    lateinit override var next: Next

}
