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
import io.github.linreal.retracker.RecompositionTrackingSettings
import io.github.linreal.retracker.SkipRecompositionTracking
import io.github.linreal.retracker.TrackRecompositions

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        RecompositionTrackingSettings.isEnabled = true
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

        RecompositionCaseCard(
            title = "Skip parameter tracking - Modifier",
            description = "Tests @SkipRecompositionTracking on Modifier parameter. Changing " +
                "padding should not log recomposition, but changing text will."
        ) {
            SkipModifierSample()
        }

        RecompositionCaseCard(
            title = "Skip parameter tracking - Animation value",
            description = "Tests @SkipRecompositionTracking on frequently-changing animation " +
                "parameter. Only text changes should be logged."
        ) {
            SkipAnimationSample()
        }

        RecompositionCaseCard(
            title = "Mixed tracking parameters",
            description = "Tests multiple parameters with some tracked and some skipped. " +
                "Observe which changes trigger logging."
        ) {
            MixedParametersSample()
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

// ============================================================================
// Parameter-level @SkipRecompositionTracking tests
// ============================================================================

@Composable
private fun SkipModifierSample() {
    var text by remember { mutableStateOf("Hello") }
    var paddingDp by remember { mutableIntStateOf(8) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SkipModifierContent(
            text = text,
            modifier = Modifier.padding(paddingDp.dp)
        )

        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("Text (tracked)") },
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Padding: ${paddingDp}dp",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f).padding(vertical = 8.dp)
            )
            Button(onClick = { paddingDp += 4 }) {
                Text("+")
            }
            Button(onClick = { paddingDp = (paddingDp - 4).coerceAtLeast(0) }) {
                Text("-")
            }
        }
    }
}

@TrackRecompositions
@Composable
private fun SkipModifierContent(
    text: String,
    @SkipRecompositionTracking modifier: Modifier = Modifier
) {
    Text(
        text = "Content: $text",
        style = MaterialTheme.typography.bodyMedium,
        modifier = modifier
    )
}

@Composable
private fun SkipAnimationSample() {
    var counter by remember { mutableIntStateOf(0) }
    var animProgress by remember { mutableIntStateOf(0) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SkipAnimationContent(
            counter = counter,
            animationProgress = animProgress / 100f
        )

        Button(onClick = { counter++ }) {
            Text("Increment counter (tracked)")
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Animation: ${animProgress}%",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f).padding(vertical = 8.dp)
            )
            Button(onClick = { animProgress = (animProgress + 10).coerceAtMost(100) }) {
                Text("+10%")
            }
            Button(onClick = { animProgress = (animProgress - 10).coerceAtLeast(0) }) {
                Text("-10%")
            }
        }
    }
}

@TrackRecompositions
@Composable
private fun SkipAnimationContent(
    counter: Int,
    @SkipRecompositionTracking animationProgress: Float
) {
    Text(
        text = "Counter: $counter | Animation: ${(animationProgress * 100).toInt()}%",
        style = MaterialTheme.typography.bodyMedium
    )
}

@Composable
private fun MixedParametersSample() {
    var importantValue by remember { mutableStateOf("Important") }
    var unimportantValue by remember { mutableStateOf("Unimportant") }
    var skippedNumber by remember { mutableIntStateOf(0) }
    var trackedNumber by remember { mutableIntStateOf(0) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        MixedParametersContent(
            important = importantValue,
            unimportant = unimportantValue,
            skippedCount = skippedNumber,
            trackedCount = trackedNumber
        )

        Text(
            text = "Tracked parameters:",
            style = MaterialTheme.typography.labelMedium
        )

        OutlinedTextField(
            value = importantValue,
            onValueChange = { importantValue = it },
            label = { Text("Important (tracked)") },
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Tracked: $trackedNumber",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f).padding(vertical = 8.dp)
            )
            Button(onClick = { trackedNumber++ }) {
                Text("+")
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Skipped parameters (won't trigger logging):",
            style = MaterialTheme.typography.labelMedium
        )

        OutlinedTextField(
            value = unimportantValue,
            onValueChange = { unimportantValue = it },
            label = { Text("Unimportant (skipped)") },
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Skipped: $skippedNumber",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f).padding(vertical = 8.dp)
            )
            Button(onClick = { skippedNumber++ }) {
                Text("+")
            }
        }
    }
}

@TrackRecompositions
@Composable
private fun MixedParametersContent(
    important: String,
    @SkipRecompositionTracking unimportant: String,
    @SkipRecompositionTracking skippedCount: Int,
    trackedCount: Int
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = "Important: $important (tracked)",
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = "Unimportant: $unimportant (skipped)",
            style = MaterialTheme.typography.bodySmall
        )
        Text(
            text = "Tracked count: $trackedCount | Skipped count: $skippedCount",
            style = MaterialTheme.typography.bodySmall
        )
    }
}
