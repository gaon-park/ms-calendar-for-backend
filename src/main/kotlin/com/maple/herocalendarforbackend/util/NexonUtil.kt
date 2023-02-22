package com.maple.herocalendarforbackend.util

import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.maple.herocalendarforbackend.code.BaseResponseCode
import com.maple.herocalendarforbackend.dto.nexon.CubeHistoryResponseDTO
import com.maple.herocalendarforbackend.exception.BaseException
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class NexonUtil {
    private val url = "https://public.api.nexon.com/openapi/maplestory/v1/cube-use-results?"
    private val mapper = jsonMapper().findAndRegisterModules()
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
        val req = "${url}count=250&date=${date}"
        return urlReq(req, apiKey)
    }

    fun whileProcess(nextCursor: String, apiKey: String): CubeHistoryResponseDTO {
        val req = "${url}count=250&cursor=${nextCursor}"
        return urlReq(req, apiKey)
    }

    private fun urlReq(req: String, apiKey: String): CubeHistoryResponseDTO {
        try {
            val nUrl = URL(req)
            val conn = nUrl.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.useCaches = false
            conn.setRequestProperty("Authorization", apiKey)
            val reader = BufferedReader(InputStreamReader(conn.inputStream, "UTF-8"))
            return mapper.readValue(reader, CubeHistoryResponseDTO::class.java)
        } catch (_: java.lang.Exception) {
            throw BaseException(BaseResponseCode.DATA_ERROR)
        }
    }
}
