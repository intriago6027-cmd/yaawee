package com.bingo.manager.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import com.bingo.manager.domain.model.Compra
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.util.*

/**
 * Genera un PDF de lista de compras usando android.graphics.pdf.PdfDocument (nativo, sin deps extra).
 */
object PdfGenerator {

    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("es", "CO"))

    fun generarComprasPdf(context: Context, compra: Compra): File {
        val pdfDoc = PdfDocument()
        val pageWidth = 595   // A4 ancho en puntos
        val pageHeight = 842  // A4 alto en puntos
        val margin = 40f
        val lineHeight = 22f

        var pageNum = 1
        var yPos = margin + 60f
        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum).create()
        var page = pdfDoc.startPage(pageInfo)
        var canvas: Canvas = page.canvas

        // ─── Pinta de cabecera ───
        fun drawHeader() {
            val paint = Paint().apply { color = Color.rgb(63, 81, 181); style = Paint.Style.FILL }
            canvas.drawRect(0f, 0f, pageWidth.toFloat(), 80f, paint)

            val titlePaint = Paint().apply {
                color = Color.WHITE; textSize = 22f; isFakeBoldText = true
            }
            canvas.drawText("🎱 BINGO MANAGER — LISTA DE COMPRAS", margin, 35f, titlePaint)

            val subPaint = Paint().apply { color = Color.WHITE; textSize = 13f }
            canvas.drawText("Fecha del Bingo: ${compra.fechaBingo}   |   Generado: ${compra.fechaGeneracion}", margin, 60f, subPaint)
        }

        // ─── Columnas ───
        fun drawTableHeader(c: Canvas, y: Float) {
            val bgPaint = Paint().apply { color = Color.rgb(232, 234, 246); style = Paint.Style.FILL }
            c.drawRect(margin, y - 16f, pageWidth - margin, y + 6f, bgPaint)

            val p = Paint().apply { color = Color.rgb(26, 35, 126); textSize = 11f; isFakeBoldText = true }
            c.drawText("PRODUCTO", margin + 4, y, p)
            c.drawText("CANT. NECES.", 230f, y, p)
            c.drawText("INVENTARIO", 330f, y, p)
            c.drawText("A COMPRAR", 420f, y, p)
            c.drawText("TOTAL", 505f, y, p)
        }

        // ─── Nueva página ───
        fun newPage() {
            pdfDoc.finishPage(page)
            pageNum++
            pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum).create()
            page = pdfDoc.startPage(pageInfo)
            canvas = page.canvas
            yPos = margin + 30f
            drawTableHeader(canvas, yPos)
            yPos += lineHeight
        }

        drawHeader()
        yPos = 100f
        drawTableHeader(canvas, yPos)
        yPos += lineHeight

        val rowPaint = Paint().apply { textSize = 11f; color = Color.DKGRAY }
        val altPaint = Paint().apply { color = Color.rgb(245, 245, 245); style = Paint.Style.FILL }
        val linePaint = Paint().apply { color = Color.LTGRAY; strokeWidth = 0.5f }

        compra.items.forEachIndexed { index, item ->
            if (yPos > pageHeight - 80) newPage()

            // Fila alternada
            if (index % 2 == 0) canvas.drawRect(margin, yPos - 14f, pageWidth - margin, yPos + 6f, altPaint)

            canvas.drawText(item.nombreProducto.take(22), margin + 4, yPos, rowPaint)
            canvas.drawText("${formatNum(item.cantidadNecesaria)} ${item.unidad}", 230f, yPos, rowPaint)
            canvas.drawText("${formatNum(item.cantidadInventario)} ${item.unidad}", 330f, yPos, rowPaint)
            canvas.drawText("${formatNum(item.cantidadReal)} ${item.unidad}", 420f, yPos, rowPaint)

            val totalPaint = Paint().apply {
                textSize = 11f
                color = if (item.cantidadReal > 0) Color.rgb(183, 28, 28) else Color.rgb(46, 125, 50)
                isFakeBoldText = item.cantidadReal > 0
            }
            canvas.drawText(currencyFormat.format(item.subtotal), 495f, yPos, totalPaint)
            canvas.drawLine(margin, yPos + 6f, (pageWidth - margin).toFloat(), yPos + 6f, linePaint)
            yPos += lineHeight
        }

        // ─── Pie de total ───
        if (yPos > pageHeight - 80) newPage()
        yPos += 10f
        val totalBgPaint = Paint().apply { color = Color.rgb(63, 81, 181); style = Paint.Style.FILL }
        canvas.drawRect(margin, yPos - 16f, (pageWidth - margin).toFloat(), yPos + 10f, totalBgPaint)
        val totalTextPaint = Paint().apply { color = Color.WHITE; textSize = 14f; isFakeBoldText = true }
        canvas.drawText("TOTAL GENERAL:", margin + 4, yPos, totalTextPaint)
        canvas.drawText(currencyFormat.format(compra.total), 430f, yPos, totalTextPaint)

        pdfDoc.finishPage(page)

        // ─── Guardar archivo ───
        val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            ?: context.filesDir
        dir.mkdirs()
        val filename = "compras_bingo_${compra.fechaBingo.replace("/", "-")}.pdf"
        val file = File(dir, filename)
        FileOutputStream(file).use { pdfDoc.writeTo(it) }
        pdfDoc.close()
        return file
    }

    private fun formatNum(n: Double): String =
        if (n == n.toLong().toDouble()) n.toLong().toString()
        else String.format("%.2f", n)
}
