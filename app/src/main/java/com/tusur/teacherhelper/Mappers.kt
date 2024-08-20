package com.tusur.teacherhelper

import com.tusur.teacherhelper.data.model.ClassDate
import com.tusur.teacherhelper.data.model.StudentTopicPerformance
import com.tusur.teacherhelper.domain.model.Date
import com.tusur.teacherhelper.domain.model.Datetime
import com.tusur.teacherhelper.domain.model.Performance
import com.tusur.teacherhelper.data.model.Deadline as DataDeadline
import com.tusur.teacherhelper.data.model.Group as DataGroup
import com.tusur.teacherhelper.data.model.Student as DataStudent
import com.tusur.teacherhelper.data.model.Subject as DataSubject
import com.tusur.teacherhelper.data.model.Topic as DataTopic
import com.tusur.teacherhelper.data.model.TopicType as DataTopicType
import com.tusur.teacherhelper.domain.model.Deadline as DomainDeadline
import com.tusur.teacherhelper.domain.model.Group as DomainGroup
import com.tusur.teacherhelper.domain.model.Student as DomainStudent
import com.tusur.teacherhelper.domain.model.Subject as DomainSubject
import com.tusur.teacherhelper.domain.model.Topic as DomainTopic
import com.tusur.teacherhelper.domain.model.TopicType as DomainTopicType

fun ClassDate.toDomain(): Datetime {
    return Datetime.fromMillis(datetimeMillis)
}

fun DataDeadline.toDomain(): DomainDeadline {
    return DomainDeadline(id, Date.fromMillis(dateMillis), creatorTopicId)
}

fun DomainDeadline.toData(): DataDeadline {
    return DataDeadline(id, date.toMillis(), owningTopicId)
}


fun DataGroup.toDomain(): DomainGroup {
    return DomainGroup(id, number)
}

fun DomainGroup.toData(): DataGroup {
    return DataGroup(id, number)
}

fun DataSubject.toDomain(): DomainSubject {
    return DomainSubject(id, name)
}

fun DomainSubject.toData(): DataSubject {
    return DataSubject(id, name)
}

fun DataTopic.toDomain(
    topicType: DomainTopicType,
    deadline: DomainDeadline?
): DomainTopic {
    return DomainTopic(
        id = id,
        type = topicType,
        name = name,
        deadline = deadline,
        isCancelled = isCancelled,
    )
}

fun DataTopicType.toDomain(): DomainTopicType {
    return DomainTopicType(
        id = id,
        name = name,
        shortName = shortName,
        canDeadlineBeSpecified = canDeadlineBeSpecified,
        isGradeAcceptable = isGradeAcceptable,
        isProgressAcceptable = isProgressAcceptable,
        isAssessmentAcceptable = isAssessmentAcceptable,
        isAttendanceAcceptable = isAttendanceAcceptable,
        isAttendanceForOneClassOnly = isAttendanceForOneClassOnly
    )
}

fun DomainTopicType.toData(): DataTopicType {
    return DataTopicType(
        id = id,
        name = name,
        shortName = shortName,
        canDeadlineBeSpecified = canDeadlineBeSpecified,
        isGradeAcceptable = isGradeAcceptable,
        isProgressAcceptable = isProgressAcceptable,
        isAssessmentAcceptable = isAssessmentAcceptable,
        isAttendanceAcceptable = isAttendanceAcceptable,
        isAttendanceForOneClassOnly = isAttendanceForOneClassOnly
    )
}

fun DomainTopic.toData(subjectId: Int, deadlineId: Int?): DataTopic {
    return DataTopic(
        id = id,
        name = name,
        subjectId = subjectId,
        topicTypeId = type.id,
        deadlineId = deadlineId,
        isCancelled = isCancelled
    )
}

fun DataStudent.toDomain(): DomainStudent {
    return DomainStudent(id, name)
}

fun DomainStudent.toData(groupId: Int): DataStudent {
    return DataStudent(id, name, groupId)
}

fun StudentTopicPerformance.toDomain(): Performance {
    return Performance(grade, progress, assessment, attendance?.let { listOf(it) })
}