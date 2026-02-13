package com.riva.watchtower.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.riva.watchtower.domain.enums.SiteStatus
import com.riva.watchtower.presentation.theme.ErrorRedBackground
import com.riva.watchtower.presentation.theme.ErrorRedValue
import com.riva.watchtower.presentation.theme.GreenBackground
import com.riva.watchtower.presentation.theme.GreenValue
import com.riva.watchtower.presentation.theme.OrangeBackground
import com.riva.watchtower.presentation.theme.OrangeValue

@Composable
fun StatusBadge(status: SiteStatus) {
    val (icon, color, bgColor) = when (status) {
        SiteStatus.PASSED -> Triple(Icons.Default.CheckCircle, GreenValue, GreenBackground)
        SiteStatus.CHANGED -> Triple(Icons.Default.Warning, OrangeValue, OrangeBackground)
        SiteStatus.ERROR -> Triple(Icons.Default.Error, ErrorRedValue, ErrorRedBackground)
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(MaterialTheme.shapes.small)
            .background(bgColor)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = status.text,
            tint = color,
            modifier = Modifier.size(14.dp)
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = status.text,
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}
