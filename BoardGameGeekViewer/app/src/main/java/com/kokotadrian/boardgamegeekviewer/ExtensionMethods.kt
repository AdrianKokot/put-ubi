package com.kokotadrian.boardgamegeekviewer

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

fun Fragment.setBarTitle(title: String) {
    (activity as AppCompatActivity?)?.supportActionBar?.title = title;
}