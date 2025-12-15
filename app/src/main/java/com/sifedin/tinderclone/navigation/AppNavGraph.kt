package com.sifedin.tinderclone.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.sifedin.tinderclone.ui.screens.auth.EmailRegisterScreen
import com.sifedin.tinderclone.ui.screens.auth.WelcomeScreen
import com.sifedin.tinderclone.ui.screens.chat.ChatScreen
import com.sifedin.tinderclone.ui.screens.chat.ChatsScreen
import com.sifedin.tinderclone.ui.screens.likes.LikesScreen
import com.sifedin.tinderclone.ui.screens.match.MatchScreen
import com.sifedin.tinderclone.ui.screens.profile.AddPhotosScreen
import com.sifedin.tinderclone.ui.screens.profile.IdentifyYourselfScreen
import com.sifedin.tinderclone.ui.screens.profile.InterestedScreen
import com.sifedin.tinderclone.ui.screens.settings.DeleteAccountScreen
import com.sifedin.tinderclone.ui.screens.settings.EditProfileScreen
import com.sifedin.tinderclone.ui.screens.settings.SettingsScreen
import com.sifedin.tinderclone.ui.screens.swipe.SwipeScreen
import com.sifedin.tinderclone.viewmodel.AuthViewModel
import com.sifedin.tinderclone.viewmodel.ChatViewModel
import com.sifedin.tinderclone.viewmodel.LikesViewModel
import com.sifedin.tinderclone.viewmodel.SettingsViewModel
import com.sifedin.tinderclone.viewmodel.SwipeViewModel

object Routes {
    const val WELCOME = "welcome"
    const val EMAIL_REGISTER = "email_register"
    const val IDENTIFY = "identify"
    const val INTEREST = "interest"
    const val PHOTOS = "photos"
    const val HOME = "swipe"
    const val MATCH = "match_found"
    const val CHATS = "chats_list"
    const val CHAT_DETAIL = "chat_detail"
    const val LIKES = "likes_placeholder"
    const val SETTINGS = "settings_menu"
    const val EDIT_PROFILE = "edit_profile"
    const val DELETE_ACCOUNT = "delete_account"
}

@Composable
fun AppNavGraph(
    nav: NavHostController,
    vm: AuthViewModel,
    modifier: Modifier = Modifier
) {
    val chatVM: ChatViewModel = viewModel()
    val likesVM: LikesViewModel = viewModel()
    val settingsVM: SettingsViewModel = viewModel()

    NavHost(
        navController = nav,
        startDestination = Routes.WELCOME,
        modifier = modifier
    ) {
        composable(Routes.WELCOME) {
            WelcomeScreen { nav.navigate(Routes.EMAIL_REGISTER) }
        }

        composable(Routes.EMAIL_REGISTER) {
            vm.clearUserData()
            chatVM.closeChat()

            EmailRegisterScreen(
                viewModel = vm,
                onSuccess = {
                    nav.navigate(Routes.IDENTIFY) {
                        popUpTo(Routes.WELCOME) { inclusive = true }
                    }
                },
                onProfileComplete = {
                    chatVM.loadMatches()
                    nav.navigate(Routes.HOME) {
                        popUpTo(Routes.WELCOME) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.IDENTIFY) {
            IdentifyYourselfScreen(vm) { nav.navigate(Routes.INTEREST) }
        }

        composable(Routes.INTEREST) {
            InterestedScreen(vm) { nav.navigate(Routes.PHOTOS) }
        }

        composable(Routes.PHOTOS) {
            AddPhotosScreen(vm) {
                nav.navigate(Routes.HOME) {
                    popUpTo(Routes.WELCOME) { inclusive = true }
                }
            }
        }

        composable(Routes.HOME) {
            val swipeVM: SwipeViewModel = viewModel()
            swipeVM.onMatchFound = { matchedUser, matchId ->
                nav.navigate("${Routes.MATCH}/${matchedUser.name}/${matchId}")
            }
            SwipeScreen(viewModel = swipeVM, navController = nav)
        }

        composable(
            route = "${Routes.MATCH}/{matchName}/{matchId}",
            arguments = listOf(
                navArgument("matchName") { type = NavType.StringType },
                navArgument("matchId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val matchName = backStackEntry.arguments?.getString("matchName") ?: "Someone"
            val matchId = backStackEntry.arguments?.getString("matchId") ?: ""

            MatchScreen(
                matchName = matchName,
                matchId = matchId,
                onSendMessage = { id ->
                    nav.popBackStack(Routes.HOME, inclusive = false)
                    nav.navigate("${Routes.CHAT_DETAIL}/${id}")
                }
            ) {
                nav.popBackStack(Routes.HOME, inclusive = false)
            }
        }

        composable(Routes.CHATS) {
            chatVM.loadMatches()
            ChatsScreen(viewModel = chatVM) { matchId, _ ->
                nav.navigate("${Routes.CHAT_DETAIL}/$matchId")
            }
        }

        composable(
            route = "${Routes.CHAT_DETAIL}/{matchId}",
            arguments = listOf(
                navArgument("matchId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val matchId = backStackEntry.arguments?.getString("matchId") ?: ""
            ChatScreen(
                matchId = matchId,
                viewModel = chatVM,
                onBack = {
                    chatVM.closeChat()
                    nav.popBackStack()
                }
            )
        }

        composable(Routes.LIKES) {
            LikesScreen(
                viewModel = likesVM,
                onBack = { nav.popBackStack() },
                onMatchCreated = { matchId, matchedUser ->
                    nav.navigate("${Routes.MATCH}/${matchedUser.name}/${matchId}")
                }
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                viewModel = settingsVM,
                onNavigateToEditProfile = { nav.navigate(Routes.EDIT_PROFILE) },
                onNavigateToDeleteAccount = { nav.navigate(Routes.DELETE_ACCOUNT) },
                onLogout = {
                    vm.signOut()
                    chatVM.closeChat()
                    nav.navigate(Routes.EMAIL_REGISTER) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.EDIT_PROFILE) {
            EditProfileScreen(
                viewModel = settingsVM,
                onBack = { nav.popBackStack() }
            )
        }

        composable(Routes.DELETE_ACCOUNT) {
            DeleteAccountScreen(
                viewModel = settingsVM,
                onBack = { nav.popBackStack() },
                onAccountDeleted = {
                    vm.signOut()
                    chatVM.closeChat()
                    nav.navigate(Routes.WELCOME) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}