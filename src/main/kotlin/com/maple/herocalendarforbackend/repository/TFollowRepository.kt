package com.maple.herocalendarforbackend.repository

import com.maple.herocalendarforbackend.entity.TFollow
import com.maple.herocalendarforbackend.entity.IProfile
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface TFollowRepository : JpaRepository<TFollow, TFollow.Key> {

    @Query(
        "select \n" +
                "   u.id as id,\n" +
                "   u.account_id as accountId,\n" +
                "   u.nick_name as nickName,\n" +
                "   u.avatar_img as avatarImg,\n" +
                "   u.world as world,\n" +
                "   u.job as job,\n" +
                "   u.job_detail as jobDetail,\n" +
                "   u.is_public as isPublic,\n" +
                "   u.created_at as createdAt," +
                "   u.updated_at as updatedAt,\n" +
                "   (\n" +
                "       select if(count(*) > 0, true, false)\n" +
                "       from t_follow f2\n" +
                "       where f2.requester_id = u.id " +
                "       and f2.status != 'ACCEPTED'\n" +
                "   ) as heFollowMe,\n" +
                "   true as iFollowHim\n" +
                "from t_follow f\n" +
                "inner join t_user u\n" +
                "on f.respondent_id = u.id\n" +
                "where f.requester_id = :userId",
        nativeQuery = true
    )
    fun findFollowByUserId(
        @Param("userId") userId: String
    ): List<IProfile>

    @Query(
        "select \n" +
                "   u.id as id,\n" +
                "   u.account_id as accountId,\n" +
                "   u.nick_name as nickName,\n" +
                "   u.avatar_img as avatarImg,\n" +
                "   u.world as world,\n" +
                "   u.job as job,\n" +
                "   u.job_detail as jobDetail,\n" +
                "   u.is_public as isPublic,\n" +
                "   u.created_at as createdAt,\n" +
                "   u.updated_at as updatedAt,\n" +
                "   (\n" +
                "       select if(count(*) > 0, true, false)\n" +
                "       from t_follow f2\n" +
                "       where f2.respondent_id = u.id \n" +
                "       and f2.status != 'ACCEPTED'\n" +
                "   ) as iFollowHim,\n" +
                "   true as heFollowMe\n" +
                "from t_follow f\n" +
                "inner join t_user u\n" +
                "on f.requester_id = u.id\n" +
                "where f.respondent_id = :userId",
        nativeQuery = true
    )
    fun findFollowerByUserId(
        @Param("userId") userId: String
    ): List<IProfile>

    @Query(
        "select *\n" +
                "from t_follow f\n" +
                "where f.requester_id = :requester\n" +
                "and f.respondent_id = :respondent",
        nativeQuery = true
    )
    fun findById(
        @Param("requester") requester: String,
        @Param("respondent") respondent: String,
    ): TFollow?

    @Query(
        "delete from t_follow f\n" +
                "where f.requester_id = :requester\n" +
                "and f.respondent_id = :respondent",
        nativeQuery = true
    )
    @Modifying
    fun deleteById(
        @Param("requester") requester: String,
        @Param("respondent") respondent: String,
    )

    @Query(
        "delete from t_follow f\n" +
                "where f.requester_id = :userId\n" +
                "or f.respondent_id = :userId",
        nativeQuery = true
    )
    @Modifying
    fun deleteByAccountRemove(
        @Param("userId") userId: String
    )
}
