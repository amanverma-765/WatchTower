package com.riva.watchtower.presentation.features.home.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.riva.watchtower.domain.enums.SiteStatus
import com.riva.watchtower.presentation.components.SiteListHeader
import com.riva.watchtower.presentation.components.StatsCard
import com.riva.watchtower.presentation.features.home.logic.HomeUiEvent
import com.riva.watchtower.presentation.features.home.logic.HomeUiState
import com.riva.watchtower.presentation.features.home.logic.HomeViewModel
import org.koin.androidx.compose.koinViewModel


@Composable
fun HomeScreenRoot(
    modifier: Modifier = Modifier,
    onSiteCLicked: (siteId: String) -> Unit
) {
    val viewmodel = koinViewModel<HomeViewModel>()
    val uiState by viewmodel.uiState.collectAsStateWithLifecycle()

    HomeScreen(
        modifier = modifier,
        onSiteCLicked = onSiteCLicked,
        uiState = uiState,
        uiEvent = viewmodel::onEvent
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreen(
    modifier: Modifier = Modifier,
    onSiteCLicked: (siteId: String) -> Unit,
    uiState: HomeUiState,
    uiEvent: (HomeUiEvent) -> Unit
) {

    var selected by remember { mutableStateOf<SiteStatus?>(null) }
    var sheetVisible by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Dashboard")
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { sheetVisible = true }) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add a website"
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(16.dp),
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            item {
                StatsCard()
            }
            item {
                SiteListHeader(
                    selected = selected,
                    onClick = { selected = it }
                )
            }
        }
    }
    SiteAddSheet(
        visible = sheetVisible,
        sheetState = sheetState,
        onSiteUrlChange = {
            uiEvent.invoke(HomeUiEvent.OnSiteAddUrlChange(it))
        },
        siteUrl = uiState.siteAddUrl,
        siteError = uiState.siteAddUrlError,
        onSave = {
            uiEvent.invoke(HomeUiEvent.AddNewSite(it))
        },
        onDismiss = {
            sheetVisible = false
            uiEvent.invoke(HomeUiEvent.ClearAddSite)
        }
    )
}