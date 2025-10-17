package com.example.myapplication

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.myapplication.audio.RecordService
import com.example.myapplication.ui.MainViewModel
import com.example.myapplication.ui.components.SecurityGate
import com.example.myapplication.ui.screens.SessionDetailScreen
import com.example.myapplication.ui.screens.SessionListScreen
import com.example.myapplication.ui.theme.OfflineTheme
import com.example.myapplication.util.ModelImportManager
import kotlinx.coroutines.launch

class MainActivity : FragmentActivity() {

    private val viewModel: MainViewModel by viewModels()

    private val voskLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { viewModel.importModel(it, ModelImportManager.ModelType.VOSK) }
    }
    private val whisperLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { viewModel.importModel(it, ModelImportManager.ModelType.WHISPER) }
    }

    private val errorReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == RecordService.ACTION_RECORDING_ERROR) {
                val message = intent.getStringExtra(RecordService.EXTRA_ERROR_MESSAGE)
                Toast.makeText(this@MainActivity, message ?: "Erreur", Toast.LENGTH_LONG).show()
                viewModel.markRecording(false)
            }
        }
    }

    private val container by lazy { (application as WordsLearningApp).container }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val filter = IntentFilter(RecordService.ACTION_RECORDING_ERROR)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(errorReceiver, filter, RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("DEPRECATION")
            registerReceiver(errorReceiver, filter)
        }

        setContent {
            OfflineTheme {
                val navController = rememberNavController()
                val sessions by viewModel.sessions.collectAsState()
                val filters by viewModel.currentFilters.collectAsState()
                val searchQuery by viewModel.currentSearch.collectAsState()
                val isRecording by viewModel.isRecording.collectAsState()
                val exportResult by viewModel.lastExportResult.collectAsState()
                val infoMessage by viewModel.infoMessage.collectAsState()
                val errorMessage by viewModel.errorMessage.collectAsState()
                val snackbarHostState = remember { SnackbarHostState() }
                val scope = rememberCoroutineScope()
                val activity = this@MainActivity

                var unlocked by rememberSaveable { mutableStateOf(!viewModel.securityManager.isPinConfigured()) }

                LaunchedEffect(exportResult) {
                    exportResult?.let {
                        snackbarHostState.showSnackbar(getString(R.string.export_success, it.absolutePath))
                        viewModel.clearExportResult()
                    }
                }
                LaunchedEffect(infoMessage) {
                    infoMessage?.let {
                        snackbarHostState.showSnackbar(it)
                        viewModel.clearInfo()
                    }
                }
                LaunchedEffect(errorMessage) {
                    errorMessage?.let {
                        snackbarHostState.showSnackbar(it)
                        viewModel.clearError()
                    }
                }

                if (!unlocked) {
                    SecurityGate(
                        securityManager = viewModel.securityManager,
                        activity = activity,
                        onUnlocked = { unlocked = true },
                        onError = { message -> scope.launch { snackbarHostState.showSnackbar(message) } }
                    )
                } else {
                    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { paddingValues ->
                        NavHost(
                            navController = navController,
                            startDestination = "sessions",
                            modifier = Modifier.padding(paddingValues)
                        ) {
                            composable("sessions") {
                                SessionListScreen(
                                    sessions = sessions,
                                    filters = filters,
                                    searchQuery = searchQuery,
                                    isRecording = isRecording,
                                    onSearchChange = viewModel::updateSearch,
                                    onFiltersChange = viewModel::updateFilters,
                                    onStartRecording = {
                                        viewModel.markRecording(true)
                                        RecordService.start(activity, "Session")
                                    },
                                    onStopRecording = {
                                        viewModel.markRecording(false)
                                        RecordService.stop(activity)
                                    },
                                    onImportVosk = { voskLauncher.launch(arrayOf("*/*")) },
                                    onImportWhisper = { whisperLauncher.launch(arrayOf("*/*")) },
                                    onSessionClick = { navController.navigate("detail/$it") },
                                    onRequestTranscription = viewModel::enqueueTranscription
                                )
                            }
                            composable(
                                route = "detail/{sessionId}",
                                arguments = listOf(navArgument("sessionId") { type = NavType.LongType })
                            ) { backStackEntry ->
                                val id = backStackEntry.arguments?.getLong("sessionId") ?: return@composable
                                SessionDetailScreen(
                                    sessionId = id,
                                    viewModel = viewModel,
                                    encryptionManager = container.encryptionManager,
                                    onExportMarkdown = { viewModel.exportSession(id, true) },
                                    onExportJson = { viewModel.exportSession(id, false) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        unregisterReceiver(errorReceiver)
        super.onDestroy()
    }
}
