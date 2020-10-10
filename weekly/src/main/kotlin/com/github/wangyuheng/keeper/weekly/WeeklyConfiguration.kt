package com.github.wangyuheng.keeper.weekly

import com.github.wangyuheng.keeper.core.client.EmailClientTemplate
import com.github.wangyuheng.keeper.core.client.GitlabApiClient
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling

@Configuration
@EnableScheduling
class WeeklyConfiguration {

    @Bean
    fun gitlabApiClient() = GitlabApiClient()

    @Bean
    @ConditionalOnMissingBean
    fun emailClient() = EmailClientTemplate()

    @Bean
    @ConditionalOnMissingBean
    fun userManager() = GitlabUserManager()

    @Bean
    fun weeklyJob() = WeeklyJob()
}