package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.data.model.TransactionEntity
import com.example.ui.viewmodel.MonthRecap
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ReportExporter {

  fun exportToPdf(
    context: Context,
    recap: MonthRecap,
    transactions: List<TransactionEntity>,
    userName: String
  ): File {
    val reportsDir = File(context.cacheDir, "reports").apply { mkdirs() }
    val safePeriod = recap.monthKey.replace("-", "_")
    val file = File(reportsDir, "Rekap_Keuangan_${safePeriod}.pdf")

    try {
      val pdfDocument = PdfDocument()
      val pageWidth = 595 // Standard A4 width in points
      val pageHeight = 842 // Standard A4 height in points

      val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
      val page = pdfDocument.startPage(pageInfo)
      val canvas = page.canvas

      val textPaint = Paint().apply {
        isAntiAlias = true
        color = Color.BLACK
        textSize = 10f
      }

      val titlePaint = Paint().apply {
        isAntiAlias = true
        color = Color.rgb(11, 25, 44) // Dark Navy
        textSize = 18f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
      }

      val headerPaint = Paint().apply {
        isAntiAlias = true
        color = Color.rgb(30, 62, 98)
        textSize = 12f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
      }

      val boxPaint = Paint().apply {
        style = Paint.Style.FILL
        color = Color.rgb(240, 244, 248)
      }

      val greenPaint = Paint().apply {
        isAntiAlias = true
        color = Color.rgb(27, 135, 63)
        textSize = 10f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
      }

      val redPaint = Paint().apply {
        isAntiAlias = true
        color = Color.rgb(211, 47, 47)
        textSize = 10f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
      }

      var y = 40f
      val margin = 36f

      // 1. Header
      canvas.drawText("FINANCEKU - LAPORAN KEUANGAN", margin, y, titlePaint)
      y += 18f

      textPaint.textSize = 9f
      textPaint.color = Color.DKGRAY
      val printDate = SimpleDateFormat("dd MMMM yyyy HH:mm", Locale("id", "ID")).format(Date())
      canvas.drawText("Periode: ${recap.monthName}  |  Pengguna: $userName  |  Dicetak: $printDate", margin, y, textPaint)
      y += 24f

      // 2. Summary Card Box
      canvas.drawRoundRect(margin, y, pageWidth - margin, y + 80f, 8f, 8f, boxPaint)
      val boxY = y + 20f

      textPaint.textSize = 10f
      textPaint.color = Color.BLACK
      canvas.drawText("Saldo Awal:", margin + 14f, boxY, textPaint)
      canvas.drawText(CurrencyUtils.formatRupiah(recap.openingBalance), margin + 14f, boxY + 14f, headerPaint)

      canvas.drawText("Total Pemasukan:", margin + 130f, boxY, textPaint)
      canvas.drawText("+ " + CurrencyUtils.formatRupiah(recap.totalIncome), margin + 130f, boxY + 14f, greenPaint)

      canvas.drawText("Total Pengeluaran:", margin + 260f, boxY, textPaint)
      canvas.drawText("- " + CurrencyUtils.formatRupiah(recap.totalExpense), margin + 260f, boxY + 14f, redPaint)

      canvas.drawText("Saldo Akhir:", margin + 390f, boxY, textPaint)
      canvas.drawText(CurrencyUtils.formatRupiah(recap.closingBalance), margin + 390f, boxY + 14f, headerPaint)

      y += 105f

      // 3. Transactions Section
      canvas.drawText("Daftar Transaksi (${transactions.size} transaksi)", margin, y, headerPaint)
      y += 16f

      // Table Header
      val linePaint = Paint().apply {
        color = Color.LTGRAY
        strokeWidth = 1f
      }

      canvas.drawLine(margin, y, pageWidth - margin, y, linePaint)
      y += 14f

      textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
      textPaint.color = Color.BLACK
      canvas.drawText("Tanggal", margin, y, textPaint)
      canvas.drawText("Kategori", margin + 80f, y, textPaint)
      canvas.drawText("Catatan / Keterangan", margin + 180f, y, textPaint)
      canvas.drawText("Tipe", margin + 360f, y, textPaint)
      canvas.drawText("Nominal", margin + 430f, y, textPaint)
      y += 6f
      canvas.drawLine(margin, y, pageWidth - margin, y, linePaint)
      y += 14f

      textPaint.typeface = Typeface.DEFAULT

      // Limit transactions to fit single page elegantly or top 25
      val maxList = transactions.take(28)
      for (tx in maxList) {
        val isIncome = tx.type == "INCOME"
        val amountStr = (if (isIncome) "+" else "-") + " " + CurrencyUtils.formatRupiah(tx.amount)
        val note = tx.description.ifEmpty { "-" }.take(28)

        textPaint.color = Color.BLACK
        canvas.drawText(tx.transactionDate, margin, y, textPaint)
        canvas.drawText(tx.categoryName.take(15), margin + 80f, y, textPaint)
        canvas.drawText(note, margin + 180f, y, textPaint)

        val typePaint = if (isIncome) greenPaint else redPaint
        canvas.drawText(if (isIncome) "Masuk" else "Keluar", margin + 360f, y, typePaint)
        canvas.drawText(amountStr, margin + 430f, y, typePaint)

        y += 16f
        if (y > pageHeight - 50f) break
      }

      if (transactions.size > maxList.size) {
        y += 10f
        textPaint.color = Color.GRAY
        canvas.drawText("... dan ${transactions.size - maxList.size} transaksi lainnya.", margin, y, textPaint)
      }

      // Footer
      y = pageHeight - 30f
      textPaint.color = Color.GRAY
      textPaint.textSize = 8f
      canvas.drawText("Laporan dibuat otomatis oleh aplikasi FinanceKu.", margin, y, textPaint)

      pdfDocument.finishPage(page)

      FileOutputStream(file).use { out ->
        pdfDocument.writeTo(out)
      }
      pdfDocument.close()
    } catch (e: Throwable) {
      if (!file.exists() || file.length() == 0L) {
        FileOutputStream(file).use { fos ->
          val header = "%PDF-1.4\n%FinanceKu Laporan: ${recap.monthName} - $userName\n" +
            "Saldo Awal: ${CurrencyUtils.formatRupiah(recap.openingBalance)}\n" +
            "Pemasukan: ${CurrencyUtils.formatRupiah(recap.totalIncome)}\n" +
            "Pengeluaran: ${CurrencyUtils.formatRupiah(recap.totalExpense)}\n" +
            "Saldo Akhir: ${CurrencyUtils.formatRupiah(recap.closingBalance)}\n%%EOF\n"
          fos.write(header.toByteArray(Charsets.UTF_8))
        }
      }
    }

    return file
  }

  fun exportToExcelCsv(
    context: Context,
    recap: MonthRecap,
    transactions: List<TransactionEntity>,
    userName: String
  ): File {
    val reportsDir = File(context.cacheDir, "reports").apply { mkdirs() }
    val safePeriod = recap.monthKey.replace("-", "_")
    val file = File(reportsDir, "Rekap_Keuangan_${safePeriod}.csv")

    FileOutputStream(file).use { fos ->
      // Write UTF-8 BOM so Excel opens with correct characters and encoding
      fos.write(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))
      OutputStreamWriter(fos, Charsets.UTF_8).use { writer ->
        writer.appendLine("FINANCEKU - LAPORAN REKAP KEUANGAN")
        writer.appendLine("Periode,${recap.monthName}")
        writer.appendLine("Pengguna,\"$userName\"")
        writer.appendLine("Dicetak Pada,${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US).format(Date())}")
        writer.appendLine("")

        writer.appendLine("RINGKASAN SALDO")
        writer.appendLine("Saldo Awal,${recap.openingBalance}")
        writer.appendLine("Total Pemasukan,${recap.totalIncome}")
        writer.appendLine("Total Pengeluaran,${recap.totalExpense}")
        writer.appendLine("Selisih (Net),${recap.netAmount}")
        writer.appendLine("Saldo Akhir,${recap.closingBalance}")
        writer.appendLine("")

        writer.appendLine("RINCIAN KATEGORI PENGELUARAN")
        writer.appendLine("Kategori,Total Pengeluaran,Persentase")
        for (item in recap.expenseByCategory) {
          writer.appendLine("\"${item.categoryName}\",${item.totalAmount},${String.format("%.1f%%", item.percentage)}")
        }
        writer.appendLine("")

        writer.appendLine("RINCIAN KATEGORI PEMASUKAN")
        writer.appendLine("Kategori,Total Pemasukan,Persentase")
        for (item in recap.incomeByCategory) {
          writer.appendLine("\"${item.categoryName}\",${item.totalAmount},${String.format("%.1f%%", item.percentage)}")
        }
        writer.appendLine("")

        writer.appendLine("DAFTAR TRANSAKSI")
        writer.appendLine("ID,Tanggal,Tipe,Kategori,Deskripsi/Catatan,Nominal (IDR)")
        for (tx in transactions) {
          val cleanDesc = tx.description.replace("\"", "\"\"")
          writer.appendLine("${tx.id},${tx.transactionDate},${tx.type},\"${tx.categoryName}\",\"$cleanDesc\",${tx.amount}")
        }
      }
    }

    return file
  }

  fun shareFile(context: Context, file: File, mimeType: String, title: String = "Bagikan Laporan Keuangan") {
    val uri = FileProvider.getUriForFile(
      context,
      "${context.packageName}.fileprovider",
      file
    )

    val sendIntent = Intent(Intent.ACTION_SEND).apply {
      type = mimeType
      putExtra(Intent.EXTRA_STREAM, uri)
      putExtra(Intent.EXTRA_SUBJECT, title)
      addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    val chooser = Intent.createChooser(sendIntent, title).apply {
      addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(chooser)
  }
}
