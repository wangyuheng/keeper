package com.github.wangyuheng.keeper.core.client

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import java.net.URLEncoder

class GitlabApiClient {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    private val client = OkHttpClient()

    @Value("\${gitlab.serverUrl}")
    private lateinit var serverUrl: String

    /**
     * 通过code去gitlab获取accessToken
     * @param code grant ticket 只能使用一次
     * @param redirectUri 回调地址，必须与授权时参数一致
     */
    fun getAccessToken(code: String, clientId: String, clientSecret: String, redirectUri: String): String? {
        val url = "$serverUrl/oauth/token?grant_type=authorization_code&client_id=$clientId&client_secret=$clientSecret&code=$code&redirect_uri=" + URLEncoder.encode(redirectUri, "utf-8")
        logger.info("request url: $url")
        val request = Request.Builder()
                .method("POST", "".toRequestBody())
                .url(url)
                .build()

        client.newCall(request).execute().use { response ->
            val body = response.body!!.string()
            logger.info("request url: $url result: $body")
            return ObjectMapper().readValue(body, ObjectNode::class.java)["access_token"].textValue()
        }
    }

    /**
     * 根据accessToken获取user信息
     */
    fun getUser(accessToken: String): String {
        val url = "$serverUrl/api/v4/user?access_token=$accessToken"
        val request = Request.Builder()
                .url(url)
                .build()
        client.newCall(request).execute().use { response ->
            val body = response.body!!.string()
            logger.info("request url: $url result: $body")
            return body
        }
    }

}

