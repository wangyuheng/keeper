package com.github.wangyuheng.keeper.core.client

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.mail.javamail.JavaMailSenderImpl
import org.springframework.mail.javamail.MimeMessageHelper


interface EmailClient {
    fun send(subject: String, content: String, sender: String, receivers: List<String>)
}

class EmailClientTemplate : EmailClient {

    @Autowired
    private lateinit var mailSender: JavaMailSenderImpl

    override fun send(subject: String, content: String, sender: String, receivers: List<String>) {
        val mimeMessage = mailSender.createMimeMessage()
        val mimeMessageHelper = MimeMessageHelper(mimeMessage)
        mimeMessageHelper.setTo(receivers.toTypedArray())
        mimeMessageHelper.setFrom(sender)
        mimeMessageHelper.setSubject(subject)
        mimeMessageHelper.setText(content, true)
        mailSender.send(mimeMessage)
    }

}

class AliyunEmailClient : EmailClient {

    override fun send(subject: String, content: String, sender: String, receivers: List<String>) {
        TODO("Not yet implemented")
    }

}