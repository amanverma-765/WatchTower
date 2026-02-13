package com.riva.watchtower.presentation.features.detail.screens

import android.content.Intent
import android.webkit.WebView
import androidx.core.graphics.toColorInt
import androidx.core.net.toUri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.riva.watchtower.domain.enums.SiteStatus
import com.riva.watchtower.presentation.components.RemoteImage
import com.riva.watchtower.presentation.components.StatusBadge
import com.riva.watchtower.presentation.features.detail.logic.DetailUiEvent
import com.riva.watchtower.presentation.features.detail.logic.DetailUiState
import com.riva.watchtower.presentation.features.detail.logic.DetailViewModel
import com.riva.watchtower.presentation.theme.ErrorRedValue
import com.riva.watchtower.presentation.theme.GreenValue
import com.riva.watchtower.presentation.theme.OrangeValue
import com.riva.watchtower.utils.DateFormatter
import org.koin.androidx.compose.koinViewModel

@Composable
fun DetailScreenRoot(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit,
    onDeleted: () -> Unit
) {
    val viewModel = koinViewModel<DetailViewModel>()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    DetailScreen(
        modifier = modifier,
        uiState = uiState,
        onEvent = { event ->
            when (event) {
                DetailUiEvent.Delete -> {
                    viewModel.onEvent(event)
                    onDeleted()
                }
                else -> viewModel.onEvent(event)
            }
        },
        onBackClick = onBackClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetailScreen(
    modifier: Modifier = Modifier,
    uiState: DetailUiState,
    onEvent: (DetailUiEvent) -> Unit,
    onBackClick: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Site") },
            text = {
                Text("Are you sure you want to delete ${uiState.site?.name ?: "this site"}? This action cannot be undone.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onEvent(DetailUiEvent.Delete)
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.site?.name ?: "Site Detail",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { onEvent(DetailUiEvent.Recheck) },
                        enabled = !uiState.isLoading
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Recheck"
                            )
                        }
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete site",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        if (uiState.isLoading && uiState.site == null) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.site != null) {
            val site = uiState.site
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Top section — scrollable info + actions
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    SiteInfoCard(
                        name = site.name,
                        url = site.url,
                        favicon = site.favicon,
                        status = site.lastStatus,
                        lastCheckedAt = site.lastCheckedAt
                    )

                    // Action buttons row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val context = LocalContext.current
                        OutlinedButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, site.url.toUri())
                                context.startActivity(intent)
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text("Open in Browser")
                        }

                        if (site.lastStatus == SiteStatus.CHANGED) {
                            Button(
                                onClick = { onEvent(DetailUiEvent.MarkResolved) },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text("Resolve")
                            }
                        }
                    }
                }

                // Bottom section — WebView or placeholder, fills remaining space
                if (uiState.hasDiff && uiState.changedHtml != null) {
                    // Divider + label
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = "Changes Detected",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(8.dp))
                    }

                    // WebView rendering the changed HTML
                    Card(
                        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFFAFAFA)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(horizontal = 16.dp)
                    ) {
                        val htmlContent = uiState.changedHtml
                        val baseUrl = site.url
                        AndroidView(
                            factory = { ctx ->
                                WebView(ctx).apply {
                                    settings.javaScriptEnabled = false
                                    setBackgroundColor("#FAFAFA".toColorInt())
                                    tag = htmlContent
                                    loadDataWithBaseURL(
                                        baseUrl,
                                        htmlContent,
                                        "text/html",
                                        "UTF-8",
                                        null
                                    )
                                }
                            },
                            update = { webView ->
                                val current = webView.tag as? String
                                if (current != htmlContent) {
                                    webView.tag = htmlContent
                                    webView.loadDataWithBaseURL(
                                        baseUrl,
                                        htmlContent,
                                        "text/html",
                                        "UTF-8",
                                        null
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                } else {
                    // No-change placeholder
                    NoChangePlaceholder(
                        status = site.lastStatus,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    )
                }
            }
        } else {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Text(
                    text = uiState.errorMessage ?: "Site not found",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun NoChangePlaceholder(
    status: SiteStatus,
    modifier: Modifier = Modifier
) {
    val (icon, tint, title, subtitle) = when (status) {
        SiteStatus.PASSED -> PlaceholderData(
            icon = Icons.Outlined.Verified,
            tint = GreenValue,
            title = "All Clear",
            subtitle = "No changes detected on this website.\nEverything looks the same as last time."
        )
        SiteStatus.ERROR -> PlaceholderData(
            icon = Icons.Default.Error,
            tint = ErrorRedValue,
            title = "Check Failed",
            subtitle = "Couldn't reach this website.\nTry rechecking or verify the URL is correct."
        )
        SiteStatus.CHANGED -> PlaceholderData(
            icon = Icons.Default.Warning,
            tint = OrangeValue,
            title = "Loading Changes...",
            subtitle = "Preparing the diff view."
        )
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.padding(32.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(tint.copy(alpha = 0.08f))
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(40.dp)
                )
            }
            Spacer(Modifier.height(20.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

private data class PlaceholderData(
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val tint: Color,
    val title: String,
    val subtitle: String
)

@Composable
private fun SiteInfoCard(
    name: String,
    url: String,
    favicon: String,
    status: SiteStatus,
    lastCheckedAt: Long
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                RemoteImage(
                    url = favicon,
                    contentDescription = "$name favicon",
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(2.dp))
                    StatusBadge(status = status)
                }
            }

            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Language,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = url,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.AccessTime,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Last scanned ${DateFormatter.formatLong(lastCheckedAt)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

