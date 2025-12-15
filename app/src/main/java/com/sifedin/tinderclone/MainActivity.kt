package com.sifedin.tinderclone

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.sifedin.tinderclone.navigation.AppNavGraph
import com.sifedin.tinderclone.navigation.Routes
import com.sifedin.tinderclone.ui.theme.HolaDatingAppTheme
import com.sifedin.tinderclone.ui.theme.HolaPinkPrimary
import com.sifedin.tinderclone.viewmodel.AuthViewModel
import com.sifedin.tinderclone.ui.components.BottomNavBar

class MainActivity : ComponentActivity() {
    private val vm: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HolaDatingAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppRoot(vm)
                }
            }
        }
    }
}

@Composable
fun AppRoot(vm: AuthViewModel) {
    val nav = rememberNavController()
    val navBackStackEntry = nav.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route
    
    val view = LocalView.current
    SideEffect {
        val window = (view.context as ComponentActivity).window
        val appNavBarColor = Color(0xFF520010).toArgb()
        val statusBarColor = when (currentRoute) {
            Routes.WELCOME -> HolaPinkPrimary.toArgb()
            Routes.EMAIL_REGISTER, Routes.IDENTIFY, Routes.INTEREST, Routes.PHOTOS -> Color.Black.toArgb()
            Routes.HOME, Routes.LIKES, Routes.SETTINGS, Routes.CHATS -> appNavBarColor
            else -> appNavBarColor
        }
        val navigationBarColor = when (currentRoute) {
            Routes.WELCOME -> HolaPinkPrimary.toArgb()
            Routes.EMAIL_REGISTER, Routes.IDENTIFY, Routes.INTEREST, Routes.PHOTOS -> Color.Black.toArgb()
            Routes.HOME, Routes.LIKES, Routes.SETTINGS, Routes.CHATS -> appNavBarColor
            else -> appNavBarColor
        }
        window.statusBarColor = statusBarColor
        window.navigationBarColor = navigationBarColor
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
    }

    Scaffold(
        bottomBar = {
            BottomNavBar(navController = nav)
        }
    ) { paddingValues ->
        AppNavGraph(
            nav = nav,
            vm = vm,
            modifier = Modifier.padding(paddingValues)
        )
    }
}