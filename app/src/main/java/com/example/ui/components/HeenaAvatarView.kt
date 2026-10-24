package com.example.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.BrightAmber
import com.example.ui.theme.MintSuccess
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.SoftRose
import kotlin.math.roundToInt

enum class AvatarState {
    IDLE,
    LISTENING,
    THINKING,
    SPEAKING,
    HAPPY,
    ERROR
}

@Composable
fun HeenaAvatarView(
    avatarState: AvatarState,
    modifier: Modifier = Modifier,
    size: Dp = 220.dp,
    reducedMotion: Boolean = false,
    soundLevelDb: Float = 0f,
    onClick: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "avatar_anim")

    // Subtle gentle breathing / floating motion
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = if (reducedMotion) 0f else -6f,
        targetValue = if (reducedMotion) 0f else 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floating"
    )

    // Holographic ring rotation for thinking
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    // Pulse scale for listening / audio reactivity
    val audioScale = if (avatarState == AvatarState.LISTENING) {
        1.0f + (soundLevelDb.coerceIn(0f, 15f) / 100f)
    } else 1.0f

    // Status Glow Color
    val glowBrush = when (avatarState) {
        AvatarState.LISTENING -> Brush.sweepGradient(listOf(NeonCyan, Color(0xFF7C4DFF), NeonCyan))
        AvatarState.THINKING -> Brush.sweepGradient(listOf(BrightAmber, Color(0xFFB388FF), BrightAmber))
        AvatarState.SPEAKING -> Brush.sweepGradient(listOf(SoftRose, Color(0xFF7C4DFF), SoftRose))
        AvatarState.HAPPY -> Brush.sweepGradient(listOf(MintSuccess, BrightAmber, MintSuccess))
        AvatarState.ERROR -> Brush.sweepGradient(listOf(Color(0xFFFF5252), Color(0xFFFF7B7B), Color(0xFFFF5252)))
        AvatarState.IDLE -> Brush.sweepGradient(listOf(Color(0xFF7C4DFF), Color(0xFFB388FF), Color(0xFF7C4DFF)))
    }

    Box(
        modifier = modifier
            .size(size + 36.dp)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        // Outer animated glowing aura rings
        Box(
            modifier = Modifier
                .size(size + 24.dp)
                .scale(audioScale)
                .rotate(if (avatarState == AvatarState.THINKING) rotationAngle else 0f)
                .clip(CircleShape)
                .background(glowBrush)
                .padding(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
            )
        }

        // Main Avatar Card with float offset
        Box(
            modifier = Modifier
                .size(size)
                .offset { IntOffset(0, floatOffset.roundToInt()) }
                .shadow(16.dp, CircleShape)
                .clip(CircleShape)
                .border(3.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.8f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            // Dynamic image expression crossfade
            Crossfade(
                targetState = avatarState,
                animationSpec = tween(400),
                label = "avatar_expression"
            ) { state ->
                val drawableRes = when (state) {
                    AvatarState.LISTENING -> R.drawable.img_heena_listening_1792791549143
                    AvatarState.THINKING -> R.drawable.img_heena_thinking_1792791581360
                    AvatarState.SPEAKING -> R.drawable.img_heena_speaking_1792791566739
                    AvatarState.HAPPY -> R.drawable.img_heena_speaking_1792791566739
                    AvatarState.ERROR -> R.drawable.img_heena_idle_1792791534818
                    AvatarState.IDLE -> R.drawable.img_heena_idle_1792791534818
                }
                Image(
                    painter = painterResource(id = drawableRes),
                    contentDescription = "Heena AI Companion",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(size)
                )
            }
        }

        // Status Badge Pill
        val (statusText, statusIcon, statusColor) = when (avatarState) {
            AvatarState.LISTENING -> Triple("Listening...", Icons.Default.Hearing, NeonCyan)
            AvatarState.THINKING -> Triple("Thinking...", Icons.Default.Psychology, BrightAmber)
            AvatarState.SPEAKING -> Triple("Speaking...", Icons.Default.RecordVoiceOver, SoftRose)
            AvatarState.HAPPY -> Triple("Happy!", Icons.Default.AutoAwesome, MintSuccess)
            AvatarState.ERROR -> Triple("Error", Icons.Default.AutoAwesome, Color(0xFFFF5252))
            AvatarState.IDLE -> Triple("Ready", Icons.Default.AutoAwesome, MaterialTheme.colorScheme.primary)
        }

        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = 12.dp)
                .shadow(6.dp, RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            tonalElevation = 4.dp
        ) {
            androidx.compose.foundation.layout.Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(statusColor)
                )
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(6.dp))
                Icon(
                    imageVector = statusIcon,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = statusColor
                )
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(4.dp))
                Text(
                    text = statusText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
