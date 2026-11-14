package com.example.shared.domain.platform


import android.graphics.Color
import android.graphics.pdf.PdfDocument
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import androidx.core.graphics.withTranslation

class AndroidPdfTextWriter : PdfTextWriter {

    override suspend fun write(text: String): ByteArray = withContext(Dispatchers.Default) {
        val pageWidth = 595                 // A4 in 1/72 Zoll
        val pageHeight = 842
        val margin = 56
        val contentWidth = pageWidth - 2 * margin
        val contentHeight = pageHeight - 2 * margin - 16     // Platz für die Seitenzahl

        val paint = TextPaint().apply { isAntiAlias = true; textSize = 11f; color = Color.BLACK }
        val footerPaint = TextPaint().apply { isAntiAlias = true; textSize = 9f; color = Color.DKGRAY }
        val layout = StaticLayout.Builder.obtain(text, 0, text.length, paint, contentWidth)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(2f, 1f)
            .build()

        val document = PdfDocument()
        try {
            var line = 0
            var pageNumber = 1
            while (line < layout.lineCount) {
                // So viele Zeilen nehmen, wie auf die Seite passen
                val top = layout.getLineTop(line)
                var end = line
                while (end < layout.lineCount && layout.getLineBottom(end) - top <= contentHeight) end++
                if (end == line) end = line + 1
                val bottom = layout.getLineBottom(end - 1)

                val page = document.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
                val canvas = page.canvas
                canvas.withTranslation(margin.toFloat(), margin.toFloat() - top) {
                    clipRect(0f, top.toFloat(), contentWidth.toFloat(), bottom.toFloat())
                    layout.draw(this)
                }
                canvas.drawText("Seite $pageNumber", margin.toFloat(), (pageHeight - margin / 2).toFloat(), footerPaint)
                document.finishPage(page)

                line = end
                pageNumber++
            }
            ByteArrayOutputStream().also { document.writeTo(it) }.toByteArray()
        } finally {
            document.close()
        }
    }
}