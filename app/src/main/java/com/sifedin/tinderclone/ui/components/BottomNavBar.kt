package com.sifedin.tinderclone.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.rounded.ChatBubble
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.sifedin.tinderclone.navigation.Routes
import com.sifedin.tinderclone.ui.theme.HolaPinkPrimary

// 🚨 إضافة عنصر Profile/Settings
sealed class BottomNavItem(val route: String, val icon: ImageVector, val label: String) {
    object Settings : BottomNavItem(Routes.SETTINGS, Icons.Default.Person, "Profile")
    object Swipe : BottomNavItem(Routes.HOME, Icons.Default.Home, "Home")
    object Likes : BottomNavItem(Routes.LIKES, Icons.Default.Favorite, "Likes")
    object Chats : BottomNavItem(Routes.CHATS, Icons.Rounded.ChatBubble, "Chats")
}

@Composable
fun BottomNavBar(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    // 🚨 تغيير ترتيب العناصر
    val items = listOf(
        BottomNavItem.Settings,
        BottomNavItem.Swipe,
        BottomNavItem.Likes,
        BottomNavItem.Chats
    )

    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route

    // يجب إضافة مسار الإعدادات هنا
    val mainRoutes = listOf(Routes.SETTINGS, Routes.HOME, Routes.LIKES, Routes.CHATS)

    if (currentRoute !in mainRoutes) return

    NavigationBar(
        modifier = modifier,
        containerColor = Color(0xFF520010),
        tonalElevation = 8.dp
    ) {
        items.forEach { item ->
            val isSelected = currentRoute == item.route
            NavigationBarItem(
                icon = { 
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) HolaPinkPrimary.copy(alpha = 0.15f) 
                                else Color.Transparent
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            item.icon, 
                            contentDescription = item.label,
                            tint = if (isSelected) HolaPinkPrimary else Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                label = { 
                    Text(
                        item.label,
                        color = if (isSelected) HolaPinkPrimary else Color.White,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    )
                },
                selected = isSelected,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = HolaPinkPrimary,
                    unselectedIconColor = Color.White,
                    selectedTextColor = HolaPinkPrimary,
                    unselectedTextColor = Color.White,
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}