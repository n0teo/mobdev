package io.github.mobdev

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
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
    val isOnline: Boolean = true,
    val error: Int? = null
)

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("chat", Context.MODE_PRIVATE)
    private val cache = ChatCache(application)
    private val connectivityManager = application.getSystemService(ConnectivityManager::class.java)
    private var token: String? = null

    var state by mutableStateOf(ChatState())
        private set

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: android.net.Network) {
            viewModelScope.launch { onNetworkChanged(true) }
        }

        override fun onLost(network: android.net.Network) {
            viewModelScope.launch { onNetworkChanged(false) }
        }
    }

    init {
        state = state.copy(isOnline = isOnlineNow(), channels = cache.loadChannels())
        connectivityManager.registerDefaultNetworkCallback(networkCallback)

        val name = prefs.getString("name", null)
        val password = prefs.getString("password", null)
        if (name != null && password != null) {
            state = state.copy(isLoggedIn = true, userName = name)
            login(name, password)
        }
    }

    override fun onCleared() {
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }

    private fun isOnlineNow(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun onNetworkChanged(online: Boolean) {
        val wasOffline = !state.isOnline
        state = state.copy(isOnline = online)

        if (online && wasOffline && state.isLoggedIn) {
            val name = prefs.getString("name", null)
            val password = prefs.getString("password", null)
            if (token == null && name != null && password != null) {
                login(name, password)
            } else {
                loadChannels()
                if (state.selectedChannel != null) {
                    loadMessages(NEWEST_ID)
                }
            }
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
                if (state.selectedChannel != null) {
                    loadMessages(NEWEST_ID)
                }
            } catch (e: HttpException) {
                prefs.edit().clear().apply()
                state = ChatState(error = R.string.error_login, isOnline = state.isOnline)
            } catch (e: Exception) {
                state = state.copy(isLoading = false)
                showNetworkError()
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
                // сервер всё равно забудет токен, ошибку можно не показывать
            }
        }
        token = null
        prefs.edit().clear().apply()
        state = ChatState(isOnline = state.isOnline)
    }

    fun loadChannels() {
        viewModelScope.launch {
            try {
                val channels = Network.api.getChannels()
                cache.saveChannels(channels)
                state = state.copy(channels = channels)
            } catch (e: Exception) {
                state = state.copy(channels = cache.loadChannels())
                handleError(e)
            }
        }
    }

    fun openChannel(channel: String) {
        state = state.copy(
            selectedChannel = channel,
            messages = cache.loadMessages(channel),
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
                if (state.selectedChannel != channel) return@launch

                val all = (state.messages + loaded)
                    .distinctBy { it.id }
                    .sortedBy { it.id }
                cache.saveMessages(channel, all)
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
        if (text.isEmpty() || !state.isOnline) return

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
            showNetworkError()
        }
    }

    private fun showNetworkError() {
        if (state.isOnline) {
            state = state.copy(error = R.string.error_network)
        }
    }
}
