package com.maple.herocalendarforbackend.service

import com.maple.herocalendarforbackend.code.BaseResponseCode
import com.maple.herocalendarforbackend.code.FriendshipAcceptStatusCode
import com.maple.herocalendarforbackend.code.FriendshipStatusCode
import com.maple.herocalendarforbackend.code.MagicVariables.SEARCH_DEFAULT_LIMIT
import com.maple.herocalendarforbackend.dto.request.PageInfo
import com.maple.herocalendarforbackend.dto.request.friend.FriendRequest
import com.maple.herocalendarforbackend.dto.response.UserResponse
import com.maple.herocalendarforbackend.entity.TFriendship
import com.maple.herocalendarforbackend.entity.TUser
import com.maple.herocalendarforbackend.exception.BaseException
import com.maple.herocalendarforbackend.repository.TFriendshipRepository
import com.maple.herocalendarforbackend.repository.TUserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class FriendshipService(
    private val tUserRepository: TUserRepository,
    private val tFriendshipRepository: TFriendshipRepository,
) {
    fun friendCheck(userId: String, searchUserId: String): Boolean {
        return tFriendshipRepository.friendCheck(
            userId, searchUserId
        ) != null
    }

    fun findByUserIdAndOppIn(loginUserId: String, users: List<TUser>): List<UserResponse> {
        return convertToUserResponse(
            loginUserId,
            tFriendshipRepository.findByUserIdAndOppIn(loginUserId, users.mapNotNull { it.id })
        )
    }

    private fun findUserById(id: String): TUser {
        tUserRepository.findById(id).let {
            if (it.isEmpty) {
                throw BaseException(BaseResponseCode.USER_NOT_FOUND)
            }
            return it.get()
        }
    }

    /**
     * friend Request
     */
    @Transactional
    fun request(loginUserId: String, request: FriendRequest) {
        val respondentId = request.personalKey
        if (loginUserId == respondentId) {
            throw BaseException(BaseResponseCode.BAD_REQUEST)
        }

        val relation = tFriendshipRepository.findByKey(
            requesterId = loginUserId,
            respondentId = request.personalKey
        )

        when {
            // send request
            relation.isEmpty() ->
                tFriendshipRepository.save(
                    TFriendship.generateSaveModel(findUserById(loginUserId), findUserById(respondentId))
                )
            relation.size == 1 && relation[0].key.respondent.id == loginUserId &&
                    relation[0].status != FriendshipAcceptStatusCode.ACCEPTED ->
                tFriendshipRepository.save(relation[0].copy(status = FriendshipAcceptStatusCode.ACCEPTED))
        }
    }

    /**
     * delete friend
     */
    @Transactional
    fun delete(requesterId: String, personalKey: String) {
        tFriendshipRepository.deleteByUserIds(requesterId, personalKey)
    }

    /**
     * accept friend
     */
    @Transactional
    fun acceptRequest(opponentId: String, loginUserId: String) {
        tFriendshipRepository.updateStatus(
            statusValue = FriendshipAcceptStatusCode.ACCEPTED.toString(),
            requesterId = opponentId,
            respondentId = loginUserId
        )
    }

    /**
     * find friends (all status)
     */
    fun findAllStatusFriends(loginUserId: String, pageInfo: PageInfo): List<UserResponse> {
        return convertToUserResponse(
            loginUserId, tFriendshipRepository.findByUserIdPagination(
                loginUserId,
                pageInfo.limit ?: SEARCH_DEFAULT_LIMIT,
                pageInfo.offset ?: SEARCH_DEFAULT_LIMIT
            )
        )
    }

    private fun convertToUserResponse(loginUserId: String, res: List<TFriendship>): List<UserResponse> {
        return res.map {
            val user: TUser
            val status: FriendshipStatusCode
            if (it.key.requester.id == loginUserId) {
                user = it.key.respondent
                status = when (it.status) {
                    FriendshipAcceptStatusCode.WAITING -> FriendshipStatusCode.WAITING_OPP_RESPONSE
                    else -> FriendshipStatusCode.FRIEND
                }
            } else {
                user = it.key.requester
                status = when (it.status) {
                    FriendshipAcceptStatusCode.WAITING -> FriendshipStatusCode.WAITING_MY_RESPONSE
                    else -> FriendshipStatusCode.FRIEND
                }
            }

            UserResponse.convert(user, status)
        }
    }
}
