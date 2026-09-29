package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.AddCategoryDialog
import com.example.ui.components.ChangePasswordDialog
import com.example.ui.components.EditProfileDialog
import com.example.ui.components.OpeningBalanceDialog
import com.example.ui.components.ThemeSelectionDialog
import com.example.ui.theme.ExpenseRed
import com.example.ui.viewmodel.FinanceViewModel
import com.example.util.CurrencyUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
  viewModel: FinanceViewModel,
  onLoggedOut: () -> Unit
) {
  val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
  val currentThemeMode by viewModel.themeMode.collectAsStateWithLifecycle()

  var showEditProfileDialog by remember { mutableStateOf(false) }
  var showChangePasswordDialog by remember { mutableStateOf(false) }
  var showOpeningBalanceDialog by remember { mutableStateOf(false) }
  var showAddCategoryDialog by remember { mutableStateOf(false) }
  var showThemeDialog by remember { mutableStateOf(false) }
  var showLogoutConfirmDialog by remember { mutableStateOf(false) }

  if (showThemeDialog) {
    ThemeSelectionDialog(
      currentMode = currentThemeMode,
      onDismiss = { showThemeDialog = false },
      onSelectMode = { mode ->
        viewModel.setThemeMode(mode)
      }
    )
  }

  if (showEditProfileDialog) {
    EditProfileDialog(
      currentName = currentUser?.name ?: "",
      onDismiss = { showEditProfileDialog = false },
      onSave = { newName ->
        viewModel.updateProfile(newName)
      }
    )
  }

  if (showChangePasswordDialog) {
    ChangePasswordDialog(
      onDismiss = { showChangePasswordDialog = false },
      onSave = { oldPass, newPass ->
        viewModel.updatePassword(oldPass, newPass)
      }
    )
  }

  if (showOpeningBalanceDialog) {
    OpeningBalanceDialog(
      currentBalance = currentUser?.openingBalance ?: 0L,
      currentNote = currentUser?.openingBalanceNote ?: "",
      onDismiss = { showOpeningBalanceDialog = false },
      onSave = { amount, note ->
        viewModel.setOpeningBalance(amount, note)
      }
    )
  }

  if (showAddCategoryDialog) {
    AddCategoryDialog(
      onDismiss = { showAddCategoryDialog = false },
      onSave = { name, type ->
        viewModel.addCustomCategory(name, type)
      }
    )
  }

  if (showLogoutConfirmDialog) {
    AlertDialog(
      onDismissRequest = { showLogoutConfirmDialog = false },
      title = { Text("Konfirmasi Keluar", fontWeight = FontWeight.Bold) },
      text = {
        Text("Apakah Anda yakin ingin keluar dari akun ini? Sesi Anda akan dihapus dan Anda dapat login kembali atau menggunakan akun lain.")
      },
      confirmButton = {
        Button(
          onClick = {
            showLogoutConfirmDialog = false
            viewModel.logout(onLoggedOut)
          },
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
          modifier = Modifier.testTag("confirm_logout_button")
        ) {
          Text("Keluar", color = MaterialTheme.colorScheme.onError)
        }
      },
      dismissButton = {
        OutlinedButton(onClick = { showLogoutConfirmDialog = false }) {
          Text("Batal")
        }
      }
    )
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = "Profil & Akun",
            fontWeight = FontWeight.Bold
          )
        }
      )
    },
    modifier = Modifier.fillMaxSize()
  ) { innerPadding ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .testTag("profile_screen"),
      contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // 1. User Info Header Card
      item {
        Card(
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp)
              )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = currentUser?.name ?: "Pengguna",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = currentUser?.email ?: "",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              val createdAt = currentUser?.createdAt ?: System.currentTimeMillis()
              val dateStr = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID")).format(Date(createdAt))
              Text(
                text = "Terdaftar sejak $dateStr",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
              )
            }
          }
        }
      }

      // 2. Data Isolation Information Banner
      item {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.Security,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
              text = "Data Anda aman dan terisolasi. Pengguna lain di perangkat ini tidak dapat melihat transaksi maupun saldo Anda.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSecondaryContainer
            )
          }
        }
      }

      // 3. Settings / Action Menu
      item {
        Card(
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(vertical = 8.dp)) {
            ProfileMenuItem(
              icon = Icons.Default.Edit,
              title = "Edit Nama",
              subtitle = currentUser?.name ?: "",
              onClick = { showEditProfileDialog = true },
              tag = "menu_edit_name"
            )

            ProfileMenuItem(
              icon = Icons.Default.AccountBalanceWallet,
              title = "Kelola Saldo Awal",
              subtitle = CurrencyUtils.formatRupiah(currentUser?.openingBalance ?: 0L),
              onClick = { showOpeningBalanceDialog = true },
              tag = "menu_manage_opening_balance"
            )

            ProfileMenuItem(
              icon = Icons.Default.Category,
              title = "Tambah Kategori Personal",
              subtitle = "Buat kategori transaksi sendiri",
              onClick = { showAddCategoryDialog = true },
              tag = "menu_add_category"
            )

            val themeSubtitle = when (currentThemeMode) {
              "LIGHT" -> "Terang (Cerah)"
              "DARK" -> "Gelap (Malam)"
              else -> "Ikuti Sistem HP"
            }
            ProfileMenuItem(
              icon = Icons.Default.DarkMode,
              title = "Tema Tampilan",
              subtitle = themeSubtitle,
              onClick = { showThemeDialog = true },
              tag = "menu_theme_mode"
            )

            ProfileMenuItem(
              icon = Icons.Default.Lock,
              title = "Ubah Password",
              subtitle = "Amankan akun Anda",
              onClick = { showChangePasswordDialog = true },
              tag = "menu_change_password"
            )

            ProfileMenuItem(
              icon = Icons.Default.SwapHoriz,
              title = "Ganti Akun",
              subtitle = "Beralih ke akun pengguna lain",
              onClick = { showLogoutConfirmDialog = true },
              tag = "menu_switch_account"
            )

            ProfileMenuItem(
              icon = Icons.Default.Logout,
              title = "Keluar (Logout)",
              subtitle = "Hapus sesi aktif pada perangkat",
              onClick = { showLogoutConfirmDialog = true },
              isDestructive = true,
              tag = "menu_logout"
            )
          }
        }
      }
    }
  }
}

@Composable
private fun ProfileMenuItem(
  icon: ImageVector,
  title: String,
  subtitle: String,
  onClick: () -> Unit,
  isDestructive: Boolean = false,
  tag: String = ""
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onClick() }
      .padding(horizontal = 20.dp, vertical = 14.dp)
      .testTag(tag),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Icon(
      imageVector = icon,
      contentDescription = null,
      tint = if (isDestructive) ExpenseRed else MaterialTheme.colorScheme.primary,
      modifier = Modifier.size(24.dp)
    )

    Spacer(modifier = Modifier.width(16.dp))

    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = title,
        style = MaterialTheme.typography.bodyLarge,
        fontWeight = FontWeight.SemiBold,
        color = if (isDestructive) ExpenseRed else MaterialTheme.colorScheme.onSurface
      )
      if (subtitle.isNotEmpty()) {
        Text(
          text = subtitle,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }

    Icon(
      imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
      contentDescription = null,
      tint = MaterialTheme.colorScheme.outline,
      modifier = Modifier.size(14.dp)
    )
  }
}
