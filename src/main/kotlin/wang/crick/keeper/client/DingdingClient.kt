package wang.crick.keeper.client

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class DingdingClient {

    val log: Logger = LoggerFactory.getLogger(this.javaClass)

    @Value("\${dingding.url}")
    private lateinit var robotUrl: String
    @Autowired
    private lateinit var okHttpClient: OkHttpClient

    fun send(json: String): String? {
        log.info("send msg. -> $json")
        val request = Request.Builder()
                .url(robotUrl)
                .post(json.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull()))
                .build()
        val response = okHttpClient.newCall(request).execute().body!!.string()
        log.info("send msg receive -> $response")
        return response
    }

}