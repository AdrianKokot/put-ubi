package com.kokotadrian.rpncalc

import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.kokotadrian.rpncalc.databinding.SettingsActivityBinding
import java.lang.Exception

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: SettingsActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        binding = SettingsActivityBinding.inflate(layoutInflater)


        val extras = intent.extras ?: return

        val precision = extras.get("precision")?.toString()
        val color = extras.get("color")?.toString()

        findViewById<EditText>(R.id.precisionValue).setText(precision)
        findViewById<EditText>(R.id.colorValue).setText(color)

        if (color != null) {
            setAccentColor(color)
        }
    }

    fun closeSettings(view: View) {
        super.finish()
    }

    fun saveSettings(view: View) {
        val data = Intent()

        val precision = findViewById<EditText>(R.id.precisionValue).text.toString()
        val color = findViewById<EditText>(R.id.colorValue).text.toString()
        setAccentColor(color)

        data.putExtra("precision", precision)
        data.putExtra("color", color)

        setResult(Activity.RESULT_OK, data)

        closeSettings(view)
    }

    private fun setAccentColor(color: String) {
        try {
            findViewById<Button>(R.id.saveButton).backgroundTintList =
                ColorStateList.valueOf(Color.parseColor(color))
        } catch (e: Exception) {
        }

    }
}