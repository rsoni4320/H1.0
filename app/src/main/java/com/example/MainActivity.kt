package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.BottomNavBar
import com.example.ui.screens.AiConfigScreen
import com.example.ui.screens.AutomationScreen
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.PermissionsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.ThemeStudioScreen
import com.example.ui.theme.HeenaAITheme
import com.example.viewmodel.HeenaViewModel
import com.example.viewmodel.Screen

class MainActivity : ComponentActivity() {

    private var viewModelInstance: HeenaViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val vm: HeenaViewModel = viewModel()
            viewModelInstance = vm

            val themeName by vm.prefs.themeName.collectAsState()
            val currentScreen by vm.currentScreen.collectAsState()
            val confirmationDialog by vm.confirmationDialog.collectAsState()
            val statusMessage by vm.statusMessage.collectAsState()
            val snackbarHostState = remember { SnackbarHostState() }

            LaunchedEffect(statusMessage) {
                statusMessage?.let { msg ->
                    snackbarHostState.showSnackbar(msg, duration = SnackbarDuration.Short)
                    vm.clearStatus()
                }
            }

            HeenaAITheme(themeName = themeName) {
                // Predictive back handler
                BackHandler(enabled = currentScreen != Screen.HOME && currentScreen != Screen.SPLASH) {
                    vm.navigateBack()
                }

                val showBottomNav = currentScreen in listOf(
                    Screen.HOME,
                    Screen.CHAT,
                    Screen.AUTOMATION,
                    Screen.SETTINGS
                )

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        if (showBottomNav) {
                            BottomNavBar(
                                currentScreen = currentScreen,
                                onNavigate = { targetScreen ->
                                    vm.navigateTo(targetScreen)
                                }
                            )
                        }
                    },
                    snackbarHost = { SnackbarHost(snackbarHostState) }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (currentScreen) {
                            Screen.SPLASH -> SplashScreen(
                                onFinished = { vm.completeSplash() }
                            )

                            Screen.ONBOARDING -> OnboardingScreen(
                                viewModel = vm,
                                onFinish = { vm.completeOnboarding() }
                            )

                            Screen.HOME -> HomeScreen(
                                viewModel = vm,
                                onNavigate = { vm.navigateTo(it) }
                            )

                            Screen.CHAT -> ChatScreen(
                                viewModel = vm
                            )

                            Screen.AUTOMATION -> AutomationScreen(
                                viewModel = vm
                            )

                            Screen.PERMISSIONS -> PermissionsScreen(
                                viewModel = vm,
                                onBack = { vm.navigateBack() }
                            )

                            Screen.AI_CONFIG -> AiConfigScreen(
                                viewModel = vm,
                                onBack = { vm.navigateBack() }
                            )

                            Screen.THEME_STUDIO -> ThemeStudioScreen(
                                viewModel = vm,
                                onBack = { vm.navigateBack() }
                            )

                            Screen.SETTINGS -> SettingsScreen(
                                viewModel = vm,
                                onNavigate = { vm.navigateTo(it) }
                            )
                        }
                    }
                }

                // Sensitive action confirmation dialog (calls, high-impact device actions)
                confirmationDialog?.let { dialogState ->
                    AlertDialog(
                        onDismissRequest = { vm.dismissConfirmationDialog() },
                        title = { Text(dialogState.title) },
                        text = { Text(dialogState.message) },
                        confirmButton = {
                            Button(
                                onClick = { dialogState.onConfirm() },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text("Proceed")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { vm.dismissConfirmationDialog() }) {
                                Text("Cancel")
                            }
                        }
                    )
                }
            }
        }

        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.getBooleanExtra("EXTRA_WAKE_WORD_ACTIVATED", false) == true) {
            viewModelInstance?.startVoiceListening()
        }
    }
}
