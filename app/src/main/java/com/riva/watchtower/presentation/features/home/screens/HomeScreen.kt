package com.riva.watchtower.presentation.features.home.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.riva.watchtower.domain.enums.SiteStatus
import com.riva.watchtower.presentation.components.SiteListCard
import com.riva.watchtower.presentation.components.SiteListHeader
import com.riva.watchtower.presentation.components.StatsCard
import com.riva.watchtower.presentation.features.home.logic.HomeUiEvent
import com.riva.watchtower.presentation.features.home.logic.HomeUiState
import com.riva.watchtower.presentation.features.home.logic.HomeViewModel
import org.koin.androidx.compose.koinViewModel


@Composable
fun HomeScreenRoot(
    modifier: Modifier = Modifier,
    onSiteClicked: (siteId: String) -> Unit
) {
    val viewmodel = koinViewModel<HomeViewModel>()
    val uiState by viewmodel.uiState.collectAsStateWithLifecycle()

    HomeScreen(
        modifier = modifier,
        onSiteClicked = onSiteClicked,
        uiState = uiState,
        uiEvent = viewmodel::onEvent
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreen(
    modifier: Modifier = Modifier,
    onSiteClicked: (siteId: String) -> Unit,
    uiState: HomeUiState,
    uiEvent: (HomeUiEvent) -> Unit
) {

    var selected by remember { mutableStateOf<SiteStatus?>(null) }
    var sheetVisible by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    // Error dialog
    if (uiState.errorMessage != null) {
        AlertDialog(
            onDismissRequest = { uiEvent(HomeUiEvent.ClearError) },
            title = { Text("Error") },
            text = { Text(uiState.errorMessage) },
            confirmButton = {
                TextButton(onClick = { uiEvent(HomeUiEvent.ClearError) }) {
                    Text("OK")
                }
            }
        )
    }

    val filteredSites = remember(uiState.sites, selected) {
        if (selected == null) uiState.sites
        else uiState.sites.filter { it.lastStatus == selected }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Dashboard")
                },
                actions = {
                    IconButton(
                        onClick = { uiEvent(HomeUiEvent.CheckAllSites) },
                        enabled = !uiState.isChecking
                    ) {
                        if (uiState.isChecking) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Check all sites"
                            )
                        }
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
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    StatsCard(
                        totalCount = uiState.sites.size,
                        changedCount = uiState.sites.count { it.lastStatus == SiteStatus.CHANGED },
                        passedCount = uiState.sites.count { it.lastStatus == SiteStatus.PASSED }
                    )
                }
                item {
                    SiteListHeader(
                        selected = selected,
                        onClick = { selected = it }
                    )
                }
                items(filteredSites, key = { it.id }) { site ->
                    SiteListCard(
                        site = site,
                        onClick = onSiteClicked
                    )
                }
            }

            // Center loader when adding a site
            if (uiState.siteAddLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
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
