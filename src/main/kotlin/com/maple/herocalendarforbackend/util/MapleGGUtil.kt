package com.maple.herocalendarforbackend.util

import org.jsoup.Jsoup
import java.io.IOException


class MapleGGUtil {

    fun getAvatarImg(name: String): String? {
        val ggUrl = "https://maple.gg/u/$name"
        return try {
            val conn = Jsoup.connect(ggUrl)
            val html = conn.get()

            // 첫번째 이미지
            html.selectFirst(".character-image")?.attr("src")
        } catch (_: IOException) {
            null
        }
    }
}
