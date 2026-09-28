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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.TransactionEntity
import com.example.ui.components.CategoryIcon
import com.example.ui.components.OpeningBalanceDialog
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.ExpenseRedContainer
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.IncomeGreenContainer
import com.example.ui.viewmodel.FinanceViewModel
import com.example.util.CurrencyUtils
import com.example.util.DateUtils

@Composable
fun DashboardScreen(
  viewModel: FinanceViewModel,
  onNavigateToAddTransaction: (isExpense: Boolean) -> Unit,
  onNavigateToHistory: () -> Unit,
  onNavigateToRecap: () -> Unit,
  onSwitchAccount: () -> Unit
) {
  val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
  val balanceSummary by viewModel.balanceSummary.collectAsStateWithLifecycle()
  val recentTransactions by viewModel.recentTransactions.collectAsStateWithLifecycle()

  var showOpeningBalanceDialog by remember { mutableStateOf(false) }

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

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .testTag("dashboard_screen"),
    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // 1. Top Bar Greeting & Switch Account
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Halo, ${currentUser?.name ?: "Pengguna"} 👋",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
          )
          Text(
            text = currentUser?.email ?: "",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        Surface(
          shape = RoundedCornerShape(12.dp),
          color = MaterialTheme.colorScheme.surfaceVariant,
          modifier = Modifier
            .clickable { onSwitchAccount() }
            .testTag("switch_account_button")
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.SwapHoriz,
              contentDescription = "Ganti Akun",
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "Ganti Akun",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.primary
            )
          }
        }
      }
    }

    // 2. Saldo Awal Alert (Section 6.3: First-time usage reminder)
    if ((currentUser?.openingBalance ?: 0L) == 0L && recentTransactions.isEmpty()) {
      item {
        Card(
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
          shape = RoundedCornerShape(16.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.HelpOutline,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onPrimaryContainer,
              modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Saldo Awal Belum Ditentukan",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
              )
              Text(
                text = "Atur saldo awal tabungan atau dompet Anda untuk memulai pencatatan.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
              )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
              onClick = { showOpeningBalanceDialog = true },
              colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
              Text("Atur", style = MaterialTheme.typography.labelSmall)
            }
          }
        }
      }
    }

    // 3. Main Hero Card: Saldo Saat Ini
    item {
      ElevatedCard(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
          containerColor = MaterialTheme.colorScheme.primary
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("current_balance_card")
      ) {
        Column(
          modifier = Modifier.padding(24.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.AccountBalanceWallet,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "Saldo Saat Ini",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
              )
            }

            // Edit Saldo Awal Button
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.18f),
              modifier = Modifier
                .clickable { showOpeningBalanceDialog = true }
                .testTag("edit_opening_balance_button")
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = Icons.Default.Edit,
                  contentDescription = "Edit Saldo Awal",
                  tint = MaterialTheme.colorScheme.onPrimary,
                  modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = "Saldo Awal",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onPrimary
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          Text(
            text = CurrencyUtils.formatRupiah(balanceSummary.currentBalance),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.testTag("current_balance_text")
          )

          Spacer(modifier = Modifier.height(10.dp))

          Text(
            text = "Saldo Awal: ${CurrencyUtils.formatRupiah(balanceSummary.openingBalance)}${if (!currentUser?.openingBalanceNote.isNullOrBlank()) " (${currentUser?.openingBalanceNote})" else ""}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
          )
        }
      }
    }

    // 4. Summary Cards: Pemasukan & Pengeluaran
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        // Card Pemasukan
        Card(
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = IncomeGreenContainer),
          modifier = Modifier
            .weight(1f)
            .testTag("summary_income_card")
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(28.dp)
                  .clip(CircleShape)
                  .background(IncomeGreen.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.ArrowUpward,
                  contentDescription = null,
                  tint = IncomeGreen,
                  modifier = Modifier.size(16.dp)
                )
              }
              Text(
                text = "Pemasukan",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = IncomeGreen
              )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = CurrencyUtils.formatRupiah(balanceSummary.totalIncome),
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = IncomeGreen,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }
        }

        // Card Pengeluaran
        Card(
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = ExpenseRedContainer),
          modifier = Modifier
            .weight(1f)
            .testTag("summary_expense_card")
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(28.dp)
                  .clip(CircleShape)
                  .background(ExpenseRed.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.ArrowDownward,
                  contentDescription = null,
                  tint = ExpenseRed,
                  modifier = Modifier.size(16.dp)
                )
              }
              Text(
                text = "Pengeluaran",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = ExpenseRed
              )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = CurrencyUtils.formatRupiah(balanceSummary.totalExpense),
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = ExpenseRed,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }
        }
      }
    }

    // 5. Quick Actions Row (Section 7.2)
    item {
      Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = "Aksi Cepat",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Spacer(modifier = Modifier.height(12.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Button(
              onClick = { onNavigateToAddTransaction(false) },
              colors = ButtonDefaults.buttonColors(containerColor = IncomeGreen),
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier
                .weight(1f)
                .height(46.dp)
                .testTag("quick_add_income_button")
            ) {
              Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Pemasukan", fontWeight = FontWeight.Bold)
            }

            Button(
              onClick = { onNavigateToAddTransaction(true) },
              colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed),
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier
                .weight(1f)
                .height(46.dp)
                .testTag("quick_add_expense_button")
            ) {
              Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Pengeluaran", fontWeight = FontWeight.Bold)
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            OutlinedButton(
              onClick = onNavigateToHistory,
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier
                .weight(1f)
                .testTag("quick_history_button")
            ) {
              Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Lihat History")
            }

            OutlinedButton(
              onClick = onNavigateToRecap,
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier
                .weight(1f)
                .testTag("quick_recap_button")
            ) {
              Icon(Icons.Default.Assessment, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Lihat Rekap")
            }
          }
        }
      }
    }

    // 6. Transaksi Terbaru Header (Section 7.2)
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Transaksi Terbaru",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onBackground
        )

        if (recentTransactions.isNotEmpty()) {
          Row(
            modifier = Modifier
              .clickable { onNavigateToHistory() }
              .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Lihat Semua",
              style = MaterialTheme.typography.labelMedium,
              color = MaterialTheme.colorScheme.primary,
              fontWeight = FontWeight.SemiBold
            )
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowForward,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(14.dp)
            )
          }
        }
      }
    }

    // 7. Recent Transactions List
    if (recentTransactions.isEmpty()) {
      item {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Text(
              text = "Belum Ada Transaksi",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "Mulai mencatat pemasukan atau pengeluaran harian Anda sekarang.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
    } else {
      items(recentTransactions, key = { it.id }) { tx ->
        TransactionItemCard(transaction = tx)
      }
    }
  }
}

@Composable
fun TransactionItemCard(
  transaction: TransactionEntity,
  onDeleteClick: (() -> Unit)? = null
) {
  val isIncome = transaction.type == "INCOME"
  val amountColor = if (isIncome) IncomeGreen else ExpenseRed

  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    modifier = Modifier
      .fillMaxWidth()
      .testTag("transaction_item_${transaction.id}")
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Category Icon
      Box(
        modifier = Modifier
          .size(44.dp)
          .clip(CircleShape)
          .background(if (isIncome) IncomeGreenContainer else ExpenseRedContainer),
        contentAlignment = Alignment.Center
      ) {
        CategoryIcon(
          categoryName = transaction.categoryName,
          tint = if (isIncome) IncomeGreen else ExpenseRed,
          modifier = Modifier.size(22.dp)
        )
      }

      Spacer(modifier = Modifier.width(12.dp))

      // Description & Category & Date
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = transaction.description.ifBlank { transaction.categoryName },
          style = MaterialTheme.typography.bodyMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Text(
            text = transaction.categoryName,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Medium
          )
          Text(
            text = "•",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline
          )
          Text(
            text = DateUtils.formatToIndonesian(transaction.transactionDate),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      Spacer(modifier = Modifier.width(8.dp))

      // Amount & Optional Delete
      Column(horizontalAlignment = Alignment.End) {
        Text(
          text = "${if (isIncome) "+" else "-"} ${CurrencyUtils.formatRupiah(transaction.amount)}",
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.ExtraBold,
          color = amountColor
        )
        Text(
          text = if (isIncome) "Pemasukan" else "Pengeluaran",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }
  }
}
