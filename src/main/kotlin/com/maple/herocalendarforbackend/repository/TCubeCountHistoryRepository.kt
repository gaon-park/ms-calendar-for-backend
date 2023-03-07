package com.maple.herocalendarforbackend.repository

import com.maple.herocalendarforbackend.entity.ICubeTypeCount
import com.maple.herocalendarforbackend.entity.ITargetItemCount
import com.maple.herocalendarforbackend.entity.IWholeRecordDashboardDate
import com.maple.herocalendarforbackend.entity.IWholeRecordDashboardMonth
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
        "select \n" +
                "\ttarget_item as targetItem, \n" +
                "\tsum(count) as count\n" +
                "from t_cube_count_history\n" +
                "where if(:loginUserId != '', user_id = :loginUserId, user_id != '')\n" +
                "and created_at >= :start and created_at <= :end\n" +
                "and cube_type = :cubeType\n" +
                "group by target_item\n" +
                "order by sum(count) desc\n" +
                "limit 5",
        nativeQuery = true
    )
    fun findTopFiveItem(
        @Param("loginUserId") loginUserId: String,
        @Param("cubeType") cubeType: String,
        @Param("start") start: LocalDate,
        @Param("end") end: LocalDate
    ): List<ITargetItemCount>

    @Query(
        "select target_item\n" +
                "from t_cube_count_history\n" +
                "where user_id = :loginUserId\n" +
                "group by target_item\n" +
                "order by target_item",
        nativeQuery = true
    )
    fun findItemFilterOption(
        @Param("loginUserId") loginUserId: String,
    ): List<String>

    @Query(
        "select target_item\n" +
                "from t_cube_count_history\n" +
                "where user_id = :loginUserId\n" +
                "and created_at >= :canSearchStartDate\n" +
                "group by target_item\n" +
                "order by target_item",
        nativeQuery = true
    )
    fun findItemFilterOptionByCanSearchStartDate(
        @Param("loginUserId") loginUserId: String,
        @Param("canSearchStartDate") canSearchStartDate: LocalDate
    ): List<String>

    @Query(
        "select \n" +
                "\tyear(created_at) as year,\n" +
                "\tmonth(created_at) as month,\n" +
                "\tcube_type as cubeType,\n" +
                "\tsum(count) as count\n" +
                "from t_cube_count_history\n" +
                "where (cube_type = '블랙 큐브' or cube_type = '레드 큐브' or cube_type = '에디셔널 큐브')\n" +
                "and date(created_at) >= :start and date(created_at) <= :end\n" +
                "and if(:loginUserId != '', user_id = :loginUserId, user_id != '')\n" +
                "group by cube_type, year(created_at), month(created_at)\n" +
                "order by year(created_at), month(created_at)",
        nativeQuery = true
    )
    fun findWholeRecordDashboardMonth(
        @Param("loginUserId") loginUserId: String,
        @Param("start") start: LocalDate,
        @Param("end") end: LocalDate
    ): List<IWholeRecordDashboardMonth>

    @Query(
        "select \n" +
                "\tdate(created_at) as date,\n" +
                "\tcube_type as cubeType,\n" +
                "\tsum(count) as count\n" +
                "from t_cube_count_history\n" +
                "where (\n" +
                "        cube_type = '블랙 큐브' \n" +
                "        or cube_type = '레드 큐브' \n" +
                "        or cube_type = '에디셔널 큐브' \n" +
                "        )\n" +
                "        and (\n" +
                "        \tdate(created_at) >= :start and date(created_at) <= :end \n" +
                "        )\n" +
                "and if(:loginUserId != '', user_id = :loginUserId, user_id != '')\n" +
                "group by cube_type, date(created_at)\n" +
                "order by date(created_at)",
        nativeQuery = true
    )
    fun findWholeRecordDashboardDate(
        @Param("loginUserId") loginUserId: String,
        @Param("start") start: LocalDate,
        @Param("end") end: LocalDate
    ): List<IWholeRecordDashboardDate>

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
