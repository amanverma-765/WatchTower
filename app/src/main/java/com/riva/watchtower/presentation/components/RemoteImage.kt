package com.riva.watchtower.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import org.koin.compose.koinInject

@Composable
fun RemoteImage(
    url: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    fallbackUrl: String? = null,
    contentScale: ContentScale = ContentScale.Crop,
    imageLoader: ImageLoader = koinInject()
) {
    var useFallback by remember(url) { mutableStateOf(false) }
    val activeUrl = if (useFallback && fallbackUrl != null) fallbackUrl else url

    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current).data(activeUrl).build(),
        imageLoader = imageLoader,
        contentDescription = contentDescription,
        contentScale = contentScale,
        onError = {
            if (!useFallback && fallbackUrl != null) {
                useFallback = true
            }
        },
        modifier = modifier
    )
}
