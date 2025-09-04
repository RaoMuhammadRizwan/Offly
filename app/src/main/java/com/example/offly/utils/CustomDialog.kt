package com.example.offly.utils

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.TextView
import com.example.offly.R

class CustomDialog(private val context: Context) {

    fun showPermissionDialog(
        title : String,
        message : String,
        positiveBtnText : String = "Grant",
        negativeBtnText : String = "Cancel",
        onPositiveButtonClick : (() -> Unit)? = null,
        onNegativeButtonClick : (() -> Unit)? = null
    ){
        val dialog = Dialog(context)
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_permissions , null)
        dialog.setContentView(view)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val tvTitle = view.findViewById<TextView>(R.id.dialogTitle)
        val tvMessage = view.findViewById<TextView>(R.id.dialogMessage)
        val btnNegative = view.findViewById<TextView>(R.id.btnCancel)
        val btnPositive = view.findViewById<TextView>(R.id.btnGrantNow)

        tvTitle.text = title
        tvMessage.text = message
        btnNegative.text = negativeBtnText
        btnPositive.text = positiveBtnText

        btnNegative.setOnClickListener {
            onNegativeButtonClick?.invoke()
            dialog.dismiss()
        }
        btnPositive.setOnClickListener {
            onPositiveButtonClick?.invoke()
            dialog.dismiss()
        }
        dialog.setCancelable(false)
        dialog.show()






    }
}