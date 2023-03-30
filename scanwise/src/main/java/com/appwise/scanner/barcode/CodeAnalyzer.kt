package com.appwise.scanner.barcode

import android.util.Log
import com.appwise.scanner.FilterResult
import com.appwise.scanner.base.BaseAnalyzer
import com.appwise.scanner.base.TargetOverlay
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.io.IOException

class CodeAnalyzer(
    overlay: () -> TargetOverlay,
    override val isFrontLens: Boolean,
    override val showTargetBoxes: Boolean,
    onValueFound: (List<TargetOverlay.Target>) -> Unit,
    override val filterResult: FilterResult?,
) : BaseAnalyzer<List<Barcode>>(overlay, onValueFound) {

    // Make sure only the typical barcodes can be scanned
    private val options = BarcodeScannerOptions.Builder()
        .setBarcodeFormats(
            Barcode.FORMAT_CODE_128,
            Barcode.FORMAT_CODE_39,
            Barcode.FORMAT_CODE_93,
            Barcode.FORMAT_CODABAR,
            Barcode.FORMAT_EAN_13,
            Barcode.FORMAT_EAN_8,
            Barcode.FORMAT_ITF,
            Barcode.FORMAT_UPC_A,
            Barcode.FORMAT_UPC_E
        )
        .build()

    private val scanner = BarcodeScanning.getClient(options)

    override fun searchInImage(image: InputImage) = scanner.process(image)

    override fun stop() {
        try {
            scanner.close()
        } catch (e: IOException) {
            Log.e("Exception while trying to close Barcode Scanner", e.message, e)
        }
    }

    override fun onFailure(e: Exception) {
        Log.w("Camera x barcode scan failed", e.message ?: "")
    }

    override fun getTargets(results: List<Barcode>, width: Int, height: Int) =
        results.filter { filterResult?.filterScannerResult(it.rawValue) ?: true }
            .map { BarcodeTarget(overlay(), it, it.boundingBox!!.transform(width, height)) }
}