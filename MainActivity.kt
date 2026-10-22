package com.example.ca2cse225

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class Meter(val reading: Int, val date: String)

// Global list of meter readings
val sampleList = listOf(
    Meter(3, "21-06-2026"),
    Meter(6, "22-06-2026"),
    Meter(8, "23-06-2026"),
    Meter(4, "24-06-2026"),
    Meter(10, "25-06-2026"),
    Meter(12, "26-06-2026")
)

suspend fun calculateReadings(): String {
    delay(1000)
    return "Calculating the readings..."
}

fun getReadings(
    scope: CoroutineScope,
    onLoadingChange: (Boolean) -> Unit,
    onNewReading: (Meter) -> Unit
) {
    scope.launch {
        onLoadingChange(true)

        withContext(Dispatchers.IO) {
            calculateReadings()
        }

        // Flow created directly inside getReadings using the global sampleList with Dispatchers.IO
        flow {
            for (item in sampleList) {
                delay(1000)
                emit(item)
            }
        }.flowOn(Dispatchers.IO).collect { reading ->
            onNewReading(reading)
        }

        onLoadingChange(false)
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MeterScreen()
                }
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
fun MeterScreen() {
    var isLoading by remember { mutableStateOf(false) }
    var readings by remember { mutableStateOf<List<Meter>>(emptyList()) }

    // Separate static variable for today's current reading
    val currentReading = Meter(12, "26-06-2026")
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Smart Electricity Meter Consumption Reading",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                readings = emptyList()
                getReadings(
                    scope = scope,
                    onLoadingChange = { isLoading = it },
                    onNewReading = { newMeter ->
                        readings = readings + newMeter
                    }
                )
            },
            enabled = !isLoading
        ) {
            if (isLoading) {
                Text("Calculating the readings...")
            } else {
                Text("Get Readings")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Fixed Current Reading of today
        Text(
            text = "Current Reading: ${currentReading.reading * 1000} W recorded on (${currentReading.date})",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Highest Readings (> 5 kW):",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Displays items streamed every second
        readings.filter { it.reading > 5 }.forEach { meter ->
            Text(
                text = "Date: ${meter.date} -> ${meter.reading * 1000} W",
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp)
            )
        }
    }
}