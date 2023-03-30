package com.appwise.scanner.scanresult

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Parcelable
import androidx.activity.result.contract.ActivityResultContract
import com.appwise.scanner.CameraSearchType
import kotlinx.parcelize.Parcelize

class ScanCodeResultContract() : ActivityResultContract<ScanResultConfig, ScanCodeResult?>() {

    companion object {
        const val SCAN_RESULT_CONFIG = "scan_result_config"
        const val SCAN_CODE_RESULT = "scan_code_result"
    }

    override fun createIntent(context: Context, input: ScanResultConfig) = Intent(context, ScanResultActivity::class.java).putExtra(SCAN_RESULT_CONFIG, input)

    override fun parseResult(resultCode: Int, intent: Intent?): ScanCodeResult? {
        if (resultCode != Activity.RESULT_OK) {
            return null
        }
        //use deprecated function because newer version is required api 33
        return intent?.getParcelableExtra(SCAN_CODE_RESULT) as ScanCodeResult?
    }
}