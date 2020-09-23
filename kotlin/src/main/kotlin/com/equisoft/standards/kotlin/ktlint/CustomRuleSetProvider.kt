package com.equisoft.standards.kotlin.ktlint

import com.pinterest.ktlint.core.RuleSet
import com.pinterest.ktlint.core.RuleSetProvider

class CustomRuleSetProvider : RuleSetProvider {
    override fun get() = RuleSet("equisoft", IndentationRule())
}
