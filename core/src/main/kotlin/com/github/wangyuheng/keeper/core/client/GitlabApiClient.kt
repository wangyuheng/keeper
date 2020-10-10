package com.github.wangyuheng.keeper.core.client

import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import java.net.URLEncoder
import java.util.ArrayList

class GitlabApiClient {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    private val client = OkHttpClient()

    @Value("\${gitlab.serverUrl}")
    private lateinit var serverUrl: String
    @Value("\${gitlab.token}")
    private lateinit var gitlabToken: String


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

    fun listActiveUsers(): Array<String> {
        val list: Array<String> = emptyArray()
        var page = 1
        do {
            val item = JSONArray.parseArray(this.get("$serverUrl/api/v4/users?active=true&per_page=100&page=$page"), String::class.java)
            list.plus(item)
            page++
            logger.info("list project issue! page -> $page size -> ${list.size} item_size -> ${item.size}")
        } while (item.size == PAGE_SIZE)
        return list
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

    fun listOpenProjectIssue(projectId: Int): List<JSONObject> {
        return listProjectIssue(projectId, "&state=opened")
    }

    fun listProjectIssue(projectId: Int, params: String): List<JSONObject> {
        val list: ArrayList<JSONObject> = ArrayList()
        var page = 1
        do {
            val item = JSONArray.parseArray(this.get("$serverUrl/api/v4/projects/$projectId/issues?per_page=100&page=$page$params"), JSONObject::class.java)
            list.addAll(item)
            page++
            logger.info("list project issue! page -> $page size -> ${list.size} item_size -> ${item.size}")
        } while (item.size == PAGE_SIZE)
        return list
    }

    fun editIssueLabels(projectId: Int, issueIid: Int, labels: String, close: Boolean) {
        var url = "$serverUrl/api/v4/projects/$projectId/issues/$issueIid?labels=$labels"
        if (close) {
            url = "$url&state_event=close"
        }
        this.put(url, "")
    }



    private fun get(url: String): String {
        val request = Request.Builder()
                .url(url)
                .header(TOKEN, gitlabToken)
                .get()
                .build()
        return client.newCall(request).execute().body!!.string()
    }

    private fun put(url: String, body: String): String {
        val request = Request.Builder()
                .url(url)
                .header(TOKEN, gitlabToken)
                .put(body.toRequestBody(MEDIA_TYPE_JSON.toMediaTypeOrNull()))
                .build()
        return client.newCall(request).execute().body!!.string()
    }

}

