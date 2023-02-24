package com.maple.herocalendarforbackend.repository

import com.maple.herocalendarforbackend.entity.ICubeTypeCount
import com.maple.herocalendarforbackend.entity.IWholeRecordDashboardDate
import com.maple.herocalendarforbackend.entity.IWholeRecordDashboardMonth
import com.maple.herocalendarforbackend.entity.TCubeHistory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Suppress("LongParameterList", "MaxLineLength", "TooManyFunctions")
@Repository
interface TCubeHistoryRepository : JpaRepository<TCubeHistory, Long> {

    @Query(
        "select id\n" +
                "from t_cube_history\n" +
                "where id in :ids",
        nativeQuery = true
    )
    fun findIdsByIdIn(
        @Param("ids") ids: List<String>
    ): List<String>

    @Query(
        "select \n" +
                "\th.cube_type as cubeType,\n" +
                "\tcount(h.cube_type) as count\n" +
                "from t_cube_history h\n" +
                "group by h.cube_type",
        nativeQuery = true
    )
    fun findCubeTypeCountCommon(): List<ICubeTypeCount>

    @Query(
        "select \n" +
                "\th.cube_type as cubeType,\n" +
                "\tcount(h.cube_type) as count\n" +
                "from t_cube_history h\n" +
                "where h.user_id = :userId\n" +
                "group by h.cube_type",
        nativeQuery = true
    )
    fun findCubeTypeCountPersonal(
        @Param("userId") userId: String,
    ): List<ICubeTypeCount>

    @Query(
        "select h.target_item\n" +
                "from t_cube_history h\n" +
                "group by h.target_item\n" +
                "order by count(*) desc",
        nativeQuery = true
    )
    fun findItemFilterOptionCommon(): List<String>

    @Query(
        "select h.target_item\n" +
                "from t_cube_history h\n" +
                "where h.user_id = :userId\n" +
                "group by h.target_item\n" +
                "order by count(*) desc",
        nativeQuery = true
    )
    fun findItemFilterOptionPersonal(
        @Param("userId") userId: String,
    ): List<String>

    @Query(
        "select *\n" +
                "from t_cube_history h\n" +
                "order by h.created_at desc\n" +
                "limit 1000",
        nativeQuery = true
    )
    fun findHistoryByPersonalOrderByCreatedAt(): List<TCubeHistory>

    @Query(
        "select *\n" +
                "from t_cube_history h\n" +
                "where h.user_id = :userId\n" +
                "order by h.created_at desc\n" +
                "limit 1000",
        nativeQuery = true
    )
    fun findHistoryByPersonalOrderByCreatedAt(
        @Param("userId") userId: String,
    ): List<TCubeHistory>

    @Query(
        "select *\n" +
                "from t_cube_history h\n" +
                "where if(:item != '', h.target_item = :item, true)\n" +
                "and if(:cube != '', h.cube_type = :cube, true)\n" +
                "and if(:option1 != '', if(\n" +
                "\th.cube_type != 'ADDITIONAL',\n" +
                "\th.after_option1 = :option1,\n" +
                "\th.after_additional_option1 = :option1\n" +
                "), true)\n" +
                "and if(:option2 != '', if(\n" +
                "\th.cube_type != 'ADDITIONAL',\n" +
                "\th.after_option2 = :option2,\n" +
                "\th.after_additional_option2 = :option2\n" +
                "), true)\n" +
                "and if(:option3 != '', if(\n" +
                "\th.cube_type != 'ADDITIONAL',\n" +
                "\th.after_option3 = :option3,\n" +
                "\th.after_additional_option3 = :option3\n" +
                "), true)\n" +
                "and if(:optionValue1 != 0, if(\n" +
                "\th.cube_type != 'ADDITIONAL',\n" +
                "\tif(\n" +
                "\t\th.after_option_value1 regexp '[\$\\%]',\n" +
                "\t\tcast(regexp_replace(h.after_option_value1, '[\$\\%]', '') as signed) >= :optionValue1,\n" +
                "\t\tfalse\n" +
                "\t),\n" +
                "\tif (\n" +
                "\t\th.after_additional_option_value1 regexp '[\$\\%]',\n" +
                "\t\tcast(regexp_replace(h.after_additional_option_value1, '[\$\\%]', '') as signed) >= :optionValue1,\n" +
                "\t\tfalse\n" +
                "\t)\n" +
                "), true)\n" +
                "and if(:optionValue2 != 0, if(\n" +
                "\th.cube_type != 'ADDITIONAL',\n" +
                "\tif(\n" +
                "\t\th.after_option_value2 regexp '[\$\\%]',\n" +
                "\t\tcast(regexp_replace(h.after_option_value2, '[\$\\%]', '') as signed) >= :optionValue2,\n" +
                "\t\tfalse\n" +
                "\t),\n" +
                "\tif (\n" +
                "\t\th.after_additional_option_value2 regexp '[\$\\%]',\n" +
                "\t\tcast(regexp_replace(h.after_additional_option_value2, '[\$\\%]', '') as signed) >= :optionValue2,\n" +
                "\t\tfalse\n" +
                "\t)\n" +
                "), true)\n" +
                "and if(:optionValue3 != 0, if(\n" +
                "\th.cube_type != 'ADDITIONAL',\n" +
                "\tif(\n" +
                "\t\th.after_option_value3 regexp '[\$\\%]',\n" +
                "\t\tcast(regexp_replace(h.after_option_value3, '[\$\\%]', '') as signed) >= :optionValue3,\n" +
                "\t\tfalse\n" +
                "\t),\n" +
                "\tif (\n" +
                "\t\th.after_additional_option_value3 regexp '[\$\\%]',\n" +
                "\t\tcast(regexp_replace(h.after_additional_option_value3, '[\$\\%]', '') as signed) >= :optionValue3,\n" +
                "\t\tfalse\n" +
                "\t)\n" +
                "), true)\n" +
                "order by h.created_at desc\n" +
                "limit 1000",
        nativeQuery = true
    )
    fun findHistoryByCondition(
        @Param("item") item: String,
        @Param("cube") cube: String,
        @Param("option1") option1: String,
        @Param("option2") option2: String,
        @Param("option3") option3: String,
        @Param("optionValue1") optionValue1: Int,
        @Param("optionValue2") optionValue2: Int,
        @Param("optionValue3") optionValue3: Int,
    ): List<TCubeHistory>

    @Query(
        "select *\n" +
                "from t_cube_history h\n" +
                "where if(:item != '', h.target_item = :item, true)\n" +
                "and if(:cube != '', h.cube_type = :cube, true)\n" +
                "and if(:option1 != '', if(\n" +
                "\th.cube_type != 'ADDITIONAL',\n" +
                "\th.after_option1 = :option1,\n" +
                "\th.after_additional_option1 = :option1\n" +
                "), true)\n" +
                "and if(:option2 != '', if(\n" +
                "\th.cube_type != 'ADDITIONAL',\n" +
                "\th.after_option2 = :option2,\n" +
                "\th.after_additional_option2 = :option2\n" +
                "), true)\n" +
                "and if(:option3 != '', if(\n" +
                "\th.cube_type != 'ADDITIONAL',\n" +
                "\th.after_option3 = :option3,\n" +
                "\th.after_additional_option3 = :option3\n" +
                "), true)\n" +
                "and if(:optionValue1 != 0, if(\n" +
                "\th.cube_type != 'ADDITIONAL',\n" +
                "\tif(\n" +
                "\t\th.after_option_value1 regexp '[\$\\%]',\n" +
                "\t\tcast(regexp_replace(h.after_option_value1, '[\$\\%]', '') as signed) >= :optionValue1,\n" +
                "\t\tfalse\n" +
                "\t),\n" +
                "\tif (\n" +
                "\t\th.after_additional_option_value1 regexp '[\$\\%]',\n" +
                "\t\tcast(regexp_replace(h.after_additional_option_value1, '[\$\\%]', '') as signed) >= :optionValue1,\n" +
                "\t\tfalse\n" +
                "\t)\n" +
                "), true)\n" +
                "and if(:optionValue2 != 0, if(\n" +
                "\th.cube_type != 'ADDITIONAL',\n" +
                "\tif(\n" +
                "\t\th.after_option_value2 regexp '[\$\\%]',\n" +
                "\t\tcast(regexp_replace(h.after_option_value2, '[\$\\%]', '') as signed) >= :optionValue2,\n" +
                "\t\tfalse\n" +
                "\t),\n" +
                "\tif (\n" +
                "\t\th.after_additional_option_value2 regexp '[\$\\%]',\n" +
                "\t\tcast(regexp_replace(h.after_additional_option_value2, '[\$\\%]', '') as signed) >= :optionValue2,\n" +
                "\t\tfalse\n" +
                "\t)\n" +
                "), true)\n" +
                "and if(:optionValue3 != 0, if(\n" +
                "\th.cube_type != 'ADDITIONAL',\n" +
                "\tif(\n" +
                "\t\th.after_option_value3 regexp '[\$\\%]',\n" +
                "\t\tcast(regexp_replace(h.after_option_value3, '[\$\\%]', '') as signed) >= :optionValue3,\n" +
                "\t\tfalse\n" +
                "\t),\n" +
                "\tif (\n" +
                "\t\th.after_additional_option_value3 regexp '[\$\\%]',\n" +
                "\t\tcast(regexp_replace(h.after_additional_option_value3, '[\$\\%]', '') as signed) >= :optionValue3,\n" +
                "\t\tfalse\n" +
                "\t)\n" +
                "), true)\n" +
                "and h.user_id = :userId\n" +
                "order by h.created_at desc\n" +
                "limit 1000",
        nativeQuery = true
    )
    fun findHistoryByConditionPersonal(
        @Param("userId") userId: String,
        @Param("item") item: String,
        @Param("cube") cube: String,
        @Param("option1") option1: String,
        @Param("option2") option2: String,
        @Param("option3") option3: String,
        @Param("optionValue1") optionValue1: Int,
        @Param("optionValue2") optionValue2: Int,
        @Param("optionValue3") optionValue3: Int,
    ): List<TCubeHistory>

    @Query(
        "select \n" +
                "\tyear(h.created_at) as year,\n" +
                "\tmonth(h.created_at) as month, \n" +
                "\th.cube_type as cubeType, \n" +
                "\tcount(h.cube_type) as count\n" +
                "from t_cube_history h\n" +
                "where (h.cube_type = 'RED' or h.cube_type = 'BLACK' or h.cube_type = 'ADDITIONAL')\n" +
                "and h.created_at >= :start and h.created_at <= :end\n" +
                "group by h.cube_type, year(h.created_at), month(h.created_at)",
        nativeQuery = true
    )
    fun findWholeRecordDashboardMonth(
        @Param("start") start: LocalDate,
        @Param("end") end: LocalDate
    ): List<IWholeRecordDashboardMonth>

    @Query(
        "select \n" +
                "\tdate(h.created_at) as date,\n" +
                "\th.cube_type as cubeType, \n" +
                "\tcount(h.cube_type) as count\n" +
                "from t_cube_history h\n" +
                "where (h.cube_type = 'RED' or h.cube_type = 'BLACK' or h.cube_type = 'ADDITIONAL')\n" +
                "and h.created_at >= :start and h.created_at <= :end\n" +
                "group by cube_type, date(h.created_at)",
        nativeQuery = true
    )
    fun findWholeRecordDashboardDate(
        @Param("start") start: LocalDate,
        @Param("end") end: LocalDate
    ): List<IWholeRecordDashboardDate>

    @Query(
        "select \n" +
                "\tyear(h.created_at) as year,\n" +
                "\tmonth(h.created_at) as month, \n" +
                "\th.cube_type as cubeType, \n" +
                "\tcount(h.cube_type) as count\n" +
                "from t_cube_history h\n" +
                "where (h.cube_type = 'RED' or h.cube_type = 'BLACK' or h.cube_type = 'ADDITIONAL')\n" +
                "and h.created_at >= :start and h.created_at <= :end\n" +
                "and h.user_id = :userId\n" +
                "group by cube_type, year(h.created_at), month(h.created_at)",
        nativeQuery = true
    )
    fun findWholeRecordDashboardMonthPersonal(
        @Param("userId") userId: String,
        @Param("start") start: LocalDate,
        @Param("end") end: LocalDate
    ): List<IWholeRecordDashboardMonth>

    @Query(
        "select \n" +
                "\tdate(h.created_at) as date,\n" +
                "\th.cube_type as cubeType, \n" +
                "\tcount(h.cube_type) as count\n" +
                "from t_cube_history h\n" +
                "where (h.cube_type = 'RED' or h.cube_type = 'BLACK' or h.cube_type = 'ADDITIONAL')\n" +
                "and h.created_at >= :start and h.created_at <= :end\n" +
                "and h.user_id = :userId\n" +
                "group by cube_type, date(h.created_at)",
        nativeQuery = true
    )
    fun findWholeRecordDashboardDatePersonal(
        @Param("userId") userId: String,
        @Param("start") start: LocalDate,
        @Param("end") end: LocalDate
    ): List<IWholeRecordDashboardDate>

    @Query(
        "delete from t_cube_history h  where h.user_id = :userId",
        nativeQuery = true
    )
    @Modifying
    fun deleteByAccount(
        @Param("userId") userId: String,
    )
}
