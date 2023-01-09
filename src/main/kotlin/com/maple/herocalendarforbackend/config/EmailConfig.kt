package com.maple.herocalendarforbackend.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.JavaMailSenderImpl
import java.util.Properties

@Configuration
@PropertySource("classpath:email.properties")
@Suppress("LongParameterList")
class EmailConfig(
    @Value("\${mail.smtp.port}")
    private val port: Int,
    @Value("\${mail.smtp.socketFactory.port}")
    private val socketPort: Int,
    @Value("\${mail.smtp.auth}")
    private val auth: Boolean,
    @Value("\${mail.smtp.starttls.enable}")
    private val starttls: Boolean,
    @Value("\${mail.smtp.starttls.required}")
    private val starttlsRequired: Boolean,
    @Value("\${mail.smtp.socketFactory.fallback}")
    private val fallback: Boolean,
    @Value("\${AdminMail.id}")
    private val id: String,
    @Value("\${AdminMail.password}")
    private val password: String,
) {

    @Bean
    fun mailService(): JavaMailSender {
        val mailSender = JavaMailSenderImpl()
        mailSender.host = "smtp.gmail.com"
        mailSender.username = id
        mailSender.password = password
        mailSender.port = port
        mailSender.javaMailProperties = getMailProperties()
        mailSender.defaultEncoding = "UTF-8"
        return mailSender
    }

    private fun getMailProperties(): Properties {
        val pt = Properties()
        pt["mail.smtp.socketFactory.port"] = socketPort
        pt["mail.smtp.auth"] = auth
        pt["mail.smtp.starttls.enable"] = starttls
        pt["mail.smtp.starttls.required"] = starttlsRequired
        pt["mail.smtp.socketFactory.fallback"] = fallback
        pt["mail.smtp.socketFactory.class"] = "javax.net.ssl.SSLSocketFactory"
        return pt
    }
}
