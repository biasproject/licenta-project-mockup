package com.example.flowpeak.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.flowpeak.state.AppState
import com.example.flowpeak.ui.theme.FlowPeakTheme
import com.google.firebase.auth.FirebaseAuth

fun getFriendlyErrorMessage(rawMessage: String): String {
    val lower = rawMessage.lowercase()
    return when {
        lower.contains("badly formatted") || lower.contains("invalid email") || lower.contains("invalid-email") -> "Adresa de email nu este validă."
        lower.contains("user-not-found") || lower.contains("no user record") || lower.contains("user_not_found") -> "Contul nu există."
        lower.contains("wrong-password") || lower.contains("invalid credentials") || lower.contains("invalid-credential") -> "Email-ul sau parola sunt incorecte."
        lower.contains("email-already-in-use") || lower.contains("email already exists") || lower.contains("email_already_in_use") -> "Această adresă de email este deja înregistrată."
        lower.contains("weak-password") || lower.contains("password should be at least 6 characters") -> "Parola trebuie să conțină cel puțin 6 caractere."
        lower.contains("network") || lower.contains("timeout") -> "Eroare de rețea. Verifică conexiunea la internet."
        else -> rawMessage
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(appState: AppState) {
    var isLoginMode by remember { mutableStateOf(true) }
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FlowPeakTheme.colors.background)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(FlowPeakTheme.colors.surface1)
                .border(0.5.dp, FlowPeakTheme.colors.border, RoundedCornerShape(20.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(FlowPeakTheme.colors.primary),
                contentAlignment = Alignment.Center
            ) {
                Text("F", color = FlowPeakTheme.colors.surface1, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "FlowPeak",
                style = MaterialTheme.typography.headlineMedium,
                color = FlowPeakTheme.colors.textPrimary,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = if (isLoginMode) "Conectează-te la contul tău" else "Creează un cont nou",
                style = MaterialTheme.typography.bodyMedium,
                color = FlowPeakTheme.colors.textMuted
            )
            
            Spacer(modifier = Modifier.height(24.dp))

            // Email input
            OutlinedTextField(
                value = emailInput,
                onValueChange = { 
                    emailInput = it
                    errorMessage = ""
                },
                enabled = !isLoading,
                label = { Text("Email") },
                leadingIcon = { Icon(Icons.Outlined.Email, contentDescription = null, tint = FlowPeakTheme.colors.textMuted) },
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = FlowPeakTheme.colors.surface2,
                    unfocusedContainerColor = FlowPeakTheme.colors.surface2,
                    focusedTextColor = FlowPeakTheme.colors.textPrimary,
                    unfocusedTextColor = FlowPeakTheme.colors.textPrimary,
                    focusedIndicatorColor = FlowPeakTheme.colors.primary,
                    unfocusedIndicatorColor = FlowPeakTheme.colors.border
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Password input
            OutlinedTextField(
                value = passwordInput,
                onValueChange = { 
                    passwordInput = it
                    errorMessage = ""
                },
                enabled = !isLoading,
                label = { Text("Parolă") },
                leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null, tint = FlowPeakTheme.colors.textMuted) },
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(image, contentDescription = null, tint = FlowPeakTheme.colors.textMuted)
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = FlowPeakTheme.colors.surface2,
                    unfocusedContainerColor = FlowPeakTheme.colors.surface2,
                    focusedTextColor = FlowPeakTheme.colors.textPrimary,
                    unfocusedTextColor = FlowPeakTheme.colors.textPrimary,
                    focusedIndicatorColor = FlowPeakTheme.colors.primary,
                    unfocusedIndicatorColor = FlowPeakTheme.colors.border
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            // Error display
            if (errorMessage.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = errorMessage,
                    color = FlowPeakTheme.colors.accentPink,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Button
            Button(
                onClick = {
                    val email = emailInput.trim()
                    val password = passwordInput
                    if (email.isBlank() || password.isBlank()) {
                        errorMessage = "Toate câmpurile trebuie completate!"
                    } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        errorMessage = "Adresă de email invalidă!"
                    } else {
                        isLoading = true
                        errorMessage = ""
                        val auth = FirebaseAuth.getInstance()
                        if (isLoginMode) {
                            auth.signInWithEmailAndPassword(email, password)
                                .addOnCompleteListener { task ->
                                    isLoading = false
                                    if (task.isSuccessful) {
                                        appState.loginUser(email)
                                    } else {
                                        val ex = task.exception
                                        errorMessage = getFriendlyErrorMessage(ex?.message ?: "Eroare de autentificare")
                                    }
                                }
                        } else {
                            auth.createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener { task ->
                                    isLoading = false
                                    if (task.isSuccessful) {
                                        appState.loginUser(email)
                                    } else {
                                        val ex = task.exception
                                        errorMessage = getFriendlyErrorMessage(ex?.message ?: "Eroare la crearea contului")
                                    }
                                }
                        }
                    }
                },
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = FlowPeakTheme.colors.primary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = if (FlowPeakTheme.colors.isDark) FlowPeakTheme.colors.background else FlowPeakTheme.colors.surface1,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = if (isLoginMode) "Autentificare" else "Înregistrare",
                        color = if (FlowPeakTheme.colors.isDark) FlowPeakTheme.colors.background else FlowPeakTheme.colors.surface1,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Mode switcher
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isLoginMode) "Nu ai un cont?" else "Ai deja un cont?",
                    color = FlowPeakTheme.colors.textMuted,
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isLoginMode) "Creează cont" else "Autentifică-te",
                    color = FlowPeakTheme.colors.primary,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable(enabled = !isLoading) {
                            isLoginMode = !isLoginMode
                            errorMessage = ""
                        }
                )
            }
        }
    }
}
