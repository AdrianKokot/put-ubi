package com.kokotadrian.rpncalc

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.kokotadrian.rpncalc.databinding.SettingsActivityBinding

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: SettingsActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        binding = SettingsActivityBinding.inflate(layoutInflater)


        val extras = intent.extras ?: return

        val precision = extras.get("precision")?.toString()

        findViewById<EditText>(R.id.precisionValue).setText(precision.toString())
    }

    fun closeSettings(view: View) {
        super.finish()
    }

    fun saveSettings(view: View) {
        val data = Intent()

        val precision = findViewById<EditText>(R.id.precisionValue).text.toString()

        Toast.makeText(this, precision, Toast.LENGTH_LONG).show()

        data.putExtra("precision", precision)
        setResult(Activity.RESULT_OK, data)

        closeSettings(view)
    }
}