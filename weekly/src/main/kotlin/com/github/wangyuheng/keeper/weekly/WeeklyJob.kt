package com.github.wangyuheng.keeper.weekly

import com.github.wangyuheng.keeper.core.client.EmailClient
import com.github.wangyuheng.keeper.core.client.GitlabApiClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import java.time.DayOfWeek
import java.time.LocalDate

class WeeklyJob {

    private val log: Logger = LoggerFactory.getLogger(this.javaClass)

    @Autowired
    private lateinit var gitlabApiClient: GitlabApiClient
    @Autowired
    private lateinit var userManager: UserManager
    @Autowired
    private lateinit var emailClient: EmailClient
    @Autowired
    private lateinit var weeklyProp: WeeklyProp

    @Scheduled(cron = "\${weekly.cron}")
    fun checkWeeklyIssue() {
        log.info("start weekly job ! props:{}", weeklyProp)
        val nameMapWeeklyIssue = weeklyProp.projectIds.flatMap {
            gitlabApiClient.listOpenProjectIssue(it)
        }.filter { issue ->
            issue.getJSONArray("labels").contains(weeklyProp.label)
        }.filter { issue ->
            !issue.getJSONArray("labels").contains(weeklyProp.archivedLabel)
        }.map { issue ->
            val name = issue.getJSONObject("author").getString("name")
            name to WeeklyIssue(issue.getIntValue("project_id"), issue.getIntValue("iid"), name, issue.getString("description"))
        }.toMap()
        log.info("map gitlab issue data! nameMapWeeklyIssue:{}", nameMapWeeklyIssue)
        // 如果一个记录也没有，可能是节假日导致。此时不发生提醒
        if (nameMapWeeklyIssue.isNotEmpty()) {
            userManager.findAll().forEach {
                val weeklyIssue = nameMapWeeklyIssue[it.name]
                this.sendEmail(weeklyIssue, it)
            }
        } else {
            log.warn("weekly issue is empty! props:{}", weeklyProp)
        }
    }

    private fun sendEmail(weeklyIssue: WeeklyIssue?, user: User) {
        if (weeklyIssue == null) {
            log.info("send a remind email! name:{}", user.name)
            emailClient.send(fillSubject(user.name), "I am ${user.name}. I do not write a weekly! Please remind me when you see me!", user.email, weeklyProp.receiverEmails)
        } else {
            log.info("send a weekly email! name:{}", user.name)
            emailClient.send(fillSubject(user.name), weeklyIssue.html(), user.email, weeklyProp.receiverEmails)
            gitlabApiClient.editIssueLabels(weeklyIssue.projectId, weeklyIssue.issueId, weeklyProp.archivedLabel, true)
        }
    }

    private fun fillSubject(user: String): String {
        val lastWeek = LocalDate.now().minusWeeks(1)
        return weeklyProp.subject.replace("__user__", user)
                .replace("__datePeriod__", "${lastWeek.with(DayOfWeek.MONDAY)}-${lastWeek.with(DayOfWeek.SUNDAY)}")
    }

}