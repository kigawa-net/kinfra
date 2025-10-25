package net.kigawa.kinfra.action.execution

import net.kigawa.kinfra.action.logging.Logger
import net.kigawa.kinfra.model.util.AnsiColors

/**
 * 共通のアクション実行パターンを提供するクラス
 */
class ActionExecutor(private val logger: Logger) {
    
    /**
     * ステップ実行の共通パターン
     */
    fun executeSteps(steps: List<ExecutionStep>): Int {
        steps.forEachIndexed { index, step ->
            println("${AnsiColors.BLUE}Step ${index + 1}/${steps.size}: ${step.description}${AnsiColors.RESET}")
            
            val result = step.execute()
            if (result != 0) {
                println("${AnsiColors.RED}✗${AnsiColors.RESET} Step '${step.description}' failed with exit code: $result")
                logger.error("Step '${step.description}' failed with exit code: $result")
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
     * 条件付き実行
     */
    fun executeIf(condition: () -> Boolean, action: () -> Int): Int {
        return if (condition()) {
            action()
        } else {
            0
        }
    }
    
    /**
     * エラーハンドリング付き実行
     */
    fun executeWithErrorHandling(
        operation: String,
        action: () -> Int
    ): Int {
        return try {
            action()
        } catch (e: Exception) {
            logger.error("$operation failed: ${e.message}")
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
    val execute: () -> Int
)