package wang.crick.keeper.model

import java.util.*
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@Entity
class Issue(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        var id: Long? = null,
        var issueIid: Int,
        var groupId: Int,
        var groupName: String,
        var projectId: Int,
        var projectName: String,
        var state: String,
        var title: String,
        var labels: String?,
        var description: String?,
        var author: String,
        var assignee: String?,
        var milestone: String?,
        var dueDate: Date?,
        var createdAt: Date,
        var updatedAt: Date?,
        var closedAt: Date?
)