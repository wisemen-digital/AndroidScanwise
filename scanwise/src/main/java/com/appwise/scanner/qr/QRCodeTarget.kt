package com.appwise.scanner.qr

import android.graphics.RectF
import com.appwise.scanner.base.TargetOverlay
import com.google.mlkit.vision.barcode.common.Barcode

class QRCodeTarget(val overlay: TargetOverlay, val barCode: Barcode, override val rectF: RectF) : TargetOverlay.Target(overlay)