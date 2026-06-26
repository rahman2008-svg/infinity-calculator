package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.CalcViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(
    viewModel: CalcViewModel,
    isScientificInitially: Boolean,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isScientific = remember{ androidx.compose.runtime.mutableStateOf(isScientificInitially) }

    val standardButtons = listOf(
        "C", "(", ")", "÷",
        "7", "8", "9", "×",
        "4", "5", "6", "-",
        "1", "2", "3", "+",
        "0", ".", "%", "="
    )

    val scientificButtons = listOf(
        "sin", "cos", "tan", "^",
        "asin", "acos", "atan", "sqrt",
        "log", "ln", "π", "e"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isScientific.value) "Scientific Calculator" else "Standard Calculator",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("calc_back_button")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Deg / Rad status indicator
                    if (isScientific.value) {
                        TextButton(
                            onClick = { viewModel.toggleDegRad() },
                            modifier = Modifier.testTag("deg_rad_toggle")
                        ) {
                            Text(
                                text = if (viewModel.isDegreeMode) "DEG" else "RAD",
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    // Toggle Standard vs Scientific
                    IconButton(
                        onClick = { isScientific.value = !isScientific.value },
                        modifier = Modifier.testTag("toggle_calc_mode_button")
                    ) {
                        Icon(
                            imageVector = if (isScientific.value) Icons.Default.Calculate else Icons.Default.Science,
                            contentDescription = "Switch Mode",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Expression Display Screen
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(bottom = 16.dp),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = viewModel.expression.ifEmpty { "0" },
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = if (viewModel.expression.length > 12) 32.sp else 44.sp,
                        fontWeight = FontWeight.Light,
                        textAlign = TextAlign.End
                    ),
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("calculator_expression_display"),
                    maxLines = 3
                )
                Spacer(modifier = Modifier.height(12.dp))
                if (viewModel.liveResult.isNotEmpty()) {
                    Text(
                        text = viewModel.liveResult,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.End
                        ),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("calculator_live_result")
                    )
                }
            }

            // Keypad Layout
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Backspace button floatable / right aligned before grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(
                        onClick = { viewModel.backspace() },
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .testTag("backspace_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Backspace,
                            contentDescription = "Backspace",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))

                // Scientific pad (visible only when isScientific.value is true)
                if (isScientific.value) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(4),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(scientificButtons) { label ->
                            ScientificBtn(
                                label = label,
                                onClick = { viewModel.inputChar(label + "(") }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Standard Pad
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(standardButtons) { label ->
                        StandardBtn(
                            label = label,
                            onClick = {
                                when (label) {
                                    "C" -> viewModel.clear()
                                    "=" -> viewModel.calculate()
                                    else -> viewModel.inputChar(label)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ScientificBtn(
    label: String,
    onClick: () -> Unit
) {
    // Elegant, smaller buttons for scientific operations
    val displayLabel = when (label) {
        "sqrt" -> "√"
        "asin" -> "sin⁻¹"
        "acos" -> "cos⁻¹"
        "atan" -> "tan⁻¹"
        else -> label
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(38.dp)
            .clip(MaterialTheme.shapes.small)
            .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f))
            .clickable { onClick() }
            .testTag("scientific_btn_$label"),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = displayLabel,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            ),
            color = MaterialTheme.colorScheme.secondary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun StandardBtn(
    label: String,
    onClick: () -> Unit
) {
    val isOperator = listOf("÷", "×", "-", "+", "=").contains(label)
    val isAction = listOf("C", "(", ")", "%").contains(label)

    val containerColor = when {
        label == "=" -> MaterialTheme.colorScheme.primary
        isOperator -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.8f)
        isAction -> MaterialTheme.colorScheme.surfaceVariant
        else -> MaterialTheme.colorScheme.surface
    }

    val contentColor = when {
        label == "=" -> MaterialTheme.colorScheme.onPrimary
        isOperator -> MaterialTheme.colorScheme.onTertiaryContainer
        isAction -> MaterialTheme.colorScheme.onSurfaceVariant
        else -> MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(containerColor)
            .clickable { onClick() }
            .testTag("btn_$label"),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = if (isOperator || isAction) 22.sp else 20.sp
            ),
            color = contentColor,
            textAlign = TextAlign.Center
        )
    }
}
