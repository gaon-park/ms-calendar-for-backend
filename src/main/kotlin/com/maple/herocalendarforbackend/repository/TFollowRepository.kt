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
        "select\n" +
                "\tu.id as id,\n" +
                "\tu.account_id as accountId,\n" +
                "    u.nick_name as nickName,\n" +
                "    u.avatar_img as avatarImg,\n" +
                "    u.world as world,\n" +
                "    u.job as job,\n" +
                "    u.job_detail as jobDetail,\n" +
                "    u.is_public as isPublic,\n" +
                "    u.created_at as createdAt,\n" +
                "    u.updated_at as updatedAt," +
                "    u.notification_flg as notificationFlg," +
                "    u.role as role,\n" +
                "\t(\n" +
                "\t\tselect if(count(*) > 0, \n" +
                "\t\t\tif(f1.status = 'ACCEPTED', 'FOLLOW', 'WAITING')\n" +
                "\t\t\t, null)\n" +
                "\t\tfrom t_follow f1\n" +
                "\t\twhere f1.requester_id = :userId\n" +
                "\t\tand f1.respondent_id = u.id\n" +
                "\t) as iamFollowHim,\n" +
                "\t(\n" +
                "\t\tselect if(count(*) > 0,\n" +
                "\t\t\tif(f2.status = 'ACCEPTED', 'FOLLOW', 'WAITING')\n" +
                "\t\t\t,null)\n" +
                "\t\tfrom t_follow f2\n" +
                "\t\twhere f2.requester_id = u.id\n" +
                "\t\tand f2.respondent_id = :userId\n" +
                "\t) as heFollowMe\n" +
                "from t_follow f\n" +
                "inner join t_user u\n" +
                "on u.id = f.respondent_id\n" +
                "where f.requester_id = :userId\n" +
                "order by f.status = 'WAITING' desc",
        nativeQuery = true
    )
    fun findAllStatusFollowByUserId(
        @Param("userId") userId: String
    ): List<IProfile>

    @Query(
        "select\n" +
                "\tu.id as id,\n" +
                "\tu.account_id as accountId,\n" +
                "    u.nick_name as nickName,\n" +
                "    u.avatar_img as avatarImg,\n" +
                "    u.world as world,\n" +
                "    u.job as job,\n" +
                "    u.job_detail as jobDetail,\n" +
                "    u.is_public as isPublic,\n" +
                "    u.created_at as createdAt,\n" +
                "    u.updated_at as updatedAt,\n" +
                "    u.notification_flg as notificationFlg,\n" +
                "    u.role as role,\n" +
                "\t(\n" +
                "\t\tselect if(count(*) > 0, \n" +
                "\t\t\tif(f1.status = 'ACCEPTED', 'FOLLOW', 'WAITING')\n" +
                "\t\t\t, null)\n" +
                "\t\tfrom t_follow f1\n" +
                "\t\twhere f1.requester_id = :userId\n" +
                "\t\tand f1.respondent_id = u.id\n" +
                "\t) as iamFollowHim,\n" +
                "\t(\n" +
                "\t\tselect if(count(*) > 0,\n" +
                "\t\t\tif(f2.status = 'ACCEPTED', 'FOLLOW', 'WAITING')\n" +
                "\t\t\t,null)\n" +
                "\t\tfrom t_follow f2\n" +
                "\t\twhere f2.requester_id = u.id\n" +
                "\t\tand f2.respondent_id = :userId\n" +
                "\t) as heFollowMe\n" +
                "from t_follow f\n" +
                "inner join t_user u\n" +
                "on u.id = f.requester_id\n" +
                "where f.respondent_id = :userId\n" +
                "order by f.status = 'WAITING' desc",
        nativeQuery = true
    )
    fun findAllStatusFollowerByUserId(
        @Param("userId") userId: String
    ): List<IProfile>

    @Query(
        "select count(*)\n" +
                "from t_follow f\n" +
                "where f.requester_id = :userId\n" +
                "and f.status = 'ACCEPTED'",
        nativeQuery = true
    )
    fun findCountJustAcceptedFollowByUserId(
        @Param("userId") userId: String
    ): Long

    @Query(
        "select count(*)\n" +
                "from t_follow f\n" +
                "where f.respondent_id = :userId\n" +
                "and f.status = 'ACCEPTED'",
        nativeQuery = true
    )
    fun findCountJustAcceptedFollowerByUserId(
        @Param("userId") userId: String
    ): Long

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
