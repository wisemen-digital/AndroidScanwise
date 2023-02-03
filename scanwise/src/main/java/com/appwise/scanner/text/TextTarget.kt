package com.appwise.scanner.text

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import com.appwise.scanner.base.TargetOverlay
import com.google.mlkit.vision.text.Text

class TextTarget(overlay: TargetOverlay, private val textBlock: Text.TextBlock, override val rectF: RectF) : TargetOverlay.Target(overlay) {

    override val rectPaint = Paint().apply {
        style = Paint.Style.STROKE
        color = Color.WHITE
        strokeWidth = 10f
    }

    private val textPaint = Paint().apply {
        color = TEXT_COLOR
        textSize = OCR_TEXT_SIZE
    }

    override fun draw(canvas: Canvas?) {
        canvas?.drawRoundRect(rectF, ROUND_RECT_CORNER, ROUND_RECT_CORNER, rectPaint)
        canvas?.drawText(textBlock.text, rectF.left, rectF.bottom, textPaint)
    }

    companion object {
        private const val TEXT_COLOR = Color.WHITE
        private const val OCR_TEXT_SIZE = 45f
        private const val ROUND_RECT_CORNER = 10f
    }
}