package com.github.wangyuheng.keeper.oauth.client

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.github.wangyuheng.keeper.oauth.conf.OauthProp
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.net.URLEncoder

@Component
class GitlabClient {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    @Autowired
    private lateinit var oauthProp: OauthProp
    private val client = OkHttpClient()
    private val objectMapper = ObjectMapper().registerModule(KotlinModule()).configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    /**
     * 通过code去gitlab获取accessToken
     * @param code grant ticket 只能使用一次
     * @param redirectUri 回调地址，必须与授权时参数一致
     */
    fun getAccessToken(code: String, redirectUri: String): String? {
        val url = oauthProp.serverUrl + "/oauth/token?grant_type=authorization_code&client_id=${oauthProp.clientId}&client_secret=${oauthProp.clientSecret}&code=$code&redirect_uri=" + URLEncoder.encode(redirectUri, "utf-8")
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
    fun getUser(accessToken: String): OauthUser {
        val url = "${oauthProp.serverUrl}/api/v4/user?access_token=$accessToken"
        val request = Request.Builder()
                .url(url)
                .build()
        client.newCall(request).execute().use { response ->
            val body = response.body!!.string()
            logger.info("request url: $url result: $body")
            return objectMapper.readValue(body, OauthUser::class.java)
        }
    }

}

