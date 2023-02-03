package com.appwise.scanner

import android.annotation.SuppressLint
import android.graphics.Rect
import android.graphics.RectF
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.core.graphics.toRectF
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage


class OCRAnalyzer(
    private val previewWidth: Int,
    private val previewHeight: Int,
    private val isFrontLens: Boolean,
    private val barcodeListener: ScanListener
) : ImageAnalysis.Analyzer {


    private val options = BarcodeScannerOptions.Builder()
        .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
        .build()

    private val scanner = BarcodeScanning.getClient(options)


    val scanAreaRect: Rect
        get() {
            val widthPercentage = 0.9
            val heightPercentage = 0.5
            val outsideWidthPercentage = (1 - widthPercentage) / 2
            val outsideHeightPercentage = (1 - heightPercentage) / 2
            val left = outsideWidthPercentage * previewWidth
            val right = (widthPercentage + outsideWidthPercentage) * previewWidth
            val top = outsideHeightPercentage * previewHeight
            val bottom = (heightPercentage + outsideHeightPercentage) * previewHeight

            return Rect(left.toInt(), top.toInt(), right.toInt(), bottom.toInt())
        }

    @SuppressLint("UnsafeExperimentalUsageError", "UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image

        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            val rotation = imageProxy.imageInfo.rotationDegrees

            image.mediaImage?.cropRect = scanAreaRect
            scanner.process(image).addOnSuccessListener { barcodes ->

                // In order to correctly display the face bounds, the orientation of the analyzed
                // image and that of the viewfinder have to match. Which is why the dimensions of
                // the analyzed image are reversed if its rotation information is 90 or 270.
                val reverseDimens = rotation == 90 || rotation == 270
                val width = if (reverseDimens) imageProxy.height else imageProxy.width
                val height = if (reverseDimens) imageProxy.width else imageProxy.height

                //so we can draw boxes where qr codes are found
                val bounds = barcodes.mapNotNull { it.boundingBox?.transform(width, height) }.toMutableList()
                barcodeListener.onBoundsFound(bounds)

                barcodes.forEach { barcode ->
                    val qrCodeRectF = barcode.boundingBox?.transform(width, height)
                    if (qrCodeRectF != null && scanAreaRect.toRectF().contains(qrCodeRectF))
                        barcode.displayValue?.let { barcodeListener.onValueFound(it) }
                }
                imageProxy.close()
            }
                .addOnFailureListener {
                    imageProxy.close()
                }
        }
    }


    private fun Rect.transform(width: Int, height: Int): RectF {
        val scaleX = previewWidth / width.toFloat()
        val scaleY = previewHeight / height.toFloat()

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