package com.maple.herocalendarforbackend.repository

import com.maple.herocalendarforbackend.entity.TCubeHistory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Suppress("LongParameterList", "MaxLineLength")
@Repository
interface TCubeHistoryRepository : JpaRepository<TCubeHistory, Long> {

    @Query(
        "select h.target_item\n" +
                "from t_cube_history h\n" +
                "group by h.target_item\n" +
                "order by count(*) desc\n" +
                "limit 10",
        nativeQuery = true
    )
    fun findItemFilterOptionCommon(): List<String>

    @Query(
        "select *\n" +
                "from t_cube_history h\n" +
                "where h.target_item in :items\n" +
                "order by h.created_at desc\n" +
                "limit 1000",
        nativeQuery = true
    )
    fun findHistoryByItemIn(
        @Param("items") items: List<String>
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
}
