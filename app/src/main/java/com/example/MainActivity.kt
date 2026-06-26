package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.CalcViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: CalcViewModel = viewModel()
            MyApplicationTheme(
                themeMode = viewModel.themeMode,
                useMaterialYou = viewModel.useMaterialYou
            ) {
                val backStack = remember { mutableStateListOf<Screen>(Screen.Dashboard) }
                val currentScreen = backStack.lastOrNull() ?: Screen.Dashboard

                fun navigateTo(screen: Screen) {
                    backStack.add(screen)
                }

                fun navigateBack() {
                    if (backStack.size > 1) {
                        backStack.removeAt(backStack.size - 1)
                    }
                }

                // Native Back Gesture Handler
                BackHandler(enabled = backStack.size > 1) {
                    navigateBack()
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    when (currentScreen) {
                        Screen.Dashboard -> {
                            DashboardScreen(
                                onNavigate = { navigateTo(it) },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                        Screen.StandardCalc -> {
                            CalculatorScreen(
                                viewModel = viewModel,
                                isScientificInitially = false,
                                onBack = { navigateBack() },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                        Screen.ScientificCalc -> {
                            CalculatorScreen(
                                viewModel = viewModel,
                                isScientificInitially = true,
                                onBack = { navigateBack() },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                        Screen.UnitConverter -> {
                            ConverterScreen(
                                viewModel = viewModel,
                                onBack = { navigateBack() },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                        Screen.FinanceCalc -> {
                            FinanceScreen(
                                viewModel = viewModel,
                                onBack = { navigateBack() },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                        Screen.HealthCalc -> {
                            HealthScreen(
                                viewModel = viewModel,
                                onBack = { navigateBack() },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                        Screen.ProgrammerCalc -> {
                            ProgrammerScreen(
                                viewModel = viewModel,
                                onBack = { navigateBack() },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                        Screen.DateCalc -> {
                            DateScreen(
                                viewModel = viewModel,
                                onBack = { navigateBack() },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                        Screen.HistoryList -> {
                            HistoryScreen(
                                viewModel = viewModel,
                                onBack = { navigateBack() },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                        Screen.NotesScreen -> {
                            NotesScreen(
                                viewModel = viewModel,
                                onBack = { navigateBack() },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                        Screen.SettingsScreen -> {
                            SettingsScreen(
                                viewModel = viewModel,
                                onBack = { navigateBack() },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                    }
                }
            }
        }
    }
}

