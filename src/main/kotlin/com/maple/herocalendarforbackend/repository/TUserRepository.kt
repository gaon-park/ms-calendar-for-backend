package com.maple.herocalendarforbackend.repository

import com.maple.herocalendarforbackend.code.MagicVariables.MAX_VALUE_OF_MEMBERS
import com.maple.herocalendarforbackend.entity.TUser
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface TUserRepository : JpaRepository<TUser, String> {
    fun findByEmail(email: String): TUser?

    @Query(
        "select *\n" +
                "from t_user u\n" +
                "where u.id != :loginUserId\n" +
                "and u.account_id like :accountId\n" +
                "limit $MAX_VALUE_OF_MEMBERS",
        nativeQuery = true
    )
    fun findByAccountIdLike(
        @Param("accountId") accountId: String,
        @Param("loginUserId") loginUserId: String
    ): List<TUser>

    @Query(
        "select *\n" +
                "from t_user u1\n" +
                "where u1.id in :ids\n" +
                "and u1.is_public = true\n" +
                "union\n" +
                "select *\n" +
                "from t_user u2\n" +
                "where u2.id in (\n" +
                "   select f.respondent_id\n" +
                "   from t_follow_relationship f\n" +
                "   where f.requester_id = :userId\n" +
                "   and accepted_status = \"ACCEPTED\"\n" +
                ")",
        nativeQuery = true
    )
    fun findPublicOrFollowing(
        @Param("ids") ids: List<String>, @Param("userId") userId: String
    ): List<TUser>

    fun findByAccountId(accountId: String): TUser?
}
