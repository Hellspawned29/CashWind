package com.cashwind.app.util

import android.content.Context
import android.util.Log
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.*

object ErrorLogger {
    private const val ERROR_FILE_NAME = "cashwind_errors.log"
    private const val TAG = "ErrorLogger"
    
    fun logError(context: Context, tag: String, message: String, throwable: Throwable? = null) {
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
        val errorMessage = buildString {
            append("[$timestamp] [$tag] $message\n")
            
            if (throwable != null) {
                append("Exception: ${throwable.message}\n")
                append("Stack trace:\n")
                val stackTrace = StringWriter()
                throwable.printStackTrace(PrintWriter(stackTrace))
                append(stackTrace.toString())
            }
            append("\n")
            append("================================================================================")
            append("\n\n")
        }
        
        // Log to Android logcat
        if (throwable != null) {
            Log.e(tag, message, throwable)
        } else {
            Log.e(tag, message)
        }
        
        // Write to file
        try {
            val errorFile = File(context.getExternalFilesDir(null), ERROR_FILE_NAME)
            errorFile.appendText(errorMessage)
            Log.d(TAG, "Error logged to: ${errorFile.absolutePath}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to write error to file", e)
        }
    }
    
    fun getErrorLogPath(context: Context): String {
        return File(context.getExternalFilesDir(null), ERROR_FILE_NAME).absolutePath
    }
    
    fun clearErrorLog(context: Context) {
        try {
            val errorFile = File(context.getExternalFilesDir(null), ERROR_FILE_NAME)
            if (errorFile.exists()) {
                errorFile.delete()
                Log.d(TAG, "Error log cleared")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear error log", e)
        }
    }
    
    fun getErrorLog(context: Context): String {
        return try {
            val errorFile = File(context.getExternalFilesDir(null), ERROR_FILE_NAME)
            if (errorFile.exists()) {
                errorFile.readText()
            } else {
                "No errors logged"
            }
        } catch (e: Exception) {
            "Failed to read error log: ${e.message}"
        }
    }
}
