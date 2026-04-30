package com.example.aidhub.utilities

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.view.marginStart
import com.example.aidhub.R
import com.example.data.dataStractures.DialogData
import com.example.data.dataStractures.DialogType
import com.example.data.dataStractures.Status
import com.example.data.dataStractures.ToastType

object DialogHelper {


    fun showAlertDialog(context: Context, dialogType: DialogType, onConfirm: (Int, String) -> Unit) {
        var showInput = false
        if (dialogType == DialogType.CONFIRM_DELETE) {
            showInput = true
        }
        showDialog(
            context,
            dialogType.data,
            showInput = showInput,
            onConfirm = onConfirm
        )
    }

    private fun showDialog(
        context: Context,
        data: DialogData,
        showInput: Boolean,
        onConfirm: (Int, String) -> Unit
    ) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_layout, null)

        val txtMessage = dialogView.findViewById<TextView>(R.id.txtMessage)
        val txtTitle = dialogView.findViewById<TextView>(R.id.txtTitle)
        val btnPositive = dialogView.findViewById<Button>(R.id.btnPositive)
        val btnNavigate = dialogView.findViewById<Button>(R.id.btnNavigate)
        val imgIcon = dialogView.findViewById<ImageView>(R.id.imgIcon)
        val inputField = dialogView.findViewById<EditText>(R.id.editTextPassword)

        if (showInput) {
            inputField.visibility = View.VISIBLE
        } else {
            inputField.visibility = View.GONE
        }

        txtTitle.text = data.title
        txtMessage.text = data.message
        imgIcon.setImageResource(data.icon)
        btnPositive.text = data.positiveButtonText
        btnNavigate.text = data.negativeButtonText

        val alertDialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        alertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        btnPositive.setOnClickListener {
            if (showInput) {
                val password = inputField.text.toString()
                if (password.isEmpty()) {
                    ToastHelper.getInstance().showError(ToastType.EMPTY_PASSWORD.message)
                    return@setOnClickListener
                } else {
                    onConfirm(Constants.BUTTON_POSITIVE_KEY, password)
                }
            } else
                onConfirm(Constants.BUTTON_POSITIVE_KEY, "")
            alertDialog.dismiss()
        }

        btnNavigate.setOnClickListener {
            onConfirm(Constants.BUTTON_NEGATIVE_KEY, "")
            alertDialog.dismiss()
        }

        alertDialog.show()
    }


    fun showRequestCompletedDialog(
        context: Context,
        status: String,
        rating: Double,
        points: Int,
        onOkPressed: () -> Unit
    ) {
        val dialogView =
            LayoutInflater.from(context).inflate(R.layout.dialog_request_completed, null)
        val txtMessage = dialogView.findViewById<TextView>(R.id.txtMessage)
        val txtTitle = dialogView.findViewById<TextView>(R.id.txtTitle)
        val btnOk = dialogView.findViewById<Button>(R.id.btnOk)

        val title = if (status == Status.COMPLETED.displayName)
            "Request Completed!" else "The request has been cancelled!"

        val message = if (status == Status.COMPLETED.displayName)
            "Rating: $rating | Scored: $points points" else ""

        val btnText = if (status == Status.COMPLETED.displayName)
            "Awesome!" else "OK"


        txtTitle.text = title
        txtMessage.text = message
        btnOk.text = btnText

        val alertDialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        alertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        btnOk.setOnClickListener {
            onOkPressed()
            alertDialog.dismiss()
        }
        alertDialog.show()

    }
}
