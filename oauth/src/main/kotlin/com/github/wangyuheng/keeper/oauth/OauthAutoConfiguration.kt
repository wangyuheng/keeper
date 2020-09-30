package com.github.wangyuheng.keeper.oauth

import com.github.wangyuheng.keeper.oauth.client.GitlabClient
import com.github.wangyuheng.keeper.oauth.conf.OauthConfig
import com.github.wangyuheng.keeper.oauth.conf.OauthProp
import org.springframework.boot.autoconfigure.AutoConfigurationPackage
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@ConditionalOnProperty(name = ["oauth2.enable"], havingValue = "true")
@Configuration
@AutoConfigurationPackage
class OauthAutoConfiguration {

    @Bean
    fun oauthProp() = OauthProp()

    @Bean
    fun gitlabClient() = GitlabClient()

    @Bean
    fun oauthConfig() = OauthConfig()

    @Bean
    fun oauthCallbackController() = OauthCallbackController()

    @Bean
    @ConditionalOnMissingBean
    fun sessionManager() = LocalSessionManager()

}