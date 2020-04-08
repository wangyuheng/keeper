package wang.crick.keeper.hook

import com.alibaba.fastjson.JSONObject
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import wang.crick.keeper.client.DingdingClient
import wang.crick.keeper.client.GitlabClient
import wang.crick.keeper.hook.GitlabHookBodyHelper.Companion.getChangedAssignee
import wang.crick.keeper.repository.DeveloperRepository

@RestController
@RequestMapping("listener")
class IssueListener {

    val log: Logger = LoggerFactory.getLogger(this.javaClass)

    @Autowired
    private lateinit var dingdingClient: DingdingClient
    @Autowired
    private lateinit var gitlabClient: GitlabClient
    @Autowired
    private lateinit var developerRepository: DeveloperRepository

    @Value("\${listen.projects:}")
    private lateinit var listenProjectList: List<String>

    @RequestMapping("/{project}")
    fun dispatcher(@RequestBody(required = false) body: JSONObject?, @PathVariable("project") project: String): String {
        log.info("receive project: $project body: $body")

        if (listenProjectList.contains(project)) {
            dispatcherEvent(body)
        }

        return "success"
    }

    private fun dispatcherEvent(body: JSONObject?) {
        when (body?.getString("event_type")) {
            "merge_request" -> handlerMr(body)
            "issue" -> handlerIssue(body)
        }
    }

    /**
     * 暂不针对MergeRequest做消息提醒
     */
    private fun handlerMr(body: JSONObject) {
        //TODO
    }

    private fun handlerIssue(body: JSONObject) {
        if (GitlabHookBodyHelper.isClose(body)) {
            //已关闭的issue不做处理
            return
        }
        val changedAssignee: GitlabHookBodyHelper.Assignee? = getChangedAssignee(body)
        log.info("handler issue changedAssignee: $changedAssignee")
        val labelTitles: MutableSet<String> = GitlabHookBodyHelper.listLabelTitle(body)
        log.info("handler issue labels: $labelTitles")
        // 是否变更assignee
        if (changedAssignee != null) {
            val developer = developerRepository.findByUsername(changedAssignee.username!!)
            // 为开发人员 && label为todo且不在进行中
            if (developer != null && labelTitles.isTodo() && !labelTitles.isProcessing()) {
                when {
                    labelTitles.isBug() -> sendBugMsg(body, developer.mobile)
                    labelTitles.isP0() -> sendEmergencyMsg(body, developer.mobile)
                    else -> log.info("ignore issue change! labelTitles -> $labelTitles")
                }
            }
        } else {
            if (GitlabHookBodyHelper.isFirstChangeToVerifyLabel(body)) {
                val author = getAuthor(body)
                val assigneeUsername = editAssignee(body, author)
                val mobile = developerRepository.findByUsername(assigneeUsername)?.mobile
                if (null != mobile) {
                    sendVerifyMsg(body, author, mobile)
                }
            }
        }
    }

    /**
     * 获取author
     *  如果未按照模板制定，默认获取issue的创建者
     */
    private fun getAuthor(body: JSONObject): String {
        val author: String? = GitlabHookBodyHelper.getAuthor(body)
        return if (author == null) {
            val userId = GitlabHookBodyHelper.getAuthorId(body)
            gitlabClient.getUser(userId).getString("name")
        } else {
            author
        }
    }

    private fun Set<String>.isTodo(): Boolean {
        return this.contains(GitlabHookBodyHelper.LABEL_TODO)
    }

    private fun Set<String>.isProcessing(): Boolean {
        return this.contains(GitlabHookBodyHelper.LABEL_VERIFY)
                || this.contains(GitlabHookBodyHelper.LABEL_DOING)
    }

    private fun Set<String>.isBug(): Boolean {
        return this.contains(GitlabHookBodyHelper.LABEL_BUG)
    }

    private fun Set<String>.isP0(): Boolean {
        return this.contains(GitlabHookBodyHelper.LABEL_P0)
    }

    private fun sendEmergencyMsg(body: JSONObject, mobile: String) {
        val author = getAuthor(body)
        val url = GitlabHookBodyHelper.getIssueUrl(body)
        val title = GitlabHookBodyHelper.getIssueTitle(body)
        val msg = buildTextMsg("$author 创建了一个 [P0 issue] 给你，请尽快确认并处理 \n Link -> $url \n title -> $title ", mobile)
        dingdingClient.send(msg)
    }

    private fun sendBugMsg(body: JSONObject, mobile: String) {
        val author = getAuthor(body)
        val url = GitlabHookBodyHelper.getIssueUrl(body)
        val title = GitlabHookBodyHelper.getIssueTitle(body)
        val msg = buildTextMsg("$author 创建了一个 [bug] 给你，请尽快确认并处理 \n Link -> $url \n title -> $title ", mobile)
        dingdingClient.send(msg)
    }

    private fun sendVerifyMsg(body: JSONObject, author: String, mobile: String) {
        val url = GitlabHookBodyHelper.getIssueUrl(body)
        val title = GitlabHookBodyHelper.getIssueTitle(body)
        val msg = buildTextMsg("Hi, $author 您有一个issue已被解决需要验证，请尽快处理并关闭 \n Link -> $url \n title -> $title ", mobile)
        dingdingClient.send(msg)
    }

    private fun editAssignee(body: JSONObject, author: String): String {
        val projectId = getProjectId(body)
        val issueIid = getIssueIid(body)
        val assigneeId = getAssigneeId(body)

        val participants = gitlabClient.listParticipants(projectId, issueIid)

        val authorId = participants.stream()
                .filter { participant -> participant.getString("username") == author || participant.getString("name") == author }
                .map { participant -> participant.getIntValue("id") }
                .findAny().orElse(assigneeId)

        if (authorId != assigneeId) {
            gitlabClient.editAssignee(projectId, issueIid, authorId)
        }

        return participants.stream()
                .filter { participant -> participant.getIntValue("id") == authorId }
                .map { participant -> participant.getString("username") }
                .findAny().orElseThrow { RuntimeException("$authorId not found!") }

    }

    private fun getProjectId(body: JSONObject): Int {
        return body.getJSONObject("object_attributes").getIntValue("project_id")
    }

    private fun getIssueIid(body: JSONObject): Int {
        return body.getJSONObject("object_attributes").getIntValue("iid")
    }

    private fun getAssigneeId(body: JSONObject): Int {
        return body.getJSONObject("object_attributes").getIntValue("assignee_id")
    }

    private fun buildTextMsg(content: String, mobile: String): String {
        return "{ \"msgtype\": \"text\", \"text\": { \"content\": \"$content\n@$mobile\" }, \"at\": { \"atMobiles\": [ \"$mobile\" ], \"isAtAll\": false } }"
    }

}
