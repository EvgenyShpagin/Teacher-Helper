package com.tusur.teacherhelper.domain.usecase

import javax.inject.Inject

class SearchInListUseCase @Inject constructor() {
    operator fun <T> invoke(
        query: String,
        items: List<T>,
        selector: (T) -> String
    ): List<T> {
        return items.filter { query in selector(it) }
    }
}
