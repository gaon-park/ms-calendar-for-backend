package com.maple.herocalendarforbackend.entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "t_post_image")
data class TPostImage(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    val postId: Long,
    val path: String,
) {
    companion object {
        fun convert(postId: Long, path: String) = TPostImage(
            postId = postId,
            path = path
        )

        fun convert(postId: Long, paths: List<String>) = paths.map {
            convert(postId, it)
        }
    }
}
