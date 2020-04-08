package wang.crick.keeper.model

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@Entity
class Participant(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        var id: Long? = null,
        var issueIid: Int,
        var groupId: Int,
        var groupName: String,
        var projectId: Int,
        var projectName: String,
        var title: String,
        var name: String,
        var username: String
)