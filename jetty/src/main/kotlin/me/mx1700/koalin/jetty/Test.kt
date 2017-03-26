package me.mx1700.koalin.jetty

import me.mx1700.koalin.servlet.Application

fun main(args: Array<String>) {
    val app = Application()
    app.use {
        response.body = "hello world"
        request.javaClass.declaredMethods
                .filter { it.name.startsWith("get") }
                .forEach {
                    val value = it.invoke(request)
                    println("${it.name}: $value")
                }
    }
    app.listen()
}
