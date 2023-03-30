package com.appwise.scanner.text

import android.util.Log
import com.appwise.scanner.FilterResult
import com.appwise.scanner.base.BaseAnalyzer
import com.appwise.scanner.base.TargetOverlay
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.IOException

class TextAnalyzer(
    overlay: () -> TargetOverlay,
    override val isFrontLens: Boolean,
    override val showTargetBoxes: Boolean,
    onValueFound: (List<TargetOverlay.Target>) -> Unit,
    override val filterResult: FilterResult?,
) : BaseAnalyzer<Text>(overlay, onValueFound) {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    override fun searchInImage(image: InputImage) = recognizer.process(image)

    override fun stop() {
        try {
            recognizer.close()
        } catch (e: IOException) {
            Log.e("Exception while trying to close Barcode Scanner", e.message, e)
        }
    }

    override fun onFailure(e: Exception) {
        Log.w("Camera x barcode scan failed", e.message ?: "")
    }

    override fun getTargets(results: Text, width: Int, height: Int) =
        results.textBlocks.filter { filterResult?.filterScannerResult(it.text) ?: true }
            .map { TextTarget(overlay(), it, it.boundingBox!!.transform(width, height)) }
}