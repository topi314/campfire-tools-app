package wtf.topi.campfire.tools

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import wtf.topi.campfire.tools.ui.theme.CampfireToolsAppTheme

class ShareImportActivity : ComponentActivity() {

    private val viewModel: ShareImportViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        handleIntent(intent)

        setContent {
            CampfireToolsAppTheme {
                val isLoading by viewModel.isLoading.collectAsState()
                val uiMessage by viewModel.uiMessage.collectAsState()
                val initialSharedLink =
                    remember { ProvideInitialSharedText() } // Capture initial link

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(
                        isLoading = isLoading,
                        message = uiMessage,
                        onOkPressed = {
                            viewModel.clearUiMessage() // Clear message in ViewModel
                            finishAndRemoveTask()            // Close the activity
                        },
                        onRetryPressed = {
                            viewModel.clearUiMessage()
                            initialSharedLink?.let { viewModel.importSharedLink(it) }
                        },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent) // Important: update the activity's intent
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.action != Intent.ACTION_SEND || intent.type != "text/plain") {
            Log.d("MainActivity", "No shared link and no current operation/message. Finishing.")
            Toast.makeText(this, "Please share a link to use this app.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        val link = intent.getStringExtra(Intent.EXTRA_TEXT)?.trim()
        if (link.isNullOrEmpty()) {
            Log.e("MainActivity", "Received empty or null link")
            Toast.makeText(this, "No link provided", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        Log.d("MainActivity", "Processing link: $link")
        viewModel.importSharedLink(link)
    }


    private fun ProvideInitialSharedText(): String? {
        // This is a bit of a workaround to get the initial shared text for display
        // before the ViewModel has a chance to fully process or if the ViewModel
        // doesn't store the raw link.
        // It's primarily for the UI to show *something* while waiting.
        return if (intent?.action == Intent.ACTION_SEND && intent.type == "text/plain") {
            intent.getStringExtra(Intent.EXTRA_TEXT)
        } else {
            null
        }
    }

}


@Composable
fun MainScreen(
    isLoading: Boolean,
    message: String?,
    onOkPressed: () -> Unit,
    onRetryPressed: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isFinishing by remember { mutableStateOf(false) }

    // Wrap onOkPressed to set isFinishing before calling the real handler
    val handleOkPressed = {
        isFinishing = true
        onOkPressed()
    }

    if (isFinishing) {
        // Don't render any UI while finishing to prevent flicker
        return
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp), contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text("Importing...", style = MaterialTheme.typography.titleMedium)
            } else if (message != null) {
                val isErrorMessage =
                    message.startsWith("Error", ignoreCase = true) || message.contains(
                        "error",
                        ignoreCase = true
                    ) || message.contains(
                        "failed",
                        ignoreCase = true
                    ) || message.contains(
                        "Could not",
                        ignoreCase = true
                    ) || message.contains("cancelled", ignoreCase = true)

                MessageDisplayBox(
                    title = if (isErrorMessage) "Error" else "Success",
                    message = message,
                    isError = isErrorMessage,
                    onDismiss = handleOkPressed,
                    onRetry = if (isErrorMessage) onRetryPressed else null
                )
            } else {
                Text(
                    text = "No link is currently being processed.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Please share a link to this app to import an event.",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun MessageDisplayBox(
    title: String,
    message: String,
    isError: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null,
) {
    val backgroundColor = if (isError)
        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f)
    else
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)
    val textColor = if (isError)
        MaterialTheme.colorScheme.onErrorContainer
    else
        MaterialTheme.colorScheme.onPrimaryContainer

    Box(
        modifier = modifier
            .fillMaxWidth(0.9f)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .padding(16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = textColor,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = textColor,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                if (isError && onRetry != null) {
                    Button(
                        onClick = onRetry,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Text("Retry")
                    }
                }
                Button(onClick = onDismiss) {
                    Text("OK")
                }
            }
        }
    }
}



