package com.appwise.scanner.base

import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.graphics.toRectF
import com.appwise.scanner.R
import com.appwise.scanner.rect

class TargetOverlay @JvmOverloads constructor(
    ctx: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(ctx, attrs, defStyleAttr) {

    @DimenRes
    private var mScanAreaCornerRadius: Int = -1

    @ColorInt
    private var mScanMaskColor: Int = -1

    @DrawableRes
    private var mScanAreaDrawable: Int = -1

    //use this to edit targets and avoid concurrentmodification error
    private val lock = Any()
    private var mTargets: MutableList<Target> = ArrayList()

    init {
        attrs?.let {
            val attributes = context.obtainStyledAttributes(it, R.styleable.TargetOverlay)
            with(attributes) {
                mScanAreaCornerRadius = getResourceId(R.styleable.TargetOverlay_scanAreaCornerRadius, -1)
                mScanMaskColor = getColor(R.styleable.TargetOverlay_scanMaskColor, -1)
                mScanAreaDrawable = getResourceId(R.styleable.TargetOverlay_scanAreaDrawable, -1)
            }
        }
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
            if (mScanAreaDrawable != -1)
                setBackgroundResource(mScanAreaDrawable)

            val scanAreaPath = Path().also {
                it.fillType = Path.FillType.WINDING
                it.addRect(0f, 0f, width.toFloat(), height.toFloat(), Path.Direction.CW)
                it.addRect(scanArea.rect.toRectF(), Path.Direction.CCW)
            }

            val transparentPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
                it.style = Paint.Style.FILL
                //backgroundCanvas.drawColor(if (mScanMaskColor != -1) mScanMaskColor else Color.BLACK)
                it.color = Color.argb(150, 0, 0, 0)
                if (mScanAreaCornerRadius != -1) {
                    it.strokeCap = Paint.Cap.ROUND
                    it.pathEffect = CornerPathEffect(resources.getDimension(mScanAreaCornerRadius))
                }// set the path effect when they join.
            }

            val backgroundBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val backgroundCanvas = Canvas(backgroundBitmap)
            backgroundCanvas.drawPath(scanAreaPath, transparentPaint)
            this@TargetOverlay.background = BitmapDrawable(context.resources, backgroundBitmap)

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