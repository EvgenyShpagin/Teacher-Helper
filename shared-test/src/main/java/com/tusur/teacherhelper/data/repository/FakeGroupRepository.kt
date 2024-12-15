package com.tusur.teacherhelper.data.repository

import com.tusur.teacherhelper.domain.model.Group
import com.tusur.teacherhelper.domain.repository.GroupRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet
import javax.inject.Inject

class FakeGroupRepository @Inject constructor() : GroupRepository {

    private val groups = MutableStateFlow(emptyList<Group>())

    // Test property for isAssociatedToAnySubject(Int).
    // If true, isAssociatedToAnySubject(Int) returns true, otherwise otherwise
    var areAllGroupsAssociatedWithSomeSubject = true

    // Test property for hasStudents(Int).
    // If true, hasStudents(Int) returns true, otherwise otherwise
    var areAllGroupsHaveStudents = true

    override fun getAll(): Flow<List<Group>> {
        return groups
    }

    override suspend fun getById(id: Int): Group {
        return groups.value.find { group -> group.id == id }!!
    }

    override suspend fun exists(number: String): Boolean {
        return groups.value.any { group -> group.number == number }
    }

    override suspend fun add(group: Group): Int {
        val newId = if (groups.value.isNotEmpty()) {
            groups.value.maxOf { group -> group.id } + 1
        } else {
            1
        }
        groups.updateAndGet { groups -> groups + group.copy(id = newId) }
        return newId
    }

    override suspend fun deleteWithStudents(groupId: Int) {
        val groupToDelete = groups.value.find { group -> group.id == groupId }!!
        groups.update { groups -> groups - groupToDelete }
    }

    override suspend fun isAssociatedToAnySubject(groupId: Int): Boolean {
        return areAllGroupsAssociatedWithSomeSubject
    }

    override suspend fun search(query: String): List<Group> {
        return groups.value.filter { group -> query in group.number }
    }

    override suspend fun hasStudents(groupId: Int): Boolean {
        return areAllGroupsHaveStudents
    }
}