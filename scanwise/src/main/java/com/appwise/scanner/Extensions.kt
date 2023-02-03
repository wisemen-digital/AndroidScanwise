package com.appwise.scanner

import android.graphics.Rect
import android.view.View

val View.rect
    get(): Rect {
        return Rect(x.toInt(), y.toInt(), x.toInt() + width, y.toInt() + height)
    }