package com.tusur.teacherhelper.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "deadline",
    foreignKeys = [ForeignKey(Topic::class, ["id"], ["creator_topic_id"])]
)
data class Deadline(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    @ColumnInfo(name = "date_millis")
    val dateMillis: Long,
    @ColumnInfo(name = "creator_topic_id")
    val creatorTopicId: Int
)