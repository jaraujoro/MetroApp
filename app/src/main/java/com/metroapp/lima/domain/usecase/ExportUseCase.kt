package com.metroapp.lima.domain.usecase

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.metroapp.lima.domain.model.Card
import com.metroapp.lima.domain.model.MetroTransaction
import com.metroapp.lima.util.CurrencyUtils
import com.metroapp.lima.util.DateUtils
import java.io.File
import java.io.FileOutputStream

class ExportUseCase(private val context: Context) {

    fun exportToCsv(transactions: List<MetroTransaction>, cardsMap: Map<Long, Card>): File {
        val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val file = File(exportDir, "movimientos_metro_lima_${System.currentTimeMillis()}.csv")

        file.printWriter().use { out ->
            out.println("ID,Tarjeta,Fecha,Hora,Tipo,Concepto,Importe,Saldo Resultante,Estado,Origen,Notas")
            for (tx in transactions) {
                val cardName = cardsMap[tx.cardId]?.name ?: "Tarjeta #${tx.cardId}"
                val date = DateUtils.formatDate(tx.timestamp)
                val time = DateUtils.formatTime(tx.timestamp)
                val type = tx.type.displayName
                val description = tx.description.replace(",", ";")
                val amount = CurrencyUtils.formatCents(tx.amountCents).replace(",", ".")
                val resulting = CurrencyUtils.formatCents(tx.resultingBalanceCents).replace(",", ".")
                val status = tx.status.displayName
                val origin = tx.origin.displayName
                val notes = (tx.notes ?: "").replace(",", ";")

                out.println("${tx.id},\"$cardName\",$date,$time,\"$type\",\"$description\",$amount,$resulting,\"$status\",\"$origin\",\"$notes\"")
            }
        }
        return file
    }

    fun exportToPdf(
        transactions: List<MetroTransaction>,
        cardsMap: Map<Long, Card>,
        totalSpentCents: Long,
        totalTopUpCents: Long
    ): File {
        val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val file = File(exportDir, "reporte_metro_lima_${System.currentTimeMillis()}.pdf")

        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val titlePaint = Paint().apply {
            color = Color.rgb(0, 128, 77) // Metro Lima emerald green
            textSize = 18f
            isFakeBoldText = true
        }

        val subtitlePaint = Paint().apply {
            color = Color.DKGRAY
            textSize = 11f
        }

        val headerPaint = Paint().apply {
            color = Color.BLACK
            textSize = 10f
            isFakeBoldText = true
        }

        val textPaint = Paint().apply {
            color = Color.BLACK
            textSize = 9f
        }

        val linePaint = Paint().apply {
            color = Color.LTGRAY
            strokeWidth = 1f
        }

        // Header
        canvas.drawText("LÍNEA 1 DEL METRO DE LIMA - REPORTE CONTABLE", 40f, 50f, titlePaint)
        val dateNow = DateUtils.formatDateTime(System.currentTimeMillis())
        canvas.drawText("Generado el: $dateNow", 40f, 70f, subtitlePaint)

        // Summary box
        canvas.drawRect(40f, 85f, 555f, 135f, Paint().apply { color = Color.rgb(240, 248, 244) })
        canvas.drawText("Total Gastado: ${CurrencyUtils.formatCents(totalSpentCents)}", 50f, 105f, headerPaint)
        canvas.drawText("Total Recargado: ${CurrencyUtils.formatCents(totalTopUpCents)}", 220f, 105f, headerPaint)
        canvas.drawText("Movimientos listados: ${transactions.size}", 390f, 105f, headerPaint)

        // Table Header
        var y = 160f
        canvas.drawText("Fecha", 40f, y, headerPaint)
        canvas.drawText("Tarjeta", 110f, y, headerPaint)
        canvas.drawText("Tipo", 200f, y, headerPaint)
        canvas.drawText("Importe", 320f, y, headerPaint)
        canvas.drawText("Saldo", 400f, y, headerPaint)
        canvas.drawText("Estado", 480f, y, headerPaint)
        y += 8f
        canvas.drawLine(40f, y, 555f, y, linePaint)
        y += 16f

        // Rows (up to ~30 rows per page in this summary)
        for (tx in transactions.take(30)) {
            val cardName = cardsMap[tx.cardId]?.name?.take(14) ?: "#${tx.cardId}"
            val date = DateUtils.formatDate(tx.timestamp)
            val type = tx.type.displayName.take(18)
            val amount = CurrencyUtils.formatCents(tx.amountCents)
            val balance = CurrencyUtils.formatCents(tx.resultingBalanceCents)
            val status = tx.status.displayName

            canvas.drawText(date, 40f, y, textPaint)
            canvas.drawText(cardName, 110f, y, textPaint)
            canvas.drawText(type, 200f, y, textPaint)
            canvas.drawText(amount, 320f, y, textPaint)
            canvas.drawText(balance, 400f, y, textPaint)
            canvas.drawText(status, 480f, y, textPaint)

            y += 18f
            if (y > 800f) break
        }

        pdfDocument.finishPage(page)

        FileOutputStream(file).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()

        return file
    }

    fun shareFile(file: File, mimeType: String) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val chooser = Intent.createChooser(shareIntent, "Compartir Reporte del Metro").apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(chooser)
    }
}
