package com.maple.herocalendarforbackend.util

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.maple.herocalendarforbackend.code.BaseResponseCode
import com.maple.herocalendarforbackend.dto.nexon.CubeHistoryResponseDTO
import com.maple.herocalendarforbackend.exception.BaseException
import org.slf4j.LoggerFactory
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class NexonUtil {
    private val url = "https://public.api.nexon.com/openapi/maplestory/v1/cube-use-results?"

    private val logger = LoggerFactory.getLogger(NexonUtil::class.java)

    private val mapper = JsonMapper.builder().addModule(JavaTimeModule()).build()
        .setSerializationInclusion(JsonInclude.Include.NON_NULL)

    fun isValidToken(apiKey: String): Boolean {
        val req = "${url}count=10&date=2022-11-25"
        return try {
            urlReq(req, apiKey)
            true
        } catch (_: java.lang.Exception) {
            false
        }
    }

    fun firstProcess(apiKey: String, date: String): CubeHistoryResponseDTO {
        val req = "${url}count=500&date=${date}"
        return urlReq(req, apiKey)
    }

    fun whileProcess(nextCursor: String, apiKey: String): CubeHistoryResponseDTO {
        val req = "${url}count=500&cursor=${nextCursor}"
        return urlReq(req, apiKey)
    }

    private fun urlReq(req: String, apiKey: String): CubeHistoryResponseDTO {
        try {
            logger.debug("$req 요청 준비")
            val nUrl = URL(req)
            val conn = nUrl.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.useCaches = false
            conn.setRequestProperty("Authorization", apiKey)
            val reader = BufferedReader(InputStreamReader(conn.inputStream, "UTF-8"))
            logger.debug("$req 응답 받았다!")
            val tmp = mapper.readValue(reader, CubeHistoryResponseDTO::class.java)
            logger.debug("$req JSON 맵핑 완료")
            return tmp
        } catch (e: java.lang.Exception) {
            logger.error("$req 에러났담..[apiKey: $apiKey]")
            logger.error(e.stackTraceToString())
            throw BaseException(BaseResponseCode.DATA_ERROR)
        }
    }
}
