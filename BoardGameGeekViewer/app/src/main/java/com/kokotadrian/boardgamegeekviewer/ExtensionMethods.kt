package com.kokotadrian.boardgamegeekviewer

import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

fun Fragment.setBarTitle(title: String) {
    (activity as AppCompatActivity?)?.supportActionBar?.title = title;
}

fun Fragment.showToast(text: String) {
    Toast.makeText(
        activity as MainActivity,
        text,
        Toast.LENGTH_LONG
    ).show()
}

fun Fragment.showPleaseWaitToast() {
    showToast("Please try again in a few seconds. The request is being processed.")
}