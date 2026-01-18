package com.cashwind.app

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import android.view.ViewGroup
import android.graphics.Color

/**
 * Minimal test activity with NO XML layout, NO ViewBinding
 * Created programmatically to test if theme is the issue
 */
class TestActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        android.util.Log.d("TestActivity", "onCreate called!")
        super.onCreate(savedInstanceState)
        
        // Create layout programmatically - NO XML
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setPadding(50, 50, 50, 50)
            setBackgroundColor(Color.WHITE)
        }
        
        val text = TextView(this).apply {
            text = "SUCCESS!\n\nTestActivity loaded without XML.\n\nThis means the theme is OK.\nThe problem is in MainActivity's XML layout or ViewBinding."
            textSize = 18f
            setTextColor(Color.BLACK)
        }
        
        layout.addView(text)
        setContentView(layout)
        
        android.util.Log.d("TestActivity", "TestActivity loaded successfully!")
    }
}
