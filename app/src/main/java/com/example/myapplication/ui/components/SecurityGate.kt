package com.example.myapplication.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import com.example.myapplication.R
import com.example.myapplication.util.SecurityManager

@Composable
fun SecurityGate(
    securityManager: SecurityManager,
    activity: FragmentActivity,
    onUnlocked: () -> Unit,
    onError: (String) -> Unit
) {
    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    val hasPin = remember { securityManager.isPinConfigured() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = stringResource(id = R.string.app_name), style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        if (hasPin) {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = pin,
                onValueChange = { pin = it },
                label = { Text(stringResource(id = R.string.label_pin)) },
                visualTransformation = PasswordVisualTransformation()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                if (securityManager.validatePin(pin)) {
                    onUnlocked()
                } else {
                    onError(stringResource(id = R.string.pin_invalid))
                }
            }) {
                Text(text = stringResource(id = R.string.label_unlock))
            }
            if (securityManager.canUseBiometrics()) {
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = {
                    securityManager.buildBiometricPrompt(activity) { success ->
                        if (success) onUnlocked() else onError(stringResource(id = R.string.pin_invalid))
                    }
                }) {
                    Text(text = stringResource(id = R.string.label_enable_biometrics))
                }
            }
        } else {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = pin,
                onValueChange = { pin = it },
                label = { Text(stringResource(id = R.string.label_pin)) },
                visualTransformation = PasswordVisualTransformation()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = confirmPin,
                onValueChange = { confirmPin = it },
                label = { Text(stringResource(id = R.string.label_confirm_pin)) },
                visualTransformation = PasswordVisualTransformation()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                if (pin.length < 4 || pin != confirmPin) {
                    onError(stringResource(id = R.string.pin_mismatch))
                } else {
                    securityManager.updatePin(pin)
                    onUnlocked()
                }
            }) {
                Text(text = stringResource(id = R.string.label_set_pin))
            }
        }
    }
}
