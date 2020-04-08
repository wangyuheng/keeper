package wang.crick.keeper.job

import org.commonmark.ext.gfm.tables.TablesExtension
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import wang.crick.keeper.client.EmailClient
import wang.crick.keeper.client.GitlabClient
import wang.crick.keeper.model.Developer
import wang.crick.keeper.model.WeeklyIssue
import wang.crick.keeper.repository.DeveloperRepository
import java.time.DayOfWeek
import java.time.LocalDate

const val WEEKLY_LABEL = "周报"
const val WEEKLY_ARCHIVE_LABEL = "周报归档"

@Component
@ConditionalOnProperty(value = ["weekly.enable"], havingValue = "true")
class WeeklyJob {

    private val log: Logger = LoggerFactory.getLogger(this.javaClass)
    private val extensions = listOf(TablesExtension.create())
    private val parser = Parser.builder()
            .extensions(extensions)
            .build()
    private val renderer = HtmlRenderer.builder()
            .extensions(extensions)
            .build()

    @Autowired
    lateinit var gitlabClient: GitlabClient
    @Autowired
    lateinit var developerRepository: DeveloperRepository
    @Autowired
    lateinit var emailClient: EmailClient

    @Value("\${weekly.projects:}")
    private lateinit var weeklyProjectList: List<Int>

    @Scheduled(cron = "\${weekly.cron}")
    fun checkWeeklyIssue() {
        log.info("start weekly job ! weeklyProjectList:{}", weeklyProjectList)
        val nameMapWeeklyIssue = weeklyProjectList.flatMap {
            gitlabClient.listOpenProjectIssue(it)
        }.filter { issue ->
            issue.getJSONArray("labels").contains(WEEKLY_LABEL)
        }.filter { issue ->
            !issue.getJSONArray("labels").contains(WEEKLY_ARCHIVE_LABEL)
        }.map { issue ->
            val name = issue.getJSONObject("author").getString("name")
            name to WeeklyIssue(issue.getIntValue("project_id"), issue.getIntValue("iid"), name, issue.getString("description"))
        }.toMap()
        log.info("map gitlab issue data! nameMapWeeklyIssue:{}", nameMapWeeklyIssue)
        // 如果一个记录也没有，可能是节假日导致。此时不发生提醒
        if (nameMapWeeklyIssue.isNotEmpty()) {
            developerRepository.findAll().forEach {
                val weeklyIssue = nameMapWeeklyIssue[it.name]
                this.sendEmail(weeklyIssue, it)
            }
        }
    }

    private fun sendEmail(weeklyIssue: WeeklyIssue?, developer: Developer) {
        if (weeklyIssue == null) {
            log.info("send a remind email! name:{}", developer.name)
            emailClient.send(fillSubject(developer.name), "I am ${developer.name}. I do not write a weekly! Please remind me when you see me!", developer.receivers)
        } else {
            log.info("send a weekly email! name:{}", developer.name)
            val content = renderer.render(parser.parse(weeklyIssue.description))
            emailClient.send(fillSubject(developer.name), content, developer.receivers)
            gitlabClient.editIssueLabels(weeklyIssue.projectId, weeklyIssue.issueId, WEEKLY_ARCHIVE_LABEL, true)
        }
    }

    private fun fillSubject(name: String): String {
        val lastWeek = LocalDate.now().minusWeeks(1)
        return "[周报][XX工程][$name][${lastWeek.with(DayOfWeek.MONDAY)}~${lastWeek.with(DayOfWeek.SUNDAY)}]"
    }

}