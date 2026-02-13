package com.riva.watchtower.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.riva.watchtower.domain.enums.SiteStatus
import com.riva.watchtower.domain.models.Site
import com.riva.watchtower.presentation.theme.GreenBackground
import com.riva.watchtower.presentation.theme.GreenValue
import com.riva.watchtower.presentation.theme.OrangeBackground
import com.riva.watchtower.presentation.theme.OrangeValue
import java.text.SimpleDateFormat
import java.util.Date
import org.koin.compose.koinInject
import java.util.Locale

@Composable
fun SiteListCard(
    modifier: Modifier = Modifier,
    site: Site,
    onClick: (siteId: String) -> Unit
) {
    val context = LocalContext.current
    val imageLoader = koinInject<ImageLoader>()
    val dateFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())

    Card(
        onClick = { onClick(site.id) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Favicon
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(site.favicon)
                    .build(),
                imageLoader = imageLoader,
                contentDescription = "${site.name} favicon",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )

            Spacer(Modifier.width(14.dp))

            // Site Info
            Column(
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = site.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = site.url,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Scanned ${dateFormat.format(Date(site.lastCheckedAt))}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            Spacer(Modifier.width(10.dp))

            // Status Indicator
            StatusBadge(status = site.lastStatus)
        }
    }
}

@Composable
private fun StatusBadge(status: SiteStatus) {
    val (icon, color, bgColor) = when (status) {
        SiteStatus.PASSED -> Triple(Icons.Default.CheckCircle, GreenValue, GreenBackground)
        SiteStatus.CHANGED -> Triple(Icons.Default.Warning, OrangeValue, OrangeBackground)
        SiteStatus.ERROR -> Triple(
            Icons.Default.Error,
            Color(0xFFC62828),
            Color(0xFFF44336).copy(alpha = 0.1f)
        )
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(MaterialTheme.shapes.small)
            .background(bgColor)
            .padding(horizontal = 10.dp, vertical = 5.dp)
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
