@file:OptIn(ExperimentalMaterial3Api::class)

package com.lhs.sefer.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lhs.sefer.AppViewModel
import com.lhs.sefer.R
import com.lhs.sefer.Tab

private val Tab.label: String
    get() = when (this) {
        Tab.READ -> "Read"
        Tab.SEARCH -> "Search"
        Tab.BOOKMARKS -> "Bookmarks"
        Tab.SETTINGS -> "Settings"
    }

private val Tab.icon: ImageVector
    get() = when (this) {
        Tab.READ -> Icons.AutoMirrored.Filled.MenuBook
        Tab.SEARCH -> Icons.Filled.Search
        Tab.BOOKMARKS -> Icons.Filled.Bookmark
        Tab.SETTINGS -> Icons.Filled.Settings
    }

@Composable
fun LhsApp(vm: AppViewModel = viewModel()) {
    LhsTheme(vm.settings.theme) {
        val snackbar = remember { SnackbarHostState() }
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            if (!vm.ready) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            painterResource(R.drawable.ic_star_of_david),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp),
                        )
                        Spacer(Modifier.height(16.dp))
                        CircularProgressIndicator()
                    }
                }
            } else {
                Box(Modifier.fillMaxSize()) {
                    MainScaffold(vm, snackbar)
                    val loc = vm.reader
                    if (loc != null) {
                        // opaque Surface so taps never fall through to the screen underneath
                        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                            ReaderScreen(vm, loc, snackbar)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MainScaffold(vm: AppViewModel, snackbar: SnackbarHostState) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    if (vm.tab == Tab.READ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painterResource(R.drawable.ic_star_of_david),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(26.dp),
                            )
                            Spacer(Modifier.width(10.dp))
                            Text("Light Hebrew Sefer")
                        }
                    } else {
                        Text(vm.tab.label)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
        bottomBar = {
            NavigationBar {
                Tab.entries.forEach { t ->
                    NavigationBarItem(
                        selected = vm.tab == t,
                        onClick = { vm.tab = t },
                        icon = { Icon(t.icon, contentDescription = t.label) },
                        label = { Text(t.label) },
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when (vm.tab) {
                Tab.READ -> ReadTab(vm)
                Tab.SEARCH -> SearchTab(vm, snackbar)
                Tab.BOOKMARKS -> BookmarksTab(vm, snackbar)
                Tab.SETTINGS -> SettingsTab(vm)
            }
        }
    }
}
