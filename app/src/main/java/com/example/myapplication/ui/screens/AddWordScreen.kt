package com.example.myapplication.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.myapplication.R

@Composable
fun AddWordScreen(
    onAddWord: (english: String, french: String, theme: String, example: String?, exampleFrench: String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var english by remember { mutableStateOf("") }
    var french by remember { mutableStateOf("") }
    var theme by remember { mutableStateOf("") }
    var example by remember { mutableStateOf("") }
    var exampleFrench by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(id = R.string.add_word_title),
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = english,
            onValueChange = { english = it },
            label = { Text(stringResource(id = R.string.add_word_english_label)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = french,
            onValueChange = { french = it },
            label = { Text(stringResource(id = R.string.add_word_french_label)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = theme,
            onValueChange = { theme = it },
            label = { Text(stringResource(id = R.string.add_word_theme_label)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = example,
            onValueChange = { example = it },
            label = { Text(stringResource(id = R.string.add_word_example_label)) },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = exampleFrench,
            onValueChange = { exampleFrench = it },
            label = { Text(stringResource(id = R.string.add_word_example_fr_label)) },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                onAddWord(
                    english.trim(),
                    french.trim(),
                    theme.trim(),
                    example.trim().takeIf { it.isNotEmpty() },
                    exampleFrench.trim().takeIf { it.isNotEmpty() }
                )
                english = ""
                french = ""
                theme = ""
                example = ""
                exampleFrench = ""
            },
            enabled = english.isNotBlank() && french.isNotBlank() && theme.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(id = R.string.add_word_submit))
        }
    }
}
