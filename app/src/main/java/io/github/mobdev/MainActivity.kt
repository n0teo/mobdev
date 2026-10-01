package io.github.mobdev

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private val viewModel: CalculatorViewModel by viewModels()
    private lateinit var display: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.screen)
        display = findViewById(R.id.display)

        bindDigit(R.id.btn_0, '0')
        bindDigit(R.id.btn_1, '1')
        bindDigit(R.id.btn_2, '2')
        bindDigit(R.id.btn_3, '3')
        bindDigit(R.id.btn_4, '4')
        bindDigit(R.id.btn_5, '5')
        bindDigit(R.id.btn_6, '6')
        bindDigit(R.id.btn_7, '7')
        bindDigit(R.id.btn_8, '8')
        bindDigit(R.id.btn_9, '9')

        bindOperator(R.id.btn_plus, '+')
        bindOperator(R.id.btn_minus, '-')
        bindOperator(R.id.btn_multiply, '*')
        bindOperator(R.id.btn_divide, '/')

        findViewById<Button>(R.id.btn_dot).setOnClickListener {
            viewModel.onDot()
            updateDisplay()
        }
        findViewById<Button>(R.id.btn_equals).setOnClickListener {
            viewModel.onEquals()
            updateDisplay()
        }
        findViewById<Button>(R.id.btn_clear).setOnClickListener {
            viewModel.onClear()
            updateDisplay()
        }
        findViewById<Button>(R.id.btn_backspace).setOnClickListener {
            viewModel.onBackspace()
            updateDisplay()
        }
        findViewById<Button>(R.id.btn_sign).setOnClickListener {
            viewModel.onSign()
            updateDisplay()
        }

        updateDisplay()
    }

    private fun bindDigit(buttonId: Int, digit: Char) {
        findViewById<Button>(buttonId).setOnClickListener {
            viewModel.onDigit(digit)
            updateDisplay()
        }
    }

    private fun bindOperator(buttonId: Int, operator: Char) {
        findViewById<Button>(buttonId).setOnClickListener {
            viewModel.onOperator(operator)
            updateDisplay()
        }
    }

    private fun updateDisplay() {
        if (viewModel.isError) {
            display.text = getString(R.string.error)
        } else {
            display.text = viewModel.display
        }
    }
}
