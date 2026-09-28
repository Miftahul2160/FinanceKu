package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.viewmodel.AuthViewModel

@Composable
fun AuthScreen(
  viewModel: AuthViewModel,
  onAuthSuccess: () -> Unit
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  var selectedTab by remember { mutableIntStateOf(0) } // 0: Login, 1: Register

  // Form fields
  var name by remember { mutableStateOf("") }
  var email by remember { mutableStateOf("") }
  var password by remember { mutableStateOf("") }
  var confirmPassword by remember { mutableStateOf("") }
  var passwordVisible by remember { mutableStateOf(false) }

  var clientError by remember { mutableStateOf<String?>(null) }

  LaunchedEffect(uiState.isRegisteredSuccessfully) {
    if (uiState.isRegisteredSuccessfully) {
      selectedTab = 0
    }
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .imePadding()
      .verticalScroll(rememberScrollState())
      .padding(horizontal = 24.dp, vertical = 32.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Spacer(modifier = Modifier.height(24.dp))

    // App Branding Header
    Box(
      modifier = Modifier
        .size(80.dp)
        .clip(CircleShape)
        .background(MaterialTheme.colorScheme.primaryContainer),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = Icons.Default.AccountBalanceWallet,
        contentDescription = "FinanceKu Logo",
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier.size(44.dp)
      )
    }

    Spacer(modifier = Modifier.height(16.dp))

    Text(
      text = "FinanceKu",
      style = MaterialTheme.typography.headlineLarge,
      fontWeight = FontWeight.ExtraBold,
      color = MaterialTheme.colorScheme.primary
    )

    Text(
      text = "Pencatatan Keuangan Pribadi Multi-Akun",
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(28.dp))

    // Tab Bar: Masuk / Daftar
    Card(
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      TabRow(
        selectedTabIndex = selectedTab,
        containerColor = MaterialTheme.colorScheme.surface
      ) {
        Tab(
          selected = selectedTab == 0,
          onClick = {
            selectedTab = 0
            clientError = null
            viewModel.clearMessages()
          },
          text = {
            Text(
              "Masuk",
              fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
            )
          },
          modifier = Modifier.testTag("tab_login")
        )
        Tab(
          selected = selectedTab == 1,
          onClick = {
            selectedTab = 1
            clientError = null
            viewModel.clearMessages()
          },
          text = {
            Text(
              "Daftar",
              fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
            )
          },
          modifier = Modifier.testTag("tab_register")
        )
      }
    }

    Spacer(modifier = Modifier.height(20.dp))

    // Status Messages
    AnimatedVisibility(visible = uiState.errorMessage != null || clientError != null) {
      Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 16.dp)
      ) {
        Text(
          text = clientError ?: uiState.errorMessage ?: "",
          color = MaterialTheme.colorScheme.onErrorContainer,
          style = MaterialTheme.typography.bodyMedium,
          modifier = Modifier.padding(12.dp)
        )
      }
    }

    AnimatedVisibility(visible = uiState.successMessage != null) {
      Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 16.dp)
      ) {
        Text(
          text = uiState.successMessage ?: "",
          color = MaterialTheme.colorScheme.onPrimaryContainer,
          style = MaterialTheme.typography.bodyMedium,
          modifier = Modifier.padding(12.dp)
        )
      }
    }

    // Input Fields
    Card(
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(20.dp)) {
        if (selectedTab == 1) {
          // Register: Nama Lengkap
          OutlinedTextField(
            value = name,
            onValueChange = { name = it; clientError = null },
            label = { Text("Nama Lengkap") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
            singleLine = true,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("register_name_input")
          )
          Spacer(modifier = Modifier.height(16.dp))
        }

        // Email
        OutlinedTextField(
          value = email,
          onValueChange = { email = it; clientError = null },
          label = { Text("Email") },
          placeholder = { Text("nama@example.com") },
          leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("email_input")
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Password
        OutlinedTextField(
          value = password,
          onValueChange = { password = it; clientError = null },
          label = { Text("Password (min 8 karakter)") },
          leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
          trailingIcon = {
            IconButton(onClick = { passwordVisible = !passwordVisible }) {
              Icon(
                imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                contentDescription = if (passwordVisible) "Sembunyikan password" else "Tampilkan password"
              )
            }
          },
          visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("password_input")
        )

        if (selectedTab == 1) {
          Spacer(modifier = Modifier.height(16.dp))
          // Confirm Password
          OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it; clientError = null },
            label = { Text("Konfirmasi Password") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("confirm_password_input")
          )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
          onClick = {
            clientError = null
            if (selectedTab == 0) {
              if (email.isBlank() || password.isBlank()) {
                clientError = "Email dan password wajib diisi."
                return@Button
              }
              viewModel.login(email.trim(), password, onSuccess = onAuthSuccess)
            } else {
              if (name.isBlank()) {
                clientError = "Nama wajib diisi."
                return@Button
              }
              if (email.isBlank() || !email.contains("@")) {
                clientError = "Format email tidak valid."
                return@Button
              }
              if (password.length < 8) {
                clientError = "Password minimal 8 karakter."
                return@Button
              }
              if (password != confirmPassword) {
                clientError = "Konfirmasi password tidak cocok."
                return@Button
              }
              viewModel.register(
                name = name,
                email = email,
                password = password,
                confirmPassword = confirmPassword,
                onSuccess = {
                  // Switch to login tab and keep email
                  selectedTab = 0
                  password = ""
                  confirmPassword = ""
                }
              )
            }
          },
          enabled = !uiState.isLoading,
          modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .testTag("auth_submit_button")
        ) {
          if (uiState.isLoading) {
            CircularProgressIndicator(
              modifier = Modifier.size(24.dp),
              color = MaterialTheme.colorScheme.onPrimary,
              strokeWidth = 2.dp
            )
          } else {
            Text(
              text = if (selectedTab == 0) "Masuk Sekarang" else "Daftar Akun Baru",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(24.dp))

    // Fast Quick-Fill Test Accounts for immediate testing and verification
    Card(
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
      shape = RoundedCornerShape(16.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(
        modifier = Modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text(
          text = "⚡ Akses Cepat Akun Demo (Untuk Pengujian)",
          style = MaterialTheme.typography.labelLarge,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
          text = "Pilih akun di bawah untuk menguji saldo & isolasi multi-user:",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          textAlign = TextAlign.Center,
          modifier = Modifier.padding(vertical = 6.dp)
        )
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OutlinedButton(
            onClick = {
              selectedTab = 0
              email = "demo@example.com"
              password = "password123"
              clientError = null
            },
            modifier = Modifier
              .weight(1f)
              .testTag("quick_fill_demo")
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text("Akun Demo", fontWeight = FontWeight.Bold)
              Text("Saldo Rp6.500.000", style = MaterialTheme.typography.labelSmall)
            }
          }

          OutlinedButton(
            onClick = {
              selectedTab = 0
              email = "budi@example.com"
              password = "password123"
              clientError = null
            },
            modifier = Modifier
              .weight(1f)
              .testTag("quick_fill_budi")
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text("Akun Budi", fontWeight = FontWeight.Bold)
              Text("Saldo Rp2.100.000", style = MaterialTheme.typography.labelSmall)
            }
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(24.dp))
  }
}
