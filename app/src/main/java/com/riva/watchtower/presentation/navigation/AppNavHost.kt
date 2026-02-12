package com.riva.watchtower.presentation.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.riva.watchtower.presentation.features.home.HomeScreen
import com.riva.watchtower.presentation.features.siteadd.SiteAddScreen


@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    startDestination: AppDestinations
) {
    val controller = rememberNavController()

    NavHost(
        navController = controller,
        startDestination = startDestination,
        enterTransition = { slideInHorizontally(tween(500)) { it } },
        exitTransition = { slideOutHorizontally(tween(500)) { -it } },
        popEnterTransition = { slideInHorizontally(tween(500)) { -it } },
        popExitTransition = { slideOutHorizontally(tween(500)) { it } },
        modifier = modifier
    ) {
        composable<AppDestinations.Home> {
            HomeScreen(
                onFabClicked = {
                    controller.navigate(AppDestinations.SiteAdd)
                }
            )
        }

        composable<AppDestinations.Alerts> {

        }

        composable<AppDestinations.Detail> {

        }

        composable<AppDestinations.SiteAdd> {
            SiteAddScreen()
        }
    }
}