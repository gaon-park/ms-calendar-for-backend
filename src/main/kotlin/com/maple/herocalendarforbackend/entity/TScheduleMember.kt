package com.maple.herocalendarforbackend.entity

import com.maple.herocalendarforbackend.code.AcceptedStatus
import jakarta.persistence.Embeddable
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "t_schedule_member")
data class TScheduleMember(
    @EmbeddedId
    val groupKey: GroupKey,
    @Enumerated(value = EnumType.STRING)
    val acceptedStatus: AcceptedStatus,
) {
    companion object {
        fun initConvert(
            user: TUser, group: TScheduleMemberGroup, acceptedStatus: AcceptedStatus
        ) = TScheduleMember(
            groupKey = GroupKey(group, user),
            acceptedStatus = acceptedStatus,
        )
    }

    @Embeddable
    data class GroupKey(
        @ManyToOne
        @JoinColumn(name = "group_id")
        val group: TScheduleMemberGroup,
        @ManyToOne
        @JoinColumn(name = "user_id")
        val user: TUser
    )
}
