package com.karim.muslimcompanion.ui.screens

import android.Manifest
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.shouldShowRationale
import com.karim.muslimcompanion.R
import com.karim.muslimcompanion.model.DailyPrayerSchedule
import com.karim.muslimcompanion.ui.components.PrayerCard
import com.karim.muslimcompanion.util.DateTimeUtils
import com.karim.muslimcompanion.viewmodel.PrayerUiState
import com.karim.muslimcompanion.viewmodel.PrayerViewModel

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PrayerTimesScreen(
    viewModel: PrayerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val now by viewModel.now.collectAsState()

    val permissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    LaunchedEffect(permissionsState.allPermissionsGranted) {
        if (permissionsState.allPermissionsGranted) {
            viewModel.refresh()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.app_name), fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
                        )
                    )
                )
        ) {
            when {
                !permissionsState.allPermissionsGranted -> {
                    PermissionRequestContent(
                        showRationale = permissionsState.shouldShowRationale,
                        onGrantClick = { permissionsState.launchMultiplePermissionRequest() }
                    )
                }
                uiState is PrayerUiState.LocatingDevice || uiState is PrayerUiState.LoadingTimings -> {
                    LoadingContent(uiState)
                }
                uiState is PrayerUiState.LocationError -> {
                    ErrorContent(
                        message = stringResource(id = R.string.location_error),
                        onRetry = { viewModel.refresh() }
                    )
                }
                uiState is PrayerUiState.NetworkError -> {
                    ErrorContent(
                        message = stringResource(id = R.string.network_error),
                        onRetry = { viewModel.refresh() }
                    )
                }
                uiState is PrayerUiState.Loaded -> {
                    val schedule = (uiState as PrayerUiState.Loaded).schedule
                    LoadedContent(schedule = schedule, now = now)
                }
            }
        }
    }
}

@Composable
private fun LoadingContent(state: PrayerUiState) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.secondary)
        Text(
            modifier = Modifier.padding(top = 16.dp),
            text = stringResource(
                id = if (state is PrayerUiState.LocatingDevice) R.string.fetching_location else R.string.fetching_timings
            ),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun ErrorContent(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = message, style = MaterialTheme.typography.bodyLarge, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        Button(modifier = Modifier.padding(top = 20.dp), onClick = onRetry) {
            Text(text = stringResource(id = R.string.retry))
        }
    }
}

@Composable
private fun PermissionRequestContent(showRationale: Boolean, onGrantClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(id = R.string.location_permission_rationale),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Button(modifier = Modifier.padding(top = 20.dp), onClick = onGrantClick) {
            Text(text = stringResource(id = R.string.grant_permission))
        }
    }
}

@Composable
private fun LoadedContent(
    schedule: DailyPrayerSchedule,
    now: java.time.LocalDateTime
) {
    val nextPrayer = schedule.nextPrayer(now)

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            NextPrayerHeader(dateLabel = schedule.date, nextPrayerName = nextPrayer?.displayName, countdown = nextPrayer?.let {
                DateTimeUtils.formatCountdown(now, it.dateTime)
            })
        }

        items(schedule.prayers) { prayer ->
            PrayerCard(prayer = prayer, isNext = nextPrayer?.key == prayer.key)
        }
    }
}

@Composable
private fun NextPrayerHeader(dateLabel: String, nextPrayerName: String?, countdown: String?) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.primaryContainer
                    )
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .padding(24.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text(text = dateLabel, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimary)

            if (nextPrayerName != null && countdown != null) {
                Text(
                    text = stringResource(id = R.string.next_prayer_in) + " " + nextPrayerName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(top = 12.dp)
                )
                Text(
                    text = countdown,
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}
