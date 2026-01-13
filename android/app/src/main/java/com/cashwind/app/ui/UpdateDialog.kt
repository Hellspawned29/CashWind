package com.cashwind.app.ui

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Window
import android.widget.Button
import android.widget.TextView
import com.cashwind.app.R
import com.cashwind.app.util.UpdateChecker

class UpdateDialog(
    context: Context,
    private val updateInfo: UpdateChecker.UpdateInfo,
    private val onUpdate: () -> Unit
) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_update)

        val titleText = findViewById<TextView>(R.id.updateTitle)
        val messageText = findViewById<TextView>(R.id.updateMessage)
        val notesText = findViewById<TextView>(R.id.releaseNotes)
        val updateButton = findViewById<Button>(R.id.updateButton)
        val laterButton = findViewById<Button>(R.id.laterButton)

        titleText.text = "Update Available"
        messageText.text = "Version ${updateInfo.version} is now available!"
        notesText.text = updateInfo.releaseNotes

        updateButton.setOnClickListener {
            onUpdate()
            dismiss()
        }

        laterButton.setOnClickListener {
            dismiss()
        }
    }
}
