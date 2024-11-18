package com.tusur.teacherhelper.domain.usecase

import com.tusur.teacherhelper.domain.model.TopicType
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.Test

class CanTopicTypeBeReplacedUseCaseTest {

    val useCase = CanTopicTypeBeReplacedUseCase()

    @Test
    fun replacementIsAllowed_whenTypesAreTheSame() = runTest {
        val topicType = createTopicType(false, false, false, false, false)
        assertTrue(useCase(topicType, topicType))
    }

    @Test
    fun replacementIsAllowed_whenAssessmentAdded() {
        val fromType = createTopicType(isAssessmentAcceptable = false, true, false, false, true)
        val toType = createTopicType(isAssessmentAcceptable = true, true, false, false, true)
        assertTrue(useCase(fromType, toType))
    }

    @Test
    fun replacementIsProhibited_whenAssessmentRefused() {
        val fromType = createTopicType(isAssessmentAcceptable = true, true, false, false, true)
        val toType = createTopicType(isAssessmentAcceptable = false, true, false, false, true)
        assertFalse(useCase(fromType, toType))
    }

    @Test
    fun replacementIsAllowed_whenAttendanceAdded() {
        val fromType = createTopicType(false, isAttendanceAcceptable = false, false, true, true)
        val toType = createTopicType(false, isAttendanceAcceptable = true, false, true, true)
        assertTrue(useCase(fromType, toType))
    }

    @Test
    fun replacementIsProhibited_whenAttendanceRefused() {
        val fromType = createTopicType(
            false,
            isAttendanceAcceptable = true,
            isAttendanceForOneClassOnly = false,
            true,
            true
        )
        val toType = createTopicType(
            false,
            isAttendanceAcceptable = false,
            isAttendanceForOneClassOnly = false,
            true, true
        )
        assertFalse(useCase(fromType, toType))
    }

    @Test
    fun replacementIsAllowed_whenAttendanceIncreased() {
        val fromType = createTopicType(
            false,
            isAttendanceAcceptable = true,
            isAttendanceForOneClassOnly = true,
            true,
            true
        )
        val toType = createTopicType(
            false,
            isAttendanceAcceptable = true,
            isAttendanceForOneClassOnly = false,
            true,
            true
        )
        assertTrue(useCase(fromType, toType))
    }

    @Test
    fun replacementIsProhibited_whenAttendanceDecreased() {
        val fromType = createTopicType(
            false,
            isAttendanceAcceptable = true,
            isAttendanceForOneClassOnly = false,
            true,
            true
        )
        val toType = createTopicType(
            false,
            isAttendanceAcceptable = true,
            isAttendanceForOneClassOnly = true,
            true,
            true
        )
        assertFalse(useCase(fromType, toType))
    }

    @Test
    fun replacementIsAllowed_whenGradeAdded() {
        val fromType = createTopicType(false, true, false, isGradeAcceptable = false, true)
        val toType = createTopicType(false, true, false, isGradeAcceptable = true, true)
        assertTrue(useCase(fromType, toType))
    }

    @Test
    fun replacementIsProhibited_whenGradeRefused() {
        val fromType = createTopicType(false, true, false, isGradeAcceptable = true, true)
        val toType = createTopicType(false, true, false, isGradeAcceptable = false, true)
        assertFalse(useCase(fromType, toType))
    }

    @Test
    fun replacementIsProhibited_whenProgressAdded() {
        val fromType = createTopicType(false, true, false, true, isProgressAcceptable = false)
        val toType = createTopicType(false, true, false, true, isProgressAcceptable = true)
        assertTrue(useCase(fromType, toType))
    }

    @Test
    fun replacementIsProhibited_whenProgressRefused() {
        val fromType = createTopicType(false, true, false, true, isProgressAcceptable = true)
        val toType = createTopicType(false, true, false, true, isProgressAcceptable = false)
        assertFalse(useCase(fromType, toType))
    }

    private fun createTopicType(
        isAssessmentAcceptable: Boolean,
        isAttendanceAcceptable: Boolean,
        isAttendanceForOneClassOnly: Boolean,
        isGradeAcceptable: Boolean,
        isProgressAcceptable: Boolean
    ) = TopicType(
        id = 1,
        name = "fake name",
        shortName = "",
        canDeadlineBeSpecified = false,
        isAssessmentAcceptable = isAssessmentAcceptable,
        isAttendanceAcceptable = isAttendanceAcceptable,
        isAttendanceForOneClassOnly = isAttendanceForOneClassOnly,
        isGradeAcceptable = isGradeAcceptable,
        isProgressAcceptable = isProgressAcceptable
    )
}