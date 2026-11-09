package com.ivy.report

import com.ivy.base.legacy.LegacyTransaction
import com.ivy.base.model.TransactionType
import com.ivy.data.model.Category
import com.ivy.data.model.CategoryId
import com.ivy.data.model.primitive.ColorInt
import com.ivy.data.model.primitive.NotBlankTrimmedString
import com.ivy.legacy.datamodel.Account
import com.ivy.reports.ReportChartDataCalculator
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.Test
import java.math.BigDecimal
import java.util.UUID

class ReportChartAggregationTest {

    private val accountId = UUID.randomUUID()
    private val account2Id = UUID.randomUUID()

    private val account = Account(
        id = accountId,
        name = "Cash",
        color = 0,
        currency = "USD"
    )

    private val account2 = Account(
        id = account2Id,
        name = "EUR Account",
        color = 0,
        currency = "EUR"
    )

    private val category1 = Category(
        id = CategoryId(UUID.randomUUID()),
        name = NotBlankTrimmedString.unsafe("Food"),
        color = ColorInt(1),
        icon = null,
        orderNum = 1.0
    )

    private val category2 = Category(
        id = CategoryId(UUID.randomUUID()),
        name = NotBlankTrimmedString.unsafe("Travel"),
        color = ColorInt(2),
        icon = null,
        orderNum = 2.0
    )

    private val accountTransfersCategory = Category(
        id = CategoryId(UUID.randomUUID()),
        name = NotBlankTrimmedString.unsafe("Transfers"),
        color = ColorInt(3),
        icon = null,
        orderNum = 0.0
    )

    private val accounts = listOf(account, account2)
    private val categories = listOf(category1, category2)

    private fun legacyTrn(
        type: TransactionType,
        amount: Double,
        categoryId: UUID? = null,
        toAccountId: UUID? = null,
        toAmount: Double = amount,
        accId: UUID = accountId
    ): LegacyTransaction {
        return LegacyTransaction(
            accountId = accId,
            type = type,
            amount = BigDecimal.valueOf(amount),
            toAccountId = toAccountId,
            toAmount = BigDecimal.valueOf(toAmount),
            categoryId = categoryId
        )
    }

    @Test
    fun `empty transactions returns empty chart list`() = runBlocking {
        val result = ReportChartDataCalculator.computeCategoryChartData(
            legacyTrns = emptyList(),
            type = TransactionType.EXPENSE,
            treatTransfersAsIncExp = false,
            baseCurrency = "USD",
            accounts = accounts,
            categories = categories,
            accountTransfersCategory = accountTransfersCategory,
            exchange = { amt, _ -> amt.toDouble() }
        )

        result.shouldBeEmpty()
    }

    @Test
    fun `single category returns 100 percent aggregation`() = runBlocking {
        val trn = legacyTrn(
            type = TransactionType.EXPENSE,
            amount = 150.0,
            categoryId = category1.id.value
        )

        val result = ReportChartDataCalculator.computeCategoryChartData(
            legacyTrns = listOf(trn),
            type = TransactionType.EXPENSE,
            treatTransfersAsIncExp = false,
            baseCurrency = "USD",
            accounts = accounts,
            categories = categories,
            accountTransfersCategory = accountTransfersCategory,
            exchange = { amt, _ -> amt.toDouble() }
        )

        result shouldHaveSize 1
        result[0].category shouldBe category1
        result[0].amount shouldBe (150.0 plusOrMinus 0.001)
        result[0].associatedTransactions shouldHaveSize 1
        result[0].isCategoryUnspecified shouldBe false
    }

    @Test
    fun `multiple categories are aggregated and sorted descending by amount`() = runBlocking {
        val trns = listOf(
            legacyTrn(TransactionType.EXPENSE, 50.0, category1.id.value),
            legacyTrn(TransactionType.EXPENSE, 200.0, category2.id.value),
            legacyTrn(TransactionType.EXPENSE, 30.0, category1.id.value)
        )

        val result = ReportChartDataCalculator.computeCategoryChartData(
            legacyTrns = trns,
            type = TransactionType.EXPENSE,
            treatTransfersAsIncExp = false,
            baseCurrency = "USD",
            accounts = accounts,
            categories = categories,
            accountTransfersCategory = accountTransfersCategory,
            exchange = { amt, _ -> amt.toDouble() }
        )

        result shouldHaveSize 2
        // First should be category2 with 200.0
        result[0].category shouldBe category2
        result[0].amount shouldBe (200.0 plusOrMinus 0.001)
        result[0].associatedTransactions shouldHaveSize 1

        // Second should be category1 with 50 + 30 = 80.0
        result[1].category shouldBe category1
        result[1].amount shouldBe (80.0 plusOrMinus 0.001)
        result[1].associatedTransactions shouldHaveSize 2
    }

    @Test
    fun `unspecified category is handled correctly when categoryId is null`() = runBlocking {
        val trns = listOf(
            legacyTrn(TransactionType.EXPENSE, 100.0, categoryId = null),
            legacyTrn(TransactionType.EXPENSE, 40.0, category1.id.value)
        )

        val result = ReportChartDataCalculator.computeCategoryChartData(
            legacyTrns = trns,
            type = TransactionType.EXPENSE,
            treatTransfersAsIncExp = false,
            baseCurrency = "USD",
            accounts = accounts,
            categories = categories,
            accountTransfersCategory = accountTransfersCategory,
            exchange = { amt, _ -> amt.toDouble() }
        )

        result shouldHaveSize 2
        result[0].category shouldBe null
        result[0].isCategoryUnspecified shouldBe true
        result[0].amount shouldBe (100.0 plusOrMinus 0.001)
    }

    @Test
    fun `transfers toggle OFF ignores transfer transactions`() = runBlocking {
        val trns = listOf(
            legacyTrn(TransactionType.EXPENSE, 100.0, category1.id.value),
            legacyTrn(
                type = TransactionType.TRANSFER,
                amount = 500.0,
                toAccountId = account2Id,
                toAmount = 500.0
            )
        )

        val result = ReportChartDataCalculator.computeCategoryChartData(
            legacyTrns = trns,
            type = TransactionType.EXPENSE,
            treatTransfersAsIncExp = false,
            baseCurrency = "USD",
            accounts = accounts,
            categories = categories,
            accountTransfersCategory = accountTransfersCategory,
            exchange = { amt, _ -> amt.toDouble() }
        )

        result shouldHaveSize 1
        result[0].category shouldBe category1
        result[0].amount shouldBe (100.0 plusOrMinus 0.001)
    }

    @Test
    fun `transfers toggle ON includes transfers in expense and income charts`() = runBlocking {
        val trns = listOf(
            legacyTrn(TransactionType.EXPENSE, 100.0, category1.id.value),
            legacyTrn(TransactionType.INCOME, 300.0, category2.id.value),
            legacyTrn(
                type = TransactionType.TRANSFER,
                amount = 75.0,
                toAccountId = account2Id,
                toAmount = 80.0
            )
        )

        val expenseResult = ReportChartDataCalculator.computeCategoryChartData(
            legacyTrns = trns,
            type = TransactionType.EXPENSE,
            treatTransfersAsIncExp = true,
            baseCurrency = "USD",
            accounts = accounts,
            categories = categories,
            accountTransfersCategory = accountTransfersCategory,
            exchange = { amt, _ -> amt.toDouble() }
        )

        expenseResult shouldHaveSize 2
        expenseResult[0].category shouldBe category1
        expenseResult[0].amount shouldBe (100.0 plusOrMinus 0.001)
        expenseResult[1].category shouldBe accountTransfersCategory
        expenseResult[1].amount shouldBe (75.0 plusOrMinus 0.001)

        val incomeResult = ReportChartDataCalculator.computeCategoryChartData(
            legacyTrns = trns,
            type = TransactionType.INCOME,
            treatTransfersAsIncExp = true,
            baseCurrency = "USD",
            accounts = accounts,
            categories = categories,
            accountTransfersCategory = accountTransfersCategory,
            exchange = { amt, _ -> amt.toDouble() }
        )

        incomeResult shouldHaveSize 2
        incomeResult[0].category shouldBe category2
        incomeResult[0].amount shouldBe (300.0 plusOrMinus 0.001)
        incomeResult[1].category shouldBe accountTransfersCategory
        incomeResult[1].amount shouldBe (80.0 plusOrMinus 0.001)
    }

    @Test
    fun `currency exchange is correctly applied to foreign currency transactions`() = runBlocking {
        val trn = legacyTrn(
            type = TransactionType.EXPENSE,
            amount = 100.0,
            categoryId = category1.id.value,
            accId = account2Id // EUR account
        )

        // Mock exchange: EUR -> USD multiplier 1.10
        val result = ReportChartDataCalculator.computeCategoryChartData(
            legacyTrns = listOf(trn),
            type = TransactionType.EXPENSE,
            treatTransfersAsIncExp = false,
            baseCurrency = "USD",
            accounts = accounts,
            categories = categories,
            accountTransfersCategory = accountTransfersCategory,
            exchange = { amt, fromCurr ->
                if (fromCurr == "EUR") amt.toDouble() * 1.10 else amt.toDouble()
            }
        )

        result shouldHaveSize 1
        result[0].amount shouldBe (110.0 plusOrMinus 0.001)
    }
}
