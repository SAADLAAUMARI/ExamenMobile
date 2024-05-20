package com.saadlaaumari.laaumari_saad_glsid_2
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.lifecycleScope
import com.saadlaaumari.laaumari_saad_glsid_2.ui.theme.LaaumariSaadGLSID2Theme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

// Extension property to create DataStore
private val Context.dataStore by preferencesDataStore(name = "user_prefs")
class MainActivity : ComponentActivity() {

    private lateinit var dataStore: DataStore<Preferences>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize dataStore
        dataStore = applicationContext.dataStore

        // Save credentials
        lifecycleScope.launch {
            saveCredentials("username", "password")
        }

        setContent {
            LaaumariSaadGLSID2Theme {
                var isAuthenticated by remember { mutableStateOf(false) }
                var showAuthenticationScreen by remember { mutableStateOf(true) }
                Surface(color = MaterialTheme.colorScheme.background) {
                    if (isAuthenticated && !showAuthenticationScreen) {
                        GameScreen(onGameFinish = {
                            // Callback function to reset state and show authentication screen
                            showAuthenticationScreen = true // Réinitialiser showAuthenticationScreen à true ici
                        })
                    } else {
                        AuthenticationScreen(onAuthenticate = { username, password ->
                            lifecycleScope.launch {
                                if (verifyCredentials(username, password)) {
                                    isAuthenticated = true
                                    showAuthenticationScreen = false // Mettre à jour showAuthenticationScreen à false lorsqu'on est authentifié
                                } else {
                                    // Afficher un message d'erreur si les informations d'identification sont incorrectes
                                    Toast.makeText(
                                        applicationContext,
                                        "Nom d'utilisateur ou mot de passe incorrect",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        })

                    }
                }
            }
        }
    }

    // Le reste du code reste le même

    private suspend fun saveCredentials(username: String, password: String) {
        dataStore.edit { prefs ->
            prefs[stringPreferencesKey("username")] = username
            prefs[stringPreferencesKey("password")] = password
        }
    }

    private suspend fun verifyCredentials(username: String, password: String): Boolean {
        val storedUsername = dataStore.data.first()[stringPreferencesKey("username")]
        val storedPassword = dataStore.data.first()[stringPreferencesKey("password")]
        return username == storedUsername && password == storedPassword
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthenticationScreen(
    onAuthenticate: (String, String) -> Unit,

) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Nom d'utilisateur") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Mot de passe") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                // Enregistrer les informations d'identification avant d'appeler onAuthenticate

                onAuthenticate(username, password)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Blue,
                contentColor = Color.White
            )
        ) {
            Text("Login")
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(onGameFinish: () -> Unit) {
    var playerFortuneText by remember { mutableStateOf("") }
    var playerFortune by remember(playerFortuneText) { mutableStateOf(playerFortuneText.toIntOrNull() ?: 0) }
    var casinoFortune by remember { mutableStateOf((0..100).random()) }
    var gameMessage by remember { mutableStateOf("Specify your fortune and start the game!") }
    var gameFinished by remember { mutableStateOf(false) }
    var fortuneConfirmed by remember { mutableStateOf(false) }
    var diceRollResult by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Player's Fortune: $playerFortune")
        Text("Casino's Fortune: $casinoFortune")

        Spacer(modifier = Modifier.height(16.dp))

        if (!gameFinished) {
            OutlinedTextField(
                value = playerFortuneText,
                onValueChange = {
                    playerFortuneText = it
                    playerFortune = it.toIntOrNull() ?: 0
                },
                label = { Text("Enter your fortune") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !fortuneConfirmed
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    if (playerFortune <= 0 || playerFortune > 100) {
                        gameMessage = "Please enter a valid fortune between 1 and 100!"
                    } else {
                        gameMessage = "Fortune set to $playerFortune"
                        fortuneConfirmed = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Blue,
                    contentColor = Color.White
                ),
                enabled = !fortuneConfirmed
            ) {
                Text("Confirm Fortune")
            }
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val roll = (1..6).random()
                    diceRollResult = "Dice roll result: $roll"
                    if (roll in 2..3) {
                        playerFortune += 1
                        casinoFortune -= 1
                        gameMessage = "Player wins this round! $diceRollResult"
                    } else {
                        playerFortune -= 1
                        casinoFortune += 1
                        gameMessage = "Casino wins this round! $diceRollResult"
                    }
                    if (playerFortune <= 0 || casinoFortune <= 0) {
                        gameMessage = if (playerFortune <= 0) "Casino wins the game!" else "Player wins the game!"
                        gameFinished = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Blue,
                    contentColor = Color.White
                ),
            ) {
                Text("Lancer le dé")
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (gameFinished) {
            Text(
                text = gameMessage,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Button(
                onClick = {
                    // Réinitialiser les valeurs et revenir à la page d'authentification
                    playerFortuneText = ""
                    casinoFortune = 0
                    gameMessage = "Specify your fortune and start the game!"
                    gameFinished = false
                    fortuneConfirmed = false
                    diceRollResult = ""
                    onGameFinish()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Blue,
                    contentColor = Color.White
                ),
            ) {
                Text("Terminer le jeu")
            }
        }
    }
}
