package com.cashwind.app

import android.os.Bundle
import android.widget.TextView
import android.widget.LinearLayout
import android.view.ViewGroup
import android.view.Gravity

/**
 * Ultra-minimal test activity with NO XML, NO ViewBinding, NO BaseActivity
 */
class SimpleTestActivity : androidx.appcompat.app.AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        android.util.Log.d("SimpleTestActivity", "onCreate started")
        
        // Create layout entirely in code
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            gravity = Gravity.CENTER
            setBackgroundColor(android.graphics.Color.WHITE)
        }
        
        val textView = TextView(this).apply {
            text = "✅ SimpleTestActivity Loaded!\n\nThis proves the click mechanism works.\n\nThe problem is in the other activities."
            textSize = 18f
            gravity = Gravity.CENTER
            setTextColor(android.graphics.Color.BLACK)
            setPadding(40, 40, 40, 40)
        }
        
        layout.addView(textView)
        setContentView(layout)
        
        android.util.Log.d("SimpleTestActivity", "onCreate completed successfully")
    }
}
