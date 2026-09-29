package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.data.model.TransactionEntity
import com.example.util.CurrencyUtils

@Composable
fun OpeningBalanceDialog(
  currentBalance: Long,
  currentNote: String,
  onDismiss: () -> Unit,
  onSave: (amount: Long, note: String) -> Unit
) {
  var amountInput by remember { mutableStateOf(if (currentBalance > 0) currentBalance.toString() else "") }
  var noteInput by remember { mutableStateOf(currentNote) }
  var error by remember { mutableStateOf<String?>(null) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = "Atur Saldo Awal",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
      )
    },
    text = {
      Column(modifier = Modifier.fillMaxWidth()) {
        Text(
          text = "Tentukan saldo awal keuangan Anda. Perubahan saldo awal akan memengaruhi saldo berjalan secara real-time.",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
          value = amountInput,
          onValueChange = { input ->
            amountInput = input.filter { it.isDigit() }
            error = null
          },
          label = { Text("Nominal Saldo Awal (Rp)") },
          placeholder = { Text("Contoh: 3000000") },
          singleLine = true,
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          isError = error != null,
          supportingText = {
            if (error != null) {
              Text(error!!, color = MaterialTheme.colorScheme.error)
            } else if (amountInput.isNotBlank()) {
              val parsed = amountInput.toLongOrNull() ?: 0L
              Text("Format: ${CurrencyUtils.formatRupiah(parsed)}", color = MaterialTheme.colorScheme.primary)
            }
          },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("opening_balance_input")
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
          value = noteInput,
          onValueChange = { noteInput = it },
          label = { Text("Catatan (Opsional)") },
          placeholder = { Text("Misal: Tabungan BCA, Kas Awal") },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("opening_balance_note_input")
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val amount = amountInput.toLongOrNull()
          if (amount == null || amount < 0) {
            error = "Nominal harus diisi dengan angka valid"
            return@Button
          }
          onSave(amount, noteInput)
          onDismiss()
        },
        modifier = Modifier.testTag("save_opening_balance_button")
      ) {
        Text("Simpan")
      }
    },
    dismissButton = {
      OutlinedButton(onClick = onDismiss) {
        Text("Batal")
      }
    }
  )
}

@Composable
fun DeleteConfirmDialog(
  transaction: TransactionEntity,
  onDismiss: () -> Unit,
  onConfirm: () -> Unit
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = "Hapus Transaksi?",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
      )
    },
    text = {
      Column {
        Text(
          text = "Apakah Anda yakin ingin menghapus transaksi ini?",
          style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = "${transaction.categoryName}: ${transaction.description.ifEmpty { "Tanpa catatan" }}",
          style = MaterialTheme.typography.bodySmall,
          fontWeight = FontWeight.SemiBold
        )
        Text(
          text = "${if (transaction.type == "INCOME") "+" else "-"} ${CurrencyUtils.formatRupiah(transaction.amount)} (${transaction.transactionDate})",
          style = MaterialTheme.typography.bodySmall,
          color = if (transaction.type == "INCOME") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = "Setelah dihapus, saldo berjalan dan rekap bulanan Anda akan dihitung kembali secara otomatis.",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          onConfirm()
          onDismiss()
        },
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
        modifier = Modifier.testTag("confirm_delete_button")
      ) {
        Text("Hapus", color = MaterialTheme.colorScheme.onError)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Batal")
      }
    }
  )
}

@Composable
fun AddCategoryDialog(
  initialType: String = "EXPENSE",
  onDismiss: () -> Unit,
  onSave: (name: String, type: String) -> Unit
) {
  var nameInput by remember { mutableStateOf("") }
  var selectedType by remember { mutableStateOf(initialType) }
  var error by remember { mutableStateOf<String?>(null) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text("Tambah Kategori Baru", fontWeight = FontWeight.Bold)
    },
    text = {
      Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth()) {
          FilterChip(
            selected = selectedType == "EXPENSE",
            onClick = { selectedType = "EXPENSE" },
            label = { Text("Pengeluaran") },
            modifier = Modifier.weight(1f)
          )
          Spacer(modifier = Modifier.width(8.dp))
          FilterChip(
            selected = selectedType == "INCOME",
            onClick = { selectedType = "INCOME" },
            label = { Text("Pemasukan") },
            modifier = Modifier.weight(1f)
          )
        }
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
          value = nameInput,
          onValueChange = {
            nameInput = it
            error = null
          },
          label = { Text("Nama Kategori") },
          placeholder = { Text("Misal: Hobi, Donasi, Gym") },
          singleLine = true,
          isError = error != null,
          supportingText = {
            if (error != null) {
              Text(error!!, color = MaterialTheme.colorScheme.error)
            }
          },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("new_category_name_input")
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val trimmed = nameInput.trim()
          if (trimmed.isEmpty()) {
            error = "Nama kategori wajib diisi"
            return@Button
          }
          onSave(trimmed, selectedType)
          onDismiss()
        },
        modifier = Modifier.testTag("save_category_button")
      ) {
        Text("Simpan")
      }
    },
    dismissButton = {
      OutlinedButton(onClick = onDismiss) {
        Text("Batal")
      }
    }
  )
}

@Composable
fun EditProfileDialog(
  currentName: String,
  onDismiss: () -> Unit,
  onSave: (name: String) -> Unit
) {
  var nameInput by remember { mutableStateOf(currentName) }
  var error by remember { mutableStateOf<String?>(null) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Edit Nama Lengkap", fontWeight = FontWeight.Bold) },
    text = {
      Column {
        OutlinedTextField(
          value = nameInput,
          onValueChange = {
            nameInput = it
            error = null
          },
          label = { Text("Nama Lengkap") },
          singleLine = true,
          isError = error != null,
          supportingText = {
            if (error != null) {
              Text(error!!, color = MaterialTheme.colorScheme.error)
            }
          },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("edit_name_input")
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val trimmed = nameInput.trim()
          if (trimmed.isEmpty()) {
            error = "Nama tidak boleh kosong"
            return@Button
          }
          onSave(trimmed)
          onDismiss()
        },
        modifier = Modifier.testTag("save_name_button")
      ) {
        Text("Simpan")
      }
    },
    dismissButton = {
      OutlinedButton(onClick = onDismiss) {
        Text("Batal")
      }
    }
  )
}

@Composable
fun ChangePasswordDialog(
  onDismiss: () -> Unit,
  onSave: (oldPass: String, newPass: String) -> Unit
) {
  var oldPassword by remember { mutableStateOf("") }
  var newPassword by remember { mutableStateOf("") }
  var confirmPassword by remember { mutableStateOf("") }
  var error by remember { mutableStateOf<String?>(null) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Ubah Password", fontWeight = FontWeight.Bold) },
    text = {
      Column {
        OutlinedTextField(
          value = oldPassword,
          onValueChange = { oldPassword = it; error = null },
          label = { Text("Password Lama") },
          singleLine = true,
          visualTransformation = PasswordVisualTransformation(),
          modifier = Modifier.fillMaxWidth().testTag("old_password_input")
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
          value = newPassword,
          onValueChange = { newPassword = it; error = null },
          label = { Text("Password Baru (min 8 karakter)") },
          singleLine = true,
          visualTransformation = PasswordVisualTransformation(),
          modifier = Modifier.fillMaxWidth().testTag("new_password_input")
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
          value = confirmPassword,
          onValueChange = { confirmPassword = it; error = null },
          label = { Text("Konfirmasi Password Baru") },
          singleLine = true,
          visualTransformation = PasswordVisualTransformation(),
          isError = error != null,
          supportingText = {
            if (error != null) {
              Text(error!!, color = MaterialTheme.colorScheme.error)
            }
          },
          modifier = Modifier.fillMaxWidth().testTag("confirm_password_input")
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (oldPassword.isBlank()) {
            error = "Password lama wajib diisi"
            return@Button
          }
          if (newPassword.length < 8) {
            error = "Password baru minimal 8 karakter"
            return@Button
          }
          if (newPassword != confirmPassword) {
            error = "Konfirmasi password baru tidak cocok"
            return@Button
          }
          onSave(oldPassword, newPassword)
          onDismiss()
        },
        modifier = Modifier.testTag("save_password_button")
      ) {
        Text("Ubah")
      }
    },
    dismissButton = {
      OutlinedButton(onClick = onDismiss) {
        Text("Batal")
      }
    }
  )
}

@Composable
fun ThemeSelectionDialog(
  currentMode: String,
  onDismiss: () -> Unit,
  onSelectMode: (String) -> Unit
) {
  val options = listOf(
    Triple("LIGHT", "Terang", "Gunakan tampilan warna cerah"),
    Triple("DARK", "Gelap", "Nyaman untuk mata di malam hari"),
    Triple("SYSTEM", "Ikuti Sistem", "Menyesuaikan otomatis dengan mode HP")
  )

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text("Pilih Tema Tampilan", fontWeight = FontWeight.Bold)
    },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { (mode, title, desc) ->
          val isSelected = currentMode.equals(mode, ignoreCase = true)
          Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
              containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            modifier = Modifier
              .fillMaxWidth()
              .clickable {
                onSelectMode(mode)
                onDismiss()
              }
              .testTag("theme_option_$mode")
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              androidx.compose.material3.RadioButton(
                selected = isSelected,
                onClick = {
                  onSelectMode(mode)
                  onDismiss()
                }
              )
              Spacer(modifier = Modifier.width(8.dp))
              Column {
                Text(
                  text = title,
                  style = MaterialTheme.typography.bodyLarge,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
                )
                Text(
                  text = desc,
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          }
        }
      }
    },
    confirmButton = {
      TextButton(onClick = onDismiss) {
        Text("Tutup")
      }
    }
  )
}

@Composable
fun ExportReportDialog(
  monthName: String,
  onDismiss: () -> Unit,
  onExportPdf: () -> Unit,
  onExportExcel: () -> Unit
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text("Export Rekap Keuangan", fontWeight = FontWeight.Bold)
    },
    text = {
      Column(modifier = Modifier.fillMaxWidth()) {
        Text(
          text = "Pilih format dokumen untuk mengekspor rekap periode $monthName:",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))

        // PDF Option
        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
          modifier = Modifier
            .fillMaxWidth()
            .clickable {
              onExportPdf()
              onDismiss()
            }
            .testTag("export_pdf_option")
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.error.copy(alpha = 0.15f)),
              contentAlignment = Alignment.Center
            ) {
              Text(
                "PDF",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
              )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text(
                "Dokumen PDF (.pdf)",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
              )
              Text(
                "Format dokumen rapi dengan tabel transaksi siap cetak",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Excel / CSV Option
        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
          modifier = Modifier
            .fillMaxWidth()
            .clickable {
              onExportExcel()
              onDismiss()
            }
            .testTag("export_excel_option")
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(com.example.ui.theme.IncomeGreen.copy(alpha = 0.15f)),
              contentAlignment = Alignment.Center
            ) {
              Text(
                "XLS",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = com.example.ui.theme.IncomeGreen
              )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text(
                "Spreadsheet Excel (.csv)",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
              )
              Text(
                "Format spreadsheet yang bisa dibuka di Microsoft Excel / Sheets",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }
      }
    },
    confirmButton = {
      TextButton(onClick = onDismiss) {
        Text("Batal")
      }
    }
  )
}
