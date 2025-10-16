package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.data.AppContainer
import com.example.myapplication.R
import com.example.myapplication.ui.WordsViewModel
import com.example.myapplication.ui.screens.AddWordScreen
import com.example.myapplication.ui.screens.LibraryScreen
import com.example.myapplication.ui.screens.QuizScreen
import com.example.myapplication.ui.screens.ReviewScreen
import com.example.myapplication.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val appContainer: AppContainer = (application as WordsLearningApp).container
        setContent {
            MyApplicationTheme {
                WordsLearningRoot(appContainer = appContainer)
            }
        }
    }
}

@Composable
private fun WordsLearningRoot(appContainer: AppContainer) {
    val navController = rememberNavController()
    val viewModel: WordsViewModel = viewModel(
        factory = WordsViewModel.provideFactory(appContainer.wordsRepository)
    )

    val reviewState by viewModel.reviewUiState.collectAsStateWithLifecycle()
    val libraryState by viewModel.libraryUiState.collectAsStateWithLifecycle()
    val quizState by viewModel.quizUiState.collectAsStateWithLifecycle()

    var lastTopLevelRoute by rememberSaveable { mutableStateOf(TopLevelDestination.REVIEW.route) }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    LaunchedEffect(currentRoute) {
        if (currentRoute != null && TopLevelDestination.entries.any { it.route == currentRoute }) {
            lastTopLevelRoute = currentRoute
        }
    }

    val selectedDestination = TopLevelDestination.entries.firstOrNull { it.route == lastTopLevelRoute }
        ?: TopLevelDestination.REVIEW

    var optionCount by rememberSaveable { mutableStateOf(WordsViewModel.DEFAULT_OPTION_COUNT) }

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            TopLevelDestination.entries.forEach { destination ->
                item(
                    icon = {
                        Icon(destination.icon, contentDescription = null)
                    },
                    label = { Text(text = stringResource(id = destination.labelRes)) },
                    selected = destination == selectedDestination,
                    onClick = {
                        lastTopLevelRoute = destination.route
                        navController.navigate(destination.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = TopLevelDestination.REVIEW.route,
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
        ) {
            composable(TopLevelDestination.REVIEW.route) {
                ReviewScreen(
                    state = reviewState,
                    optionCount = optionCount,
                    onOptionCountChange = { optionCount = it },
                    onThemeToggle = viewModel::toggleTheme,
                    onSelectAll = viewModel::selectAllThemes,
                    onClearThemes = viewModel::clearThemes,
                    onStartQuiz = {
                        viewModel.startQuiz(optionCount)
                        navController.navigate(AppScreens.QUIZ.route)
                    }
                )
            }

            composable(TopLevelDestination.LIBRARY.route) {
                LibraryScreen(
                    state = libraryState,
                    availableThemes = reviewState.themes,
                    onThemeToggle = viewModel::toggleTheme,
                    onSelectAll = viewModel::selectAllThemes,
                    onClearThemes = viewModel::clearThemes
                )
            }

            composable(TopLevelDestination.ADD.route) {
                AddWordScreen(
                    onAddWord = viewModel::addWord
                )
            }

            composable(AppScreens.QUIZ.route) {
                QuizScreen(
                    state = quizState,
                    onAnswerSelected = viewModel::submitAnswer,
                    onNextQuestion = viewModel::loadNextQuestion,
                    onExit = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}

private enum class TopLevelDestination(
    val route: String,
    @androidx.annotation.StringRes val labelRes: Int,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    REVIEW("review", R.string.nav_review, Icons.Default.PlayArrow),
    LIBRARY("library", R.string.nav_library, Icons.Default.LibraryBooks),
    ADD("add", R.string.nav_add, Icons.Default.Add)
}

private enum class AppScreens(val route: String) {
    QUIZ("quiz")
}