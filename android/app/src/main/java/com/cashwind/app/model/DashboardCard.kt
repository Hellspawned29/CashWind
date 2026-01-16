package com.cashwind.app.model

data class DashboardCard(
    val id: String,
    val emoji: String,
    val title: String,
    val subtitle: String,
    val activityClass: Class<*>?,
    var isDynamic: Boolean = false,
    var location: String = "home" // "home" or "more"
)
