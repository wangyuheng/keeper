package com.github.wangyuheng.keeper.oauth

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.github.wangyuheng.keeper.core.client.GitlabApiClient
import com.github.wangyuheng.keeper.oauth.client.OauthUser
import com.github.wangyuheng.keeper.oauth.conf.AUTHORIZATION_KEY
import com.github.wangyuheng.keeper.oauth.conf.OauthProp
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import java.util.*
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * 授权后回调
 * 基于浏览器发送请求，可以配置为localhost
 *
 * @author wangyuheng@outlook.com
 */
@Controller
class OauthCallbackController {

    @Autowired
    private lateinit var oauth2Prop: OauthProp

    @Autowired
    private lateinit var gitlabApiClient: GitlabApiClient

    @Autowired
    private lateinit var sessionManager: SessionManager

    private val objectMapper = ObjectMapper().registerModule(KotlinModule()).configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    /**
     * 授权后redirect url
     *
     * @param code 用于获取accessToken，只能使用一次
     */
    @GetMapping("/callback")
    fun callback(@RequestParam(value = "code", required = false) code: String?,
                 request: HttpServletRequest, response: HttpServletResponse): String {
        val referer = request.getParameter("referer")
        val accessToken = gitlabApiClient.getAccessToken(code!!, oauth2Prop.clientId, oauth2Prop.clientSecret, oauth2Prop.buildCallbackUrl(referer))
        val user = objectMapper.readValue(gitlabApiClient.getUser(accessToken!!), OauthUser::class.java)
        val uuid = UUID.randomUUID().toString()
        sessionManager.put(uuid, user)
        //set cookie
        response.addCookie(Cookie(AUTHORIZATION_KEY, uuid))
        return "redirect:$referer"
    }

}