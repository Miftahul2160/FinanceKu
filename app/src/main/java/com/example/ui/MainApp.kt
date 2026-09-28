package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.AppContainer
import com.example.ui.screens.AddTransactionScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.RecapScreen
import com.example.ui.viewmodel.AuthViewModel
import com.example.ui.viewmodel.FinanceViewModel
import kotlinx.coroutines.flow.collectLatest

@Composable
fun MainApp(
  container: AppContainer
) {
  val sessionManager = container.sessionManager
  val currentUserId by sessionManager.currentUserId.collectAsStateWithLifecycle()

  val authViewModel = remember {
    AuthViewModel(container.authRepository)
  }

  if (currentUserId == null) {
    AuthScreen(
      viewModel = authViewModel,
      onAuthSuccess = {
        // Logged in
      }
    )
  } else {
    AuthenticatedContent(
      container = container,
      onLoggedOut = {
        // Will trigger currentUserId == null
      }
    )
  }
}

@Composable
private fun AuthenticatedContent(
  container: AppContainer,
  onLoggedOut: () -> Unit
) {
  val financeViewModel = remember(container.sessionManager.currentUserId.value) {
    FinanceViewModel(container.authRepository, container.financeRepository)
  }

  val snackbarHostState = remember { SnackbarHostState() }
  var selectedTab by remember { mutableIntStateOf(0) }
  var initialExpenseForAdd by remember { mutableStateOf(false) }

  // Listen to snackbar events from ViewModel
  LaunchedEffect(financeViewModel) {
    financeViewModel.snackbarEvent.collectLatest { message ->
      snackbarHostState.showSnackbar(message)
    }
  }

  // Handle back button on secondary tabs
  BackHandler(enabled = selectedTab != 0) {
    selectedTab = 0
  }

  Scaffold(
    snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    bottomBar = {
      NavigationBar(
        modifier = Modifier.testTag("main_bottom_nav")
      ) {
        NavigationBarItem(
          selected = selectedTab == 0,
          onClick = { selectedTab = 0 },
          icon = {
            Icon(
              imageVector = if (selectedTab == 0) Icons.Default.Home else Icons.Outlined.Home,
              contentDescription = "Dashboard"
            )
          },
          label = { Text("Dashboard") },
          modifier = Modifier.testTag("nav_dashboard")
        )

        NavigationBarItem(
          selected = selectedTab == 1,
          onClick = {
            initialExpenseForAdd = false
            selectedTab = 1
          },
          icon = {
            Icon(
              imageVector = Icons.Default.AddCircle,
              contentDescription = "Transaksi"
            )
          },
          label = { Text("Transaksi") },
          modifier = Modifier.testTag("nav_transaction")
        )

        NavigationBarItem(
          selected = selectedTab == 2,
          onClick = { selectedTab = 2 },
          icon = {
            Icon(
              imageVector = if (selectedTab == 2) Icons.Default.History else Icons.Outlined.History,
              contentDescription = "History"
            )
          },
          label = { Text("History") },
          modifier = Modifier.testTag("nav_history")
        )

        NavigationBarItem(
          selected = selectedTab == 3,
          onClick = { selectedTab = 3 },
          icon = {
            Icon(
              imageVector = if (selectedTab == 3) Icons.Default.Assessment else Icons.Outlined.Assessment,
              contentDescription = "Rekap"
            )
          },
          label = { Text("Rekap") },
          modifier = Modifier.testTag("nav_recap")
        )

        NavigationBarItem(
          selected = selectedTab == 4,
          onClick = { selectedTab = 4 },
          icon = {
            Icon(
              imageVector = if (selectedTab == 4) Icons.Default.Person else Icons.Outlined.Person,
              contentDescription = "Profil"
            )
          },
          label = { Text("Profil") },
          modifier = Modifier.testTag("nav_profile")
        )
      }
    }
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      when (selectedTab) {
        0 -> DashboardScreen(
          viewModel = financeViewModel,
          onNavigateToAddTransaction = { isExpense ->
            initialExpenseForAdd = isExpense
            selectedTab = 1
          },
          onNavigateToHistory = { selectedTab = 2 },
          onNavigateToRecap = { selectedTab = 3 },
          onSwitchAccount = {
            financeViewModel.logout(onLoggedOut)
          }
        )
        1 -> AddTransactionScreen(
          viewModel = financeViewModel,
          initialIsExpense = initialExpenseForAdd,
          onNavigateBack = { selectedTab = 0 }
        )
        2 -> HistoryScreen(
          viewModel = financeViewModel
        )
        3 -> RecapScreen(
          viewModel = financeViewModel
        )
        4 -> ProfileScreen(
          viewModel = financeViewModel,
          onLoggedOut = onLoggedOut
        )
      }
    }
  }
}
