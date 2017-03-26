package me.mx1700.koalin.core

class Context(
        val app: Application,
        val request: Request,
        val response: Response
) {
    lateinit var next: Next
}
