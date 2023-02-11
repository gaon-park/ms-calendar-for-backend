package com.maple.herocalendarforbackend.repository

import com.maple.herocalendarforbackend.entity.TFriendship
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface TFriendshipRepository : JpaRepository<TFriendship, TFriendship.Key> {

    @Query(
        "select *\n" +
                "from t_friendship f\n" +
                "where f.requester_id=:userId\n" +
                "or f.respondent_id=:userId\n" +
                "limit :offset, :limit",
        nativeQuery = true
    )
    fun findByUserIdPagination(
        @Param("userId") userId: String,
        @Param("limit") limit: Int,
        @Param("offset") offset: Int,
    ): List<TFriendship>

    @Query(
        "select *\n" +
                "from t_friendship f\n" +
                "where (f.requester_id=:userId and f.respondent_id in :searchIds)\n" +
                "or (f.respondent_id=:userId and f.requester_id in :searchIds)",
        nativeQuery = true
    )
    fun findByUserIdAndOppIn(
        @Param("userId") userId: String,
        @Param("searchIds") searchIds: List<String>
    ): List<TFriendship>

    @Query(
        "select *\n" +
                "from t_friendship f\n" +
                "where (f.requester_id=:requesterId and f.respondent_id=:respondentId)\n" +
                "or (f.requester_id=:respondentId and f.respondent_id=:requesterId)",
        nativeQuery = true
    )
    fun findByKey(
        @Param("requesterId") requesterId: String,
        @Param("respondentId") respondentId: String,
    ): List<TFriendship>

    @Query(
        "delete from t_friendship f\n" +
                "where (f.requester_id=:requesterId and f.respondent_id=:respondentId)\n" +
                "or (f.requester_id=:respondentId and f.respondent_id=:requesterId)",
        nativeQuery = true
    )
    @Modifying
    fun deleteByUserIds(
        @Param("requesterId") requesterId: String,
        @Param("respondentId") respondentId: String,
    )

    @Query(
        "update t_friendship f\n" +
                "set f.accepted_status = :statusValue\n" +
                "where f.requester_id = :requester\n" +
                "and f.respondent_id = :respondent\n" +
                "and f.accepted_status != :statusValue",
        nativeQuery = true
    )
    @Modifying
    fun updateStatus(
        @Param("statusValue") statusValue: String,
        @Param("requester") requesterId: String,
        @Param("respondent") respondentId: String,
    )

    @Query(
        "select *\n" +
                "from t_friendship f\n" +
                "where f.respondent_id = :userId\n" +
                "and f.accepted_status = \"WAITING\"",
        nativeQuery = true
    )
    fun findWaitingRequest(@Param("userId") userId: String): List<TFriendship>

    @Query(
        "select *\n" +
                "from t_friendship f\n" +
                "where (" +
                "(f.requester_id= :requester and f.respondent_id= :respondent) \n" +
                "or (f.requester_id= :respondent and f.respondent_id= :requester) \n" +
                ") and accept_status=\"ACCEPTED\"",
        nativeQuery = true
    )
    fun friendCheck(
        @Param("requester") requester: String,
        @Param("respondent") respondent: String
    ): TFriendship?

    /**
     * 회원 탈퇴시
     */
    @Query(
        "delete from t_friendship f\n" +
                "where f.requester_id=:userId\n" +
                "or f.respondent_id=:userId",
        nativeQuery = true
    )
    @Modifying
    fun deleteByDeletedAccount(@Param("userId") userId: String)
}
