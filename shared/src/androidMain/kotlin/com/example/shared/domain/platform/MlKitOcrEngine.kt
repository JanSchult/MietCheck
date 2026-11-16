package com.example.shared.domain.platform


import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.exifinterface.media.ExifInterface
import com.example.shared.domain.model.OcrText
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.math.abs
import kotlin.math.max
import androidx.core.graphics.createBitmap

class MlKitOcrEngine(context: Context) : OcrEngine {

    private val appContext = context.applicationContext
    private val recognizer by lazy { TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS) }

    override val isAvailable: Boolean = true

    override suspend fun recognize(bytes: ByteArray, mimeType: String): OcrText = withContext(Dispatchers.Default) {
        if (mimeType == "application/pdf") recognizePdf(bytes) else recognizeImage(bytes)
    }

    private suspend fun recognizeImage(bytes: ByteArray): OcrText {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
        var sample = 1
        while (max(bounds.outWidth, bounds.outHeight) / sample > MAX_IMAGE_SIDE) sample *= 2

        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, BitmapFactory.Options().apply { inSampleSize = sample })
            ?: error("Das Bild konnte nicht gelesen werden.")
        try {
            val result = process(InputImage.fromBitmap(bitmap, exifRotation(bytes)))
            return OcrText(rowsOf(result), pagesRead = 1, pagesTotal = 1)
        } finally {
            bitmap.recycle()
        }
    }

    /** PDF-Seiten werden zu Bildern gerendert und erkannt. Die temporäre Datei wird immer gelöscht. */
    private suspend fun recognizePdf(bytes: ByteArray): OcrText {
        val dir = File(appContext.cacheDir, "ocr").apply { mkdirs() }
        dir.listFiles()?.forEach { it.delete() }
        val temp = File(dir, "bill-${System.currentTimeMillis()}.pdf")
        temp.writeBytes(bytes)
        try {
            ParcelFileDescriptor.open(temp, ParcelFileDescriptor.MODE_READ_ONLY).use { pfd ->
                val renderer = PdfRenderer(pfd)
                try {
                    val total = renderer.pageCount
                    val read = minOf(total, MAX_PDF_PAGES)
                    val rows = mutableListOf<String>()
                    for (index in 0 until read) {
                        val bitmap = renderer.openPage(index).use { page ->
                            val scale = PDF_TARGET_WIDTH.toFloat() / page.width
                            createBitmap(
                                PDF_TARGET_WIDTH,
                                (page.height * scale).toInt().coerceAtLeast(1)
                            ).also {
                                it.eraseColor(Color.WHITE)                       // PdfRenderer zeichnet auf transparent
                                page.render(it, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                            }
                        }
                        try {
                            rows += rowsOf(process(InputImage.fromBitmap(bitmap, 0)))
                        } finally {
                            bitmap.recycle()
                        }
                    }
                    return OcrText(rows, pagesRead = read, pagesTotal = total)
                } finally {
                    renderer.close()
                }
            }
        } finally {
            temp.delete()
        }
    }

    private suspend fun process(image: InputImage): Text = suspendCancellableCoroutine { continuation ->
        recognizer.process(image)
            .addOnSuccessListener { continuation.resume(it) }
            .addOnFailureListener { continuation.resumeWithException(it) }
    }

    /** Fasst Textstücke mit gleicher Höhe zu einer Zeile zusammen, Spalten durch Tab getrennt. */
    private fun rowsOf(text: Text): List<String> {
        class Piece(val text: String, val left: Int, val centerY: Float, val height: Int)

        val pieces = text.textBlocks.flatMap { it.lines }.mapNotNull { line ->
            line.boundingBox?.let { Piece(line.text, it.left, it.exactCenterY(), it.height()) }
        }.sortedBy { it.centerY }

        val rows = mutableListOf<MutableList<Piece>>()
        for (piece in pieces) {
            val current = rows.lastOrNull()
            if (current != null) {
                val centerY = current.map { it.centerY }.average()
                val height = current.map { it.height }.average()
                if (abs(piece.centerY - centerY) < height * 0.55) {
                    current.add(piece)
                    continue
                }
            }
            rows.add(mutableListOf(piece))
        }
        return rows.map { row -> row.sortedBy { it.left }.joinToString("\t") { it.text.trim() } }
    }

    private fun exifRotation(bytes: ByteArray): Int = runCatching {
        when (ExifInterface(ByteArrayInputStream(bytes))
            .getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            ExifInterface.ORIENTATION_ROTATE_270 -> 270
            else -> 0
        }
    }.getOrDefault(0)

    private companion object {
        const val MAX_IMAGE_SIDE = 3000
        const val PDF_TARGET_WIDTH = 1800
        const val MAX_PDF_PAGES = 8
    }
}