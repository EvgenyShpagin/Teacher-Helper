package com.tusur.teacherhelper.domain.usecase

import com.tusur.teacherhelper.domain.model.Performance
import com.tusur.teacherhelper.domain.model.SumProgress
import com.tusur.teacherhelper.domain.model.Topic
import com.tusur.teacherhelper.domain.model.TopicType
import kotlinx.coroutines.flow.first
import javax.inject.Inject

private typealias TypeToProgress = Pair<TopicType, SumProgress<Float>>

class GetSubjectStudentSummaryPerformanceUseCase @Inject constructor(
    private val getTotalStudentPerformance: GetSubjectStudentPerformanceUseCase,
    private val getSuggestedProgressForGrade: GetSuggestedProgressForGradeUseCase,
    private val getSuggestedProgressForAssessment: GetSuggestedProgressForAssessmentUseCase
) {
    suspend operator fun invoke(
        studentId: Int,
        subjectId: Int,
        takenInAccountTopicIds: List<Int>
    ): List<TypeToProgress> {
        val typesPerformance = ArrayList<TypeToProgress>(takenInAccountTopicIds.count())

        getTotalStudentPerformance(studentId, subjectId).first()
            .filter { (topic, _) ->
                topic.id in takenInAccountTopicIds
                        && (
                        topic.type.isProgressAcceptable
                                || topic.type.isGradeAcceptable
                                || topic.type.isAssessmentAcceptable
                        )
            }
            .forEach { (topic, performance) ->
                val typeIsAlreadyAdded = typesPerformance.any { (type, _) -> type == topic.type }
                if (typeIsAlreadyAdded) {
                    typesPerformance.updateExisting(topic, performance)
                } else {
                    typesPerformance.addNew(topic, performance)
                }
            }
        return typesPerformance
    }

    private fun ArrayList<TypeToProgress>.updateExisting(topic: Topic, performance: Performance) {
        val editableTypePerformanceIndex = indexOfFirst { it.first == topic.type }
        val editableTypePerformance = this[editableTypePerformanceIndex]
        val totalPerformance = editableTypePerformance.second
        val topicProgress = getTopicProgressValue(topic, performance)
        this[editableTypePerformanceIndex] = editableTypePerformance.copy(
            second = totalPerformance.copy(
                reached = totalPerformance.reached + topicProgress,
                total = totalPerformance.total + 1
            )
        )
    }

    private fun ArrayList<TypeToProgress>.addNew(topic: Topic, performance: Performance) {
        add(
            topic.type to SumProgress(
                reached = getTopicProgressValue(topic, performance),
                total = 1f
            )
        )
    }

    private fun getTopicProgressValue(topic: Topic, performance: Performance): Float {
        return when {
            topic.type.isProgressAcceptable -> performance.progress!!.value
            topic.type.isGradeAcceptable -> getSuggestedProgressForGrade(performance.grade).value
            else -> getSuggestedProgressForAssessment(performance.assessment!!).value
        }
    }
}