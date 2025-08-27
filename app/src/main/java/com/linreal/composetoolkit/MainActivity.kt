package com.linreal.composetoolkit

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.linreal.composetoolkit.ui.theme.ComposeToolkitTheme
import com.linreal.logging.Logging

class MainActivity : ComponentActivity() {
    @Logging
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ComposeToolkitTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
        Log.d("MainActivity", "onCreate")
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
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ComposeToolkitTheme {
        Greeting("Android")
    }
}

@Logging
fun helperFunction(): String {
    return "Helper result updated"
}