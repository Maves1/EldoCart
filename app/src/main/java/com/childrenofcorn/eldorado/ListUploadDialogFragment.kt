package com.childrenofcorn.eldorado

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment

class ListUploadDialogFragment(var productsArrayList: ArrayList<Product>,
                               var activity: ListActivity) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {

            val builder = AlertDialog.Builder(it)
            builder.setMessage(R.string.dialog_anonymous_list_message)
                .setPositiveButton(R.string.yes,
                    DialogInterface.OnClickListener { dialog, id ->
                        if (productsArrayList.size > 0) {
                            activity.uploadListToFirebase(productsArrayList,
                                true,
                                "")
                        }
                    })
                .setNegativeButton(R.string.no,
                    DialogInterface.OnClickListener { dialog, id ->
                        if (productsArrayList.size > 0) {

                        }
                    })

            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}