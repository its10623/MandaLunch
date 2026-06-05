package com.example.mandalunch.domain.usecase

import com.example.mandalunch.domain.model.Category
import javax.inject.Inject

class RecommendCategoryUseCase @Inject constructor() {
    operator fun invoke(categories: List<Category>): Category? {
        if (categories.isEmpty()) return null
        return categories.random()
    }
}
