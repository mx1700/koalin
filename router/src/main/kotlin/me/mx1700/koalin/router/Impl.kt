package me.mx1700.koalin.router

import java.util.regex.Pattern
import kotlin.coroutines.experimental.buildSequence

typealias RouteInfo<T> = Pair<Map<String, String>, T>

interface RouterMatcher<T> {
    fun add(method: String, url: String, other: T)
    fun add(method: String, url: String, rule: Map<String, String>, other: T)
    fun matches(method: String, url: String): Iterable<Pair<Map<String, String>, T>>
}

interface UrlMatcher<T> {
    fun add(url: String, rule: Map<String, String>,other: T)
    fun matches(url: String): Iterable<Pair<Map<String, String>, T>>
}

interface UrlRegexMatcher<T> {
    fun add(p: Pattern, paramNames: Array<String>, other: T)
    /**
     * 返回 参数字典 和 附加参数
     */
    fun matches(url: String): Iterable<RouteInfo<T>>
}

class RouterMatcherImpl<T> : RouterMatcher<T> {
    private val matcherList = mutableMapOf<String, UrlMatcher<T>>()
    override fun add(method: String, url: String, rule: Map<String, String>, other: T) {
        val matcher = matcherList[method]
        if (matcher != null) {
            matcher.add(url, rule, other)
        } else {
            val newMatcher: UrlMatcher<T> = UrlMatcherImpl()
            newMatcher.add(url, rule, other)
            matcherList[method] = newMatcher
        }
    }

    override fun add(method: String, url: String, other: T) {
        val matcher = matcherList[method]
        if (matcher != null) {
            matcher.add(url, emptyMap(),other)
        } else {
            val newMatcher: UrlMatcher<T> = UrlMatcherImpl()
            newMatcher.add(url, emptyMap(), other)
            matcherList[method] = newMatcher
        }
    }

    override fun matches(method: String, url: String): Iterable<RouteInfo<T>> {
        val matcher = matcherList[method] ?: return emptyList()
        return matcher.matches(url)
    }
}

class UrlMatcherImpl<T>: UrlMatcher<T> {
    val regexMatcher: UrlRegexMatcher<T> = UrlRegexMatcherImpl()

    override fun add(url: String, rule: Map<String, String>, other: T) {
        val (p, params) = buildRegex(url, rule)
        regexMatcher.add(p, params, other)
    }

    private fun buildRegex(url: String, rules: Map<String, String>): Pair<Pattern, Array<String>> {
        val params = mutableListOf<String>()
        val urlRegex = StringBuffer()

        val pattern = "\\{(\\w+\\??)\\}";
        val r = Pattern.compile(pattern);
        val m = r.matcher(url.replace("[", "(").replace("]", ")?"))

        while (m.find()) {
            val param = m.group(1)
            params.add(param)
            // \ 和 $ 在 replace 时是特殊字符，需要转义
            val rule = (rules[param] ?: "[^/.]+").replace("\\", "\\\\").replace("$", "\\$")
            m.appendReplacement(urlRegex, "(?<$1>$rule)")
        }
        m.appendTail(urlRegex)
        return Pattern.compile("^$urlRegex$") to params.toTypedArray()
    }

    override fun matches(url: String): Iterable<RouteInfo<T>> {
        return regexMatcher.matches(url)
    }
}

class UrlRegexMatcherImpl<T>: UrlRegexMatcher<T> {
    data class RouterItem<T>(
            val pattern: Pattern,
            val paramNames: Array<String>,
            val other: T
    )
    val regexMap = mutableListOf<RouterItem<T>>()
    override fun add(p: Pattern, paramNames: Array<String>, other: T) {
        regexMap.add(RouterItem(p, paramNames, other))
    }

    override fun matches(url: String): Iterable<RouteInfo<T>> {
        return buildSequence {
            for ((p, paramNames, other) in regexMap) {
                val m = p.matcher (url);
                if (m.find()) {
                    yield(paramNames.map { it to m.group(it) }.toMap() to other)
                }
            }
        }.asIterable()
    }
}
