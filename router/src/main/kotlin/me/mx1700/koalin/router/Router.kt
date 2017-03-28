package me.mx1700.koalin.router

import me.mx1700.koalin.core.Middleware
import me.mx1700.koalin.core.Context

typealias Action = RouteContext.() -> Any?

/**
 * TODO: ResultHandler
 */
class Router(): Middleware {

    private val matcher = RouterMatcherImpl<Action>()

    override fun invoke(ctx: Context) {
        val path = ctx.request.path
        val method = ctx.request.method

        val matches = matcher.matches(method, path).iterator()
        if (matches.hasNext()) {
            next(matches, ctx)
        } else {
            ctx.response.status = 404
        }
        ctx.next()
    }

    fun next(matches: Iterator<RouteInfo<Action>>, ctx: Context) {
        if (matches.hasNext()) {
            val match = matches.next()
            val (params, action) = match
            val routeCtx = RouteContext(ctx, params)
            routeCtx.next = {
                next(matches, ctx)
            }
            val body = action(routeCtx)
            if (body != null && body != kotlin.Unit) {
                ctx.response.body = action(routeCtx)
            }
        }
    }

    fun get(url: String, action: Action) {
        matcher.add("GET", url, action)
    }

    fun get(url: String, rule: Map<String, String>, action: Action) {
        matcher.add("GET", url, rule, action)
    }

    fun post(url: String, action: Action) {
        matcher.add("POST", url, action)
    }

    fun post(url: String, rule: Map<String, String>, action: Action) {
        matcher.add("POST", url, rule, action)
    }
}
