package com.appwise.scanner.base

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.graphics.toRectF
import com.appwise.scanner.R
import com.appwise.scanner.rect

class TargetOverlay @JvmOverloads constructor(
    ctx: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(ctx, attributeSet, defStyleAttr) {

    //use this to edit targets and avoid concurrentmodification error
    private val lock = Any()
    private var mTargets: MutableList<Target> = ArrayList()

    init {
        post {
            drawBackgroundAndScanArea()
        }
    }

    override fun dispatchDraw(canvas: Canvas) {
        //always do this first otherwise Targetboxes get cleared when they enter scan area
        super.dispatchDraw(canvas)

        mTargets.forEach { it.draw(canvas) }
    }

    private fun drawBackgroundAndScanArea() {
        findViewById<View>(R.id.ivScanArea)?.let { scanArea ->
            val path = Path().also {
                it.fillType = Path.FillType.WINDING
                it.addRect(0f, 0f, width.toFloat(), height.toFloat(), Path.Direction.CW)
                it.addRect(scanArea.rect.toRectF(), Path.Direction.CCW)
            }
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).also {
                it.style = Paint.Style.FILL
                it.color = Color.argb(150, 0, 0, 0)
            }
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val newCanvas = Canvas(bitmap)
            newCanvas.drawPath(path, paint)
            this.background = BitmapDrawable(context.resources, bitmap)

            postInvalidate()
        }
    }

    fun clear() {
        synchronized(lock) { mTargets.clear() }
        postInvalidate()
    }

    fun add(graphic: Target) {
        synchronized(lock) { mTargets.add(graphic) }
        postInvalidate()
    }

    fun remove(graphic: Target) {
        synchronized(lock) { mTargets.remove(graphic) }
        postInvalidate()
    }

    fun addTargets(targets: List<Target>) {
        if (mTargets.isEmpty() && targets.isEmpty()) return

        synchronized(lock) {
            this.mTargets.clear()
            this.mTargets.addAll(targets)
        }
        postInvalidate()
    }

    abstract class Target(private val overlay: TargetOverlay) {

        abstract val rectF: RectF

        open val rectPaint = Paint().apply {
            style = Paint.Style.STROKE
            color = ContextCompat.getColor(overlay.context, android.R.color.holo_red_dark)
            strokeWidth = 10f
        }

        open fun draw(canvas: Canvas?) {
            canvas?.drawRect(rectF, rectPaint)
        }
    }
}