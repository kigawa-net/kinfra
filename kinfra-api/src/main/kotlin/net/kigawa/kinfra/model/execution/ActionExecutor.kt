package net.kigawa.kinfra.model.execution

import net.kigawa.kodel.api.log.Kogger
import net.kigawa.kinfra.model.util.AnsiColors
import net.kigawa.kodel.api.log.traceignore.error

/**
 * 共通のアクション実行パターンを提供するクラス
 */
class ActionExecutor(private val kogger: Kogger) {
    /**
     * ステップ実行の共通パターン
     */
    fun executeSteps(steps: List<ExecutionStep>): Int {
        steps.forEachIndexed { index, step ->
            println("${AnsiColors.BLUE}Step ${index + 1}/${steps.size}: ${step.description}${AnsiColors.RESET}")

            val result = step.execute()
            if (result != 0) {
                println("${AnsiColors.RED}✗${AnsiColors.RESET} Step '${step.description}' failed with exit code: $result")
                kogger.error("Step '${step.description}' failed with exit code: $result")
                return result
            } else {
                println("${AnsiColors.GREEN}✓${AnsiColors.RESET} Step '${step.description}' completed successfully")
            }

            if (index < steps.size - 1) {
                println()
            }
        }

        return 0
    }

    /**
     * エラーハンドリング付き実行
     */
    fun executeWithErrorHandling(
        operation: String,
        action: () -> Int,
    ): Int {
        return try {
            action()
        } catch (e: Exception) {
            kogger.error("$operation failed: ${e.message}")
            println("${AnsiColors.RED}Error:${AnsiColors.RESET} $operation failed: ${e.message}")
            1
        }
    }
}

/**
 * 実行ステップを表すデータクラス
 */
data class ExecutionStep(
    val description: String,
    val execute: () -> Int,
)
