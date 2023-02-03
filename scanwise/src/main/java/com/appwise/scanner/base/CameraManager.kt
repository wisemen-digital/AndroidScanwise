package com.appwise.scanner.base

import android.annotation.SuppressLint
import android.content.Context
import android.util.DisplayMetrics
import android.util.Log
import android.util.Size
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceOrientedMeteringPointFactory
import androidx.camera.core.TorchState
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.appwise.scanner.CameraSearchType
import com.appwise.scanner.barcode.CodeAnalyzer
import com.appwise.scanner.qr.QRAnalyzer
import com.appwise.scanner.text.TextAnalyzer
import java.util.concurrent.ExecutionException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class CameraManager(
    private val context: Context,
    private val finderView: PreviewView,
    private val lifecycleOwner: LifecycleOwner,
    private val targetOverlay: () -> TargetOverlay,
    // the default for this is the barcode scanner
    var cameraSearchType: CameraSearchType = CameraSearchType.Barcode
) {

    interface CameraManagerListener {
        fun analyzerChanged(cameraSearchType: CameraSearchType) {}
        fun targetsFound(targets: List<TargetOverlay.Target>)
    }

    private val targetFoundListener: (List<TargetOverlay.Target>) -> Unit = {
        mCameraManagerListener?.targetsFound(it)
    }

    private var preview: Preview? = null
    private var camera: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var imageAnalyzer: ImageAnalysis? = null

    private val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    private var cameraSelectorOption = CameraSelector.LENS_FACING_BACK

    var showTargetBoxes = false
    private var mCameraManagerListener: CameraManagerListener? = null

    private fun getSelectedAnalyzer() = when (cameraSearchType) {
        CameraSearchType.Barcode -> CodeAnalyzer(targetOverlay, isFrontLens, showTargetBoxes, targetFoundListener)
        CameraSearchType.OCR -> TextAnalyzer(targetOverlay, isFrontLens, showTargetBoxes, targetFoundListener)
        CameraSearchType.QR -> QRAnalyzer(targetOverlay, isFrontLens, showTargetBoxes, targetFoundListener)
    }

    private val isFrontLens get() = cameraSelectorOption == CameraSelector.LENS_FACING_FRONT

    @SuppressLint("UnsafeOptInUsageError")
    fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener(
            {
                try {
                    cameraProvider = cameraProviderFuture.get()
                    val builder = Preview.Builder()
                    val metrics = DisplayMetrics().also { finderView.display.getRealMetrics(it) }
                    val screenSize = Size(metrics.widthPixels, metrics.heightPixels)

                    // Preview
                    preview = builder
                        .setTargetResolution(screenSize)
                        .build()

                    // Image analyzer
                    imageAnalyzer = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .setTargetResolution(screenSize)
                        .build()
                        .also {
                            it.setAnalyzer(cameraExecutor, getSelectedAnalyzer())
                        }

                    val cameraSelector = CameraSelector.Builder()
                        .requireLensFacing(cameraSelectorOption)
                        .build()

                    // Must unbind the use-cases before rebinding them
                    cameraProvider?.unbindAll()
                    camera = cameraProvider?.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageAnalyzer)

                    val autoFocusAction = FocusMeteringAction.Builder(SurfaceOrientedMeteringPointFactory(1f, 1f).createPoint(0.5f, 0.5f), FocusMeteringAction.FLAG_AF).apply {
                        setAutoCancelDuration(2, TimeUnit.SECONDS)
                    }.build()
                    camera?.cameraControl?.startFocusAndMetering(autoFocusAction)

                    preview?.setSurfaceProvider(finderView.surfaceProvider)
                } catch (e: ExecutionException) {
                    Log.e("Execution Exception", e.message, e)
                } catch (e: InterruptedException) {
                    Log.e("Interrupted Exception", e.message, e)
                }
            },
            ContextCompat.getMainExecutor(context)
        )
    }

    fun toggleTorch() {
        camera?.let {
            if (it.cameraInfo.hasFlashUnit()) {
                it.cameraControl.enableTorch(it.cameraInfo.torchState.value == TorchState.OFF)
            }
        }
    }

    fun changeCameraType(cameraSearchType: CameraSearchType) {
        if (cameraSearchType != this.cameraSearchType) {
            cameraProvider?.unbindAll()
            this.cameraSearchType = cameraSearchType
            startCamera()
            mCameraManagerListener?.analyzerChanged(this.cameraSearchType)
//            targetOverlay().redrawBackground()
        }
    }

    fun changeCameraSelector() {
        cameraProvider?.unbindAll()
        cameraSelectorOption = if (cameraSelectorOption == CameraSelector.LENS_FACING_BACK) CameraSelector.LENS_FACING_FRONT else CameraSelector.LENS_FACING_BACK
        startCamera()
    }

    fun setCameraManagerListener(cameraManagerListener: CameraManagerListener) {
        mCameraManagerListener = cameraManagerListener
    }

    fun stopCamera() {
        cameraExecutor.shutdown()
        cameraProvider?.unbindAll()
        getSelectedAnalyzer().stop()
    }
}