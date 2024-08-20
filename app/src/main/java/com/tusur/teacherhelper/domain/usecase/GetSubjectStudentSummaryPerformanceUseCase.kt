package com.tusur.teacherhelper.domain.usecase

import com.tusur.teacherhelper.domain.model.Performance
import com.tusur.teacherhelper.domain.model.SumProgress
import com.tusur.teacherhelper.domain.model.Topic
import com.tusur.teacherhelper.domain.model.TopicType
import kotlinx.coroutines.flow.first

class GetSubjectStudentSummaryPerformanceUseCase(
    private val getTotalStudentPerformance: GetSubjectStudentPerformanceUseCase,
    private val getSubjectTopics: GetSubjectTopicsUseCase,
    private val getSuggestedProgressForGrade: GetSuggestedProgressForGradeUseCase
) {
    suspend operator fun invoke(
        studentId: Int,
        subjectId: Int,
        takenInAccountTopicIds: List<Int>
    ): List<Pair<TopicType, SumProgress<Float>>> {
        val studentPerformance = getTotalStudentPerformance(studentId, subjectId)
        val takenTopics = getSubjectTopics(
            subjectId = subjectId,
            withCancelled = false
        ).first().filter { it.id in takenInAccountTopicIds }
        val typesPerformance = ArrayList<Pair<TopicType, SumProgress<Float>>>(takenTopics.count())

        studentPerformance.first().forEach { (topic, performance) ->
            if (takenTopics.any { it.id == topic.id } && (topic.type.isProgressAcceptable || topic.type.isGradeAcceptable)) {
                val typeIsAlreadyAdded = typesPerformance.any { (type, _) -> type == topic.type }
                if (typeIsAlreadyAdded) {
                    val editableTypePerformance = typesPerformance.find { it.first == topic.type }!!
                    val totalPerformance = editableTypePerformance.second
                    val topicProgress = getTopicProgressValue(topic, performance)
                    typesPerformance.add(
                        editableTypePerformance.copy(
                            second = totalPerformance.copy(
                                reached = totalPerformance.reached + topicProgress,
                                total = totalPerformance.total + 1
                            )
                        )
                    )
                } else {
                    typesPerformance.add(
                        topic.type to SumProgress(
                            reached = getTopicProgressValue(topic, performance),
                            total = 1f
                        )
                    )
                }
            }
        }
        return typesPerformance
    }

    private fun getTopicProgressValue(topic: Topic, performance: Performance): Float {
        return if (topic.type.isProgressAcceptable) {
            performance.progress!!.value
        } else {
            getSuggestedProgressForGrade(performance.grade!!).value
        }
    }
}