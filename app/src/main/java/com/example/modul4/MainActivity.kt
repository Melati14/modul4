package com.example.modul4

import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.example.modul4.ui.theme.Modul4Theme
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.modul4.service.ApiClient
import kotlinx.coroutines.launch
import androidx.compose.material3.Button
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Modul4Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    CatFactScreen(modifier = Modifier.padding(innerPadding))

                }
            }
        }
    }
}

@Composable
fun CatFactScreen(modifier: Modifier = Modifier) {
    var factText by remember {
        mutableStateOf("Tekan tombol dibawah untuk memuat data")
    }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {


        Text(
            text = factText,
            fontSize = 18.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 32.dp),
        )

        Button(onClick = {
            factText = "Memuat data ..."

            coroutineScope.launch {
                try {
                    val response = ApiClient.apiService.getRandomFact()

                    if (response.isSuccessful && response.body() != null) {
                       val fact = response.body()!!.fact
                        factText = fact

                    } else {
                        factText = "Gagal memuat data."
                    }
                } catch (e: Exception) {
                    factText = "Terjadi kesalahan."
                }
            }
        }) {
            Text("Muat fakta baru")
        }
    }
}



