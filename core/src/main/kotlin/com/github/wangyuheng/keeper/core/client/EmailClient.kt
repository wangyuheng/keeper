package com.github.wangyuheng.keeper.core.client

import org.springframework.stereotype.Component


interface EmailClient {

    fun send(subject: String, content: String, receivers: String): Boolean

}

@Component
class AliyunEmailClient : EmailClient {
    override fun send(subject: String, content: String, receivers: String): Boolean {
        println("mock send a email to $receivers by aliyun")
        return true
    }
}