package com.appwise.scanner.barcode

import android.graphics.RectF
import com.appwise.scanner.base.TargetOverlay
import com.google.mlkit.vision.barcode.common.Barcode

class BarcodeTarget(val overlay: TargetOverlay, val barCode: Barcode, override val rectF: RectF) : TargetOverlay.Target(overlay)