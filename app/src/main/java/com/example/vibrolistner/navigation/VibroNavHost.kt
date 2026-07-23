package com.example.vibrolistner.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.vibrolistner.feature.addkeyword.AddKeywordScreen
import com.example.vibrolistner.feature.home.HomeScreen

@Composable
fun VibroNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = "home",
        modifier = modifier,
    ) {
        composable("home") {
            HomeScreen(
                onNavigateToAddKeyword = { navController.navigate("addKeyword") },
            )
        }
        composable("addKeyword") {
            AddKeywordScreen(
                onNavigateBack = { navController.popBackStack() },
            )
        }
    }
}
