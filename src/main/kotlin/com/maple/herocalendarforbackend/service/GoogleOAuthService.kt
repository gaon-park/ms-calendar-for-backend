package com.maple.herocalendarforbackend.service

import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.maple.herocalendarforbackend.code.BaseResponseCode
import com.maple.herocalendarforbackend.dto.oauth2.GoogleOAuthGetToken
import com.maple.herocalendarforbackend.dto.oauth2.GoogleOAuthTokenInfo
import com.maple.herocalendarforbackend.entity.TUser
import com.maple.herocalendarforbackend.exception.BaseException
import com.maple.herocalendarforbackend.properties.GoogleProperties
import com.maple.herocalendarforbackend.repository.TUserRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

@Service
class GoogleOAuthService(
    private val tUserRepository: TUserRepository,
    private val gp: GoogleProperties
) {

    private val logger: Logger = LoggerFactory.getLogger(GoogleOAuthService::class.java)

    fun process(code: String): String? {
        logger.info("start process!!")
        googleAccessToken(code).also {
            googleTokenInfo(it.idToken).let { info ->
                dataProcess(info.email)
                return info.email
            }
        }
    }

    private fun googleAccessToken(code: String): GoogleOAuthGetToken {
        // access token 발급
        val url = "https://oauth2.googleapis.com/token"
        val body = "code=$code" +
                "&client_id=${gp.clientId}" +
                "&client_secret=${gp.clientSecretKey}" +
                "&redirect_uri=${gp.redirectUrl}" +
                "&scope=${gp.scope}" +
                "&response_type=code" +
                "&grant_type=${gp.grantType}"

        try {
            val gUrl = URL(url)
            val conn = gUrl.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.doOutput = true
            conn.setRequestProperty("Content-Type", MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            conn.setRequestProperty("Content-Length", body.length.toString())
            conn.useCaches = false
            DataOutputStream(conn.outputStream).use { it.writeBytes(body) }
            logger.info("googleAccessToken: before read")
            val reader = BufferedReader(InputStreamReader(conn.inputStream, "UTF-8"))
            logger.info("googleAccessToken: after read")
            val json =jsonMapper().readValue(reader, GoogleOAuthGetToken::class.java)
            logger.info("json:$json")
            return json
        } catch (e: java.lang.Exception) {
            logger.error(e.toString())
            throw BaseException(BaseResponseCode.INVALID_TOKEN)
        }
    }

    private fun googleTokenInfo(idToken: String): GoogleOAuthTokenInfo {
        // 무결성 확인
        val url = "https://oauth2.googleapis.com/tokeninfo?" +
                "id_token=$idToken" +
                "&client_id=${gp.clientId}" +
                "&client_secret=${gp.clientSecretKey}"

        try {
            val gUrl = URL(url)
            val conn = gUrl.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.useCaches = false
            logger.info("googleTokenInfo: before read")
            val reader = BufferedReader(InputStreamReader(conn.inputStream, "UTF-8"))
            logger.info("googleTokenInfo: after read")
            val json = jsonMapper().readValue(reader, GoogleOAuthTokenInfo::class.java)
            logger.info("json:$json")
            return json
        } catch (e: java.lang.Exception) {
            logger.error(e.toString())
            throw BaseException(BaseResponseCode.INVALID_TOKEN)
        }
    }

    @Transactional
    private fun dataProcess(email: String) {
        val user = tUserRepository.findByEmail(email)
        if (user == null) {
            tUserRepository.save(TUser.generateOAuthSaveModel(email))
        }
    }
}
