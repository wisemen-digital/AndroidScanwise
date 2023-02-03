package com.appwise.scanner.base

import android.annotation.SuppressLint
import android.graphics.Rect
import android.graphics.RectF
import android.view.View
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.core.graphics.toRectF
import com.appwise.scanner.R
import com.appwise.scanner.rect
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage

abstract class BaseAnalyzer<T>(val overlay: () -> TargetOverlay, val onValueFound: (results: List<TargetOverlay.Target>) -> Unit) : ImageAnalysis.Analyzer {

    abstract val isFrontLens: Boolean
    abstract val showTargetBoxes: Boolean

    abstract fun searchInImage(image: InputImage): Task<T>
    abstract fun stop()

    protected abstract fun onFailure(e: Exception)
    protected abstract fun getTargets(results: T, width: Int, height: Int): List<TargetOverlay.Target>

    @SuppressLint("UnsafeExperimentalUsageError", "UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        val rotation = imageProxy.imageInfo.rotationDegrees

        mediaImage?.let {
            searchInImage(InputImage.fromMediaImage(it, imageProxy.imageInfo.rotationDegrees))
                .addOnSuccessListener { results ->
                    val reverseDimens = rotation == 90 || rotation == 270
                    val width = if (reverseDimens) imageProxy.height else imageProxy.width
                    val height = if (reverseDimens) imageProxy.width else imageProxy.height

                    val targets = getTargets(results, width, height)
                    if (showTargetBoxes)
                        overlay().addTargets(targets)

                    val scanArea2 = overlay().findViewById<View>(R.id.ivScanArea)

                    val filteredTargets = targets.filter { target -> scanArea2.rect.toRectF().contains(target.rectF) }
                    onValueFound(filteredTargets)
                }
                .addOnFailureListener {
                    overlay().postInvalidate()
                    onFailure(it)
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        }
    }

    fun Rect.transform(width: Int, height: Int): RectF {
        val scaleX = overlay().width / width.toFloat()
        val scaleY = overlay().height / height.toFloat()

        // If the front camera lens is being used, reverse the right/left coordinates
        val flippedLeft = if (isFrontLens) width - right else left
        val flippedRight = if (isFrontLens) width - left else right

        // Scale all coordinates to match preview
        val scaledLeft = scaleX * flippedLeft
        val scaledTop = scaleY * top
        val scaledRight = scaleX * flippedRight
        val scaledBottom = scaleY * bottom
        return RectF(scaledLeft, scaledTop, scaledRight, scaledBottom)
    }
}