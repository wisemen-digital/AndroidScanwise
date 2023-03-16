package com.wisemen.scanwise

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.appwise.scanner.CameraSearchType
import com.appwise.scanner.barcode.BarcodeTarget
import com.appwise.scanner.base.CameraManager
import com.appwise.scanner.base.TargetOverlay
import com.google.android.material.snackbar.Snackbar
import com.wisemen.scanwise.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var mBinding: ActivityMainBinding

    private lateinit var cameraManager: CameraManager

    override fun onCreate(savedInstanceState: Bundle?) {
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        mBinding.lifecycleOwner = this

        super.onCreate(savedInstanceState)

        cameraManager = CameraManager().init(
            this,
            mBinding.previewView,
            this,
            { mBinding.overlay }, // This is done with a high-order-function because of changing the scan Analyzer
            CameraSearchType.QR
        ).apply {
            showTargetBoxes = BuildConfig.DEBUG
            setCameraManagerListener(object : CameraManager.CameraManagerListener {
                override fun targetsFound(targets: List<TargetOverlay.Target>) {
                    if (targets.isEmpty()) return
                    when (val target = targets.firstOrNull()) {
                        is BarcodeTarget -> {

                            Log.d("TAG", "targetsFound: $target")
                        }
                    }
                }
            })
        }

        cameraManager.start()

        mBinding.btnFlashlight.setOnClickListener {
            cameraManager.toggleTorch()
        }

        mBinding.btnSwitchCamera.setOnClickListener {
            cameraManager.changeCameraSelector()
        }

        mBinding.btnSwitchAnalyzer.setOnClickListener {
            when (cameraManager.cameraSearchType) {
                CameraSearchType.Barcode -> cameraManager.changeCameraType(CameraSearchType.QR)
                CameraSearchType.QR -> cameraManager.changeCameraType(CameraSearchType.OCR)
                CameraSearchType.OCR -> cameraManager.changeCameraType(CameraSearchType.Barcode)
            }
            Snackbar.make(mBinding.root, "Analyzer changed to ${cameraManager.cameraSearchType}", Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        cameraManager.stopCamera()
    }
}
