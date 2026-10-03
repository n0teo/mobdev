package io.github.mobdev

import android.content.Context
import com.google.gson.Gson
import java.io.File
import java.net.URLEncoder

class ChatCache(context: Context) {

    private val dir = context.filesDir
    private val gson = Gson()

    fun saveChannels(channels: List<String>) {
        File(dir, "channels.json").writeText(gson.toJson(channels))
    }

    fun loadChannels(): List<String> {
        val file = File(dir, "channels.json")
        if (!file.exists()) return emptyList()
        return try {
            gson.fromJson(file.readText(), Array<String>::class.java).toList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveMessages(channel: String, messages: List<Message>) {
        messagesFile(channel).writeText(gson.toJson(messages))
    }

    fun loadMessages(channel: String): List<Message> {
        val file = messagesFile(channel)
        if (!file.exists()) return emptyList()
        return try {
            gson.fromJson(file.readText(), Array<Message>::class.java).toList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun messagesFile(channel: String): File {
        val name = URLEncoder.encode(channel, "UTF-8")
        return File(dir, "messages_$name.json")
    }
}
