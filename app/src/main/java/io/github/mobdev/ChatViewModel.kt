package io.github.mobdev

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import retrofit2.HttpException

const val PAGE_SIZE = 20
const val NEWEST_ID = 9_999_999_999_999L

data class ChatState(
    val isLoggedIn: Boolean = false,
    val userName: String = "",
    val channels: List<String> = emptyList(),
    val selectedChannel: String? = null,
    val messages: List<Message> = emptyList(),
    val canLoadMore: Boolean = true,
    val messageText: String = "",
    val openedImage: String? = null,
    val isLoading: Boolean = false,
    val error: Int? = null
)

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("chat", Context.MODE_PRIVATE)
    private var token: String? = null

    var state by mutableStateOf(ChatState())
        private set

    init {
        val name = prefs.getString("name", null)
        val password = prefs.getString("password", null)
        if (name != null && password != null) {
            state = state.copy(isLoggedIn = true, userName = name)
            login(name, password)
        }
    }

    fun login(name: String, password: String) {
        if (name.isBlank() || password.isBlank()) {
            state = state.copy(error = R.string.error_empty)
            return
        }
        viewModelScope.launch {
            state = state.copy(isLoading = true)
            try {
                token = Network.api.login(LoginRequest(name, password)).trim()
                prefs.edit().putString("name", name).putString("password", password).apply()
                state = state.copy(isLoggedIn = true, userName = name, isLoading = false)
                loadChannels()
            } catch (e: HttpException) {
                prefs.edit().clear().apply()
                state = ChatState(error = R.string.error_login)
            } catch (e: Exception) {
                state = state.copy(isLoading = false, error = R.string.error_network)
            }
        }
    }

    fun logout() {
        val oldToken = token
        viewModelScope.launch {
            try {
                if (oldToken != null) {
                    Network.api.logout(oldToken)
                }
            } catch (e: Exception) {
            }
        }
        token = null
        prefs.edit().clear().apply()
        state = ChatState()
    }

    fun loadChannels() {
        viewModelScope.launch {
            try {
                val channels = Network.api.getChannels()
                state = state.copy(channels = channels)
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }

    fun openChannel(channel: String) {
        state = state.copy(
            selectedChannel = channel,
            messages = emptyList(),
            canLoadMore = true,
            messageText = ""
        )
        loadMessages(NEWEST_ID)
    }

    fun closeChannel() {
        state = state.copy(selectedChannel = null, messages = emptyList(), openedImage = null)
    }

    fun loadOlderMessages() {
        if (state.isLoading || !state.canLoadMore) return
        val oldestId = state.messages.firstOrNull()?.id ?: return
        loadMessages(oldestId)
    }

    private fun loadMessages(lastKnownId: Long) {
        val channel = state.selectedChannel ?: return
        viewModelScope.launch {
            state = state.copy(isLoading = true)
            try {
                val loaded = Network.api.getMessages(channel, PAGE_SIZE, lastKnownId, true)
                val older = loaded.reversed()
                val all = if (lastKnownId == NEWEST_ID) older else older + state.messages
                state = state.copy(
                    messages = all,
                    canLoadMore = loaded.size == PAGE_SIZE,
                    isLoading = false
                )
            } catch (e: Exception) {
                state = state.copy(isLoading = false)
                handleError(e)
            }
        }
    }

    fun onMessageTextChange(text: String) {
        state = state.copy(messageText = text)
    }

    fun sendMessage() {
        val text = state.messageText.trim()
        val channel = state.selectedChannel ?: return
        val currentToken = token ?: return
        if (text.isEmpty()) return

        val message = Message(
            id = null,
            from = state.userName,
            to = channel,
            data = MessageData(text = TextData(text), image = null),
            time = null
        )
        viewModelScope.launch {
            try {
                Network.api.sendMessage(currentToken, message)
                state = state.copy(messageText = "")
                loadMessages(NEWEST_ID)
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }

    fun openImage(link: String) {
        state = state.copy(openedImage = link)
    }

    fun closeImage() {
        state = state.copy(openedImage = null)
    }

    fun dismissError() {
        state = state.copy(error = null)
    }

    private fun handleError(e: Exception) {
        if (e is HttpException && e.code() == 401) {
            val name = prefs.getString("name", null)
            val password = prefs.getString("password", null)
            if (name != null && password != null) {
                login(name, password)
            } else {
                logout()
            }
        } else {
            state = state.copy(error = R.string.error_network)
        }
    }
}