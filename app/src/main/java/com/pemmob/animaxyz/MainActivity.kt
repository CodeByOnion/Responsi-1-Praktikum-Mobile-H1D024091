package com.pemmob.animaxyz

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.pemmob.animaxyz.ui.screen.DetailScreen
import com.pemmob.animaxyz.ui.screen.HomeScreen
import com.pemmob.animaxyz.ui.theme.AnimaxyzTheme
import com.pemmob.animaxyz.ui.viewmodel.AnimeViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AnimaxyzTheme {
                val navController = rememberNavController()
                val viewModel: AnimeViewModel = viewModel()

                NavHost(
                    navController = navController,
                    startDestination = "home"
                ) {
                    composable("home") {
                        HomeScreen(
                            viewModel = viewModel,
                            onAnimeClick = { animeId ->
                                navController.navigate("detail/$animeId")
                            }
                        )
                    }

                    composable(
                        route = "detail/{animeId}",
                        arguments = listOf(
                            navArgument("animeId") { type = NavType.IntType }
                        )
                    ) { backStackEntry ->
                        val animeId = backStackEntry.arguments?.getInt("animeId") ?: 0
                        DetailScreen(
                            animeId = animeId,
                            viewModel = viewModel,
                            onBackClick = {
                                navController.popBackStack()
                            }
                        )
                    }
                }
            }
        }
    }
}