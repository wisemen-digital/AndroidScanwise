package com.appwise.scanner.scanresult

import android.os.Parcelable
import com.appwise.scanner.CameraSearchType
import kotlinx.parcelize.Parcelize

@Parcelize
data class ScanResultConfig(
    val autoReturnScanResult: Boolean,
    val cameraSearchType: CameraSearchType,
    val switchAnalyzerEnabled: Boolean = false,
    val toggleFlashLightEnabled: Boolean = false,
    val switchCameraEnabled: Boolean = false,
    val scanFilter : String
) : Parcelable