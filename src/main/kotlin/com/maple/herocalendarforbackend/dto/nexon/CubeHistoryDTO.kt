package com.maple.herocalendarforbackend.dto.nexon

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import lombok.Builder
import java.time.LocalDateTime

@Builder
@JsonSerialize
data class CubeHistoryDTO(
    @JsonProperty("id")
    val id: String,
    @JsonProperty("character_name")
    val characterName: String,
    @JsonProperty("create_date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    val createDate: LocalDateTime,
    @JsonProperty("cube_type")
    val cubeType: String,
    @JsonProperty("item_upgrade_result")
    val itemUpgradeResult: String,
    @JsonProperty("miracle_time_flag")
    val miracleTimeFlag: String,
    @JsonProperty("item_equip_part")
    val itemEquipPart: String,
    @JsonProperty("item_level")
    val itemLevel: Int,
    @JsonProperty("target_item")
    val targetItem: String,
    @JsonProperty("potential_option_grade")
    val potentialOptionGrade: String,
    @JsonProperty("additional_potential_option_grade")
    val additionalPotentialOptionGrade: String,
    @JsonProperty("before_potential_options")
    val beforePotentialOptions: List<CubeResultOptionDTO>,
    @JsonProperty("before_additional_potential_options")
    val beforeAdditionalPotentialOptions: List<CubeResultOptionDTO>,
    @JsonProperty("after_potential_options")
    val afterPotentialOptions: List<CubeResultOptionDTO>,
    @JsonProperty("after_additional_potential_options")
    val afterAdditionalPotentialOptions: List<CubeResultOptionDTO>
)
