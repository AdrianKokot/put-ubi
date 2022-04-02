package com.kokotadrian.rpncalc

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.children
import com.kokotadrian.rpncalc.databinding.ActivityMainBinding
import java.util.ArrayDeque
import kotlin.math.sqrt

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var currentValue = "";

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        val containers =
//            arrayOf(
//                binding.calcFirstRow,
//                binding.calcSecondRow,
//                binding.calcThirdRow,
//                binding.calcFourthRow
//            )
//
//        val actionIds = arrayOf(
//            R.id.actionButtonBack,
//            R.id.actionButtonEnter,
//            R.id.actionButtonMinus,
//            R.id.actionButtonMultiply,
//            R.id.actionButtonPlus
//        );
//
//        for (container in containers) {
//            for (i in 0..container.childCount) {
//                val child = container.getChildAt(i)
//
//                if (child is Button && !actionIds.contains(child.id)) {
//                    child.setOnClickListener() {
//                        currentValue += child.text;
//                        binding.statusText.text = currentValue;
//                    }
//                }
//            }
//        }
//
//        binding.actionButtonBack.setOnClickListener() {
//            currentValue = currentValue.dropLast(1)
//            binding.statusText.text = currentValue;
//        }
    }

    private var canAddDot = true
    private var canAddNumber = true
    private var canAddOperator = false

    private var stack = ArrayDeque<Double>()

    fun onNumberButtonClick(view: View) {
        if (view is Button) {
            if (canAddNumber) {
                binding.statusText.append(view.text);
                canAddOperator = false
            }
        }
    }

    fun onDotButtonClick(view: View) {
        if (canAddDot) {
            binding.statusText.append(".");
            canAddDot = false
        }
    }

    fun onBackButtonClick(view: View) {
        binding.statusText.text = binding.statusText.text.dropLast(1)
        canAddDot = !binding.statusText.text.contains(".");
        canAddOperator = binding.statusText.text.isEmpty() && stack.size >= 2
    }

    fun onEnterButtonClick(view: View) {
        if (binding.statusText.text.isNotEmpty()) {
            val newNumber = binding.statusText.text.toString().toDouble()
            pushOnStack(newNumber)
            binding.statusText.text = ""

            canAddOperator = stack.size >= 2
        }
    }

    fun onOperatorButtonClick(view: View) {
        if (view is Button) {
            if (canAddOperator) {
                val num1 = stack.pop()
                val num2 = stack.pop()

                val result: Double = when (view.id) {
                    R.id.actionButtonPlus -> num1 + num2
                    R.id.actionButtonMinus -> num2 - num1
                    R.id.actionButtonDivide -> num2 / num1
                    R.id.actionButtonMultiply -> num1 * num2
                    R.id.actionButtonSqrt -> {
                        stack.push(num2)
                        sqrt(num1)
                    }
                    R.id.actionButtonPower -> Math.pow(num1, num2)
                    else -> 0.0
                }

                pushOnStack(result)
                binding.statusText.text = result.toString()
            }
        }
    }

    fun onChangeSignButtonClick(view: View) {}
    fun onDropButtonClick(view: View) {}
    fun onSwapButtonClick(view: View) {}
    fun onACButtonClick(view: View) {}

    fun pushOnStack(value: Double) {
        stack.push(value)
    }
}