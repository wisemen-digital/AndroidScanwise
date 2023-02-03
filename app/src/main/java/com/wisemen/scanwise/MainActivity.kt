package com.wisemen.scanwise

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.appwise.scanner.CameraSearchType
import com.appwise.scanner.barcode.BarcodeTarget
import com.appwise.scanner.base.CameraManager
import com.appwise.scanner.base.TargetOverlay
import com.wisemen.scanwise.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityMainBinding

    private val requestCameraPermissions =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { permissionGranted ->

            if (permissionGranted) {
                cameraManager.startCamera()
            }
        }

    private lateinit var cameraManager: CameraManager

    override fun onCreate(savedInstanceState: Bundle?) {
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        mBinding.lifecycleOwner = this

        super.onCreate(savedInstanceState)

        cameraManager = CameraManager(
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
        requestCameraPermissions.launch(Manifest.permission.CAMERA)

        mBinding.btnFlashlight.setOnClickListener {
            cameraManager.toggleTorch()
        }

        mBinding.btnSwitchCamera.setOnClickListener {
            cameraManager.changeCameraSelector()
        }

        mBinding.btnSwitchAnalyzer.setOnClickListener {
            cameraManager.changeCameraType(CameraSearchType.Barcode)
        }
    }

    override fun onResume() {
        super.onResume()

        if (hasPermission(Manifest.permission.CAMERA)) {
            cameraManager.startCamera()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        cameraManager.stopCamera()
    }
}

fun Activity.hasPermission(permission: String): Boolean {
    return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
}
