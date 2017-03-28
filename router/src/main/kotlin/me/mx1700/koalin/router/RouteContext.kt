package me.mx1700.koalin.router

import me.mx1700.koalin.core.Context
import me.mx1700.koalin.core.Next

class RouteContext(
        ctx: Context,
        val params: Map<String, String>
): Context by ctx {

    private lateinit var _next: Next

    override var next: Next
        get() = _next
        set(value) { _next = value }
}
