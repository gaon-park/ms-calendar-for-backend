package com.maple.heroforbackend.service

import com.maple.heroforbackend.entity.TEmailToken
import jakarta.mail.Message
import jakarta.mail.internet.InternetAddress
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.PropertySource
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service

@Service
@PropertySource("classpath:email.properties")
class EmailSendService(
    private val mailSender: JavaMailSender,
    @Value("\${auth-mail.path}")
    private val authMailPath: String,
    @Value("\${AdminMail.personal}")
    private val personal: String,
    @Value("\${AdminMail.id}")
    private val adminId: String,
) {

    /**
     * 인증 메일 전송
     */
    @Async
    fun sendAuthEmail(token: TEmailToken, to: String) {
        val message = mailSender.createMimeMessage()
        message.addRecipients(Message.RecipientType.TO, to)
        message.subject = "회원가입 이메일 인증"

        var msg = EmailSendService::class.java.getResource(authMailPath)?.readText()
        // todo 도메인 프로퍼티화
        msg = msg?.replace("\${LINK_WITH_TOKEN}", "http://localhost:8080/confirm-email?token=${token.id}")

        message.setText(msg, "utf-8", "html")
        message.setFrom(InternetAddress(adminId, personal))

        mailSender.send(message)
    }
}
