package com.kokotadrian.boardgamegeekviewer

import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*

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

fun getFormatter(formatStyle: FormatStyle): DateTimeFormatter {
    return DateTimeFormatter.ofLocalizedDateTime(formatStyle)
        .withLocale(Locale.GERMAN)
        .withZone(ZoneId.systemDefault())
}

fun formatInstant(instant: Instant, formatStyle: FormatStyle): String {
    return getFormatter(formatStyle).format(instant)
}