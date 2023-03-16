package com.wisemen.scanwise.scanresult

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Parcelable
import androidx.activity.result.contract.ActivityResultContract
import com.appwise.scanner.CameraSearchType
import kotlinx.parcelize.Parcelize

class ScanCodeResultContract() : ActivityResultContract<ScanCodeResultContract.ScanResultConfig, ScanCodeResult?>() {

    companion object {
        const val SCAN_RESULT_CONFIG = "scan_result_config"
        const val SCAN_CODE_RESULT = "scan_code_result"
    }

    @Parcelize
    data class ScanResultConfig(
        val autoReturnScanResult : Boolean,
        val cameraSearchType: CameraSearchType
    ) : Parcelable

    override fun createIntent(context: Context, input: ScanResultConfig) = Intent(context, ScanResultActivity::class.java).putExtra(SCAN_RESULT_CONFIG, input)

    override fun parseResult(resultCode: Int, intent: Intent?): ScanCodeResult? {
        if (resultCode != Activity.RESULT_OK) {
            return null
        }
        //use deprecated function because newer version is required api 33
        return intent?.getParcelableExtra(SCAN_CODE_RESULT) as ScanCodeResult?
    }
}