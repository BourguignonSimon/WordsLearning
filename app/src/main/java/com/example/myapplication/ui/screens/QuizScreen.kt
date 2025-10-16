package com.example.myapplication.ui.screens

import android.speech.tts.TextToSpeech
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.icons.Icons
import androidx.compose.material3.icons.filled.CheckCircle
import androidx.compose.material3.icons.filled.VolumeUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.R
import com.example.myapplication.model.QuizDirection
import com.example.myapplication.model.QuizOption
import com.example.myapplication.ui.WordsViewModel
import java.util.Locale

@Composable
fun QuizScreen(
    state: WordsViewModel.QuizUiState,
    onAnswerSelected: (Int) -> Unit,
    onNextQuestion: () -> Unit,
    onExit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isTtsReady by remember { mutableStateOf(false) }
    val textToSpeech = remember {
        TextToSpeech(context) { status ->
            isTtsReady = status == TextToSpeech.SUCCESS
        }
    }
    DisposableEffect(textToSpeech) {
        onDispose { textToSpeech.shutdown() }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(id = R.string.quiz_header, state.questionsAnswered),
            style = MaterialTheme.typography.titleMedium
        )

        when {
            state.isLoading -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                }
            }

            state.currentQuestion == null -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = stringResource(id = R.string.quiz_no_words))
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onExit) {
                        Text(text = stringResource(id = R.string.quiz_back_to_dashboard))
                    }
                }
            }

            else -> {
                QuizQuestionCard(
                    prompt = state.currentQuestion.prompt,
                    direction = state.currentQuestion.direction,
                    options = state.currentQuestion.options,
                    lastAnswerCorrect = state.lastAnswerCorrect,
                    onAnswerSelected = onAnswerSelected,
                    onNextQuestion = onNextQuestion,
                    onSpeakRequested = {
                        val wordToSpeak = when (state.currentQuestion.direction) {
                            QuizDirection.EN_TO_FR -> state.currentQuestion.word.english
                            QuizDirection.FR_TO_EN -> state.currentQuestion.word.french
                        }
                        if (isTtsReady) {
                            textToSpeech.language = when (state.currentQuestion.direction) {
                                QuizDirection.EN_TO_FR -> Locale.US
                                QuizDirection.FR_TO_EN -> Locale.FRANCE
                            }
                            textToSpeech.speak(wordToSpeak, TextToSpeech.QUEUE_FLUSH, null, "quiz_word")
                        }
                    }
                )
                OutlinedButton(onClick = onExit, modifier = Modifier.fillMaxWidth()) {
                    Text(text = stringResource(id = R.string.quiz_exit))
                }
            }
        }
    }
}

@Composable
private fun QuizQuestionCard(
    prompt: String,
    direction: QuizDirection,
    options: List<QuizOption>,
    lastAnswerCorrect: Boolean?,
    onAnswerSelected: (Int) -> Unit,
    onNextQuestion: () -> Unit,
    onSpeakRequested: () -> Unit
) {
    var hasAnswered by remember { mutableStateOf(lastAnswerCorrect != null) }
    if (lastAnswerCorrect == null && hasAnswered) {
        hasAnswered = false
    }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            RowHeader(prompt = prompt, onSpeakRequested = onSpeakRequested)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = when (direction) {
                    QuizDirection.EN_TO_FR -> stringResource(id = R.string.quiz_direction_en_fr)
                    QuizDirection.FR_TO_EN -> stringResource(id = R.string.quiz_direction_fr_en)
                },
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.height(16.dp))
            options.forEach { option ->
                QuizOptionButton(
                    option = option,
                    enabled = !hasAnswered,
                    showSolution = hasAnswered && option.isCorrect,
                    onClick = {
                        hasAnswered = true
                        onAnswerSelected(option.id)
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            if (hasAnswered) {
                FeedbackRow(lastAnswerCorrect = lastAnswerCorrect)
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = {
                    hasAnswered = false
                    onNextQuestion()
                }, modifier = Modifier.fillMaxWidth()) {
                    Text(text = stringResource(id = R.string.quiz_next_question))
                }
            }
        }
    }
}

@Composable
private fun RowHeader(
    prompt: String,
    onSpeakRequested: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = prompt,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = onSpeakRequested) {
            Icon(Icons.Default.VolumeUp, contentDescription = stringResource(id = R.string.quiz_speak_word))
        }
    }
}

@Composable
private fun QuizOptionButton(
    option: QuizOption,
    enabled: Boolean,
    showSolution: Boolean,
    onClick: () -> Unit
) {
    val buttonColors = if (showSolution) {
        ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    } else {
        ButtonDefaults.buttonColors()
    }
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth(),
        colors = buttonColors
    ) {
        Text(text = option.text)
    }
    if (showSolution) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.CheckCircle, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = stringResource(id = R.string.quiz_correct_answer))
        }
    }
}

@Composable
private fun FeedbackRow(lastAnswerCorrect: Boolean?) {
    if (lastAnswerCorrect == null) return
    val (text, color) = if (lastAnswerCorrect) {
        stringResource(id = R.string.quiz_feedback_correct) to MaterialTheme.colorScheme.primary
    } else {
        stringResource(id = R.string.quiz_feedback_incorrect) to MaterialTheme.colorScheme.error
    }
    Text(text = text, color = color)
}
