package com.riva.watchtower.presentation.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.riva.watchtower.presentation.features.detail.screens.DetailScreenRoot
import com.riva.watchtower.presentation.features.home.screens.HomeScreenRoot
import com.riva.watchtower.presentation.features.settings.screens.SettingsScreenRoot


@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    startDestination: AppDestinations
) {
    val controller = rememberNavController()

    NavHost(
        navController = controller,
        startDestination = startDestination,
        enterTransition = { slideInHorizontally(tween(300)) { it } },
        exitTransition = { slideOutHorizontally(tween(300)) { -it } },
        popEnterTransition = { slideInHorizontally(tween(300)) { -it } },
        popExitTransition = { slideOutHorizontally(tween(300)) { it } },
        modifier = modifier
    ) {
        composable<AppDestinations.Home> {
            HomeScreenRoot(
                onSiteClicked = { siteId ->
                    controller.navigate(AppDestinations.Detail(siteId))
                },
                onSettingsClick = {
                    controller.navigate(AppDestinations.Settings)
                }
            )
        }

        composable<AppDestinations.Settings> {
            SettingsScreenRoot(
                onBackClick = { controller.popBackStack() }
            )
        }

        composable<AppDestinations.Detail> {
            DetailScreenRoot(
                onBackClick = { controller.popBackStack() },
                onDeleted = { controller.popBackStack() }
            )
        }
    }
}
