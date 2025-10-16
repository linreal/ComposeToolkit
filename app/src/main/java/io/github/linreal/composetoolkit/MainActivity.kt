package io.github.linreal.composetoolkit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.linreal.composetoolkit.ui.theme.ComposeToolkitTheme
import io.github.linreal.retracker.SkipRecompositionTracking
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
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Recomposition Tracking Playground",
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = "Each card wires @TrackRecompositions into a different scenario so " +
                    "you can validate the IR transformer output quickly.",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        RecompositionCaseCard(
            title = "Stateless baseline",
            description = "Emits static text. Useful to confirm that the tracker stays quiet " +
                "until its inputs actually change."
        ) {
            StatelessSample()
        }

        RecompositionCaseCard(
            title = "Local state mutations",
            description = "Tracks a button-driven counter that keeps state inside the " +
                "composable to highlight immediate recomposition feedback."
        ) {
            LocalStateCounterSample()
        }

        RecompositionCaseCard(
            title = "Hoisted state with nested tracking",
            description = "Parent opts into includeNested to ensure both the harness and its " +
                "child composable are instrumented by the plugin."
        ) {
            HoistedStateHarness()
        }

        RecompositionCaseCard(
            title = "Derived state filtering",
            description = "Combines derivedStateOf with text input so you can inspect how " +
                "snapshot reads drive recompositions."
        ) {
            DerivedStateSample()
        }
    }
}

@Composable
private fun RecompositionCaseCard(
    title: String,
    description: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall
            )
            content()
        }
    }
}

@TrackRecompositions
@Composable
private fun StatelessSample() {
    Text(
        text = "This block holds no Compose state. If you see recompositions in logs, the " +
            "parent changed its parameters.",
        style = MaterialTheme.typography.bodyMedium
    )
}

@TrackRecompositions
@Composable
private fun LocalStateCounterSample() {
    var taps by remember { mutableIntStateOf(0) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Local counter: $taps",
            style = MaterialTheme.typography.bodyMedium
        )
        Button(onClick = { taps++ }) {
            Text(text = "Increment local state")
        }
    }
}

@TrackRecompositions(includeNested = true)
@Composable
private fun HoistedStateHarness() {
    var count by remember { mutableIntStateOf(0) }

    HoistedCounter(
        value = count,
        onIncrement = { count++ },
        onReset = { count = 0 }
    )
}

@Composable
private fun HoistedCounter(
    value: Int,
    onIncrement: () -> Unit,
    onReset: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Child receives hoisted state: $value",
            style = MaterialTheme.typography.bodyMedium
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onIncrement) {
                Text("Increment")
            }
            OutlinedButton(onClick = onReset) {
                Text("Reset")
            }
        }
    }
}

@TrackRecompositions
@Composable
private fun DerivedStateSample() {
    var query by remember { mutableStateOf("") }
    val catalog = remember {
        listOf(
            "Baseline Profiles",
            "Compose Recomposer",
            "Snapshot State",
            "Skia Renderer",
            "Text Layout Inspector"
        )
    }
    val matches by remember {
        derivedStateOf {
            val trimmedQuery = query.trim()
            if (trimmedQuery.isEmpty()) {
                catalog
            } else {
                catalog.filter { item ->
                    item.contains(trimmedQuery, ignoreCase = true)
                }
            }
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text("Filter catalog entries") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = if (matches.isEmpty()) {
                "No matches for \"$query\""
            } else {
                "Matches: ${matches.joinToString(separator = ", ")}"
            },
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
