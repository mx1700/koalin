package me.mx1700.koalin.core

data class Cookie(val name: String,
             val value: String?,
             val path: String?,
             val domain: String?,
             val secure: Boolean,
             val httpOnly: Boolean,
             val maxAge: Int?)
