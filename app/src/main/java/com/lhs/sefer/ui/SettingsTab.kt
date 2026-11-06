@file:OptIn(ExperimentalMaterial3Api::class)

package com.lhs.sefer.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lhs.sefer.AppViewModel

@Composable
fun SettingsTab(vm: AppViewModel) {
    val settings = vm.settings
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = 24.dp),
    ) {
        SectionTitle("Theme", Modifier.padding(top = 4.dp))
        ThemePicker(settings.theme, vm::setTheme)

        SectionTitle("Text")
        ReadingControls(settings, vm)

        SectionTitle("God's name")
        DivineNamePicker(settings, vm)

        SectionTitle("About")
        Text(
            "Light Hebrew Sefer (LHS)\nThe Hebrew Scriptures (Tanakh) · New Living Translation\n" +
                "Torah, Nevi'im and Ketuvim · 39 books · works fully offline.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
