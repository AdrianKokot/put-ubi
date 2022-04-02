package com.kokotadrian.rpncalc

import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.SettingsSlicesContract
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.kokotadrian.rpncalc.databinding.ActivityMainBinding
import java.lang.Exception
import java.text.DecimalFormat
import java.util.*
import kotlin.math.max
import kotlin.math.sqrt

class MainActivity : AppCompatActivity() {

    private lateinit var stackTexts: Array<TextView>
    private lateinit var binding: ActivityMainBinding

    private var currentAccentColor: String = "";

    private val df: DecimalFormat = DecimalFormat("0")

    private val REQUEST_CODE = 2000

    private var canAddDot = true

    private var stack = ArrayDeque<Double>()

    private val calcTextFieldValue: Double
        get() = calcTextField.toDouble()

    private var calcTextField: String
        get() = binding.calcText.text.toString()
        set(value) {
            binding.calcText.text = value
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)

        stackTexts = arrayOf(
            binding.stackValue1,
            binding.stackValue2,
            binding.stackValue3,
            binding.stackValue4
        )

        df.maximumFractionDigits = 4
        currentAccentColor = "#" + Integer.toHexString(resources.getColor(R.color.accent_700))

        setContentView(binding.root)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                if (data.hasExtra("precision")) {
                    val precision = data.extras?.get("precision").toString().toInt()
                    df.maximumFractionDigits = precision

                    try {
                        val color = Color.parseColor(data.extras?.get("color").toString())
                        binding.actionButtonAC.setBackgroundTintList(ColorStateList.valueOf(color))
                        currentAccentColor = data.extras?.get("color").toString()
                    } catch (e: Exception) {

                    }
                    renderStackValues()
                }
            }
        }
    }


    private fun appendToCalcTextField(value: CharSequence) {
        binding.calcText.append(value);
    }

    private fun resetCalcTextFieldValue() {
        calcTextField = ""
        showTopStackValueRow()
    }

    private fun pushOnStack(value: Double) {
        stack.push(value)
        renderStackValues()
    }

    private fun renderStackValues() {
        val textViewCount = stackTexts.size
        val stackSize = if (stack.size >= textViewCount) textViewCount else stack.size
        val stackValues = stack.take(stackSize).toTypedArray()

        for (v in 0 until textViewCount) {
            stackTexts[v].text =
                if (v >= stackSize)
                    ""
                else {
                    if (stackValues[v].equals(0.0)) "0"
                    else df.format(stackValues[v])
                }
        }
    }

    fun onNumberButtonClick(view: View) {
        if (view is Button) {
            appendToCalcTextField(view.text);

            hideTopStackValueRow()
        }
    }

    fun onDotButtonClick(view: View) {
        if (canAddDot) {
            binding.calcText.append(".");
            canAddDot = false

            hideTopStackValueRow()
        }
    }

    private fun hideTopStackValueRow() {
        binding.stackDisplay4.visibility = View.GONE
    }

    private fun showTopStackValueRow() {
        binding.stackDisplay4.visibility = View.VISIBLE
    }

    fun onBackButtonClick(view: View) {
        calcTextField = calcTextField.dropLast(1)
        canAddDot = !calcTextField.contains(".")

        if (calcTextField.isEmpty()) {
            showTopStackValueRow()
        }
    }

    fun onEnterButtonClick(view: View) {
        if (calcTextField.isNotEmpty()) {
            pushOnStack(calcTextFieldValue)
        } else if (stack.size > 0) {
            pushOnStack(stack.first())
        }

        resetCalcTextFieldValue()
        canAddDot = true
    }

    private fun showToast(text: String) {
        val toast =
            Toast.makeText(this, text, Toast.LENGTH_LONG)
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0)
        toast.show()
    }

    fun onOperatorButtonClick(view: View) {
        if (view is Button) {

            val isSingleArgumentOperator = view.id == R.id.actionButtonSqrt

            val result: Double

            if (isSingleArgumentOperator) {

                val value = if (calcTextField.isEmpty()) stack.pop() else calcTextFieldValue

                result = when (view.id) {
                    R.id.actionButtonSqrt -> {
                        if (value >= 0) {
                            sqrt(value)
                        } else {
                            showToast("You cannot square negative number")
                            value
                        }
                    }
                    else -> 0.0
                }

            } else {

                if (calcTextField.isEmpty() && stack.size < 2 || calcTextField.isNotEmpty() && stack.size == 0) {
                    return
                }

                val values = arrayOf(
                    (if (calcTextField.isEmpty()) stack.pop() else calcTextFieldValue),
                    stack.pop()
                )

                result = when (view.id) {
                    R.id.actionButtonPlus -> values[1] + values[0]
                    R.id.actionButtonMinus -> values[1] - values[0]
                    R.id.actionButtonDivide -> {
                        if (values[0] == 0.0) {
                            showToast("You cannot divide by 0")
                            values[1]
                        } else {
                            values[1] / values[0]
                        }
                    }
                    R.id.actionButtonMultiply -> values[1] * values[0]
                    R.id.actionButtonPower -> Math.pow(values[1], values[0])
                    else -> 0.0
                }

            }

            pushOnStack(result)
            resetCalcTextFieldValue()
        }
    }

    fun onChangeSignButtonClick(view: View) {
        if (stack.size > 0) {
            pushOnStack(stack.pop() * -1)
        }
    }

    fun onDropButtonClick(view: View) {
        if (stack.size > 0) {
            stack.pop()
            renderStackValues()
        }
    }

    fun onSwapButtonClick(view: View) {
        if (stack.size >= 2) {
            val a = stack.removeLast()
            val b = stack.removeLast()
            stack.addLast(a)
            stack.addLast(b)

            renderStackValues()
        }
    }

    fun onACButtonClick(view: View) {
        stack.clear()
        resetCalcTextFieldValue()
        renderStackValues()
    }

    fun openSettings(view: View) {
        val i = Intent(this, SettingsActivity::class.java)
        i.putExtra("precision", df.maximumFractionDigits)
        i.putExtra("color", currentAccentColor)

        startActivityForResult(i, REQUEST_CODE)
    }
}