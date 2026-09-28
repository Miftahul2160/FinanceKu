package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

fun getCategoryIcon(name: String, iconKey: String = ""): ImageVector {
  val lowerName = name.lowercase()
  val lowerKey = iconKey.lowercase()

  return when {
    lowerKey == "work" || lowerName.contains("gaji") -> Icons.Default.Work
    lowerKey == "card_giftcard" || lowerName.contains("bonus") || lowerName.contains("hadiah") -> Icons.Default.CardGiftcard
    lowerKey == "laptop" || lowerName.contains("freelance") || lowerName.contains("proyek") -> Icons.Default.Laptop
    lowerKey == "store" || lowerName.contains("bisnis") || lowerName.contains("toko") || lowerName.contains("jual") -> Icons.Default.Store
    lowerKey == "trending_up" || lowerName.contains("invest") || lowerName.contains("saham") || lowerName.contains("reksa") -> Icons.Default.TrendingUp
    lowerKey == "restaurant" || lowerName.contains("makan") || lowerName.contains("kuliner") || lowerName.contains("minum") -> Icons.Default.Restaurant
    lowerKey == "directions_car" || lowerName.contains("trans") || lowerName.contains("bensin") || lowerName.contains("ojek") -> Icons.Default.DirectionsCar
    lowerKey == "shopping_bag" || lowerName.contains("belanja") || lowerName.contains("mall") -> Icons.Default.ShoppingCart
    lowerKey == "receipt" || lowerName.contains("tagihan") || lowerName.contains("listrik") || lowerName.contains("air") || lowerName.contains("wifi") -> Icons.Default.Receipt
    lowerKey == "movie" || lowerName.contains("hiburan") || lowerName.contains("game") || lowerName.contains("nonton") -> Icons.Default.Movie
    lowerKey == "school" || lowerName.contains("didik") || lowerName.contains("kuliah") || lowerName.contains("buku") -> Icons.Default.School
    lowerKey == "medical_services" || lowerName.contains("sehat") || lowerName.contains("obat") || lowerName.contains("dokter") -> Icons.Default.LocalHospital
    lowerKey == "home" || lowerName.contains("rumah") || lowerName.contains("sewa") || lowerName.contains("kos") -> Icons.Default.Home
    lowerName.contains("masuk") || lowerName.contains("pendapatan") -> Icons.Default.AttachMoney
    else -> Icons.Default.Category
  }
}

@Composable
fun CategoryIcon(
  categoryName: String,
  iconKey: String = "",
  modifier: Modifier = Modifier,
  tint: Color = Color.Unspecified
) {
  Icon(
    imageVector = getCategoryIcon(categoryName, iconKey),
    contentDescription = categoryName,
    modifier = modifier,
    tint = tint
  )
}
