package com.wisemen.scanwise.scanresult

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class ScanCodeResult(
    val code: String? = null,
) : Parcelable