package com.example.my_digital_lord

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vk.id.onetap.compose.onetap.sheet.OneTapBottomSheet
import com.vk.id.onetap.compose.onetap.sheet.rememberOneTapBottomSheetState
import com.vk.id.onetap.compose.onetap.sheet.style.OneTapBottomSheetStyle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.vk.id.auth.AuthCodeData

@Composable
fun AuthScreen(
    onLoginSuccess: () -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
    val isLoading by authViewModel.isLoading.collectAsState()
    val authError by authViewModel.authError.collectAsState()
    val sheetState = rememberOneTapBottomSheetState()
    val scope = rememberCoroutineScope()
    val authParams by authViewModel.authParams.collectAsState()

    if (isLoggedIn) {
        LaunchedEffect(true) {
            delay(100)
            onLoginSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.lord_splash),
                    contentDescription = "Господин",
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "ПРИВЕТ, РАБОТЯГА.",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = {
                    authViewModel.prepareAuth()
                    scope.launch { sheetState.show() }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(8.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Войти через VK ID", style = MaterialTheme.typography.labelLarge)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { authViewModel.guestLogin() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Продолжить как гость", style = MaterialTheme.typography.labelLarge)
            }

            if (authError != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "ОШИБКА: $authError",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
            }
        }
    }

    if (authParams != null) {
        OneTapBottomSheet(
            state = sheetState,
            serviceName = "My Digital Lord",
            style = OneTapBottomSheetStyle.Dark(),
            authParams = authParams!!,
            onAuth = { _, _ -> },
            onAuthCode = { authCodeData, _ ->
                authViewModel.exchangeCode(
                    code = authCodeData.code,
                    deviceId = authCodeData.deviceId
                )
            },
            onFail = { _, fail ->
                authViewModel.onAuthFail("VK ID: $fail")
            }
        )
    }
}