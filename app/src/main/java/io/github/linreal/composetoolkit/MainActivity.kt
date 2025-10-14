package io.github.linreal.composetoolkit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.linreal.composetoolkit.ui.theme.ComposeToolkitTheme
import io.github.linreal.retracker.TrackRecompositions

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ComposeToolkitTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ToolkitDemoHost(
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize()
                    )
                }
            }
        }

    }
}


@Composable
private fun ToolkitDemoHost(modifier: Modifier = Modifier) {
    var counter by remember { mutableIntStateOf(0) }
    var text by remember { mutableStateOf("") }
    Column {
        Spacer(modifier = Modifier.height(80.dp))
        IncrementedBlock(counter = counter){
            counter++
        }
    }
}

@Composable
@TrackRecompositions
private fun Counter(value: Int, onIncrement: () -> Unit) {
    Column {
        Text(text = "Counter: $value")
        Button(onClick = onIncrement) {
            Text(text = "Increment")
        }
    }
}

@TrackRecompositions(includeNested = true)
@Composable
private fun TextFieldCounter(value: String, onValueChange: (String) -> Unit) {
    var showButton = value.isNotEmpty()
    var counter = remember { mutableIntStateOf(0) }
    Column {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text("Enter text") }
        )
        Text(text = "Entered text: $value")

        if (showButton) {
            Counter(counter.value) {
                counter.intValue++
            }
        }

    }
}


@Composable
private fun IncrementedBlock(counter: Int, onIncrement: () -> Unit, ) {
    var text by remember { mutableStateOf("") }

    Column {
        Counter(counter, onIncrement)
        TextFieldCounter(text) { text = it }
    }
}
