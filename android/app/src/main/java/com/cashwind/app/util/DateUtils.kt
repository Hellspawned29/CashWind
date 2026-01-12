package com.cashwind.app.util

import java.text.SimpleDateFormat
import java.util.*

/**
 * Centralized date formatting utilities to avoid SimpleDateFormat duplication
 * and ensure consistent date handling across the app.
 */
object DateUtils {
    private val isoFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val displayFormat = SimpleDateFormat("MMM dd, yyyy", Locale.US)
    private val timestampFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
    private val monthYearFormat = SimpleDateFormat("MMM yyyy", Locale.US)
    private val fullMonthYearFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    
    /**
     * Parse ISO date string (yyyy-MM-dd) to Date object
     */
    fun parseIsoDate(dateString: String): Date? = try {
        isoFormat.parse(dateString)
    } catch (e: Exception) {
        null
    }
    
    /**
     * Format Date object to ISO string (yyyy-MM-dd)
     */
    fun formatIsoDate(date: Date): String = isoFormat.format(date)
    
    /**
     * Format Date object to display string (MMM dd, yyyy)
     */
    fun formatDisplayDate(date: Date): String = displayFormat.format(date)
    
    /**
     * Format Date object to timestamp (yyyy-MM-dd HH:mm:ss)
     */
    fun formatTimestamp(date: Date): String = timestampFormat.format(date)
    
    /**
     * Format Date object to month-year (MMM yyyy)
     */
    fun formatMonthYear(date: Date): String = monthYearFormat.format(date)
    
    /**
     * Format Date object to full month-year (MMMM yyyy)
     */
    fun formatFullMonthYear(date: Date): String = fullMonthYearFormat.format(date)
    
    /**
     * Get current date as ISO string (yyyy-MM-dd)
     */
    fun getCurrentIsoDate(): String = formatIsoDate(Date())
    
    /**
     * Get current timestamp (yyyy-MM-dd HH:mm:ss)
     */
    fun getCurrentTimestamp(): String = formatTimestamp(Date())
    
    /**
     * Get Calendar instance for today
     */
    fun getToday(): Calendar = Calendar.getInstance()
    
    /**
     * Format timestamp (long) to display date string
     */
    fun formatDisplayDate(timestamp: Long): String = formatDisplayDate(Date(timestamp))
    
    /**
     * Format timestamp (long) to ISO date string
     */
    fun formatIsoDate(timestamp: Long): String = formatIsoDate(Date(timestamp))
}
