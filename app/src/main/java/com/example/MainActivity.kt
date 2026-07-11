package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.Tv
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.AppDatabase
import com.example.data.Repository
import com.example.ui.ZeSportViewModel
import com.example.ui.ZeSportViewModelFactory
import com.example.ui.screens.*
import com.example.ui.theme.*

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize local Room DB and repository
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = Repository(database)

        // Instantiate our Ze Sport State ViewModel
        val viewModel: ZeSportViewModel by viewModels {
            ZeSportViewModelFactory(repository)
        }

        setContent {
            ZeSportTheme {
                MainAppContainer(viewModel = viewModel)
            }
        }
    }
}

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val selectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val unselectedIcon: androidx.compose.ui.graphics.vector.ImageVector
) {
    object Home : BottomNavItem("home", "Home", Icons.Filled.Home, Icons.Outlined.Home)
    object Highlights : BottomNavItem("highlights", "Recaps", Icons.Filled.PlayCircle, Icons.Outlined.PlayCircle)
    object Channels : BottomNavItem("channels", "Channels", Icons.Filled.Tv, Icons.Outlined.Tv)
    object WorldCup : BottomNavItem("world_cup", "World Cup", Icons.Filled.Star, Icons.Outlined.Star)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContainer(viewModel: ZeSportViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Verify if current screen is a top-level tab (to display bottom navigation bar)
    val showBottomBar = remember(currentRoute) {
        currentRoute in listOf("home", "highlights", "channels", "world_cup")
    }

    Scaffold(
        topBar = {
            if (showBottomBar) {
                CenterAlignedTopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(ZeSportAccent, RoundedCornerShape(6.dp))
                                    .size(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "ZE",
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 14.sp
                                )
                            }
                            Column {
                                Text(
                                    text = "Ze Sport",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = "Live broadcasts & Match Center",
                                    color = ZeSportMuted,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = ZeSportSurface,
                        titleContentColor = ZeSportText
                    ),
                    modifier = Modifier.border(0.dp, ZeSportLine)
                )
            }
        },
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = ZeSportSurface,
                    contentColor = ZeSportText,
                    tonalElevation = 8.dp,
                    modifier = Modifier
                        .navigationBarsPadding() // Ensures bottom safe zone is respected
                        .border(1.dp, ZeSportLine, RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                ) {
                    val items = listOf(
                        BottomNavItem.Home,
                        BottomNavItem.Highlights,
                        BottomNavItem.Channels,
                        BottomNavItem.WorldCup
                    )
                    items.forEach { item ->
                        val isSelected = currentRoute == item.route
                        NavigationBarItem(
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
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.title
                                )
                            },
                            label = {
                                Text(
                                    text = item.title,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.White,
                                selectedTextColor = Color.White,
                                indicatorColor = ZeSportAccent,
                                unselectedIconColor = ZeSportMuted,
                                unselectedTextColor = ZeSportMuted
                            )
                        )
                    }
                }
            }
        },
        containerColor = ZeSportBg,
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            // Core Bottom Tab: Home Screen
            composable("home") {
                HomeScreen(
                    viewModel = viewModel,
                    onNavigateToMatch = { matchId ->
                        navController.navigate("match_details/$matchId")
                    },
                    onNavigateToChannel = { channelId ->
                        navController.navigate("channel_details/$channelId")
                    },
                    onNavigateToHighlight = { highlightId ->
                        navController.navigate("highlight_details/$highlightId")
                    }
                )
            }

            // Core Bottom Tab: Highlights Screen
            composable("highlights") {
                HighlightsScreen(
                    viewModel = viewModel,
                    onNavigateToHighlight = { highlightId ->
                        navController.navigate("highlight_details/$highlightId")
                    }
                )
            }

            // Core Bottom Tab: Channels Screen
            composable("channels") {
                ChannelsScreen(
                    viewModel = viewModel,
                    onNavigateToChannel = { channelId ->
                        navController.navigate("channel_details/$channelId")
                    }
                )
            }

            // Core Bottom Tab: World Cup Standings & Fixtures
            composable("world_cup") {
                WorldCupScreen(
                    viewModel = viewModel,
                    onNavigateToMatch = { matchId ->
                        navController.navigate("match_details/$matchId")
                    }
                )
            }

            // Match Detail Scoreboard + Stream Screen
            composable(
                route = "match_details/{matchId}",
                arguments = listOf(navArgument("matchId") { type = NavType.StringType })
            ) { backStackEntry ->
                val matchId = backStackEntry.arguments?.getString("matchId") ?: ""
                MatchDetailScreen(
                    matchId = matchId,
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            // Channel Detailed Stream Screen
            composable(
                route = "channel_details/{channelId}",
                arguments = listOf(navArgument("channelId") { type = NavType.StringType })
            ) { backStackEntry ->
                val channelId = backStackEntry.arguments?.getString("channelId") ?: ""
                ChannelDetailScreen(
                    channelId = channelId,
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            // Video Recap Highlight Detailed Screen
            composable(
                route = "highlight_details/{highlightId}",
                arguments = listOf(navArgument("highlightId") { type = NavType.StringType })
            ) { backStackEntry ->
                val highlightId = backStackEntry.arguments?.getString("highlightId") ?: ""
                HighlightDetailScreen(
                    highlightId = highlightId,
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onNavigateToHighlight = { targetHighlightId ->
                        navController.navigate("highlight_details/$targetHighlightId") {
                            popUpTo("highlight_details/{highlightId}") { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
