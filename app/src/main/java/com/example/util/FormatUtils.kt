package com.example.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CurrencyUtils {
  private val rupiahFormat: DecimalFormat by lazy {
    val symbols = DecimalFormatSymbols(Locale("id", "ID")).apply {
      groupingSeparator = '.'
      decimalSeparator = ','
    }
    DecimalFormat("#,###", symbols)
  }

  fun formatRupiah(amount: Long): String {
    return if (amount < 0) {
      "- Rp " + rupiahFormat.format(-amount)
    } else {
      "Rp " + rupiahFormat.format(amount)
    }
  }

  fun parseRupiahInput(input: String): Long {
    val cleaned = input.filter { it.isDigit() }
    return cleaned.toLongOrNull() ?: 0L
  }
}

object DateUtils {
  private val dbFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
  private val indoFormat = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
  private val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale("id", "ID"))
  private val yearMonthKeyFormat = SimpleDateFormat("yyyy-MM", Locale.US)

  fun getTodayDbDate(): String {
    return dbFormat.format(Date())
  }

  fun formatToIndonesian(dateStr: String): String {
    return try {
      val parsed = dbFormat.parse(dateStr)
      if (parsed != null) indoFormat.format(parsed) else dateStr
    } catch (_: Exception) {
      dateStr
    }
  }

  fun formatToMonthYear(dateStr: String): String {
    return try {
      val parsed = dbFormat.parse(dateStr)
      if (parsed != null) monthYearFormat.format(parsed) else dateStr
    } catch (_: Exception) {
      dateStr
    }
  }

  fun getYearMonthKey(dateStr: String): String {
    return try {
      val parsed = dbFormat.parse(dateStr)
      if (parsed != null) yearMonthKeyFormat.format(parsed) else dateStr.take(7)
    } catch (_: Exception) {
      dateStr.take(7)
    }
  }

  fun getCurrentYearMonthKey(): String {
    return yearMonthKeyFormat.format(Date())
  }

  fun getCurrentMonthYearName(): String {
    return monthYearFormat.format(Date())
  }
}
