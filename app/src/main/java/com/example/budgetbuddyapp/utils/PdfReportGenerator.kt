package com.example.budgetbuddyapp.utils

import android.content.ContentValues
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.example.budgetbuddyapp.data.model.MonthlyReport
import com.example.budgetbuddyapp.data.relation.TransactionWithCategory
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

object PdfReportGenerator {

    private const val PAGE_WIDTH = 595  // A4 width in points (72dpi)
    private const val PAGE_HEIGHT = 842 // A4 height in points
    private const val MARGIN = 40f

    sealed class ExportResult {
        data class Success(val fileName: String, val displayPath: String) : ExportResult()
        data class Error(val message: String) : ExportResult()
    }

    fun generate(
        context: Context,
        report: MonthlyReport,
        transactions: List<TransactionWithCategory>
    ): ExportResult {
        return try {
            val document = PdfDocument()
            val titlePaint = Paint().apply {
                color = Color.BLACK
                textSize = 18f
                isFakeBoldText = true
            }
            val headerPaint = Paint().apply {
                color = Color.BLACK
                textSize = 12f
                isFakeBoldText = true
            }
            val normalPaint = Paint().apply {
                color = Color.BLACK
                textSize = 10f
            }
            val grayPaint = Paint().apply {
                color = Color.DKGRAY
                textSize = 9f
            }
            val linePaint = Paint().apply {
                color = Color.LTGRAY
                strokeWidth = 1f
            }

            var pageNumber = 1
            var page = document.startPage(
                PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
            )
            var canvas = page.canvas
            var y = MARGIN

            // ===== JUDUL =====
            canvas.drawText("BudgetBuddy Monthly Report", MARGIN, y, titlePaint)
            y += 24f

            // ===== PERIODE =====
            val periodLabel = formatPeriodLabel(report.yearMonth)
            canvas.drawText("Periode: $periodLabel", MARGIN, y, normalPaint)
            y += 24f

            canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, linePaint)
            y += 20f

            canvas.drawText("Ringkasan", MARGIN, y, headerPaint)
            y += 18f

            canvas.drawText("Total Pemasukan: ${CurrencyFormatter.format(report.totalIncome)}", MARGIN, y, normalPaint)
            y += 16f
            canvas.drawText("Total Pengeluaran: ${CurrencyFormatter.format(report.totalExpense)}", MARGIN, y, normalPaint)
            y += 16f
            canvas.drawText("Saldo: ${CurrencyFormatter.format(report.balance)}", MARGIN, y, normalPaint)
            y += 16f
            canvas.drawText("Jumlah Transaksi: ${report.transactionCount}", MARGIN, y, normalPaint)
            y += 16f

            if (report.topExpenseCategoryName != null) {
                canvas.drawText(
                    "Kategori Pengeluaran Terbesar: ${report.topExpenseCategoryName} (${CurrencyFormatter.format(report.topExpenseCategoryAmount ?: 0.0)})",
                    MARGIN, y, normalPaint
                )
                y += 16f
            }

            y += 8f
            canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, linePaint)
            y += 20f

            // ===== DAFTAR TRANSAKSI =====
            canvas.drawText("Daftar Transaksi", MARGIN, y, headerPaint)
            y += 18f

            // Header tabel
            val colDate = MARGIN
            val colCategory = MARGIN + 90f
            val colType = MARGIN + 220f
            val colAmount = MARGIN + 290f
            val colNote = MARGIN + 380f

            canvas.drawText("Tanggal", colDate, y, headerPaint)
            canvas.drawText("Kategori", colCategory, y, headerPaint)
            canvas.drawText("Jenis", colType, y, headerPaint)
            canvas.drawText("Nominal", colAmount, y, headerPaint)
            canvas.drawText("Catatan", colNote, y, headerPaint)
            y += 6f
            canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, linePaint)
            y += 14f

            val dateFormat = SimpleDateFormat("dd/MM/yy", Locale("id", "ID"))

            for (twc in transactions) {
                // Cek apakah perlu halaman baru
                if (y > PAGE_HEIGHT - MARGIN - 30f) {
                    document.finishPage(page)
                    pageNumber++
                    page = document.startPage(
                        PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
                    )
                    canvas = page.canvas
                    y = MARGIN

                    // Ulangi header tabel di halaman baru
                    canvas.drawText("Tanggal", colDate, y, headerPaint)
                    canvas.drawText("Kategori", colCategory, y, headerPaint)
                    canvas.drawText("Jenis", colType, y, headerPaint)
                    canvas.drawText("Nominal", colAmount, y, headerPaint)
                    canvas.drawText("Catatan", colNote, y, headerPaint)
                    y += 6f
                    canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, linePaint)
                    y += 14f
                }

                val t = twc.transaction
                val catName = twc.category?.name ?: "Lainnya"
                val jenis = if (t.type == "INCOME") "Masuk" else "Keluar"
                val nominal = CurrencyFormatter.formatShort(t.amount)
                val note = if (t.description.length > 18) t.description.take(18) + "…" else t.description.ifEmpty { "-" }

                canvas.drawText(dateFormat.format(Date(t.date)), colDate, y, normalPaint)
                canvas.drawText(truncate(catName, 18), colCategory, y, normalPaint)
                canvas.drawText(jenis, colType, y, normalPaint)
                canvas.drawText(nominal, colAmount, y, normalPaint)
                canvas.drawText(note, colNote, y, normalPaint)

                y += 16f
            }

            if (transactions.isEmpty()) {
                canvas.drawText("Tidak ada transaksi pada periode ini.", MARGIN, y, grayPaint)
                y += 16f
            }

            // ===== FOOTER (di halaman terakhir) =====
            y += 16f
            if (y > PAGE_HEIGHT - MARGIN - 40f) {
                document.finishPage(page)
                pageNumber++
                page = document.startPage(
                    PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
                )
                canvas = page.canvas
                y = MARGIN
            }

            canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, linePaint)
            y += 16f
            canvas.drawText("Total Transaksi: ${transactions.size}", MARGIN, y, normalPaint)
            y += 14f

            val exportDateFormat = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("id", "ID"))
            canvas.drawText("Tanggal Export: ${exportDateFormat.format(Date())}", MARGIN, y, grayPaint)

            document.finishPage(page)

            // ===== SIMPAN FILE =====
            val fileName = "BudgetBuddy_Report_${report.yearMonth}.pdf"
            val savedPath = savePdfToDocuments(context, document, fileName)
            document.close()

            if (savedPath != null) {
                ExportResult.Success(fileName, savedPath)
            } else {
                ExportResult.Error("Gagal menyimpan file PDF")
            }
        } catch (e: Exception) {
            ExportResult.Error(e.message ?: "Terjadi kesalahan saat membuat PDF")
        }
    }

    private fun truncate(text: String, maxLen: Int): String {
        return if (text.length > maxLen) text.take(maxLen) + "…" else text
    }

    private fun formatPeriodLabel(yearMonth: String): String {
        return try {
            val parts = yearMonth.split("-")
            val cal = Calendar.getInstance()
            cal.set(parts[0].toInt(), parts[1].toInt() - 1, 1)
            SimpleDateFormat("MMMM yyyy", Locale("id", "ID")).format(cal.time)
        } catch (e: Exception) {
            yearMonth
        }
    }

    private fun savePdfToDocuments(context: Context, document: PdfDocument, fileName: String): String? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            saveViaMediaStore(context, document, fileName)
        } else {
            saveViaLegacyStorage(document, fileName)
        }
    }

    private fun saveViaMediaStore(context: Context, document: PdfDocument, fileName: String): String? {
        val resolver = context.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS)
        }

        val uri = resolver.insert(MediaStore.Files.getContentUri("external"), contentValues)
            ?: return null

        var outputStream: OutputStream? = null
        return try {
            outputStream = resolver.openOutputStream(uri)
            if (outputStream != null) {
                document.writeTo(outputStream)
                "Documents/$fileName"
            } else {
                null
            }
        } catch (e: Exception) {
            null
        } finally {
            outputStream?.close()
        }
    }

    private fun saveViaLegacyStorage(document: PdfDocument, fileName: String): String? {
        return try {
            val documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
            if (!documentsDir.exists()) {
                documentsDir.mkdirs()
            }
            val file = File(documentsDir, fileName)
            val outputStream = FileOutputStream(file)
            document.writeTo(outputStream)
            outputStream.close()
            file.absolutePath
        } catch (e: Exception) {
            null
        }
    }
}