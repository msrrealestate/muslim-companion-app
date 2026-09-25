package com.karim.muslimcompanion.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness2
import androidx.compose.material.icons.filled.Brightness5
import androidx.compose.material.icons.filled.Brightness7
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.karim.muslimcompanion.model.PrayerKey
import com.karim.muslimcompanion.model.PrayerTime
import com.karim.muslimcompanion.util.DateTimeUtils

private fun iconFor(key: PrayerKey): ImageVector = when (key) {
    PrayerKey.FAJR -> Icons.Filled.Brightness5
    PrayerKey.SUNRISE -> Icons.Filled.WbTwilight
    PrayerKey.DHUHR -> Icons.Filled.WbSunny
    PrayerKey.ASR -> Icons.Filled.Brightness7
    PrayerKey.MAGHRIB -> Icons.Filled.Brightness2
    PrayerKey.ISHA -> Icons.Filled.NightsStay
}

@Composable
fun PrayerCard(
    prayer: PrayerTime,
    isNext: Boolean,
    modifier: Modifier = Modifier
) {
    val containerColor = if (isNext) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = if (isNext) 0.25f else 0.12f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = iconFor(prayer.key),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }

                Column(modifier = Modifier.padding(start = 14.dp)) {
                    Text(text = prayer.displayName, style = MaterialTheme.typography.titleMedium)
                    if (isNext) {
                        Text(
                            text = "الصلاة القادمة",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }

            Text(
                text = DateTimeUtils.formatClock(prayer.dateTime),
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}
