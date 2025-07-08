package com.tusur.teacherhelper.domain.model

import kotlinx.datetime.LocalDate

data class Topic(
    val id: Int,
    val type: TopicType,
    val name: Name,
    val deadline: Deadline? = null,
    val isCancelled: Boolean = false
) {
    init {
        assert(type.name == name.typeName)
    }

    data class Name(
        val typeName: String,
        val shortTypeName: String,
        val addText: String?,
        val ordinal: Int?,
        val date: LocalDate?
    )
}

val Topic.isDeadlineOwner get() = deadline?.owningTopicId == id