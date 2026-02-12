package com.riva.watchtower.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.riva.watchtower.domain.enums.SiteStatus
import com.riva.watchtower.domain.models.Site
import com.riva.watchtower.presentation.theme.BlueBackground
import com.riva.watchtower.presentation.theme.BlueValue
import com.riva.watchtower.presentation.theme.GreenBackground
import com.riva.watchtower.presentation.theme.GreenValue
import com.riva.watchtower.presentation.theme.OrangeBackground
import com.riva.watchtower.presentation.theme.OrangeValue

@Composable
fun SiteListCard(
    modifier: Modifier = Modifier,
    site: Site,
    onClick: (siteId: String) -> Unit
) {
    val context = LocalContext.current

    Card(
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(.5.dp, MaterialTheme.colorScheme.outline),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick(site.id) }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Left side - Favicon + Info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Favicon
                AsyncImage(
                    imageLoader = ImageLoader(context),
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(site.favicon)
                        .crossfade(true)
                        .build(),
                    contentDescription = "${site.name} favicon",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )

                Spacer(Modifier.width(16.dp))

                // Site Info
                Column(
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = site.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.size(4.dp))
                    Text(
                        text = site.url,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            // Right side - Status Indicator
            StatusBadge(status = site.lastStatus)
        }
    }
}

@Composable
private fun StatusBadge(status: SiteStatus) {
    data class StatusStyle(
        val icon: ImageVector,
        val color: Color,
        val backgroundColor: Color
    )

    val style = when (status) {
        SiteStatus.PASSED -> StatusStyle(
            icon = Icons.Default.CheckCircle,
            color = GreenValue,
            backgroundColor = GreenBackground
        )
        SiteStatus.CHANGED -> StatusStyle(
            icon = Icons.Default.Warning,
            color = OrangeValue,
            backgroundColor = OrangeBackground
        )
        SiteStatus.ERROR -> StatusStyle(
            icon = Icons.Default.Error,
            color = Color(0xFFC62828),
            backgroundColor = Color(0xFFF44336).copy(alpha = 0.1f)
        )
        SiteStatus.RESOLVED -> StatusStyle(
            icon = Icons.Default.Info,
            color = BlueValue,
            backgroundColor = BlueBackground
        )
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier
            .clip(MaterialTheme.shapes.small)
            .background(style.backgroundColor)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Icon(
            imageVector = style.icon,
            contentDescription = status.text,
            tint = style.color,
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = status.text,
            style = MaterialTheme.typography.labelSmall,
            color = style.color
        )
    }
}