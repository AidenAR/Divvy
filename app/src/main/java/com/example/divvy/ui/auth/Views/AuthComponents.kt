package com.example.divvy.ui.auth.Views

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val PurplePrimary = Color(0xFF5F2DE8)
val PurpleSecondary = Color(0xFF7C3AED)
val AuthBackground = Color(0xFFF3F2F9)
val MutedText = Color(0xFF8F8F96)

@Composable
fun AuthTopBar(
    title: String,
    onBack: (() -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .padding(top = 8.dp, bottom = 16.dp)
            .fillMaxWidth()
            .height(32.dp),
        contentAlignment = Alignment.Center
    ) {
        if (onBack != null) {
            Icon(
                imageVector = Icons.Filled.ArrowBack,
                contentDescription = "Back",
                modifier = Modifier
                    .size(28.dp)
                    .align(Alignment.CenterStart)
                    .clickable { onBack() }
            )
        }
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun AuthPrimaryButton(
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(52.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
    ) {
        Text(text = label, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun AuthOutlinedButton(
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(52.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(text = label, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun StepIndicator(
    step: Int,
    total: Int = 3
) {
    Row(horizontalArrangement = Arrangement.Center) {
        repeat(total) { index ->
            val active = index < step
            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .width(16.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (active) PurplePrimary else Color(0xFFE1DDF3))
            )
        }
    }
}

@Composable
fun OtpInputRow(
    value: String,
    length: Int = 6,
    onValueChange: (String) -> Unit
) {
    BasicTextField(
        value = value,
        onValueChange = {
            val filtered = it.filter { ch -> ch.isDigit() }.take(length)
            onValueChange(filtered)
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Done
        ),
        cursorBrush = SolidColor(PurplePrimary),
        decorationBox = {
            Row(horizontalArrangement = Arrangement.Center) {
                repeat(length) { index ->
                    val char = value.getOrNull(index)?.toString() ?: ""
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(width = 44.dp, height = 48.dp)
                            .border(
                                width = 1.dp,
                                color = if (index == value.length) PurplePrimary else Color(0xFFD7D6DF),
                                shape = RoundedCornerShape(10.dp)
                            )
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = char, fontSize = 18.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    )
}

@Composable
fun PasswordRuleRow(
    label: String,
    satisfied: Boolean
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = null,
            tint = if (satisfied) Color(0xFF16A34A) else Color(0xFFBDBCC6),
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = if (satisfied) Color(0xFF111827) else MutedText
        )
    }
}

@Composable
fun AuthGradientBackground(): Brush {
    return Brush.verticalGradient(
        colors = listOf(
            Color(0xFF2C145C),
            Color(0xFF3C177B),
            Color(0xFF4A1D98)
        )
    )
}
