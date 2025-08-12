package wtf.topi.campfire.tools;

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge

class ShareOpenActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.action != Intent.ACTION_SEND || intent.type != "text/plain") {
            return
        }

        val eventLink = intent.getStringExtra(Intent.EXTRA_TEXT)?.trim()
        if (eventLink.isNullOrEmpty()) {
            Log.e("OpenActivity", "Received empty or null event link")
            Toast.makeText(this, "No event link provided", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val link = Uri.Builder().apply {
            scheme("https")
            authority("campfire-tools.topi.wtf")
            path("event")
            appendQueryParameter("event", eventLink)
        }.build()

        Log.d("OpenActivity", "Opening link: $link")
        startActivity(Intent(Intent.ACTION_VIEW, link))
    }
}
