package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AvatarState
import com.example.ui.components.HeenaAvatarView
import com.example.viewmodel.HeenaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeStudioScreen(
    viewModel: HeenaViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val prefs = viewModel.prefs
    val currentTheme by prefs.themeName.collectAsState()
    val assistantName by prefs.assistantName.collectAsState()
    val greeting by prefs.greetingMessage.collectAsState()
    val speechRate by prefs.speechRate.collectAsState()
    val speechPitch by prefs.speechPitch.collectAsState()
    val reducedMotion by prefs.reducedMotion.collectAsState()

    var nameInput by remember(assistantName) { mutableStateOf(assistantName) }
    var greetingInput by remember(greeting) { mutableStateOf(greeting) }
    var previewAvatarState by remember { mutableStateOf(AvatarState.IDLE) }

    val themes = listOf(
        "purple" to ("Purple Cyber" to Color(0xFF7C4DFF)),
        "pastel" to ("Pastel Lavender" to Color(0xFF9575CD)),
        "pink" to ("Sakura Pink" to Color(0xFFEC407A)),
        "blue" to ("Electric Blue" to Color(0xFF0288D1)),
        "dark" to ("AMOLED Dark" to Color(0xFF1B1433)),
        "light" to ("Crisp Light" to Color(0xFF673AB7))
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Avatar & Theme Studio", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { prefs.resetToDefaults() }) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Reset Defaults")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Live Interactive Avatar Preview
            HeenaAvatarView(
                avatarState = previewAvatarState,
                reducedMotion = reducedMotion,
                size = 170.dp,
                onClick = {
                    previewAvatarState = when (previewAvatarState) {
                        AvatarState.IDLE -> AvatarState.HAPPY
                        AvatarState.HAPPY -> AvatarState.SPEAKING
                        AvatarState.SPEAKING -> AvatarState.THINKING
                        AvatarState.THINKING -> AvatarState.LISTENING
                        else -> AvatarState.IDLE
                    }
                }
            )

            Text(
                text = "Tap avatar to test expressions (${previewAvatarState.name.lowercase()})",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )

            // Color Themes Selector
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Palette, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("App Color Theme", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        themes.forEach { (key, pair) ->
                            val (label, color) = pair
                            val isSelected = currentTheme == key
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { prefs.setThemeName(key) }
                                    .padding(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                        .border(
                                            width = if (isSelected) 3.dp else 1.dp,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = label.split(" ").first(),
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }

            // Identity Customization (Name & Greeting)
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Assistant Identity & Greeting", fontWeight = FontWeight.Bold, fontSize = 15.sp)

                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Assistant Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = greetingInput,
                        onValueChange = { greetingInput = it },
                        label = { Text("Custom Greeting Message") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            prefs.setAssistantName(nameInput)
                            prefs.setGreetingMessage(greetingInput)
                            viewModel.showStatus("Saved assistant identity")
                        },
                        modifier = Modifier
                            .align(Alignment.End)
                            .testTag("save_identity_button")
                    ) {
                        Text("Save")
                    }
                }
            }

            // Voice & Speech Tuning
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.RecordVoiceOver, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Speech Engine Controls", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text("Speech Rate (Speed): ${"%.2f".format(speechRate)}x", fontSize = 13.sp)
                    Slider(
                        value = speechRate,
                        onValueChange = { prefs.setSpeechRate(it) },
                        valueRange = 0.7f..1.5f,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Voice Pitch: ${"%.2f".format(speechPitch)}", fontSize = 13.sp)
                    Slider(
                        value = speechPitch,
                        onValueChange = { prefs.setSpeechPitch(it) },
                        valueRange = 0.7f..1.6f,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            viewModel.ttsManager.speak(
                                text = "नमस्ते! मैं ${viewModel.prefs.assistantName.value} हूँ। यह आवाज़ का परीक्षण है।",
                                pitch = speechPitch,
                                speed = speechRate,
                                languageCode = viewModel.prefs.language.value
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("test_voice_tts_button")
                    ) {
                        Icon(imageVector = Icons.Default.RecordVoiceOver, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Test Voice Readout")
                    }
                }
            }

            // Accessibility / Reduced Motion
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Reduced Motion Mode", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(
                            "Disables avatar bouncing and pulsing for battery & comfort",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                    Switch(checked = reducedMotion, onCheckedChange = { prefs.setReducedMotion(it) })
                }
            }
        }
    }
}
