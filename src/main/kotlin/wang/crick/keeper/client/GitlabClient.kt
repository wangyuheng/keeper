package wang.crick.keeper.client

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.*

const val MEDIA_TYPE_JSON = "application/json; charset=utf-8"
const val TOKEN = "Private-Token"
const val PAGE_SIZE = 100

@Component
class GitlabClient {

    private val log: Logger = LoggerFactory.getLogger(this.javaClass)

    @Value("\${gitlab.token}")
    private lateinit var gitlabToken: String
    @Value("\${gitlab.api}")
    private lateinit var gitlabApi: String
    @Autowired
    private lateinit var okHttpClient: OkHttpClient

    fun getUser(userId: String): JSONObject {
        return JSONObject.parseObject(get("$gitlabApi/users/$userId"))
    }

    fun listProjectIssue(projectId: Int, params: String): List<JSONObject> {
        val list: ArrayList<JSONObject> = ArrayList()
        var page = 1
        do {
            val item = JSONArray.parseArray(this.get("$gitlabApi/projects/$projectId/issues?per_page=100&page=$page$params"), JSONObject::class.java)
            list.addAll(item)
            page++
            log.info("list project issue! page -> $page size -> ${list.size} item_size -> ${item.size}")
        } while (item.size == PAGE_SIZE)
        return list
    }

    fun listOpenProjectIssue(projectId: Int): List<JSONObject> {
        return listProjectIssue(projectId, "&state=opened")
    }

    fun listParticipants(projectId: Int, issueIid: Int): List<JSONObject> {
        return JSONArray.parseArray(get("$gitlabApi/projects/$projectId/issues/$issueIid/participants"), JSONObject::class.java)
    }

    fun getGroupByName(name: String): JSONObject {
        val response = get("$gitlabApi/groups?search=$name")
        return JSONArray.parseArray(response, JSONObject::class.java)
                .find { it.getString("name") == name }!!
    }

    fun getGroupDetailById(id: Int): JSONObject {
        val response = get("$gitlabApi/groups/$id")
        return JSON.parseObject(response)
    }

    fun listGroupProject(groupId: Int): List<JSONObject> {
        return JSONArray.parseArray(get("$gitlabApi/groups/$groupId/projects?per_page=100"), JSONObject::class.java)
    }


    fun listGroupIssue(groupId: Int): List<JSONObject> {
        val list: ArrayList<JSONObject> = ArrayList()

        var page = 1
        do {
            val item = JSONArray.parseArray(get("$gitlabApi/groups/$groupId/issues?per_page=100&page=$page"), JSONObject::class.java)
            list.addAll(item)
            page++
            log.info("listGroupIssue -> " + list.size + "item -> " + item.size)
        } while (item.size == 100)

        return list
    }

    fun editIssueLabels(projectId: Int, issueIid: Int, labels: String, close: Boolean) {
        var url = "$gitlabApi/projects/$projectId/issues/$issueIid?labels=$labels"
        if (close) {
            url = "$url&state_event=close"
        }
        this.put(url, "")
    }

    fun editAssignee(projectId: Int, issueIid: Int, authorId: Int) {
        this.put("$gitlabApi/projects/$projectId/issues/$issueIid?assignee_ids=$authorId", "")
    }

    private fun get(url: String): String {
        val request = Request.Builder()
                .url(url)
                .header(TOKEN, gitlabToken)
                .get()
                .build()
        return okHttpClient.newCall(request).execute().body!!.string()
    }

    private fun put(url: String, body: String): String {
        val request = Request.Builder()
                .url(url)
                .header(TOKEN, gitlabToken)
                .put(body.toRequestBody(MEDIA_TYPE_JSON.toMediaTypeOrNull()))
                .build()
        return okHttpClient.newCall(request).execute().body!!.string()
    }


}