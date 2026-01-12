package com.cashwind.app

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.cashwind.app.database.CashwindDatabase
import com.google.android.material.button.MaterialButton

/**
 * Base activity providing common functionality for all activities in the app.
 * Handles:
 * - Database instance initialization
 * - Back button setup (supports both Button and MaterialButton)
 * - Common lifecycle logging
 */
abstract class BaseActivity : AppCompatActivity() {

    protected lateinit var database: CashwindDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        database = CashwindDatabase.getInstance(this)
    }

    /**
     * Sets up a back button that finishes the activity when clicked.
     * Call this after setContentView() with the ID of your back button.
     */
    protected fun setupBackButton(buttonId: Int) {
        findViewById<Button>(buttonId)?.setOnClickListener {
            finish()
        }
    }

    /**
     * Auto-wires the back button after content view is set.
     * Supports both Button and MaterialButton with R.id.backButton.
     */
    private fun autoWireBackButton() {
        // Try MaterialButton first (it's a subclass of Button)
        val backButton = findViewById<MaterialButton>(R.id.backButton)
            ?: findViewById<Button>(R.id.backButton)
        
        backButton?.setOnClickListener {
            finish()
        }
    }

    /**
     * Override this to provide the layout resource ID for the activity.
     * Return null if you want to set content view manually.
     */
    protected open fun getLayoutResId(): Int? = null

    override fun setContentView(layoutResID: Int) {
        super.setContentView(layoutResID)
        autoWireBackButton()
    }

    override fun setContentView(view: View?) {
        super.setContentView(view)
        autoWireBackButton()
    }
}
