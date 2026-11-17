package com.luycaslima.harpia.ui.presentation.feature.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable

import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.luycaslima.harpia.ui.presentation.feature.capture.CaptureScreen
import com.luycaslima.harpia.ui.presentation.feature.home.HomeScreen


@Composable
fun Nav () {
    val navController = rememberNavController()
    NavHost(
        navController=navController,
        startDestination = "Home",
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(300)
            )
        },
        exitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(300)
            )
        },
        popEnterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(300)
            )
        },
        popExitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(300)
            )
        }
        ){
        composable (route = "Home"){
            HomeScreen(navController,)
        }
        composable (route = "Capture"){
            CaptureScreen(navController,)
        }
    }
}