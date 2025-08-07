package wtf.topi.campfire.tools

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.util.Log
import kotlinx.coroutines.CancellationException

class ShareImportViewModel : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _uiMessage = MutableStateFlow<String?>(null) // Renamed for clarity
    val uiMessage: StateFlow<String?> = _uiMessage.asStateFlow()

    fun importSharedLink(link: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _uiMessage.value = null // Clear previous messages
            try {
                val payload = listOf(link.trim())
                val response: HttpResponse = CampfireKtorService.importEvents(payload)

                if (response.status.isSuccess()) {
                    Log.d("MainViewModel", "Link imported successfully: $link, Status: ${response.status}")
                    // You might want a different message for success, or clear it if the UI just reverts
                    _uiMessage.value = "Link imported successfully!"
                } else {
                    val errorBody = response.bodyAsText()
                    Log.e("MainViewModel", "Error importing link: ${response.status} - Body: $errorBody")
                    // Construct a user-friendly error message
                    _uiMessage.value = "Error ${response.status.value}: Could not import the link. ${errorBody.take(100)}" // Example
                }
            } catch (e: CancellationException) {
                Log.w("MainViewModel", "Ktor request was cancelled: ${e.message}")
                _uiMessage.value = "Import operation was cancelled."
            } catch (e: Exception) {
                Log.e("MainViewModel", "Exception during Ktor link import", e)
                _uiMessage.value = "A network error occurred. Please check your connection and try again."
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Call this from UI if you want a way to manually clear the message,
    // for example, on a retry button press.
    fun clearUiMessage() {
        _uiMessage.value = null
    }
}
