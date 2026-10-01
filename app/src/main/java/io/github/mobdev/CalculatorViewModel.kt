package io.github.mobdev

import androidx.lifecycle.ViewModel

class
CalculatorViewModel : ViewModel() {

    var display = "0"
        private set
    var isError = false
        private set

    private var firstNumber = 0.0
    private var operator: Char? = null
    private var startNewNumber = true

    fun onDigit(digit: Char) {
        if (isError) onClear()
        if (startNewNumber || display == "0") {
            display = digit.toString()
            startNewNumber = false
        } else {
            display += digit
        }
    }

    fun onDot() {
        if (isError) onClear()
        if (startNewNumber) {
            display = "0."
            startNewNumber = false
        } else if (!display.contains('.')) {
            display += "."
        }
    }

    fun onOperator(op: Char) {
        if (isError) return
        if (operator != null && !startNewNumber) {
            calculate()
            if (isError) return
        }
        firstNumber = display.toDoubleOrNull() ?: 0.0
        operator = op
        startNewNumber = true
    }

    fun onEquals() {
        if (isError || operator == null || startNewNumber) return
        calculate()
        operator = null
        startNewNumber = true
    }

    fun onClear() {
        display = "0"
        isError = false
        firstNumber = 0.0
        operator = null
        startNewNumber = true
    }

    fun onBackspace() {
        if (isError || startNewNumber) return
        display = display.dropLast(1)
        if (display.isEmpty() || display == "-") {
            display = "0"
        }
    }

    fun onSign() {
        if (isError || display == "0") return
        if (display.startsWith("-")) {
            display = display.drop(1)
        } else {
            display = "-$display"
        }
    }

    private fun calculate() {
        val secondNumber = display.toDoubleOrNull() ?: 0.0
        val result = when (operator) {
            '+' -> firstNumber + secondNumber
            '-' -> firstNumber - secondNumber
            '*' -> firstNumber * secondNumber
            '/' -> firstNumber / secondNumber
            else -> secondNumber
        }
        if (result.isNaN() || result.isInfinite()) {
            isError = true
            operator = null
            startNewNumber = true
            return
        }
        display = format(result)
    }

    private fun format(value: Double): String {
        return if (value == Math.floor(value) && Math.abs(value) < 1e15) {
            value.toLong().toString()
        } else {
            value.toString()
        }
    }
}