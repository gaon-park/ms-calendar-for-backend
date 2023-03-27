package com.maple.herocalendarforbackend.repository

import com.maple.herocalendarforbackend.code.MagicVariables.MAX_SEARCH_LIMIT
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
                "    h.cube_type != '에디셔널 큐브',\n" +
                "    h.after_option1 = :option1,\n" +
                "    h.after_additional_option1 = :option1\n" +
                "), true)\n" +
                "and if(:option2 != '', if(\n" +
                "    h.cube_type != '에디셔널 큐브',\n" +
                "    h.after_option2 = :option2,\n" +
                "    h.after_additional_option2 = :option2\n" +
                "), true)\n" +
                "and if(:option3 != '', if(\n" +
                "    h.cube_type != '에디셔널 큐브',\n" +
                "    h.after_option3 = :option3,\n" +
                "    h.after_additional_option3 = :option3\n" +
                "), true)\n" +
                "and if(:optionValue1 != 0, if(\n" +
                "    h.cube_type != '에디셔널 큐브',\n" +
                "    if(\n" +
                "        h.after_option_value1 regexp '[\$\\%]',\n" +
                "        cast(regexp_replace(h.after_option_value1, '[\$\\%]', '') as signed) >= :optionValue1,\n" +
                "        false\n" +
                "    ),\n" +
                "    if (\n" +
                "        h.after_additional_option_value1 regexp '[\$\\%]',\n" +
                "        cast(regexp_replace(h.after_additional_option_value1, '[\$\\%]', '') as signed) >= :optionValue1,\n" +
                "        false\n" +
                "    )\n" +
                "), true)\n" +
                "and if(:optionValue2 != 0, if(\n" +
                "    h.cube_type != '에디셔널 큐브',\n" +
                "    if(\n" +
                "        h.after_option_value2 regexp '[\$\\%]',\n" +
                "        cast(regexp_replace(h.after_option_value2, '[\$\\%]', '') as signed) >= :optionValue2,\n" +
                "        false\n" +
                "    ),\n" +
                "    if (\n" +
                "        h.after_additional_option_value2 regexp '[\$\\%]',\n" +
                "        cast(regexp_replace(h.after_additional_option_value2, '[\$\\%]', '') as signed) >= :optionValue2,\n" +
                "        false\n" +
                "    )\n" +
                "), true)\n" +
                "and if(:optionValue3 != 0, if(\n" +
                "    h.cube_type != '에디셔널 큐브',\n" +
                "    if(\n" +
                "        h.after_option_value3 regexp '[\$\\%]',\n" +
                "        cast(regexp_replace(h.after_option_value3, '[\$\\%]', '') as signed) >= :optionValue3,\n" +
                "        false\n" +
                "    ),\n" +
                "    if (\n" +
                "        h.after_additional_option_value3 regexp '[\$\\%]',\n" +
                "        cast(regexp_replace(h.after_additional_option_value3, '[\$\\%]', '') as signed) >= :optionValue3,\n" +
                "        false\n" +
                "    )\n" +
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
        "select *\n" +
                "from t_cube_history\n" +
                "where \n" +
                "    if(:item != '', target_item = :item, true)\n" +
                "    and if(:cube != '', cube_type = :cube, true)\n" +
                "    and \n" +
                "        if (cube_type != '에디셔널 큐브',\n" +
                "            (if (:option1 != '', after_option1 = :option1 || (:option1 in ('STR', 'INT', 'DEX', 'LUK') && after_option1 = '올스탯'), after_option1 != '')\n" +
                "            and if (:option2 != '', after_option2 = :option2 || (:option2 in ('STR', 'INT', 'DEX', 'LUK') && after_option2 = '올스탯'), after_option2 != '')\n" +
                "            and if (:option3 != '', after_option3 = :option3 || (:option3 in ('STR', 'INT', 'DEX', 'LUK') && after_option3 = '올스탯'), after_option3 != ''))\n" +
                "            or(if (:option1 != '', after_option2 = :option1 || (:option1 in ('STR', 'INT', 'DEX', 'LUK') && after_option2 = '올스탯'), after_option2 != '')\n" +
                "            and if (:option2 != '', after_option3 = :option2 || (:option2 in ('STR', 'INT', 'DEX', 'LUK') && after_option3 = '올스탯'), after_option3 != '')\n" +
                "            and if (:option3 != '', after_option1 = :option3 || (:option3 in ('STR', 'INT', 'DEX', 'LUK') && after_option1 = '올스탯'), after_option1 != ''))\n" +
                "            or(if (:option1 != '', after_option3 = :option1 || (:option1 in ('STR', 'INT', 'DEX', 'LUK') && after_option3 = '올스탯'), after_option3 != '')\n" +
                "            and if (:option2 != '', after_option1 = :option2 || (:option2 in ('STR', 'INT', 'DEX', 'LUK') && after_option1 = '올스탯'), after_option1 != '')\n" +
                "            and if (:option3 != '', after_option2 = :option3 || (:option3 in ('STR', 'INT', 'DEX', 'LUK') && after_option2 = '올스탯'), after_option2 != '')),\n" +
                "            \n" +
                "            (if (:option1 != '', after_additional_option1 = :option1 || (:option1 in ('STR', 'INT', 'DEX', 'LUK') && after_additional_option1 = '올스탯'), after_additional_option1 != '')\n" +
                "            and if (:option2 != '', after_additional_option2 = :option2 || (:option2 in ('STR', 'INT', 'DEX', 'LUK') && after_additional_option2 = '올스탯'), after_additional_option2 != '')\n" +
                "            and if (:option3 != '', after_additional_option3 = :option3 || (:option3 in ('STR', 'INT', 'DEX', 'LUK') && after_additional_option3 = '올스탯'), after_additional_option3 != ''))\n" +
                "            or(if (:option1 != '', after_additional_option2 = :option1 || (:option1 in ('STR', 'INT', 'DEX', 'LUK') && after_additional_option2 = '올스탯'), after_additional_option2 != '')\n" +
                "            and if (:option2 != '', after_additional_option3 = :option2 || (:option2 in ('STR', 'INT', 'DEX', 'LUK') && after_additional_option3 = '올스탯'), after_additional_option3 != '')\n" +
                "            and if (:option3 != '', after_additional_option1 = :option3 || (:option3 in ('STR', 'INT', 'DEX', 'LUK') && after_additional_option1 = '올스탯'), after_additional_option1 != ''))\n" +
                "            or(if (:option1 != '', after_additional_option3 = :option1 || (:option1 in ('STR', 'INT', 'DEX', 'LUK') && after_additional_option3 = '올스탯'), after_additional_option3 != '')\n" +
                "            and if (:option2 != '', after_additional_option1 = :option2 || (:option2 in ('STR', 'INT', 'DEX', 'LUK') && after_additional_option1 = '올스탯'), after_additional_option1 != '')\n" +
                "            and if (:option3 != '', after_additional_option2 = :option3 || (:option3 in ('STR', 'INT', 'DEX', 'LUK') && after_additional_option2 = '올스탯'), after_additional_option2 != ''))\n" +
                "        )\n" +
                "        and user_id = :userId\n" +
                "order by created_at desc",
        nativeQuery = true
    )
    fun findHistoryByOption(
        @Param("userId") userId: String,
        @Param("item") item: String,
        @Param("cube") cube: String,
        @Param("option1") option1: String,
        @Param("option2") option2: String,
        @Param("option3") option3: String,
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
        "delete from t_cube_history h\n" +
                "where date(h.created_at) = :createdAt",
        nativeQuery = true
    )
    @Modifying
    @Transactional
    fun deleteByCreatedAtByBatch(
        @Param("createdAt") createdAt: LocalDate
    )
}
