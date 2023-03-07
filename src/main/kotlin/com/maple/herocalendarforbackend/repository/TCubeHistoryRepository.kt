package com.maple.herocalendarforbackend.repository

import com.maple.herocalendarforbackend.code.MagicVariables.MAX_SEARCH_LIMIT
import com.maple.herocalendarforbackend.entity.ICubeCountBatch
import com.maple.herocalendarforbackend.entity.TCubeHistory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Suppress("LongParameterList", "MaxLineLength", "TooManyFunctions")
@Repository
interface TCubeHistoryRepository : JpaRepository<TCubeHistory, ByteArray> {

    @Query(
        "select *\n" +
                "from t_cube_history h\n" +
                "where h.user_id = :userId\n" +
                "order by h.created_at desc\n" +
                "limit $MAX_SEARCH_LIMIT",
        nativeQuery = true
    )
    fun findHistoryOrderByCreatedAt(
        @Param("userId") userId: String,
    ): List<TCubeHistory>

    @Query(
        "select *\n" +
                "from t_cube_history h\n" +
                "where if(:item != '', h.target_item = :item, true)\n" +
                "and if(:cube != '', h.cube_type = :cube, true)\n" +
                "and if(:option1 != '', if(\n" +
                "\th.cube_type != '에디셔널 큐브',\n" +
                "\th.after_option1 = :option1,\n" +
                "\th.after_additional_option1 = :option1\n" +
                "), true)\n" +
                "and if(:option2 != '', if(\n" +
                "\th.cube_type != '에디셔널 큐브',\n" +
                "\th.after_option2 = :option2,\n" +
                "\th.after_additional_option2 = :option2\n" +
                "), true)\n" +
                "and if(:option3 != '', if(\n" +
                "\th.cube_type != '에디셔널 큐브',\n" +
                "\th.after_option3 = :option3,\n" +
                "\th.after_additional_option3 = :option3\n" +
                "), true)\n" +
                "and if(:optionValue1 != 0, if(\n" +
                "\th.cube_type != '에디셔널 큐브',\n" +
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
                "\th.cube_type != '에디셔널 큐브',\n" +
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
                "\th.cube_type != '에디셔널 큐브',\n" +
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
                "limit $MAX_SEARCH_LIMIT",
        nativeQuery = true
    )
    fun findHistoryByCondition(
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
        "delete from t_cube_history h where h.user_id = :userId",
        nativeQuery = true
    )
    @Modifying
    @Transactional
    fun deleteByAccount(
        @Param("userId") userId: String,
    )

    @Query(
        "delete from t_cube_history h\n" +
                "where date(h.created_at) < :beforeDeleteDate",
        nativeQuery = true
    )
    @Modifying
    @Transactional
    fun deleteByCreatedAt(
        @Param("beforeDeleteDate") beforeDeleteDate: LocalDate
    )

    @Query(
        "select \n" +
                "\tuser_id as userId,\n" +
                "\tdate(created_at) as createdAt,\n" +
                "\tpotential_option_grade as potentialOptionGrade,\n" +
                "\tadditional_potential_option_grade as additionalPotentialOptionGrade,\n" +
                "\tcube_type as cubeType,\n" +
                "\ttarget_item as targetItem,\n" +
                "\tcount(cube_type) as count,\n" +
                "\tsum(item_upgrade) as itemUpgradeCount\n" +
                "from t_cube_history\n" +
                "where user_id in :userIds\n" +
                "group by \n" +
                "\tuser_id,\n" +
                "\tdate(created_at),\n" +
                "\ttarget_item, \n" +
                "\tcube_type, \n" +
                "\tpotential_option_grade,\n" +
                "\tadditional_potential_option_grade",
        nativeQuery = true
    )
    fun findCubeCountForBatch(
        @Param("userIds") userIds: List<String>
    ): List<ICubeCountBatch>
}
