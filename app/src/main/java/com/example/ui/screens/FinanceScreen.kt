package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.CalcViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceScreen(
    viewModel: CalcViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Loan EMI", "Compound Interest", "Discount")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Finance Calculators", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("finance_back_button")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
        ) {
            // Tab row to toggle calculators
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.background
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                        modifier = Modifier.testTag("finance_tab_$index")
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when (selectedTab) {
                    0 -> LoanEmiLayout(viewModel)
                    1 -> CompoundInterestLayout(viewModel)
                    2 -> DiscountLayout(viewModel)
                }
            }
        }
    }
}

@Composable
fun LoanEmiLayout(viewModel: CalcViewModel) {
    Text("Loan / EMI Calculator", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

    OutlinedTextField(
        value = viewModel.loanPrincipal,
        onValueChange = { viewModel.onLoanPrincipalChanged(it) },
        label = { Text("Principal Amount ($)") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        modifier = Modifier.fillMaxWidth().testTag("loan_principal_input")
    )

    OutlinedTextField(
        value = viewModel.loanRate,
        onValueChange = { viewModel.onLoanRateChanged(it) },
        label = { Text("Interest Rate (% per Year)") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        modifier = Modifier.fillMaxWidth().testTag("loan_rate_input")
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = viewModel.loanTenure,
            onValueChange = { viewModel.onLoanTenureChanged(it) },
            label = { Text("Tenure") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.weight(1f).testTag("loan_tenure_input")
        )

        // Tenure Type (years vs months)
        Row(
            modifier = Modifier
                .weight(1f)
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            listOf("years", "months").forEach { type ->
                FilterChip(
                    selected = viewModel.loanTenureType == type,
                    onClick = { viewModel.onLoanTenureTypeChanged(type) },
                    label = { Text(type.uppercase(), fontSize = 11.sp) },
                    modifier = Modifier.testTag("tenure_chip_$type")
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Results Box
    if (viewModel.emiResult.isNotEmpty()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            )
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FinanceResultRow("Monthly EMI:", "$ ${viewModel.emiResult}", isPrimary = true)
                HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f))
                FinanceResultRow("Total Interest Payable:", "$ ${viewModel.totalInterestResult}")
                FinanceResultRow("Total Payment (P + I):", "$ ${viewModel.totalPaymentResult}")
            }
        }
    }
}

@Composable
fun CompoundInterestLayout(viewModel: CalcViewModel) {
    Text("Compound Interest Calculator", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

    OutlinedTextField(
        value = viewModel.ciPrincipal,
        onValueChange = { viewModel.onCiPrincipalChanged(it) },
        label = { Text("Initial Deposit ($)") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        modifier = Modifier.fillMaxWidth().testTag("ci_principal_input")
    )

    OutlinedTextField(
        value = viewModel.ciRate,
        onValueChange = { viewModel.onCiRateChanged(it) },
        label = { Text("Annual Interest Rate (%)") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        modifier = Modifier.fillMaxWidth().testTag("ci_rate_input")
    )

    OutlinedTextField(
        value = viewModel.ciTime,
        onValueChange = { viewModel.onCiTimeChanged(it) },
        label = { Text("Duration (Years)") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        modifier = Modifier.fillMaxWidth().testTag("ci_time_input")
    )

    Text("Compounding Frequency", style = MaterialTheme.typography.labelMedium)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        val frequencies = listOf("1" to "Yearly", "4" to "Quarterly", "12" to "Monthly")
        frequencies.forEach { (freq, label) ->
            FilterChip(
                selected = viewModel.ciFrequency == freq,
                onClick = { viewModel.onCiFrequencyChanged(freq) },
                label = { Text(label, fontSize = 11.sp) },
                modifier = Modifier.testTag("ci_frequency_$freq")
            )
        }
    }

    Spacer(modifier = Modifier.height(8.dp))

    if (viewModel.ciFinalAmountResult.isNotEmpty()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            )
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FinanceResultRow("Future Value:", "$ ${viewModel.ciFinalAmountResult}", isPrimary = true)
                HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f))
                FinanceResultRow("Total Interest Earned:", "$ ${viewModel.ciInterestEarnedResult}")
                FinanceResultRow("Initial Principal:", "$ ${viewModel.ciPrincipal}")
            }
        }
    }
}

@Composable
fun DiscountLayout(viewModel: CalcViewModel) {
    Text("Discount & Tax Calculator", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

    OutlinedTextField(
        value = viewModel.discountOriginalPrice,
        onValueChange = { viewModel.onDiscountOriginalPriceChanged(it) },
        label = { Text("Original Price ($)") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        modifier = Modifier.fillMaxWidth().testTag("discount_price_input")
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        OutlinedTextField(
            value = viewModel.discountPercentage,
            onValueChange = { viewModel.onDiscountPercentageChanged(it) },
            label = { Text("Discount (%)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.weight(1f).testTag("discount_percent_input")
        )

        OutlinedTextField(
            value = viewModel.discountTaxPercentage,
            onValueChange = { viewModel.onDiscountTaxPercentageChanged(it) },
            label = { Text("Tax (%)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.weight(1f).testTag("discount_tax_input")
        )
    }

    Spacer(modifier = Modifier.height(8.dp))

    if (viewModel.discountFinalPriceResult.isNotEmpty()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            )
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FinanceResultRow("Final Price:", "$ ${viewModel.discountFinalPriceResult}", isPrimary = true)
                HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f))
                FinanceResultRow("You Save:", "$ ${viewModel.discountSavedAmountResult}")
                FinanceResultRow("Tax Amount:", "$ ${viewModel.discountTaxAmountResult}")
            }
        }
    }
}

@Composable
fun FinanceResultRow(
    label: String,
    value: String,
    isPrimary: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = if (isPrimary) MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold) else MaterialTheme.typography.bodyMedium,
            color = if (isPrimary) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
        )
        Text(
            text = value,
            style = if (isPrimary) MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black) else MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
            color = if (isPrimary) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}
