package wang.crick.keeper.service

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import wang.crick.keeper.client.GitlabClient
import wang.crick.keeper.model.Issue
import wang.crick.keeper.model.Participant
import wang.crick.keeper.repository.IssueRepository
import wang.crick.keeper.repository.ParticipantRepository

@Service
class WorkloadService {

    private val log: Logger = LoggerFactory.getLogger(this.javaClass)

    @Autowired
    private lateinit var issueRepository: IssueRepository
    @Autowired
    private lateinit var participantRepository: ParticipantRepository
    @Autowired
    private lateinit var gitlabClient: GitlabClient


    fun statistics(workloadGroups: List<Int>) {
        log.info("start workload job ! workloadGroups:{}", workloadGroups)

        if (workloadGroups.isNotEmpty()) {
            issueRepository.deleteAllInBatch()
            participantRepository.deleteAllInBatch()
            workloadGroups.forEach { groupId ->
                val group = gitlabClient.getGroupDetailById(groupId)
                val projects = gitlabClient.listGroupProject(groupId)
                val projectIdMapName = projects.map { it.getIntValue("id") to it.getString("name") }.toMap()
                val issueList = gitlabClient.listGroupIssue(groupId)
                issueList.map { }
                val poList = issueList.map { issue ->
                    Issue(
                            issueIid = issue.getIntValue("iid"),
                            groupId = group.getIntValue("id"),
                            groupName = group.getString("name"),
                            projectId = issue.getIntValue("project_id"),
                            projectName = projectIdMapName.getValue(issue.getIntValue("project_id")),
                            state = issue.getString("state"),
                            title = issue.getString("title"),
                            labels = issue.getJSONArray("labels")?.toJSONString(),
                            description = issue.getString("description"),
                            author = issue.getJSONObject("author").toJSONString(),
                            assignee = issue.getJSONObject("assignee")?.toJSONString(),
                            milestone = issue.getJSONObject("milestone")?.toJSONString(),
                            dueDate = issue.getDate("due_date"),
                            createdAt = issue.getDate("created_at"),
                            updatedAt = issue.getDate("updated_at"),
                            closedAt = issue.getDate("closed_at")
                    )
                }
                log.info("persistent issue data! group -> $group size=" + poList.size)
                issueRepository.saveAll(poList)
                val participantPoList = poList.flatMap { issue ->
                    val participants = gitlabClient.listParticipants(issue.projectId, issue.issueIid)
                    participants.map {
                        Participant(
                                groupId = issue.groupId,
                                issueIid = issue.issueIid,
                                groupName = issue.groupName,
                                projectId = issue.projectId,
                                projectName = issue.projectName,
                                title = issue.title,
                                name = it.getString("name"),
                                username = it.getString("username")
                        )
                    }
                }
                participantRepository.saveAll(participantPoList)
            }
        }
    }

}