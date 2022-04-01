package com.kokotadrian.rpncalc

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.children
import com.kokotadrian.rpncalc.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var currentValue = "";

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val containers =
            arrayOf(
                binding.calcFirstRow,
                binding.calcSecondRow,
                binding.calcThirdRow,
                binding.calcFourthRow
            )

        val actionIds = arrayOf(
            R.id.actionButtonBack,
            R.id.actionButtonEnter,
            R.id.actionButtonMinus,
            R.id.actionButtonMultiply,
            R.id.actionButtonPlus
        );

        for (container in containers) {
            for (i in 0..container.childCount) {
                val child = container.getChildAt(i)

                if (child is Button && !actionIds.contains(child.id)) {
                    child.setOnClickListener() {
                        currentValue += child.text;
                        binding.statusText.text = currentValue;
                    }
                }
            }
        }

        binding.actionButtonBack.setOnClickListener() {
            currentValue = currentValue.dropLast(1)
            binding.statusText.text = currentValue;
        }
    }
}