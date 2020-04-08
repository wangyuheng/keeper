package wang.crick.keeper.hook

import com.alibaba.fastjson.JSONObject
import java.util.stream.Collectors

class GitlabHookBodyHelper {

    companion object {
        const val LABEL_TODO = "To Do"
        const val LABEL_DOING = "Doing"
        const val LABEL_VERIFY = "verify"
        const val LABEL_P0 = "P0"
        const val LABEL_BUG = "bug"
        /**
         * issue 是否已关闭
         */
        fun isClose(body: JSONObject): Boolean {
            return "closed" == body.getJSONObject("object_attributes").getString("state")
        }

        fun getChangedAssignee(body: JSONObject): Assignee? {
            return if (null == body.getJSONObject("changes")?.getJSONObject("assignees")?.getJSONArray("current")) {
                null
            } else {
                val current: Map<String, String>? = body.getJSONObject("changes")?.getJSONObject("assignees")?.getJSONArray("current")?.get(0) as LinkedHashMap<String, String>
                if (current == null) {
                    null
                } else {
                    JSONObject.parseObject(JSONObject.toJSONString(current), Assignee::class.java)
                }
            }
        }

        fun getAuthor(body: JSONObject): String? {
            var author: String? = null
            val list = body.getJSONObject("object_attributes").getString("description").split("Author:")
            if (list.size > 1) {
                val tmp = list[1].trim().replace("@", "")
                author = tmp.split(" ")[0]
            }
            return author
        }

        fun getAuthorId(body: JSONObject): String {
            return body.getJSONObject("object_attributes").getString("author_id")
        }

        fun getIssueUrl(body: JSONObject): String {
            return body.getJSONObject("object_attributes").getString("url")
        }

        fun getIssueTitle(body: JSONObject): String {
            return body.getJSONObject("object_attributes").getString("title")
        }

        fun listLabelTitle(body: JSONObject): MutableSet<String> {
            val labels = body.getJSONArray("labels")
            return labels.stream()
                    .map { JSONObject.toJSONString(it) }
                    .map { JSONObject.parseObject(it) }
                    .map { it.getString("title") }
                    .collect(Collectors.toSet())
        }

        fun isFirstChangeToVerifyLabel(body: JSONObject): Boolean {
            var toggle = false
            if (body.getJSONObject("changes").containsKey("labels")) {
                val previous = body.getJSONObject("changes").getJSONObject("labels").getJSONArray("previous")
                        .stream()
                        .map { label -> JSONObject.parseObject(JSONObject.toJSONString(label)).getString("title") }
                        .collect(Collectors.toSet())
                val current = body.getJSONObject("changes").getJSONObject("labels").getJSONArray("current")
                        .stream()
                        .map { label -> JSONObject.parseObject(JSONObject.toJSONString(label)).getString("title") }
                        .collect(Collectors.toSet())

                toggle = !previous.contains(LABEL_VERIFY) && current.contains(LABEL_VERIFY)
            }
            return toggle
        }
    }

    class Assignee {
        var name: String? = null
        var username: String? = null

        override fun toString(): String {
            return "Assignee(name=$name, username=$username)"
        }

    }

}