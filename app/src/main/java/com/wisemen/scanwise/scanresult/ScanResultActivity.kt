package com.wisemen.scanwise.scanresult

import android.Manifest
import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import com.appwise.scanner.barcode.BarcodeTarget
import com.appwise.scanner.base.CameraManager
import com.appwise.scanner.base.TargetOverlay
import com.appwise.scanner.qr.QRCodeTarget
import com.wisemen.scanwise.BuildConfig
import com.wisemen.scanwise.R
import com.wisemen.scanwise.databinding.ActivityScanResultBinding
import com.wisemen.scanwise.hasPermission
import com.wisemen.scanwise.scanresult.ScanCodeResultContract.Companion.SCAN_RESULT_CONFIG

class ScanResultActivity : AppCompatActivity() {

    private var mScanResultConfig: ScanCodeResultContract.ScanResultConfig? = null
    private lateinit var mBinding: ActivityScanResultBinding

    private val requestCameraPermissions = registerForActivityResult(ActivityResultContracts.RequestPermission()) { permissionGranted ->
        if (permissionGranted) {
            cameraManager.startCamera()
        }
    }

    private lateinit var cameraManager: CameraManager

    override fun onCreate(savedInstanceState: Bundle?) {
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_scan_result)
        mBinding.lifecycleOwner = this
        super.onCreate(savedInstanceState)

        mScanResultConfig = intent.getParcelableExtra(SCAN_RESULT_CONFIG) as ScanCodeResultContract.ScanResultConfig?

        mScanResultConfig?.cameraSearchType?.let {
            cameraManager = CameraManager(
                this,
                mBinding.previewView,
                this,
                { mBinding.overlay }, // This is done with a high-order-function because of changing the scan Analyzer
                it
            ).apply {
                showTargetBoxes = BuildConfig.DEBUG
                setCameraManagerListener(object : CameraManager.CameraManagerListener {
                    override fun targetsFound(targets: List<TargetOverlay.Target>) {
                        if (targets.isEmpty()) return
                        val code = when (val target = targets.firstOrNull()) {
                            is QRCodeTarget -> target.barCode.displayValue
                            is BarcodeTarget -> target.barCode.displayValue
                            else -> {
                                null
                            }
                        }
                        if (mScanResultConfig?.autoReturnScanResult == true)
                            setResult(code)
                        else
                            Log.d("TAG", "targetsFound: $code")
                    }
                })
            }
            requestCameraPermissions.launch(Manifest.permission.CAMERA)
        }

        /* mBinding.btnFlashlight.setOnClickListener {
             cameraManager.toggleTorch()
         }

         mBinding.btnSwitchCamera.setOnClickListener {
             cameraManager.changeCameraSelector()
         }

         mBinding.btnSwitchAnalyzer.setOnClickListener {
             cameraManager.changeCameraType(CameraSearchType.Barcode)
         }*/
    }

    private fun setResult(code: String?) {
        val scanCodeResultIntent = Intent().putExtra(ScanCodeResultContract.SCAN_CODE_RESULT, ScanCodeResult(code))
        setResult(Activity.RESULT_OK, scanCodeResultIntent)
        finish()
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



