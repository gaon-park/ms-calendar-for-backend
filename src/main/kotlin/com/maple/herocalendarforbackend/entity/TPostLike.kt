package com.maple.herocalendarforbackend.entity

import jakarta.persistence.Embeddable
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "t_post_like")
data class TPostLike(
    @EmbeddedId
    val likeKey: LikeKey
) {
    companion object {
        @Embeddable
        data class LikeKey(
            @ManyToOne
            @JoinColumn(name = "post_id")
            val post: TPost,
            @ManyToOne
            @JoinColumn(name = "user_id")
            val user: TUser
        )
    }
}
