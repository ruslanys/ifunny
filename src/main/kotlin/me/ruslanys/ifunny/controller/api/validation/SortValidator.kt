package me.ruslanys.ifunny.controller.api.validation

import me.ruslanys.ifunny.controller.api.dto.PageRequest
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext

class SortValidator : ConstraintValidator<SortConstraint, PageRequest> {

    override fun isValid(pageRequest: PageRequest, context: ConstraintValidatorContext): Boolean =
            pageRequest.getMaySortBy().contains(pageRequest.sortBy)

    override fun initialize(annotation: SortConstraint) {
    }

}
