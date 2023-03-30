package com.appwise.scanner

class FilterResult(val prefix: String? = null, val regex: Regex? = null) {
    fun filterScannerResult(value: String?): Boolean {
        return value?.let {
            startsWithPrefix(it) && regexMatches(it)
        } ?: false
    }

    private fun startsWithPrefix(value: String): Boolean {
        return prefix?.let {
            value.startsWith(it)
        } ?: true
    }

    private fun regexMatches(value: String): Boolean {
        return regex?.matches(value) ?: true
    }
}
