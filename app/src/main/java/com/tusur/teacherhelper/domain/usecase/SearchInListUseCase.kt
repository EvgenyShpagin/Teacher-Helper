package com.tusur.teacherhelper.domain.usecase

class SearchInListUseCase {
    operator fun <T> invoke(
        query: String,
        items: List<T>,
        selector: (T) -> String
    ): List<T> {
        return items.filter { query in selector(it) }
    }
}
