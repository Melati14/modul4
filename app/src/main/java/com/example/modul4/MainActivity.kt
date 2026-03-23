package com.example.modul4

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.*
import com.example.modul4.model.LoginRequest
import com.example.modul4.model.User
import com.example.modul4.service.ApiClient
import com.example.modul4.service.TokenManager
import kotlinx.coroutines.launch
// Pastikan untuk mengimpor ApiClient, ApiService, LoginRequest, dll dari paket Anda

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }

    // Tentukan layar awal berdasarkan ketersediaan token
    val startDestination = if (tokenManager.getToken() != null) "home" else "login"

    NavHost(navController = navController, startDestination = startDestination) {
        composable("login") {
            LoginScreen(onLoginSuccess = {
                navController.navigate("home") {
                    popUpTo("login") { inclusive = true } // Hapus layar login dari riwayat
                }
            })
        }
        composable("home") {
            HomeScreen(onLogout = {
                tokenManager.clearToken()
                navController.navigate("login") {
                    popUpTo("home") { inclusive = true }
                }
            })
        }
    }
}

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val apiService = remember { ApiClient.getApiService(context) }
    val tokenManager = remember { TokenManager(context) }

    var email by remember { mutableStateOf("eve.holt@reqres.in") } // Kredensial default API
    var password by remember { mutableStateOf("cityslicka") }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Aplikasi Beresin", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Kata Sandi") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                isLoading = true
                coroutineScope.launch {
                    try {
                        val response = apiService.loginUser(LoginRequest(email, password))
                        if (response.isSuccessful && response.body() != null) {
                            tokenManager.saveToken(response.body()!!.token)
                            onLoginSuccess()
                        } else {
                            Toast.makeText(context, "Login Gagal", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "Terjadi Kesalahan Jaringan",
                            Toast.LENGTH_SHORT).show()
                    } finally {
                        isLoading = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            Text(if (isLoading) "Memproses..." else "Masuk")
        }
    }
}

@Composable
fun HomeScreen(onLogout: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val apiService = remember { ApiClient.getApiService(context) }

    var users by remember { mutableStateOf<List<User>>(emptyList()) }
    var errorMessage by remember { mutableStateOf("") }

    // Memanggil API secara otomatis saat layar pertama kali dibuka
    LaunchedEffect(Unit) {
        try {
            val response = apiService.getUsers()
            if (response.isSuccessful && response.body() != null) {
                users = response.body()!!.data
            } else {
                errorMessage = "Gagal mengambil data"
            }
        } catch (e: Exception) {
            errorMessage = "Terjadi Kesalahan Jaringan"
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // App Bar Sederhana
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Daftar Pengguna", style = MaterialTheme.typography.titleLarge)
            Button(onClick = onLogout) { Text("Keluar") }
        }

        // List Data (Pengganti RecyclerView)
        if (errorMessage.isNotEmpty()) {
            Text(errorMessage, modifier = Modifier.padding(16.dp), color =
                MaterialTheme.colorScheme.error)
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                items(users) { user ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = "${user.firstName} ${user.lastName}", style =
                                MaterialTheme.typography.titleMedium)
                            Text(text = user.email, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}