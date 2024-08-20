package com.tusur.teacherhelper.domain.model

data class Student(
    val id: Int,
    val name: Name
) {
    data class Name(
        val lastName: String,
        val firstName: String,
        val middleName: String
    ) {
        val full = if (middleName == "") {
            "$lastName $firstName"
        } else {
            "$lastName $firstName $middleName"
        }

        override fun toString(): String {
            return full
        }
    }
}