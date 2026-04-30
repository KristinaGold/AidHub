package com.example.aidhub.utilities

import android.annotation.SuppressLint
import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.view.ContextThemeWrapper
import com.example.aidhub.R

class ToastHelper(private val context: Context) {

    companion object {

        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var instance: ToastHelper? = null

        fun init(context: Context): ToastHelper {
            return instance ?: synchronized(this) {
                instance ?: ToastHelper(context).also { instance = it }
            }
        }

        fun getInstance(): ToastHelper {
            return instance ?: throw IllegalStateException(
                "ToastHelper must be initialized by calling init(context) before use."
            )
        }
    }

    private var currentToast: Toast? = null

    private fun show(message: String, icon: Int, isLong: Boolean = false) {
        currentToast?.cancel()
        
        val themedContext = ContextThemeWrapper(context, R.style.Theme_AidHub)
        val inflater = LayoutInflater.from(themedContext)
        val layout = inflater.inflate(R.layout.layout_custom_toast, null)
        
        val text: TextView = layout.findViewById(R.id.toastText)
        val toastIcon: ImageView = layout.findViewById(R.id.toastIcon)
        val duration = if (isLong) Toast.LENGTH_LONG else Toast.LENGTH_SHORT

        text.text = message
        toastIcon.setImageResource(icon)
        
        currentToast = Toast(context)
        currentToast?.duration = duration
        currentToast?.view = layout
        currentToast?.setGravity(Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, 150)

        currentToast?.show()
    }

    fun showError(message: String) {
        show(message, R.drawable.ic_fail)
    }

    fun showSuccess(message: String) {
        show(message, R.drawable.ic_success)
    }
}