package com.maple.heroforbackend.service

import com.maple.heroforbackend.entity.TEmailToken
import com.maple.heroforbackend.entity.TUser
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
    @Value("\${owner-change-request-mail.path}")
    private val ownerChangeRequestMailPath: String,
    @Value("\${auth-mail.path}")
    private val authMailPath: String,
    @Value("\${add-friend.path}")
    private val addFriendMailPath: String,
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

    @Async
    fun sendOwnerChangeRequestEmail(scheduleId: Long, to: String) {
        val message = mailSender.createMimeMessage()
        message.addRecipients(Message.RecipientType.TO, to)
        message.subject = "이벤트 소유자 변경 요청"

        var msg = EmailSendService::class.java.getResource(ownerChangeRequestMailPath)?.readText()
        msg = msg?.replace(
            "\${ACCEPT_LINK}",
            "http://localhost:8080/user/calendar/schedule/${scheduleId}/owner-change/accept"
        )
        msg = msg?.replace(
            "\${REFUSE_LINK}",
            "http://localhost:8080/user/calendar/schedule/${scheduleId}/owner-change/refuse"
        )

        message.setText(msg, "utf-8", "html")
        message.setFrom(InternetAddress(adminId, personal))

        mailSender.send(message)
    }

    @Async
    fun sendFriendRequestEmail(requester: TUser, to: String) {
        val message = mailSender.createMimeMessage()
        message.addRecipients(Message.RecipientType.TO, to)
        message.subject = "친구 요청"

        var msg = EmailSendService::class.java.getResource(addFriendMailPath)?.readText()
        msg = msg?.replace(
            "\${REQUESTER}",
            requester.nickName
        )
        msg = msg?.replace(
            "\${ACCEPT_LINK}",
            "http://localhost:8080/user/friend/accept?from=${requester.id}"
        )
        msg = msg?.replace(
            "\${REFUSE_LINK}",
            "http://localhost:8080/user/friend/refuse?from=${requester.id}"
        )

        message.setText(msg, "utf-8", "html")
        message.setFrom(InternetAddress(adminId, personal))

        mailSender.send(message)
    }
}
