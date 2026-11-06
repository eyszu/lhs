@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.lhs.sefer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lhs.sefer.AppViewModel
import com.lhs.sefer.data.AppTheme
import com.lhs.sefer.data.DivineName
import com.lhs.sefer.data.PALEO_YHWH
import com.lhs.sefer.data.ReaderFont
import com.lhs.sefer.data.Settings
import com.lhs.sefer.data.displayText
import java.util.Locale

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier.padding(top = 20.dp, bottom = 10.dp),
    )
}

/** Four swatches: Light, Dark, Dusk, Cool. */
@Composable
fun ThemePicker(selected: AppTheme, onSelect: (AppTheme) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        AppTheme.entries.forEach { t ->
            val cs = colorSchemeFor(t)
            val isSelected = t == selected
            Column(
                Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { onSelect(t) },
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(cs.background)
                        .border(
                            width = if (isSelected) 3.dp else 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outlineVariant,
                            shape = RoundedCornerShape(14.dp),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Aa", color = cs.onBackground, style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.width(6.dp))
                        Box(Modifier.size(10.dp).clip(CircleShape).background(cs.primary))
                    }
                }
                Text(
                    t.label,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
        }
    }
}

@Composable
private fun LabeledSlider(
    label: String,
    valueLabel: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    steps: Int,
    onChange: (Float) -> Unit,
) {
    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            Text(valueLabel, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Slider(value = value, onValueChange = onChange, valueRange = range, steps = steps)
    }
}

/** Font family, size and line spacing, plus a live preview. Used in Settings and the reader sheet. */
@Composable
fun ReadingControls(settings: Settings, vm: AppViewModel) {
    Column(Modifier.fillMaxWidth()) {
        Text("Font", style = MaterialTheme.typography.bodyLarge)
        Row(Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ReaderFont.entries.forEach { f ->
                FilterChip(
                    selected = settings.font == f,
                    onClick = { vm.setFont(f) },
                    label = { Text(f.label, fontFamily = f.family) },
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        LabeledSlider(
            label = "Text size",
            valueLabel = "${settings.fontSize.toInt()} sp",
            value = settings.fontSize,
            range = 14f..34f,
            steps = 19,
            onChange = vm::setFontSize,
        )
        LabeledSlider(
            label = "Line spacing",
            valueLabel = String.format(Locale.US, "%.1f×", settings.lineSpacing),
            value = settings.lineSpacing,
            range = 1.2f..2.2f,
            steps = 9,
            onChange = vm::setLineSpacing,
        )
        OutlinedCard(Modifier.fillMaxWidth().padding(top = 8.dp)) {
            Text(
                text = "In the beginning God created the heavens and the earth.",
                modifier = Modifier.padding(16.dp),
                style = TextStyle(
                    fontFamily = settings.font.family,
                    fontSize = settings.fontSize.sp,
                    lineHeight = (settings.fontSize * settings.lineSpacing).sp,
                    color = MaterialTheme.colorScheme.onSurface,
                ),
            )
        }
    }
}

/** How God's name is displayed (the database stores it in Paleo-Hebrew letters). */
@Composable
fun DivineNamePicker(settings: Settings, vm: AppViewModel) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        DivineName.entries.forEach { d ->
            FilterChip(
                selected = settings.divineName == d,
                onClick = { vm.setDivineName(d) },
                label = { Text(d.label) },
            )
        }
    }
    Text(
        "Preview: “" + displayText("Praise $PALEO_YHWH!", settings.divineName) + "”",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 8.dp),
    )
}
