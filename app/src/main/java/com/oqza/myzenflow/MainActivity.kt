package com.oqza.myzenflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.oqza.myzenflow.presentation.components.BottomNavigationBar
import com.oqza.myzenflow.presentation.navigation.NavGraph
import com.oqza.myzenflow.presentation.navigation.Screen
import com.oqza.myzenflow.presentation.theme.MyZenFlowTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyZenFlowTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                // Hide bottom bar on immersive screens
                val shouldShowBottomBar = currentRoute != Screen.Breathing.route &&
                        currentRoute != Screen.Focus.route

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        if (shouldShowBottomBar) {
                            BottomNavigationBar(navController = navController)
                        }
                    }
                ) { innerPadding ->
                    NavGraph(
                        navController = navController,
                        startDestination = Screen.Home.route
                    )
                }
            }
        }
    }
}
