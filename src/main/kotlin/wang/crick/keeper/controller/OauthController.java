package wang.crick.keeper.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.Serializable;
import java.net.URLEncoder;
import java.util.*;

@Controller
public class OauthController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${oauth2.server.url:https://gitlab.com}")
    private String gitlabServerUrl;
    @Value("${oauth2.client.id:2f7e660699a37776a91fcccc855b6f3e007914bc34335ca7a0fc77233e818643}")
    private String clientId;
    @Value("${oauth2.client.secret:324012c49eaaa60a6dc1147f166427757b4502968b94e07d3da1cbc8f931deec}")
    private String clientSecret;
    @Value("${oauth2.client.callback.url:http://localhost:9000/callback}")
    private String callbackUrl;

    private static final String CURRENT_USER = "CurrentUser";
    private static final String AUTHORIZATION_KEY = "Authorization";
    private Map<String, User> userStore = new HashMap<>();
    private RestTemplate restTemplate = new RestTemplate();

    @GetMapping({"/main","/"})
    @ResponseBody
    public String main() {
        User user = (User) RequestContextHolder.getRequestAttributes().getAttribute(CURRENT_USER, RequestAttributes.SCOPE_SESSION);
        return "<html><body>hi:" + user.username + " This is Main</body></html>";
    }

    /**
     * 授权后redirect url
     * @param code 用于获取accessToken，只能使用一次
     */
    @GetMapping("/callback")
    public String callback(@RequestParam(value = "code", required = false) String code,
                           HttpServletRequest request, HttpServletResponse response) {
        String referer = request.getParameter("referer");
        String accessToken = getAccessToken(code, buildCallbackUrl(referer));
        User user = getUser(accessToken);

        String uuid = UUID.randomUUID().toString();
        userStore.put(uuid, user);
        //set cookie
        response.addCookie(new Cookie(AUTHORIZATION_KEY, uuid));
        return "redirect:" + referer;
    }

    private String buildCallbackUrl(String referer) {
        return callbackUrl + "?referer=" + referer;
    }

    private User getUser(String accessToken) {
        return restTemplate.getForObject(gitlabServerUrl + "/api/v4/user?access_token=" + accessToken, User.class);
    }

    /**
     * 通过code去gitlab获取accessToken
     * @param code grant ticket 只能使用一次
     * @param redirectUri 回调地址，必须与授权时参数一致
     */
    private String getAccessToken(String code, String redirectUri) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("code", code);
        params.add("redirect_uri", redirectUri);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(params, headers);

        ResponseEntity<JSONAccessTokenResponse> response =
                restTemplate.exchange(gitlabServerUrl + "/oauth/token",
                        HttpMethod.POST,
                        entity,
                        JSONAccessTokenResponse.class);
        return Objects.requireNonNull(response.getBody()).access_token;
    }

    @Configuration
    class WebConfig implements WebMvcConfigurer {
        @Override
        public void addInterceptors(InterceptorRegistry registry) {
            registry.addInterceptor(
                    new HandlerInterceptorAdapter() {
                        @Override
                        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
                            Optional<String> authorizationKeyOp = Arrays.stream(request.getCookies())
                                    .filter(it->it.getName().equals(AUTHORIZATION_KEY))
                                    .map(Cookie::getValue)
                                    .findAny();
                            if (authorizationKeyOp.isPresent()) {
                                // 授权信息存在，获取user信息放入session
                                RequestContextHolder.getRequestAttributes().setAttribute(CURRENT_USER, userStore.get(authorizationKeyOp.get()), RequestAttributes.SCOPE_SESSION);
                                return super.preHandle(request, response, handler);
                            } else {
                                // 授权信息不存在，去gitlab进行验证
                                String referer = request.getRequestURL().toString();
                                String redirectUri = URLEncoder.encode(buildCallbackUrl(referer), "utf-8");
                                String gitlabAuthUrl = gitlabServerUrl + "/oauth/authorize?response_type=code&redirect_uri=" + redirectUri + "&client_id=" + clientId;
                                logger.info("gitlabAuthUrl:{}", gitlabAuthUrl);
                                response.sendRedirect(gitlabAuthUrl);
                                return false;
                            }
                        }
                    })
                    .addPathPatterns("/main", "/test");
        }
    }

    static class JSONAccessTokenResponse implements Serializable {
        public String access_token;
    }

    static class User implements Serializable {
        public String name;
        public String username;
    }
}