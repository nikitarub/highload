package ru.nikitarub.highload.myhttp

enum class RequestMethod {
    GET,
    HEAD,
    UNKNOWN;

    companion object {
        fun valueOfString(key: String): RequestMethod =
                when (key) {
                    "GET" -> GET
                    "HEAD" -> HEAD
                    else -> UNKNOWN
                }
    }
}