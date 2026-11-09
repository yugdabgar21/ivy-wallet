package com.ivy.reports

import com.ivy.base.legacy.LegacyTransaction
import com.ivy.base.model.TransactionType
import com.ivy.data.model.Category
import com.ivy.legacy.datamodel.Account
import com.ivy.piechart.CategoryAmount
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import java.math.BigDecimal
import java.util.UUID

object ReportChartDataCalculator {

    suspend fun computeCategoryChartData(
        legacyTrns: List<LegacyTransaction>,
        type: TransactionType,
        treatTransfersAsIncExp: Boolean,
        baseCurrency: String,
        accounts: List<Account>,
        categories: List<Category>,
        accountTransfersCategory: Category,
        exchange: suspend (amount: BigDecimal, fromCurrency: String) -> Double
    ): ImmutableList<CategoryAmount> {
        if (legacyTrns.isEmpty()) return persistentListOf()

        val accountCurrencyMap = accounts.associate { it.id to (it.currency ?: baseCurrency) }

        // Map: categoryId? -> Pair(totalBaseAmount, associatedTrns)
        val categoryData = mutableMapOf<UUID?, Pair<Double, MutableList<LegacyTransaction>>>()
        var transferTotal = 0.0
        val transferTrns = mutableListOf<LegacyTransaction>()

        for (trn in legacyTrns) {
            when {
                trn.type == type -> {
                    val trnCurr = accountCurrencyMap[trn.accountId] ?: baseCurrency
                    val baseAmt = exchange(trn.amount, trnCurr)

                    val catId = trn.categoryId
                    val existing = categoryData[catId]
                    if (existing == null) {
                        categoryData[catId] = Pair(baseAmt, mutableListOf(trn))
                    } else {
                        categoryData[catId] = Pair(
                            existing.first + baseAmt,
                            existing.second.also { it.add(trn) }
                        )
                    }
                }

                treatTransfersAsIncExp && trn.type == TransactionType.TRANSFER -> {
                    val (amt, accId) = if (type == TransactionType.INCOME) {
                        Pair(trn.toAmount, trn.toAccountId)
                    } else {
                        Pair(trn.amount, trn.accountId)
                    }
                    val trnCurr = accId?.let { accountCurrencyMap[it] } ?: baseCurrency
                    val baseAmt = exchange(amt, trnCurr)
                    transferTotal += baseAmt
                    transferTrns.add(trn)
                }
            }
        }

        val result = mutableListOf<CategoryAmount>()
        for ((catId, data) in categoryData) {
            val (total, trns) = data
            if (total == 0.0) continue
            val category = categories.find { it.id.value == catId }
            result.add(
                CategoryAmount(
                    category = category,
                    amount = total,
                    associatedTransactions = trns,
                    isCategoryUnspecified = catId == null
                )
            )
        }

        if (treatTransfersAsIncExp && transferTotal > 0.0) {
            result.add(
                CategoryAmount(
                    category = accountTransfersCategory,
                    amount = transferTotal,
                    associatedTransactions = transferTrns,
                    isCategoryUnspecified = false
                )
            )
        }

        return result.sortedByDescending { it.amount }.toImmutableList()
    }
}
