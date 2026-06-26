package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.CalcViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgrammerScreen(
    viewModel: CalcViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val bases = listOf("Binary", "Octal", "Decimal", "Hexadecimal")
    var baseExpanded by remember { mutableStateOf(false) }

    fun copyToClipboard(text: String, label: String) {
        if (text.isEmpty() || text == "Invalid Input") return
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "$label copied to clipboard!", Toast.LENGTH_SHORT).show()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Programmer Converter", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("programmer_back_button")) {
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Multi-Base Programmer Calculator", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

            // Selected Base Selector
            Text("Select Input Base", style = MaterialTheme.typography.labelMedium)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .clickable { baseExpanded = true }
                    .padding(16.dp)
                    .testTag("programmer_base_dropdown")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(viewModel.programmerBase, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Expand")
                }
                DropdownMenu(
                    expanded = baseExpanded,
                    onDismissRequest = { baseExpanded = false },
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    bases.forEach { base ->
                        DropdownMenuItem(
                            text = { Text(base) },
                            onClick = {
                                viewModel.onProgrammerBaseChanged(base)
                                baseExpanded = false
                            },
                            modifier = Modifier.testTag("programmer_base_item_$base")
                        )
                    }
                }
            }

            // Input Field
            OutlinedTextField(
                value = viewModel.programmerInput,
                onValueChange = { viewModel.onProgrammerInputChanged(it) },
                label = { Text("Input Value (${viewModel.programmerBase})") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("programmer_input_field"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Conversion Results
            Text("Base Converter Outputs", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)

            ProgrammerResultRow("HEX (Hexadecimal)", viewModel.programmerHexResult, "HEX") {
                copyToClipboard(viewModel.programmerHexResult, "Hexadecimal")
            }
            ProgrammerResultRow("DEC (Decimal)", viewModel.programmerDecResult, "DEC") {
                copyToClipboard(viewModel.programmerDecResult, "Decimal")
            }
            ProgrammerResultRow("OCT (Octal)", viewModel.programmerOctResult, "OCT") {
                copyToClipboard(viewModel.programmerOctResult, "Octal")
            }
            ProgrammerResultRow("BIN (Binary)", viewModel.programmerBinResult, "BIN") {
                copyToClipboard(viewModel.programmerBinResult, "Binary")
            }
        }
    }
}

@Composable
fun ProgrammerResultRow(
    label: String,
    value: String,
    tag: String,
    onCopy: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = value.ifEmpty { "0" },
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.testTag("programmer_result_$tag")
                )
            }
            IconButton(onClick = onCopy, modifier = Modifier.testTag("copy_button_$tag")) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
    }
}
