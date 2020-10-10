package com.github.wangyuheng.keeper.weekly

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "weekly")
data class WeeklyProp(
        var cron: String = "",
        var subject: String = "[Weekly][__user__][__datePeriod__]",
        var label: String = "weekly",
        var archivedLabel: String = "archived_weekly",
        var alert: Boolean = false,
        var projectIds: List<Int> = emptyList(),
        var receiverEmails: List<String> = emptyList()
)