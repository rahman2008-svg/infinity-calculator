package com.example.util

import kotlin.math.*

object MathEvaluator {
    fun evaluate(expression: String, isDegreeMode: Boolean = true): Double {
        val sanitized = expression
            .replace("×", "*")
            .replace("÷", "/")
            .replace("π", Math.PI.toString())
            .replace("e", Math.E.toString())
            .replace(" ", "")

        return try {
            Parser(sanitized, isDegreeMode).parse()
        } catch (e: Exception) {
            Double.NaN
        }
    }

    private class Parser(val input: String, val isDegreeMode: Boolean) {
        var pos = -1
        var ch = 0

        fun nextChar() {
            pos++
            ch = if (pos < input.length) input[pos].code else -1
        }

        fun eat(charToEat: Int): Boolean {
            while (ch == ' '.code) nextChar()
            if (ch == charToEat) {
                nextChar()
                return true
            }
            return false
        }

        fun parse(): Double {
            nextChar()
            val x = parseExpression()
            if (pos < input.length) throw RuntimeException("Unexpected: " + ch.toChar())
            return x
        }

        // Expression = Term + Term | Term - Term
        fun parseExpression(): Double {
            var x = parseTerm()
            while (true) {
                if (eat('+'.code)) x += parseTerm() // addition
                else if (eat('-'.code)) x -= parseTerm() // subtraction
                else return x
            }
        }

        // Term = Factor * Factor | Factor / Factor
        fun parseTerm(): Double {
            var x = parseFactor()
            while (true) {
                if (eat('*'.code)) x *= parseFactor() // multiplication
                else if (eat('/'.code)) {
                    val divisor = parseFactor()
                    if (divisor == 0.0) throw ArithmeticException("Division by zero")
                    x /= divisor // division
                } else return x
            }
        }

        // Factor = +Factor | -Factor | (Expression) | Number | Function | Factor ^ Factor
        fun parseFactor(): Double {
            if (eat('+'.code)) return parseFactor() // unary plus
            if (eat('-'.code)) return -parseFactor() // unary minus

            var x: Double
            val startPos = this.pos
            if (eat('('.code)) { // parentheses
                x = parseExpression()
                eat(')'.code)
            } else if ((ch >= '0'.code && ch <= '9'.code) || ch == '.'.code) { // numbers
                while ((ch >= '0'.code && ch <= '9'.code) || ch == '.'.code) nextChar()
                x = input.substring(startPos, this.pos).toDouble()
            } else if (ch >= 'a'.code && ch <= 'z'.code) { // functions
                while (ch >= 'a'.code && ch <= 'z'.code) nextChar()
                val func = input.substring(startPos, this.pos)
                x = if (eat('('.code)) {
                    val arg = parseExpression()
                    eat(')'.code)
                    evaluateFunction(func, arg)
                } else {
                    throw RuntimeException("Unknown function/format")
                }
            } else {
                throw RuntimeException("Unexpected: " + ch.toChar())
            }

            // Handle percentage (e.g. 50%)
            if (eat('%'.code)) {
                x *= 0.01
            }

            // Handle exponentiation
            if (eat('^'.code)) x = x.pow(parseFactor())

            return x
        }

        private fun evaluateFunction(func: String, arg: Double): Double {
            return when (func) {
                "sin" -> {
                    val radians = if (isDegreeMode) Math.toRadians(arg) else arg
                    val rawResult = sin(radians)
                    // Clean up near-zero values to exact zero
                    if (abs(rawResult) < 1e-15) 0.0 else rawResult
                }
                "cos" -> {
                    val radians = if (isDegreeMode) Math.toRadians(arg) else arg
                    val rawResult = cos(radians)
                    if (abs(rawResult) < 1e-15) 0.0 else rawResult
                }
                "tan" -> {
                    val radians = if (isDegreeMode) Math.toRadians(arg) else arg
                    val rawResult = tan(radians)
                    if (abs(rawResult) > 1e15) Double.NaN else rawResult
                }
                "asin" -> {
                    val rad = asin(arg)
                    if (isDegreeMode) Math.toDegrees(rad) else rad
                }
                "acos" -> {
                    val rad = acos(arg)
                    if (isDegreeMode) Math.toDegrees(rad) else rad
                }
                "atan" -> {
                    val rad = atan(arg)
                    if (isDegreeMode) Math.toDegrees(rad) else rad
                }
                "log" -> log10(arg)
                "ln" -> ln(arg)
                "sqrt" -> {
                    if (arg < 0.0) throw ArithmeticException("Square root of negative number")
                    sqrt(arg)
                }
                "abs" -> abs(arg)
                else -> throw RuntimeException("Unknown function: $func")
            }
        }
    }
}
