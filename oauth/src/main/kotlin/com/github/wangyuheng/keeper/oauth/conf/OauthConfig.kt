package com.github.wangyuheng.keeper.oauth.conf

import com.github.wangyuheng.keeper.oauth.SessionManager
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.web.context.request.RequestAttributes
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.net.URLEncoder
import java.util.*
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

const val CURRENT_USER = "CurrentUser"
const val AUTHORIZATION_KEY = "Authorization"

@Configuration
class OauthConfig : WebMvcConfigurer {

    @Autowired
    private lateinit var oauthProp: OauthProp

    @Autowired
    private lateinit var sessionManager: SessionManager

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(OauthInterceptor(oauthProp, sessionManager))
                .addPathPatterns(oauthProp.pathPatterns)
                .excludePathPatterns("/callback")
    }

}

class OauthInterceptor(var oauthProp: OauthProp, var sessionManager: SessionManager) : HandlerInterceptor {

    private val logger = LoggerFactory.getLogger(this.javaClass)


    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {

        if (request.cookies != null) {
            val authorizationKeyOp = Arrays.stream(request.cookies)
                    .filter { it.name == AUTHORIZATION_KEY }
                    .map { obj: Cookie -> obj.value }
                    .findAny()
            if (authorizationKeyOp.isPresent) {
                // 授权信息存在，获取user信息放入session
                RequestContextHolder.getRequestAttributes().setAttribute(CURRENT_USER, sessionManager.get(authorizationKeyOp.get()), RequestAttributes.SCOPE_SESSION)
                return super.preHandle(request, response, handler)
            }
        }
        // 授权信息不存在，去gitlab进行验证
        val referer = request.requestURL.toString()
        val redirectUri = URLEncoder.encode("${oauthProp.clientCallbackUrl}?referer=$referer", "utf-8")
        val gitlabAuthUrl: String = "${oauthProp.serverUrl}/oauth/authorize?response_type=code&redirect_uri=$redirectUri&client_id=${oauthProp.clientId}"
        logger.info("gitlabAuthUrl:{}", gitlabAuthUrl)
        response.sendRedirect(gitlabAuthUrl)
        return false
    }
}

