package me.mx1700.koalin.core

interface Context {
    val app: Application
    val request: Request
    val response: Response
    var next: Next
}
