package com.example.vibrolistner.feature.home

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.vibrolistner.core.database.KeywordEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToAddKeyword: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    // Permission checks
    var isNotificationListenerEnabled by remember {
        mutableStateOf(isNotificationListenerEnabled(context))
    }
    var isBatteryOptimizationIgnored by remember {
        mutableStateOf(isBatteryOptimizationIgnored(context))
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    Text("VibroListener")
                },
                scrollBehavior = scrollBehavior,
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddKeyword,
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Add keyword",
                )
            }
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Permission Status Cards
            item {
                Spacer(modifier = Modifier.height(4.dp))
                PermissionCard(
                    title = "Notification Listener Access",
                    description = if (isNotificationListenerEnabled) {
                        "Granted — the app can read notifications."
                    } else {
                        "Required to intercept notifications and match keywords."
                    },
                    isGranted = isNotificationListenerEnabled,
                    buttonText = if (isNotificationListenerEnabled) "Granted" else "Grant Access",
                    onRequestPermission = {
                        context.startActivity(
                            Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                        )
                    },
                    onResume = {
                        isNotificationListenerEnabled = isNotificationListenerEnabled(context)
                    },
                )
            }

            item {
                PermissionCard(
                    title = "Ignore Battery Optimization",
                    description = if (isBatteryOptimizationIgnored) {
                        "Granted — background service won't be killed."
                    } else {
                        "Critical for OnePlus devices. Prevents OxygenOS from killing the service."
                    },
                    isGranted = isBatteryOptimizationIgnored,
                    buttonText = if (isBatteryOptimizationIgnored) "Granted" else "Disable Optimization",
                    onRequestPermission = {
                        val intent = Intent(
                            Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                            Uri.parse("package:${context.packageName}"),
                        )
                        context.startActivity(intent)
                    },
                    onResume = {
                        isBatteryOptimizationIgnored = isBatteryOptimizationIgnored(context)
                    },
                )
            }

            // Section header
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Search Keywords",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            if (uiState.keywords.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "No keywords yet.\nTap + to add one.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            items(
                items = uiState.keywords,
                key = { it.id },
            ) { keyword ->
                KeywordItem(
                    keyword = keyword,
                    onDelete = { viewModel.deleteKeyword(keyword) },
                )
            }

            // Bottom spacing for FAB
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
private fun PermissionCard(
    title: String,
    description: String,
    isGranted: Boolean,
    buttonText: String,
    onRequestPermission: () -> Unit,
    onResume: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Refresh permission status when the composable is resumed
    androidx.lifecycle.compose.LifecycleResumeEffect(Unit) {
        onResume()
        onPauseOrDispose { }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isGranted) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.errorContainer
            },
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = if (isGranted) Icons.Filled.CheckCircle else Icons.Filled.Warning,
                contentDescription = null,
                modifier = Modifier.size(28.dp),
                tint = if (isGranted) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                },
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isGranted) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onErrorContainer
                    },
                )
            }
            AnimatedVisibility(
                visible = !isGranted,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                FilledTonalButton(onClick = onRequestPermission) {
                    Text(text = buttonText)
                }
            }
        }
    }
}

@Composable
private fun KeywordItem(
    keyword: KeywordEntity,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = keyword.keyword,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Delete keyword",
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

/** Checks if this app's NotificationListenerService is enabled in system settings. */
private fun isNotificationListenerEnabled(context: Context): Boolean {
    val componentName = ComponentName(context, com.example.vibrolistner.service.KeywordNotificationListenerService::class.java)
    val enabledListeners = Settings.Secure.getString(
        context.contentResolver,
        "enabled_notification_listeners",
    ) ?: return false
    return enabledListeners.contains(componentName.flattenToString())
}

/** Checks if the app is ignoring battery optimizations. */
private fun isBatteryOptimizationIgnored(context: Context): Boolean {
    val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    return powerManager.isIgnoringBatteryOptimizations(context.packageName)
}
