package me.mx1700.koalin.core

import org.slf4j.LoggerFactory
import java.io.InputStream
import java.lang.Exception
import java.util.logging.Logger

typealias Next = () -> Unit
typealias Middleware = (Context) -> Unit
typealias OnException = Context.(Exception) -> Unit

open class Application(
        /**
         * 是否是通过代理访问，当设置为 true 的时候，会通过代理头获取 ip 和 host
         */
        val proxy: Boolean = false
) {

    /**
     * 日志
     */
    val logger = LoggerFactory.getLogger(javaClass.name)!!

    /**
     * callback 的引用
     */
    val callback: (Request, Response) -> Context = this::callback

    /**
     * 中间件列表
     */
    private val middlewareList = arrayListOf<Middleware>()

    /**
     * 异常事件
     */
    private var onExceptionAction: OnException = { e ->
        logger.error("request exception", e)
        response.status = 500
        response.character = "UTF-8"
        response.body = e.stackTrace.contentToString()
    }

    /**
     * 添加中间件
     */
    fun use(middleware: Context.() -> Unit) {
        middlewareList.add(middleware)
    }

    /**
     * 添加并配置中间件
     */
    fun <T : Middleware> use(middleware: T, config: T.() -> Unit) {
        config(middleware)
        middlewareList.add(middleware)
    }

    /**
     * 添加并配置中间件
     */
    fun <T : Middleware> use(middlewareAction: () -> T, config: T.() -> Unit = {}) {
        val middleware = middlewareAction()
        config(middleware)
        middlewareList.add(middleware)
    }

    /**
     * 开始执行中间件
     */
    private fun callback(request: Request,
                         response: Response): Context {
        val ctx = Context(this, request, response)
        try {
            next(0, ctx)
        } catch (err: Exception) {
            onExceptionAction.invoke(ctx, err)
        }
        return ctx
    }

    /**
     * 执行下一个中间件
     */
    private fun next(index: Int, ctx: Context) {
        if (index == middlewareList.count()) {
            return
        }
        val middleware = middlewareList[index]
        ctx.next = {
            next(index + 1, ctx)
        }
        middleware(ctx)
    }
}
