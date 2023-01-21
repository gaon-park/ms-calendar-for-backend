package com.maple.herocalendarforbackend.repository

import com.maple.herocalendarforbackend.entity.TUser
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface TUserRepository : JpaRepository<TUser, String> {
    fun findByEmail(email: String): TUser?
    fun findByIdIn(ids: List<String>): List<TUser>

    @Query(
        "select *\n" +
                "from t_user t\n" +
                "where (substring_index(t.email, \"@\", 1) like %:value% or t.nick_name like %:value%)\n" +
                "and t.is_public= true",
        nativeQuery = true
    )
    fun findByEmailOrNickNameAndIsPublic(
        @Param("value") value: String,
    ): List<TUser>

    @Query(
        "select *\n" +
                "from t_user  t\n" +
                "where t.id in :ids \n" +
                "and t.is_public = true",
        nativeQuery = true
    )
    fun findPublicByIdIn(@Param("ids") ids: List<String>): List<TUser>

    @Query(
        "select * \n" +
                "from t_user u1 \n" +
                "where \n" +
                "  u1.id in :ids\n" +
                "  and u1.is_public = true \n" +
                "union \n" +
                "select * \n" +
                "from t_user u2 \n" +
                "where \n" +
                "  u2.id in (\n" +
                "    select \n" +
                "      if (\n" +
                "        f.requester_id = :userId, \n" +
                "        f.respondent_id, f.requester_id\n" +
                "      ) as id \n" +
                "    from t_friendship f \n" +
                "    where \n" +
                "      (\n" +
                "        f.requester_id = :userId \n" +
                "        and f.respondent_id in :ids\n" +
                "      ) \n" +
                "      or (\n" +
                "        f.requester_id in :ids\n" +
                "        and f.respondent_id = :userId\n" +
                "      )\n" +
                "  )\n",
        nativeQuery = true
    )
    fun findPublicOrFriendByIdIn(@Param("ids") ids: List<String>, @Param("userId") userId: String): List<TUser>
}
