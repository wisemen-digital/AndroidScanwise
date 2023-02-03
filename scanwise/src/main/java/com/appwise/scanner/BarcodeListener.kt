package com.appwise.scanner

import android.graphics.RectF

interface ScanListener {
    fun onValueFound(value: String)
    fun onBoundsFound(bounds: MutableList<RectF>)
}