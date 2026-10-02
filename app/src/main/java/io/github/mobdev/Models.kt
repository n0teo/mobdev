package io.github.mobdev

import com.google.gson.annotations.SerializedName

data class Message(
    val id: Long?,
    val from: String,
    val to: String?,
    val data: MessageData,
    val time: Long?
)

data class MessageData(
    @SerializedName("Text") val text: TextData?,
    @SerializedName("Image") val image: ImageData?
)

data class TextData(val text: String)

data class ImageData(val link: String?)

data class LoginRequest(val name: String, val pwd: String)