package com.tusur.teacherhelper.domain.usecase

import junit.framework.TestCase.assertEquals
import org.junit.Test

class FormatStudentNameUseCaseTest {

    private val useCase = FormatStudentNameUseCase()

    @Test
    fun shouldReturnFormattedName_whenFullNameIsProvided() {
        val inputName = "иванов иван иванович"

        val result = useCase(inputName)

        assertEquals("Иванов", result.lastName)
        assertEquals("Иван", result.firstName)
        assertEquals("Иванович", result.middleName)
    }

    @Test
    fun shouldReturnFormattedName_whenOnlyFirstAndLastNameAreProvided() {
        val inputName = "Петров Петр"

        val result = useCase(inputName)

        assertEquals("Петров", result.lastName)
        assertEquals("Петр", result.firstName)
        assertEquals("", result.middleName)
    }

    @Test
    fun shouldReturnFormattedName_whenOnlyLastNameIsProvided() {
        val inputName = "Сидоров"

        val result = useCase(inputName)

        assertEquals("Сидоров", result.lastName)
        assertEquals("", result.firstName)
        assertEquals("", result.middleName)
    }

    @Test
    fun shouldReturnEmptyFields_whenEmptyStringIsProvided() {
        val inputName = ""

        val result = useCase(inputName)

        assertEquals("", result.lastName)
        assertEquals("", result.firstName)
        assertEquals("", result.middleName)
    }

    @Test
    fun shouldFormatNameProperly_whenNameContainsExtraSpaces() {
        val inputName = "   Васильев   Алексей   Сергеевич   "

        val result = useCase(inputName)

        assertEquals("Васильев", result.lastName)
        assertEquals("Алексей", result.firstName)
        assertEquals("Сергеевич", result.middleName)
    }

    @Test
    fun shouldHandleNameWithMixedCaseProperly_whenNameHasIrregularCase() {
        val inputName = "ивАНОВ ПетР иВаНОВИЧ"

        val result = useCase(inputName)

        assertEquals("Иванов", result.lastName)
        assertEquals("Петр", result.firstName)
        assertEquals("Иванович", result.middleName)
    }
}