package com.example.modul4

import android.app.AlertDialog
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
import com.example.modul4.model.UserRequest
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

    var email by remember { mutableStateOf("eve.holt@reqres.in") }
    var password by remember { mutableStateOf("cityslicka") }
    var isLoading by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Beresin App",
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    text = "Masuk untuk melanjutkan",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(24.dp))

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

                Spacer(modifier = Modifier.height(24.dp))

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
                                Toast.makeText(context, "Kesalahan Jaringan", Toast.LENGTH_SHORT).show()
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    } else {
                        Text("Masuk")
                    }
                }
            }
        }
    }
}

@Composable
fun HomeScreen(onLogout: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val apiService = remember { ApiClient.getApiService(context) }

    var users by remember { mutableStateOf<List<User>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf("") }

    var showDialog by remember { mutableStateOf(false) }
    var selectedUser by remember { mutableStateOf<User?>(null) }

    fun loadData() {
        isLoading = true
        coroutineScope.launch {
            try {
                val response = apiService.getUsers()
                if (response.isSuccessful) {
                    users = response.body()?.data ?: emptyList()
                } else {
                    errorMessage = "Gagal mengambil data"
                }
            } catch (e: Exception) {
                errorMessage = "Kesalahan jaringan"
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) { loadData() }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {
                selectedUser = null
                showDialog = true
            }) {
                Text("+", style = MaterialTheme.typography.headlineSmall)
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("👥 Users List", style = MaterialTheme.typography.titleLarge)
                Button(onClick = onLogout) { Text("Logout") }
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (errorMessage.isNotEmpty()) {
                Text(errorMessage, modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.error)
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                    items(users) { user ->
                        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("${user.firstName} ${user.lastName}", style = MaterialTheme.typography.titleMedium)
                                    Text(user.email, style = MaterialTheme.typography.bodyMedium)
                                }
                                Row {
                                    TextButton(onClick = {
                                        selectedUser = user
                                        showDialog = true
                                    }) { Text("Ubah") }

                                    TextButton(onClick = {
                                        coroutineScope.launch {
                                            try {
                                                val response = apiService.deleteUser(user.id)
                                                if (response.isSuccessful) {
                                                    Toast.makeText(context, "Berhasil dihapus", Toast.LENGTH_SHORT).show()
                                                    loadData()
                                                }
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "Gagal menghapus", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }) { Text("Hapus", color = MaterialTheme.colorScheme.error) }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        UserFormDialog(
            initialName = selectedUser?.firstName ?: "",
            isEditMode = selectedUser != null,
            onDismiss = { showDialog = false },
            onConfirm = { inputName, inputJob ->
                showDialog = false
                coroutineScope.launch {
                    try {
                        val requestData = UserRequest(inputName, inputJob)
                        val response = if (selectedUser == null) {
                            apiService.createUser(requestData)
                        } else {
                            apiService.updateUser(selectedUser!!.id, requestData)
                        }

                        if (response.isSuccessful) {
                            Toast.makeText(context, "Data berhasil diproses", Toast.LENGTH_SHORT).show()
                            loadData()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "Kesalahan jaringan", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
    }
}
                @Composable
                fun UserFormDialog(
                    initialName: String = "",
                    initialJob: String = "",
                    isEditMode: Boolean = false,
                    onDismiss: () -> Unit,
                    onConfirm: (name: String, job: String) -> Unit
                ) {
                    var name by remember { mutableStateOf(initialName) }
                    var job by remember { mutableStateOf(initialJob) }

                    AlertDialog(
                        onDismissRequest = onDismiss,
                        title = { Text(if (isEditMode) "Ubah Pengguna" else "Tambah Pengguna Baru") },
                        text = {
                            Column {
                                OutlinedTextField(
                                    value = name,
                                    onValueChange = { name = it },
                                    label = { Text("Nama") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = job,
                                    onValueChange = { job = it },
                                    label = { Text("Pekerjaan") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = { onConfirm(name, job) },
                                enabled = name.isNotBlank() && job.isNotBlank()
                            ) {
                                Text("Simpan")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = onDismiss) {
                                Text("Batal")
                            }
                        }
                    )
                }






