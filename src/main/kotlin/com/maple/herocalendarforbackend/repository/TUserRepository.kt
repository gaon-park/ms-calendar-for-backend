package com.maple.herocalendarforbackend.repository

import com.maple.herocalendarforbackend.code.MagicVariables.MAX_SEARCH_LIMIT
import com.maple.herocalendarforbackend.entity.TUser
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Suppress("LongParameterList")
@Repository
interface TUserRepository : JpaRepository<TUser, String> {
    fun findByEmail(email: String): TUser?

    @Query(
        "select *\n" +
                "from t_user u\n" +
                "where if(:keyword != ''," +
                "   u.account_id like :keyword or u.nick_name like :keyword," +
                "   u.account_id is not null)\n" +
                "and if(:world != '', u.world = :world, u.world is not null)\n" +
                "and if(:job != '', u.job = :job, u.job is not null)\n" +
                "and if(:jobDetail != '', u.job_detail = :jobDetail, u.job_detail is not null)\n" +
                "limit $MAX_SEARCH_LIMIT",
        nativeQuery = true
    )
    fun findByCondition(
        @Param("keyword") keyword: String,
        @Param("world") world: String,
        @Param("job") job: String,
        @Param("jobDetail") jobDetail: String,
    ): List<TUser>

    @Query(
        "select count(*)\n" +
                "from t_user u\n" +
                "where if(:keyword != ''," +
                "   u.account_id like :keyword or u.nick_name like :keyword," +
                "   u.account_id is not null)\n" +
                "and if(:world != '', u.world = :world, u.world is not null)\n" +
                "and if(:job != '', u.job = :job, u.job is not null)\n" +
                "and if(:jobDetail != '', u.job_detail = :jobDetail, u.job_detail is not null)",
        nativeQuery = true
    )
    fun findByConditionCount(
        @Param("keyword") keyword: String,
        @Param("world") world: String,
        @Param("job") job: String,
        @Param("jobDetail") jobDetail: String,
    ): Long

    @Query(
        "select *\n" +
                "from t_user u\n" +
                "order by updated_at desc\n" +
                "limit $MAX_SEARCH_LIMIT",
        nativeQuery = true
    )
    fun findByUpdatedAt(): List<TUser>

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
