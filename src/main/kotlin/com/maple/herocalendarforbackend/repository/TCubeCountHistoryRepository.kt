package com.maple.herocalendarforbackend.repository

import com.maple.herocalendarforbackend.entity.ICubeTypeCount
import com.maple.herocalendarforbackend.entity.TCubeCountHistory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Suppress("MaxLineLength")
@Repository
interface TCubeCountHistoryRepository : JpaRepository<TCubeCountHistory, Long> {

    @Query(
        "select cube_type as cubeType,\n" +
                "sum(count) as count\n" +
                "from t_cube_count_history\n" +
                "where if(:loginUserId != '', user_id = :loginUserId, user_id != '')\n" +
                "group by cube_type",
        nativeQuery = true
    )
    fun findAllCubeCount(
        @Param("loginUserId") loginUserId: String
    ): List<ICubeTypeCount>

    @Query(
        "select \n" +
                "\tcube_type as cubeType,\n" +
                "\tsum(count) as count\n" +
                "from t_cube_count_history\n" +
                "where \n" +
                "if(cube_type != '에디셔널 큐브', potential_option_grade = :gradeKor, additional_potential_option_grade = :gradeKor)\n" +
                "and if(:loginUserId != '', user_id = :loginUserId, user_id != '')\n" +
                "and if(:item != '', target_item = :item, target_item != '')\n" +
                "and date(created_at) >= :start and date(created_at) <= :end\n" +
                "group by cube_type",
        nativeQuery = true
    )
    fun findStateGradeCount(
        @Param("loginUserId") loginUserId: String,
        @Param("item") item: String,
        @Param("start") start: LocalDate,
        @Param("end") end: LocalDate,
        @Param("gradeKor") gradeKor: String,
    ): List<ICubeTypeCount>

    @Query(
        "select \n" +
                "\tcube_type as cubeType,\n" +
                "\tsum(upgrade_count) as count\n" +
                "from t_cube_count_history\n" +
                "where \n" +
                "(upgrade_count > 0 and if(cube_type != '에디셔널 큐브', potential_option_grade = :gradeKor, additional_potential_option_grade = :gradeKor))\n" +
                "and if(:loginUserId != '', user_id = :loginUserId, user_id != '')\n" +
                "and if(:item != '', target_item = :item, target_item != '')\n" +
                "and date(created_at) >= :start and date(created_at) <= :end\n" +
                "group by cube_type",
        nativeQuery = true
    )
    fun findUpgradeGradeCount(
        @Param("loginUserId") loginUserId: String,
        @Param("item") item: String,
        @Param("start") start: LocalDate,
        @Param("end") end: LocalDate,
        @Param("gradeKor") gradeKor: String,
    ): List<ICubeTypeCount>
}
