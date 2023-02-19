package com.maple.herocalendarforbackend.dto.response

import com.maple.herocalendarforbackend.entity.TPost
import com.maple.herocalendarforbackend.entity.TUser
import lombok.Builder

@Builder
data class PostResponse(
    val postId: Long?,
    val postImages: List<String>,
    val postLikeUsers: List<SimpleUserResponse>,
    val userId: String?,
    val userAccountId: String,
    val userNickName: String,
    val userAvatarImg: String?,
) {
    companion object {
        fun convert(post: TPost, postImages: List<String>, postLikeUsers: List<TUser>) = PostResponse(
            postId = post.id,
            postImages = postImages,
            postLikeUsers = postLikeUsers.map {
                SimpleUserResponse.convert(it)
            },
            userId = post.user.id,
            userAccountId = post.user.accountId,
            userNickName = post.user.nickName,
            userAvatarImg = post.user.avatarImg
        )
    }
}
