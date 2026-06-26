package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.media.AudioManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.CalcDatabase
import com.example.data.model.CalcNote
import com.example.data.model.HistoryItem
import com.example.data.repository.CalcRepository
import com.example.util.MathEvaluator
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

class CalcViewModel(application: Application) : AndroidViewModel(application) {

    private val sharedPreferences = application.getSharedPreferences("infinity_calc_prefs", Context.MODE_PRIVATE)
    private val database = CalcDatabase.getDatabase(application)
    private val repository = CalcRepository(database.calcDao())

    // Database flows
    val historyItems: StateFlow<List<HistoryItem>> = repository.allHistory.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val favoriteItems: StateFlow<List<HistoryItem>> = repository.favoriteHistory.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val notes: StateFlow<List<CalcNote>> = repository.allNotes.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // --- SETTINGS STATES ---
    var themeMode by mutableStateOf(sharedPreferences.getString("theme_mode", "dark") ?: "dark")
        private set
    var useMaterialYou by mutableStateOf(sharedPreferences.getBoolean("material_you", true))
        private set
    var soundEnabled by mutableStateOf(sharedPreferences.getBoolean("sound_enabled", true))
        private set
    var hapticsEnabled by mutableStateOf(sharedPreferences.getBoolean("haptics_enabled", true))
        private set
    var decimalPrecision by mutableStateOf(sharedPreferences.getInt("decimal_precision", 4))
        private set

    // --- STANDARD / SCIENTIFIC CALCULATOR STATES ---
    var expression by mutableStateOf("")
        private set
    var liveResult by mutableStateOf("")
        private set
    var isDegreeMode by mutableStateOf(sharedPreferences.getBoolean("degree_mode", true))
        private set

    // --- UNIT CONVERTER STATES ---
    var converterCategory by mutableStateOf("Length")
    var converterInput by mutableStateOf("1")
    var converterFromUnit by mutableStateOf("m")
    var converterToUnit by mutableStateOf("km")
    var converterResult by mutableStateOf("0.001")

    // --- FINANCE CALCULATOR STATES ---
    // Loan / EMI
    var loanPrincipal by mutableStateOf("100000")
    var loanRate by mutableStateOf("7.5")
    var loanTenure by mutableStateOf("5")
    var loanTenureType by mutableStateOf("years") // "years" or "months"
    var emiResult by mutableStateOf("")
    var totalInterestResult by mutableStateOf("")
    var totalPaymentResult by mutableStateOf("")

    // Compound Interest
    var ciPrincipal by mutableStateOf("10000")
    var ciRate by mutableStateOf("6.0")
    var ciTime by mutableStateOf("3")
    var ciFrequency by mutableStateOf("12") // 1=yearly, 4=quarterly, 12=monthly
    var ciFinalAmountResult by mutableStateOf("")
    var ciInterestEarnedResult by mutableStateOf("")

    // Discount
    var discountOriginalPrice by mutableStateOf("1200")
    var discountPercentage by mutableStateOf("15")
    var discountTaxPercentage by mutableStateOf("5")
    var discountSavedAmountResult by mutableStateOf("")
    var discountTaxAmountResult by mutableStateOf("")
    var discountFinalPriceResult by mutableStateOf("")

    // --- HEALTH CALCULATOR STATES ---
    var healthWeight by mutableStateOf("70") // kg
    var healthHeight by mutableStateOf("175") // cm
    var healthAge by mutableStateOf("25")
    var healthGender by mutableStateOf("Male") // "Male" or "Female"
    var bmiValueResult by mutableStateOf("")
    var bmiCategoryResult by mutableStateOf("")
    var bmrResult by mutableStateOf("")
    var dailyWaterResult by mutableStateOf("")
    var dailyCalorieResult by mutableStateOf("")

    // --- PROGRAMMER CALCULATOR STATES ---
    var programmerBase by mutableStateOf("Decimal") // Binary, Octal, Decimal, Hexadecimal
    var programmerInput by mutableStateOf("123")
    var programmerBinResult by mutableStateOf("1111011")
    var programmerOctResult by mutableStateOf("173")
    var programmerDecResult by mutableStateOf("123")
    var programmerHexResult by mutableStateOf("7B")

    // --- DATE CALCULATOR STATES ---
    var dateCalcBirthdate by mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()))
    var dateCalcTargetDate by mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()))
    var dateDifferenceResult by mutableStateOf("")
    var ageResultYears by mutableStateOf("")
    var ageResultMonthsAndDays by mutableStateOf("")
    var ageNextBirthdayDays by mutableStateOf("")

    init {
        // Run initial calculations
        updateUnitConversion()
        calculateEmi()
        calculateCompoundInterest()
        calculateDiscount()
        calculateHealth()
        updateProgrammerBases()
        calculateAge()
    }

    // --- SOUND & HAPTIC HELPERS ---
    private fun playSoundAndHaptic() {
        try {
            if (soundEnabled) {
                val audioManager = getApplication<Application>().getSystemService(Context.AUDIO_SERVICE) as? AudioManager
                audioManager?.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD, 0.5f)
            }
            if (hapticsEnabled) {
                val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val vibratorManager = getApplication<Application>().getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                    vibratorManager?.defaultVibrator
                } else {
                    @Suppress("DEPRECATION")
                    getApplication<Application>().getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                }
                if (vibrator != null && vibrator.hasVibrator()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createOneShot(15, VibrationEffect.DEFAULT_AMPLITUDE))
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator.vibrate(15)
                    }
                }
            }
        } catch (e: Exception) {
            // Safely swallow exceptions (e.g. SecurityException on restricted devices) to avoid app crashes
        }
    }

    // --- SETTINGS OPERATIONS ---
    fun updateThemeMode(mode: String) {
        playSoundAndHaptic()
        themeMode = mode
        sharedPreferences.edit().putString("theme_mode", mode).apply()
    }

    fun updateUseMaterialYou(enabled: Boolean) {
        playSoundAndHaptic()
        useMaterialYou = enabled
        sharedPreferences.edit().putBoolean("material_you", enabled).apply()
    }

    fun updateSoundEnabled(enabled: Boolean) {
        soundEnabled = enabled
        sharedPreferences.edit().putBoolean("sound_enabled", enabled).apply()
        playSoundAndHaptic()
    }

    fun updateHapticsEnabled(enabled: Boolean) {
        hapticsEnabled = enabled
        sharedPreferences.edit().putBoolean("haptics_enabled", enabled).apply()
        playSoundAndHaptic()
    }

    fun updateDecimalPrecision(precision: Int) {
        playSoundAndHaptic()
        decimalPrecision = precision.coerceIn(0, 10)
        sharedPreferences.edit().putInt("decimal_precision", decimalPrecision).apply()
        // Re-evaluate live result if exists
        evaluateLive()
    }

    fun formatDouble(value: Double): String {
        if (value.isNaN() || value.isInfinite()) return "Error"
        if (value == 0.0) return "0"
        
        // Custom formatting based on precision
        val pattern = StringBuilder("#.")
        for (i in 0 until decimalPrecision) {
            pattern.append("#")
        }
        val df = DecimalFormat(pattern.toString())
        return df.format(value)
    }

    // --- CALC ACTIONS ---
    fun inputChar(char: String) {
        playSoundAndHaptic()
        
        // Prevent multiple operators together (e.g. ++, +*)
        val isOperator = listOf("+", "-", "×", "÷", "^").contains(char)
        val endsWithOperator = expression.isNotEmpty() && listOf('+', '-', '×', '÷', '^').contains(expression.last())
        
        if (isOperator && endsWithOperator) {
            // Replace previous operator
            expression = expression.dropLast(1) + char
        } else {
            expression += char
        }
        evaluateLive()
    }

    fun backspace() {
        playSoundAndHaptic()
        if (expression.isNotEmpty()) {
            expression = expression.dropLast(1)
            evaluateLive()
        }
    }

    fun clear() {
        playSoundAndHaptic()
        expression = ""
        liveResult = ""
    }

    fun toggleDegRad() {
        playSoundAndHaptic()
        isDegreeMode = !isDegreeMode
        sharedPreferences.edit().putBoolean("degree_mode", isDegreeMode).apply()
        evaluateLive()
    }

    private fun evaluateLive() {
        if (expression.isEmpty()) {
            liveResult = ""
            return
        }
        val eval = MathEvaluator.evaluate(expression, isDegreeMode)
        liveResult = if (eval.isNaN()) {
            ""
        } else {
            formatDouble(eval)
        }
    }

    fun calculate() {
        playSoundAndHaptic()
        if (expression.isEmpty()) return
        val eval = MathEvaluator.evaluate(expression, isDegreeMode)
        if (!eval.isNaN()) {
            val formatted = formatDouble(eval)
            val currentExpr = expression
            expression = formatted
            liveResult = ""

            // Save to database
            viewModelScope.launch {
                repository.insertHistory(
                    HistoryItem(
                        expression = currentExpr,
                        result = formatted,
                        category = if (isScientific(currentExpr)) "Scientific" else "Standard"
                    )
                )
            }
        } else {
            liveResult = "Error"
        }
    }

    private fun isScientific(expr: String): Boolean {
        val scientificOperators = listOf("sin", "cos", "tan", "log", "ln", "sqrt", "^", "π", "e")
        return scientificOperators.any { expr.contains(it) }
    }

    // --- DATABASE OPERATIONS ---
    fun toggleFavorite(item: HistoryItem) {
        playSoundAndHaptic()
        viewModelScope.launch {
            repository.updateHistory(item.copy(isFavorite = !item.isFavorite))
        }
    }

    fun deleteHistory(item: HistoryItem) {
        playSoundAndHaptic()
        viewModelScope.launch {
            repository.deleteHistory(item)
        }
    }

    fun clearAllHistory() {
        playSoundAndHaptic()
        viewModelScope.launch {
            repository.clearAllHistory()
        }
    }

    // Notes
    fun addNote(title: String, content: String) {
        playSoundAndHaptic()
        viewModelScope.launch {
            repository.insertNote(CalcNote(title = title, content = content))
        }
    }

    fun updateNote(note: CalcNote) {
        playSoundAndHaptic()
        viewModelScope.launch {
            repository.updateNote(note)
        }
    }

    fun deleteNote(note: CalcNote) {
        playSoundAndHaptic()
        viewModelScope.launch {
            repository.deleteNote(note)
        }
    }

    // --- UNIT CONVERTER OPERATIONS ---
    fun onConverterCategoryChanged(category: String) {
        converterCategory = category
        val units = getUnitsForCategory(category)
        converterFromUnit = units.firstOrNull() ?: ""
        converterToUnit = units.getOrNull(1) ?: units.firstOrNull() ?: ""
        updateUnitConversion()
    }

    fun onConverterInputChanged(input: String) {
        converterInput = input
        updateUnitConversion()
    }

    fun onConverterFromUnitChanged(unit: String) {
        converterFromUnit = unit
        updateUnitConversion()
    }

    fun onConverterToUnitChanged(unit: String) {
        converterToUnit = unit
        updateUnitConversion()
    }

    fun swapConverterUnits() {
        playSoundAndHaptic()
        val temp = converterFromUnit
        converterFromUnit = converterToUnit
        converterToUnit = temp
        updateUnitConversion()
    }

    fun getUnitsForCategory(category: String): List<String> {
        return when (category) {
            "Length" -> listOf("m", "km", "cm", "mm", "mile", "yard", "foot", "inch")
            "Weight" -> listOf("kg", "g", "mg", "lb", "oz")
            "Temperature" -> listOf("°C", "°F", "K")
            "Speed" -> listOf("m/s", "km/h", "mph", "knot")
            "Data" -> listOf("B", "KB", "MB", "GB", "TB")
            "Area" -> listOf("m²", "km²", "mile²", "acre", "hectare")
            "Angle" -> listOf("Degree", "Radian")
            "Currency" -> listOf("USD", "EUR", "GBP", "JPY", "CAD", "AUD", "INR", "CNY", "BDT")
            else -> emptyList()
        }
    }

    private fun updateUnitConversion() {
        val value = converterInput.toDoubleOrNull() ?: 0.0
        val converted = convertUnits(converterCategory, value, converterFromUnit, converterToUnit)
        converterResult = formatDouble(converted)
    }

    private fun convertUnits(category: String, value: Double, from: String, to: String): Double {
        if (from == to) return value
        return when (category) {
            "Length" -> {
                // Base: meters
                val inMeters = when (from) {
                    "m" -> value
                    "km" -> value * 1000.0
                    "cm" -> value * 0.01
                    "mm" -> value * 0.001
                    "mile" -> value * 1609.344
                    "yard" -> value * 0.9144
                    "foot" -> value * 0.3048
                    "inch" -> value * 0.0254
                    else -> value
                }
                when (to) {
                    "m" -> inMeters
                    "km" -> inMeters / 1000.0
                    "cm" -> inMeters / 0.01
                    "mm" -> inMeters / 0.001
                    "mile" -> inMeters / 1609.344
                    "yard" -> inMeters / 0.9144
                    "foot" -> inMeters / 0.3048
                    "inch" -> inMeters / 0.0254
                    else -> inMeters
                }
            }
            "Weight" -> {
                // Base: grams
                val inGrams = when (from) {
                    "g" -> value
                    "kg" -> value * 1000.0
                    "mg" -> value * 0.001
                    "lb" -> value * 453.59237
                    "oz" -> value * 28.34952
                    else -> value
                }
                when (to) {
                    "g" -> inGrams
                    "kg" -> inGrams / 1000.0
                    "mg" -> inGrams / 0.001
                    "lb" -> inGrams / 453.59237
                    "oz" -> inGrams / 28.34952
                    else -> inGrams
                }
            }
            "Temperature" -> {
                // Conversion directly
                val inCelsius = when (from) {
                    "°C" -> value
                    "°F" -> (value - 32.0) * 5.0 / 9.0
                    "K" -> value - 273.15
                    else -> value
                }
                when (to) {
                    "°C" -> inCelsius
                    "°F" -> inCelsius * 9.0 / 5.0 + 32.0
                    "K" -> inCelsius + 273.15
                    else -> inCelsius
                }
            }
            "Speed" -> {
                // Base: m/s
                val inMs = when (from) {
                    "m/s" -> value
                    "km/h" -> value / 3.6
                    "mph" -> value * 0.44704
                    "knot" -> value * 0.51444
                    else -> value
                }
                when (to) {
                    "m/s" -> inMs
                    "km/h" -> inMs * 3.6
                    "mph" -> inMs / 0.44704
                    "knot" -> inMs / 0.51444
                    else -> inMs
                }
            }
            "Data" -> {
                // Base: Bytes
                val inBytes = when (from) {
                    "B" -> value
                    "KB" -> value * 1024.0
                    "MB" -> value * 1024.0 * 1024.0
                    "GB" -> value * 1024.0 * 1024.0 * 1024.0
                    "TB" -> value * 1024.0 * 1024.0 * 1024.0 * 1024.0
                    else -> value
                }
                when (to) {
                    "B" -> inBytes
                    "KB" -> inBytes / 1024.0
                    "MB" -> inBytes / (1024.0 * 1024.0)
                    "GB" -> inBytes / (1024.0 * 1024.0 * 1024.0)
                    "TB" -> inBytes / (1024.0 * 1024.0 * 1024.0 * 1024.0)
                    else -> inBytes
                }
            }
            "Area" -> {
                // Base: sq meters
                val inSqm = when (from) {
                    "m²" -> value
                    "km²" -> value * 1_000_000.0
                    "mile²" -> value * 2_589_988.11
                    "acre" -> value * 4046.856
                    "hectare" -> value * 10000.0
                    else -> value
                }
                when (to) {
                    "m²" -> inSqm
                    "km²" -> inSqm / 1_000_000.0
                    "mile²" -> inSqm / 2_589_988.11
                    "acre" -> inSqm / 4046.856
                    "hectare" -> inSqm / 10000.0
                    else -> inSqm
                }
            }
            "Angle" -> {
                if (from == "Degree" && to == "Radian") Math.toRadians(value)
                else if (from == "Radian" && to == "Degree") Math.toDegrees(value)
                else value
            }
            "Currency" -> {
                // Base: USD
                // Static offline rates for reliable offline usage (User: "work completely offline")
                val toUsd = when (from) {
                    "USD" -> 1.0
                    "EUR" -> 1.08
                    "GBP" -> 1.27
                    "JPY" -> 0.0064
                    "CAD" -> 0.73
                    "AUD" -> 0.66
                    "INR" -> 0.012
                    "CNY" -> 0.14
                    "BDT" -> 0.0085
                    else -> 1.0
                }
                val inUsd = value * toUsd
                val fromUsd = when (to) {
                    "USD" -> 1.0
                    "EUR" -> 1.0 / 1.08
                    "GBP" -> 1.0 / 1.27
                    "JPY" -> 1.0 / 0.0064
                    "CAD" -> 1.0 / 0.73
                    "AUD" -> 1.0 / 0.66
                    "INR" -> 1.0 / 0.012
                    "CNY" -> 1.0 / 0.14
                    "BDT" -> 1.0 / 0.0085
                    else -> 1.0
                }
                inUsd * fromUsd
            }
            else -> value
        }
    }

    // --- FINANCE OPERATIONS ---
    fun onLoanPrincipalChanged(input: String) { loanPrincipal = input; calculateEmi() }
    fun onLoanRateChanged(input: String) { loanRate = input; calculateEmi() }
    fun onLoanTenureChanged(input: String) { loanTenure = input; calculateEmi() }
    fun onLoanTenureTypeChanged(type: String) { loanTenureType = type; calculateEmi() }

    private fun calculateEmi() {
        val p = loanPrincipal.toDoubleOrNull() ?: 0.0
        val r = (loanRate.toDoubleOrNull() ?: 0.0) / 12.0 / 100.0 // Monthly rate
        val n = (loanTenure.toDoubleOrNull() ?: 0.0) * if (loanTenureType == "years") 12 else 1

        if (p > 0 && r > 0 && n > 0) {
            val emi = p * r * Math.pow(1 + r, n) / (Math.pow(1 + r, n) - 1)
            val totalPayment = emi * n
            val totalInterest = totalPayment - p

            emiResult = formatDouble(emi)
            totalInterestResult = formatDouble(totalInterest)
            totalPaymentResult = formatDouble(totalPayment)
        } else if (p > 0 && r == 0.0 && n > 0) {
            // No interest loan
            val emi = p / n
            emiResult = formatDouble(emi)
            totalInterestResult = "0"
            totalPaymentResult = formatDouble(p)
        } else {
            emiResult = ""
            totalInterestResult = ""
            totalPaymentResult = ""
        }
    }

    // Compound Interest
    fun onCiPrincipalChanged(input: String) { ciPrincipal = input; calculateCompoundInterest() }
    fun onCiRateChanged(input: String) { ciRate = input; calculateCompoundInterest() }
    fun onCiTimeChanged(input: String) { ciTime = input; calculateCompoundInterest() }
    fun onCiFrequencyChanged(input: String) { ciFrequency = input; calculateCompoundInterest() }

    private fun calculateCompoundInterest() {
        val p = ciPrincipal.toDoubleOrNull() ?: 0.0
        val r = (ciRate.toDoubleOrNull() ?: 0.0) / 100.0
        val t = ciTime.toDoubleOrNull() ?: 0.0
        val n = ciFrequency.toDoubleOrNull() ?: 12.0

        if (p > 0 && t > 0) {
            val amount = p * Math.pow(1 + (r / n), n * t)
            val interest = amount - p
            ciFinalAmountResult = formatDouble(amount)
            ciInterestEarnedResult = formatDouble(interest)
        } else {
            ciFinalAmountResult = ""
            ciInterestEarnedResult = ""
        }
    }

    // Discount
    fun onDiscountOriginalPriceChanged(input: String) { discountOriginalPrice = input; calculateDiscount() }
    fun onDiscountPercentageChanged(input: String) { discountPercentage = input; calculateDiscount() }
    fun onDiscountTaxPercentageChanged(input: String) { discountTaxPercentage = input; calculateDiscount() }

    private fun calculateDiscount() {
        val price = discountOriginalPrice.toDoubleOrNull() ?: 0.0
        val disc = discountPercentage.toDoubleOrNull() ?: 0.0
        val tax = discountTaxPercentage.toDoubleOrNull() ?: 0.0

        val saved = price * (disc / 100.0)
        val discountedPrice = price - saved
        val taxAmount = discountedPrice * (tax / 100.0)
        val finalPrice = discountedPrice + taxAmount

        discountSavedAmountResult = formatDouble(saved)
        discountTaxAmountResult = formatDouble(taxAmount)
        discountFinalPriceResult = formatDouble(finalPrice)
    }

    // --- HEALTH OPERATIONS ---
    fun onHealthWeightChanged(input: String) { healthWeight = input; calculateHealth() }
    fun onHealthHeightChanged(input: String) { healthHeight = input; calculateHealth() }
    fun onHealthAgeChanged(input: String) { healthAge = input; calculateHealth() }
    fun onHealthGenderChanged(input: String) { healthGender = input; calculateHealth() }

    private fun calculateHealth() {
        val w = healthWeight.toDoubleOrNull() ?: 0.0
        val h = healthHeight.toDoubleOrNull() ?: 0.0
        val age = healthAge.toIntOrNull() ?: 0

        if (w > 0 && h > 0) {
            // BMI
            val heightInMeters = h / 100.0
            val bmi = w / (heightInMeters * heightInMeters)
            bmiValueResult = formatDouble(bmi)
            bmiCategoryResult = when {
                bmi < 18.5 -> "Underweight"
                bmi < 25.0 -> "Normal"
                bmi < 30.0 -> "Overweight"
                else -> "Obese"
            }

            // BMR (Mifflin-St Jeor Equation)
            val bmr = if (healthGender == "Male") {
                10.0 * w + 6.25 * h - 5.0 * age + 5.0
            } else {
                10.0 * w + 6.25 * h - 5.0 * age - 161.0
            }
            bmrResult = "${bmr.roundToInt()} kcal/day"

            // Daily Calories (lightly active estimate)
            val dailyCalories = bmr * 1.375
            dailyCalorieResult = "${dailyCalories.roundToInt()} kcal/day"

            // Water Intake (35ml per kg)
            val waterLiters = w * 0.035
            dailyWaterResult = "${formatDouble(waterLiters)} Liters"
        } else {
            bmiValueResult = ""
            bmiCategoryResult = ""
            bmrResult = ""
            dailyWaterResult = ""
            dailyCalorieResult = ""
        }
    }

    // --- PROGRAMMER CONVERTER OPERATIONS ---
    fun onProgrammerBaseChanged(base: String) {
        programmerBase = base
        updateProgrammerBases()
    }

    fun onProgrammerInputChanged(input: String) {
        programmerInput = input
        updateProgrammerBases()
    }

    private fun updateProgrammerBases() {
        if (programmerInput.isEmpty()) {
            programmerBinResult = ""
            programmerOctResult = ""
            programmerDecResult = ""
            programmerHexResult = ""
            return
        }

        try {
            val radix = when (programmerBase) {
                "Binary" -> 2
                "Octal" -> 8
                "Decimal" -> 10
                "Hexadecimal" -> 16
                else -> 10
            }
            val value = programmerInput.toLong(radix)

            programmerBinResult = value.toString(2)
            programmerOctResult = value.toString(8)
            programmerDecResult = value.toString(10)
            programmerHexResult = value.toString(16).uppercase()
        } catch (e: Exception) {
            programmerBinResult = "Invalid Input"
            programmerOctResult = "Invalid Input"
            programmerDecResult = "Invalid Input"
            programmerHexResult = "Invalid Input"
        }
    }

    // --- DATE CALCULATOR OPERATIONS ---
    fun onDateBirthdateChanged(date: String) {
        dateCalcBirthdate = date
        calculateAge()
    }

    fun onDateTargetDateChanged(date: String) {
        dateCalcTargetDate = date
        calculateAge()
    }

    private fun calculateAge() {
        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val birthDate = sdf.parse(dateCalcBirthdate) ?: return
            val targetDate = sdf.parse(dateCalcTargetDate) ?: return

            // Calculate Difference
            val diffMs = targetDate.time - birthDate.time
            val diffDays = (diffMs / (1000 * 60 * 60 * 24)).coerceAtLeast(0)
            dateDifferenceResult = "$diffDays Days"

            // Simple Age Calculation
            val birthCal = Calendar.getInstance().apply { time = birthDate }
            val targetCal = Calendar.getInstance().apply { time = targetDate }

            var years = targetCal.get(Calendar.YEAR) - birthCal.get(Calendar.YEAR)
            var months = targetCal.get(Calendar.MONTH) - birthCal.get(Calendar.MONTH)
            var days = targetCal.get(Calendar.DAY_OF_MONTH) - birthCal.get(Calendar.DAY_OF_MONTH)

            if (days < 0) {
                months--
                val prevMonthCal = (targetCal.clone() as Calendar).apply { add(Calendar.MONTH, -1) }
                days += prevMonthCal.getActualMaximum(Calendar.DAY_OF_MONTH)
            }
            if (months < 0) {
                years--
                months += 12
            }

            if (years < 0) {
                ageResultYears = "0"
                ageResultMonthsAndDays = "0 Months, 0 Days"
            } else {
                ageResultYears = "$years Years"
                ageResultMonthsAndDays = "$months Months, $days Days"
            }

            // Next Birthday Days
            val nextBday = Calendar.getInstance().apply {
                time = birthDate
                set(Calendar.YEAR, targetCal.get(Calendar.YEAR))
            }
            if (nextBday.before(targetCal)) {
                nextBday.add(Calendar.YEAR, 1)
            }
            val nextBdayDiffMs = nextBday.timeInMillis - targetCal.timeInMillis
            val nextBdayDiffDays = nextBdayDiffMs / (1000 * 60 * 60 * 24)
            ageNextBirthdayDays = "$nextBdayDiffDays Days"

        } catch (e: Exception) {
            dateDifferenceResult = "Error"
            ageResultYears = "Error"
            ageResultMonthsAndDays = ""
            ageNextBirthdayDays = ""
        }
    }
}
