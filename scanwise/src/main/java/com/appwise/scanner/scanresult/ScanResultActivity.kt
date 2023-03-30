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
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import com.appwise.scanner.BuildConfig
import com.appwise.scanner.CameraSearchType
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

        mScanResultConfig?.let {
            cameraManager = CameraManager(
                this,
                mBinding.previewView,
                this,
                { mBinding.overlay }, // This is done with a high-order-function because of changing the scan Analyzer
                it.cameraSearchType,
                it.filterResult
            ).apply {
                showTargetBoxes = BuildConfig.DEBUG
                setCameraManagerListener(object : CameraManager.CameraManagerListener {
                    override fun analyzerChanged(cameraSearchType: CameraSearchType) {

                    }

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

                    override fun flashLightStateChanged(flashlightOn: Boolean) {
                        mBinding.ivFlashLight.setImageResource(if(flashlightOn) R.drawable.baseline_flash_on_24 else R.drawable.baseline_flash_off_24)
                    }
                })
            }
        }
        cameraManager.start()

         mBinding.ivFlashLight.setOnClickListener {
             cameraManager.toggleTorch()
         }

         mBinding.ivSwitchCamera.setOnClickListener {
             cameraManager.changeCameraSelector()
         }

         mBinding.ivSwitchAnalyzer.setOnClickListener {
             cameraManager.changeCameraType(CameraSearchType.Barcode)
         }

        manageConfig(mScanResultConfig)
    }

    private fun manageConfig(mScanResultConfig: ScanResultConfig?) {
        with(mBinding){
            ivFlashLight.isVisible = mScanResultConfig?.toggleFlashLightEnabled == true
            ivSwitchCamera.isVisible = mScanResultConfig?.switchCameraEnabled == true
            ivSwitchAnalyzer.isVisible = mScanResultConfig?.switchAnalyzerEnabled == true
        }
    }

    private fun setResult(code: String?) {
        val scanCodeResultIntent = Intent().putExtra(ScanCodeResultContract.SCAN_CODE_RESULT, ScanCodeResult(code))
        setResult(Activity.RESULT_OK, scanCodeResultIntent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraManager.stopCamera()
    }
}



