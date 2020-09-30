package com.github.wangyuheng.keeperoauthsample;

import com.github.wangyuheng.keeper.oauth.client.OauthUser;
import com.github.wangyuheng.keeper.oauth.conf.OauthConfigKt;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

@RestController
@SpringBootApplication
public class OauthApp {
    public static void main(String[] args) {
        SpringApplication.run(OauthApp.class, args);
    }

    @GetMapping({"/main", "/"})
    @ResponseBody
    public String main() {
        OauthUser user = (OauthUser) RequestContextHolder.getRequestAttributes().getAttribute(OauthConfigKt.CURRENT_USER, RequestAttributes.SCOPE_SESSION);
        if (null == user) {
            return "<html><body>hi: anonymous</body></html>";
        } else {
            return "<html><body>hi:" + user.getUsername() + " This is Main</body></html>";
        }
    }
}