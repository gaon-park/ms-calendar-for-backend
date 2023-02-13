package com.maple.herocalendarforbackend.service

import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.maple.herocalendarforbackend.code.BaseResponseCode
import com.maple.herocalendarforbackend.dto.oauth2.GoogleOAuthGetToken
import com.maple.herocalendarforbackend.dto.oauth2.GoogleOAuthTokenInfo
import com.maple.herocalendarforbackend.entity.TUser
import com.maple.herocalendarforbackend.exception.BaseException
import com.maple.herocalendarforbackend.properties.GoogleProperties
import com.maple.herocalendarforbackend.repository.TUserRepository
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

    fun process(code: String): String? {
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
            val reader = BufferedReader(InputStreamReader(conn.inputStream, "UTF-8"))
            return jsonMapper().readValue(reader, GoogleOAuthGetToken::class.java)
        } catch (_: java.lang.Exception) {
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
            val reader = BufferedReader(InputStreamReader(conn.inputStream, "UTF-8"))
            return jsonMapper().readValue(reader, GoogleOAuthTokenInfo::class.java)
        } catch (_: java.lang.Exception) {
            throw BaseException(BaseResponseCode.INVALID_TOKEN)
        }
    }

    @Transactional
    private fun dataProcess(email: String) {
        val user = tUserRepository.findByEmail(email)
        if (user == null) {
            tUserRepository.save(TUser.generateGoogleOAuthSaveModel(email))
        }
    }
}
