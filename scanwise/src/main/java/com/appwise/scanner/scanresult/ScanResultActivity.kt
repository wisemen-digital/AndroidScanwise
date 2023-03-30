package com.appwise.scanner.scanresult

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.appwise.scanner.BuildConfig
import com.appwise.scanner.R
import com.appwise.scanner.barcode.BarcodeTarget
import com.appwise.scanner.base.CameraManager
import com.appwise.scanner.base.TargetOverlay
import com.appwise.scanner.databinding.ActivityScanResultBinding
import com.appwise.scanner.qr.QRCodeTarget
import com.appwise.scanner.scanresult.ScanCodeResultContract.Companion.SCAN_RESULT_CONFIG

class ScanResultActivity : AppCompatActivity() {

    private var mScanResultConfig: ScanResultConfig? = null
    private lateinit var mBinding: ActivityScanResultBinding

    private lateinit var cameraManager: CameraManager

    override fun onCreate(savedInstanceState: Bundle?) {
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_scan_result)
        mBinding.lifecycleOwner = this
        super.onCreate(savedInstanceState)

        mScanResultConfig = intent.getParcelableExtra(SCAN_RESULT_CONFIG) as ScanResultConfig?

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

    fun Activity.hasPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
    }
}



