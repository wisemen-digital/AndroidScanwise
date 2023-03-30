package com.appwise.scanner

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class FilterResult(val prefix: String? = null, val regex: Regex? = null) : Parcelable {
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
