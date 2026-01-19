package com.cashwind.app.util

import android.content.Context
import com.cashwind.app.MainActivity
import com.cashwind.app.AccountsActivity
import com.cashwind.app.AnalyticsActivity
import com.cashwind.app.PastDueBillsActivity
import com.cashwind.app.BudgetActivity
import com.cashwind.app.GoalsActivity
import com.cashwind.app.PaycheckActivity
import com.cashwind.app.CalendarActivity
import com.cashwind.app.ExportActivity
import com.cashwind.app.model.DashboardCard

object DashboardCardManager {
    private const val PREFS_NAME = "dashboard_prefs"
    private const val CARD_ORDER_KEY = "card_order"
    private const val CARD_LOCATIONS_KEY = "card_locations"

    private val allCards = listOf(
        DashboardCard("bills", "💰", "Bills", "Manage your bills", MainActivity::class.java, isDynamic = true, location = "home"),
        DashboardCard("accounts", "🏦", "Accounts", "Track your accounts", AccountsActivity::class.java, location = "home"),
        DashboardCard("analytics", "📊", "Analytics", "View insights", AnalyticsActivity::class.java, location = "home"),
        DashboardCard("pastdue", "⚠️", "Past Due", "0 bills", PastDueBillsActivity::class.java, isDynamic = true, location = "home"),
        DashboardCard("budgets", "📋", "Budgets", "Set spending limits", BudgetActivity::class.java, location = "home"),
        DashboardCard("goals", "🎯", "Goals", "Track savings", GoalsActivity::class.java, location = "home"),
        DashboardCard("paycheck", "💵", "Paycheck", "Allocate funds", PaycheckActivity::class.java, location = "more"),
        DashboardCard("calendar", "📅", "Calendar", "View schedule", CalendarActivity::class.java, location = "more"),
        DashboardCard("export", "📤", "Export", "Export data", ExportActivity::class.java, location = "more")
    )

    fun getCards(context: Context, location: String): MutableList<DashboardCard> {
        val savedLocations = getSavedLocations(context)
        val savedOrder = getSavedOrder(context)
        
        // Update card locations from saved preferences
        val updatedCards = allCards.map { card ->
            card.copy(location = savedLocations[card.id] ?: card.location)
        }
        
        // Filter cards by location
        val locationCards = updatedCards.filter { it.location == location }
        
        // Apply saved order
        val orderedCards = mutableListOf<DashboardCard>()
        if (savedOrder.isNotEmpty()) {
            savedOrder.forEach { cardId ->
                locationCards.find { it.id == cardId }?.let { orderedCards.add(it) }
            }
            // Add any new cards not in saved order
            locationCards.forEach { card ->
                if (!orderedCards.any { it.id == card.id }) {
                    orderedCards.add(card)
                }
            }
        } else {
            orderedCards.addAll(locationCards)
        }
        
        return orderedCards
    }
    
    fun getAllCards(context: Context): MutableList<DashboardCard> {
        val savedOrder = getSavedOrder(context)
        
        // Apply saved order to all cards
        val orderedCards = mutableListOf<DashboardCard>()
        if (savedOrder.isNotEmpty()) {
            savedOrder.forEach { cardId ->
                allCards.find { it.id == cardId }?.let { orderedCards.add(it) }
            }
            // Add any new cards not in saved order
            allCards.forEach { card ->
                if (!orderedCards.any { it.id == card.id }) {
                    orderedCards.add(card)
                }
            }
        } else {
            orderedCards.addAll(allCards)
        }
        
        return orderedCards
    }

    fun saveCardOrder(context: Context, cards: List<DashboardCard>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val order = cards.joinToString(",") { it.id }
        prefs.edit().putString(CARD_ORDER_KEY, order).apply()
    }

    fun moveCard(context: Context, cardId: String, newLocation: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedLocations = getSavedLocations(context).toMutableMap()
        savedLocations[cardId] = newLocation
        
        val locationsString = savedLocations.entries.joinToString(",") { "${it.key}:${it.value}" }
        prefs.edit().putString(CARD_LOCATIONS_KEY, locationsString).apply()
    }

    private fun getSavedOrder(context: Context): List<String> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val orderString = prefs.getString(CARD_ORDER_KEY, "") ?: ""
        return if (orderString.isEmpty()) emptyList() else orderString.split(",")
    }

    private fun getSavedLocations(context: Context): Map<String, String> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val locationsString = prefs.getString(CARD_LOCATIONS_KEY, "") ?: ""
        if (locationsString.isEmpty()) return emptyMap()
        
        return locationsString.split(",").associate { entry ->
            val parts = entry.split(":")
            parts[0] to parts[1]
        }
    }
}
