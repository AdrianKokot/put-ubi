package com.kokotadrian.rpncalc

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.children
import com.kokotadrian.rpncalc.databinding.ActivityMainBinding
import java.util.ArrayDeque
import kotlin.math.sqrt

class MainActivity : AppCompatActivity() {

    private lateinit var stackTexts: Array<TextView>
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)

        stackTexts = arrayOf(
            binding.stackValue1,
            binding.stackValue2,
            binding.stackValue3,
            binding.stackValue4
        )

        setContentView(binding.root)
    }

    private var canAddDot = true
    private var canAddNumber = true

    private var stack = ArrayDeque<Double>()

    fun onNumberButtonClick(view: View) {
        if (view is Button) {
            if (canAddNumber) {
                appendToCalcTextField(view.text);
            }
        }
    }

    fun onDotButtonClick(view: View) {
        if (canAddDot) {
            binding.calcText.append(".");
            canAddDot = false
        }
    }

    fun onBackButtonClick(view: View) {
        calcTextField = calcTextField.dropLast(1)
        canAddDot = !calcTextField.contains(".")
    }

    fun onEnterButtonClick(view: View) {
        if (calcTextField.isNotEmpty()) {
            pushOnStack(calcTextFieldValue)
            resetCalcTextFieldValue()
            canAddDot = true
        }
    }

    private fun appendToCalcTextField(value: CharSequence) {
        binding.calcText.append(value);
    }

    private fun appendToCalcTextField(value: String) {
        binding.calcText.append(value);
    }

    private val calcTextFieldValue: Double
        get() = calcTextField.toDouble()

    private fun resetCalcTextFieldValue() {
        calcTextField = ""
    }

    private var calcTextField: String
        get() = binding.calcText.text.toString()
        set(value) {
            binding.calcText.text = value
        }


    fun onOperatorButtonClick(view: View) {
        if (view is Button) {

            val isSingleArgumentOperator = view.id == R.id.actionButtonSqrt

            val result: Double

            if (isSingleArgumentOperator) {

                val value = if (calcTextField.isEmpty()) stack.pop() else calcTextFieldValue

                result = when (view.id) {
                    R.id.actionButtonSqrt -> sqrt(value)
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
                            val toast =
                                Toast.makeText(this, "You cannot divide by 0", Toast.LENGTH_LONG)
                            toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0)
                            toast.show()

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

    fun onChangeSignButtonClick(view: View) {}
    fun onDropButtonClick(view: View) {}
    fun onSwapButtonClick(view: View) {}

    fun onACButtonClick(view: View) {
        stack.clear()
        resetCalcTextFieldValue()
        renderStackValues()
    }

    private var precisionDigits = 8

    private fun renderStackValues() {

        val stackTextViewCount = stackTexts.size

        val stackSize = if (stack.size >= stackTextViewCount) stackTextViewCount else stack.size

        val stackValues = stack.take(stackSize).toTypedArray()

        for (v in 0 until stackTextViewCount) {
            stackTexts[v].text =
                if (v >= stackSize) "" else "%.${precisionDigits}f".format(stackValues[v]).trimEnd('0', ',', '.')
        }
    }

    private fun pushOnStack(value: Double) {
        stack.push(value)
        renderStackValues()
    }
}