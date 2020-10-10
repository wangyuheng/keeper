package com.github.wangyuheng.keeper.oauth.conf

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "oauth2")
data class OauthProp(
        var clientCallbackUrl: String = "",
        var clientId: String = "",
        var clientSecret: String = "",
        var pathPatterns: List<String> = arrayListOf("/**")

) {
    fun buildCallbackUrl(referer: String): String {
        return "$clientCallbackUrl?referer=$referer"
    }

}