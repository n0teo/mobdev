package io.github.mobdev

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

const val BASE_URL = "https://faerytea.name/"

interface ChatApi {

    @POST("login")
    suspend fun login(@Body body: LoginRequest): String

    @POST("logout")
    suspend fun logout(@Header("X-Auth-Token") token: String): String

    @GET("channels")
    suspend fun getChannels(): List<String>

    @GET("channel/{name}")
    suspend fun getMessages(
        @Path("name") channel: String,
        @Query("limit") limit: Int,
        @Query("lastKnownId") lastKnownId: Long,
        @Query("reverse") reverse: Boolean
    ): List<Message>

    @POST("messages")
    suspend fun sendMessage(
        @Header("X-Auth-Token") token: String,
        @Body message: Message
    ): String
}

object Network {
    val api: ChatApi = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(ScalarsConverterFactory.create())
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ChatApi::class.java)
}