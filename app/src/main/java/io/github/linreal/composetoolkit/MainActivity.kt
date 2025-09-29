package io.github.linreal.composetoolkit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import io.github.linreal.composetoolkit.ui.theme.ComposeToolkitTheme
import io.github.linreal.logging.Logging
import io.github.linreal.retracker.TrackRecompositions

class MainActivity : ComponentActivity() {
    @Logging
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ComposeToolkitTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    RecompositionDemoScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
        // Test some functions
        initializeApp()
        performCalculation(5, 10)
    }
    
    @Logging
    private fun initializeApp() {
        // Simulate initialization work
        Thread.sleep(50)
    }
    
    @Logging
    private fun performCalculation(a: Int, b: Int): Int {
        return a + b
    }
}

@Composable
fun RecompositionDemoScreen(modifier: Modifier = Modifier) {
    var counter by remember { mutableStateOf(0) }
    var label by remember { mutableStateOf("World") }

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        TrackedGreeting(name = label)
        Spacer(Modifier.height(12.dp))
        TrackedCounter(value = counter)
        Spacer(Modifier.height(12.dp))
        Row {
            Button(onClick = { counter++ }) { Text("Increment") }
            Spacer(Modifier.height(0.dp).weight(1f, fill = true))
            Button(onClick = { label = if (label == "World") "Compose" else "World" }) { Text("Toggle Label") }
        }
    }
}

@TrackRecompositions
@Composable
fun TrackedGreeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@TrackRecompositions
@Composable
fun TrackedCounter(value: Int, modifier: Modifier = Modifier) {
    Text(text = "Count: $value", modifier = modifier)
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ComposeToolkitTheme {
        TrackedGreeting("Android")
    }
}

@Logging
fun helperFunction(): String {
    return "Helper result updated"
}
