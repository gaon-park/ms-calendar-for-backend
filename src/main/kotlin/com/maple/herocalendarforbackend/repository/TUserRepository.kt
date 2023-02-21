package com.maple.herocalendarforbackend.repository

import com.maple.herocalendarforbackend.entity.IProfile
import com.maple.herocalendarforbackend.entity.TUser
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Suppress("LongParameterList", "TooManyFunctions")
@Repository
interface TUserRepository : JpaRepository<TUser, String> {
    fun findByEmail(email: String): TUser?
    fun findByIdIn(ids: List<String>): List<TUser>

    @Query(
        "select *\n" +
                "from t_user u\n" +
                "where u.id = :userId\n" +
                "and u.role = 'ADMIN'",
        nativeQuery = true
    )
    fun findAdminByUserId(
        @Param("userId") userId: String
    ): TUser?

    @Query(
        "select \n" +
                "    u.id as id,\n" +
                "    u.account_id as accountId,\n" +
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
                "    (\n" +
                "        select if(count(*) > 0, \n" +
                "           if(f1.status = 'ACCEPTED', 'FOLLOW', 'WAITING')\n" +
                "           , null)\n" +
                "        from t_follow f1\n" +
                "        where f1.requester_id = :id\n" +
                "        and f1.respondent_id = u.id\n" +
                "    ) as iFollowHim,\n" +
                "    (\n" +
                "        select if(count(*) > 0, \n" +
                "           if(f2.status = 'ACCEPTED', 'FOLLOW', 'WAITING')\n" +
                "           , null)\n" +
                "        from t_follow f2\n" +
                "        where f2.requester_id = u.id\n" +
                "        and f2.respondent_id = :id\n" +
                "    ) as heFollowMe\n" +
                "from t_user u\n" +
                "where u.id = :id\n",
        nativeQuery = true
    )
    fun findByIdToIProfile(
        @Param("id") id: String
    ): IProfile?

    @Query(
        "select \n" +
                "    u.id as id,\n" +
                "    u.account_id as accountId,\n" +
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
                "    (\n" +
                "        select if(count(*) > 0, \n" +
                "           if(f1.status = 'ACCEPTED', 'FOLLOW', 'WAITING')\n" +
                "           , null)\n" +
                "        from t_follow f1\n" +
                "        where f1.requester_id = :userId\n" +
                "        and f1.respondent_id = u.id\n" +
                "    ) as iFollowHim,\n" +
                "    (\n" +
                "        select if(count(*) > 0, \n" +
                "           if(f2.status = 'ACCEPTED', 'FOLLOW', 'WAITING')\n" +
                "           , null)\n" +
                "        from t_follow f2\n" +
                "        where f2.requester_id = u.id\n" +
                "        and f2.respondent_id = :userId\n" +
                "    ) as heFollowMe\n" +
                "from t_user u\n" +
                "where if(:keyword != ''," +
                "   u.account_id like :keyword or u.nick_name like :keyword," +
                "   u.account_id is not null)\n" +
                "and if(:world != '', u.world = :world, u.world is not null)\n" +
                "and if(:job != '', u.job = :job, u.job is not null)\n" +
                "and if(:jobDetail != '', u.job_detail = :jobDetail, u.job_detail is not null)\n",
        nativeQuery = true
    )
    fun findByConditionAndUserId(
        @Param("keyword") keyword: String,
        @Param("world") world: String,
        @Param("job") job: String,
        @Param("jobDetail") jobDetail: String,
        @Param("userId") loginUserId: String,
    ): List<IProfile>

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
                "from t_user u1\n" +
                "where u1.id in :ids\n" +
                "and u1.is_public = true\n" +
                "union\n" +
                "select *\n" +
                "from t_user u2\n" +
                "where u2.id in (\n" +
                "   select f.requester_id\n" +
                "   from t_follow f\n" +
                "   where f.respondent_id = :userId\n" +
                "   and status = \"ACCEPTED\"\n" +
                ")",
        nativeQuery = true
    )
    fun findPublicOrFollower(
        @Param("ids") ids: List<String>, @Param("userId") userId: String
    ): List<TUser>

    fun findByAccountId(accountId: String): TUser?

    @Query(
        "select \n" +
                "    u.id as id,\n" +
                "    u.account_id as accountId,\n" +
                "    u.nick_name as nickName,\n" +
                "    u.avatar_img as avatarImg,\n" +
                "    u.world as world,\n" +
                "    u.job as job,\n" +
                "    u.job_detail as jobDetail,\n" +
                "    u.is_public as isPublic,\n" +
                "    u.created_at as createdAt,\n" +
                "    u.updated_at as updatedAt,\n" +
                "    u.notification_flg as notificationFlg,\n" +
                "    u.role as role\n," +
                "    (\n" +
                "        select if(count(*) > 0, \n" +
                "           if(f1.status = 'ACCEPTED', 'FOLLOW', 'WAITING')\n" +
                "           , null)\n" +
                "        from t_follow f1\n" +
                "        where f1.requester_id = :loginUserId\n" +
                "        and f1.respondent_id = u.id\n" +
                "    ) as iFollowHim,\n" +
                "    (\n" +
                "        select if(count(*) > 0, \n" +
                "           if(f2.status = 'ACCEPTED', 'FOLLOW', 'WAITING')\n" +
                "           , null)\n" +
                "        from t_follow f2\n" +
                "        where f2.requester_id = u.id\n" +
                "        and f2.respondent_id = :loginUserId\n" +
                "    ) as heFollowMe\n" +
                "from t_user u\n" +
                "where u.account_id = :accountId\n",
        nativeQuery = true
    )
    fun findByAccountIdToIProfile(
        @Param("accountId") accountId: String,
        @Param("loginUserId") loginUserId: String
    ): IProfile?

    @Query(
        "select u.id\n" +
                "from t_user u\n" +
                "where u.id in :searchUserIds\n" +
                "and (\n" +
                "u.is_public = true\n" +
                "or \n" +
                "(\n" +
                "\tselect count(*) > 0\n" +
                "\tfrom t_follow f\n" +
                "\twhere f.respondent_id = u.id\n" +
                "\tand f.requester_id = :loginUserId\n" +
                ")\n" +
                ")",
        nativeQuery = true
    )
    fun findTargetUserForScheduleSearch(
        @Param("searchUserIds") searchUserIds: List<String>,
        @Param("loginUserId") loginUserId: String
    ): List<String>

    @Query(
        "select *\n" +
                "from t_user u\n" +
                "where (u.is_public = true\n" +
                "or (" +
                "   select count(*) > 0\n" +
                "   from t_follow f\n" +
                "   where f.requester_id = :loginUserId\n" +
                "   and f.respondent_id = u.id\n" +
                "   and f.status = 'ACCEPTED'\n" +
                ") or u.id = :loginUserId\n" +
                ")\n" +
                "and if(:keyword != ''," +
                "   u.account_id like :keyword or u.nick_name like :keyword," +
                "   u.account_id is not null)",
        nativeQuery = true
    )
    fun findUserListForScheduleSearch(
        @Param("keyword") keyword: String,
        @Param("loginUserId") loginUserId: String
    ): List<TUser>
}
