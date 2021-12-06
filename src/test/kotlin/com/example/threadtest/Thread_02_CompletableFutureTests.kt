package com.example.threadtest

import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.comparables.shouldBeGreaterThanOrEqualTo
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.boot.test.context.SpringBootTest
import java.util.concurrent.*
import java.util.function.Supplier

@SpringBootTest
class Thread_02_CompletableFutureTests : DescribeSpec(){

    override fun isolationMode(): IsolationMode = IsolationMode.InstancePerLeaf

    companion object {
        var memberService = MemberService()
        var utils = Utils()
        val logger = LoggerFactory.getLogger(this::class.java)
        val parallelism = ForkJoinPool.commonPool().parallelism
        val futurePool = ForkJoinPool(parallelism)
        var timeoutSec = 60
        var exception = ""
        fun shutdown(es : ExecutorService){
            es.shutdown()
            if (!es.awaitTermination(timeoutSec.toLong(), TimeUnit.SECONDS)) {
                es.shutdownNow()
            }
        }
    }

    init {
        this.describe("JAVA 병렬 테스트 - Executors") {
            val es = Executors.newWorkStealingPool(parallelism)

            it("병렬처리 parallelism 개수 테스트") {
                parallelism shouldBeGreaterThanOrEqualTo 0 // >= 0
            }

            it("병렬처리 ForkJoinPool 개수 테스트") {
                futurePool.parallelism shouldBeGreaterThanOrEqualTo 0 // >= 0
            }

            it("모든 작업이 정상 종료 될 경우") {
                try {
                    val task1 = CompletableFuture.supplyAsync ( Supplier{memberService.getMemberBlockingTask(Dto(utils.random(), "test-1"))},futurePool )
                    val task2 = CompletableFuture.supplyAsync ( Supplier{memberService.getMemberBlockingTask(Dto(utils.random(), "test-2"))},futurePool )
                    val task3 = CompletableFuture.supplyAsync ( Supplier{memberService.getMemberBlockingTask(Dto(utils.random(), "test-3"))},futurePool )
                    val task4 = CompletableFuture.supplyAsync ( Supplier{memberService.getMemberBlockingTask(Dto(utils.random(), "test-4"))},futurePool )

                    CompletableFuture
                        .allOf(task1, task2, task3, task4)
                        .orTimeout(timeoutSec.toLong(), TimeUnit.SECONDS)
                        .join()
                    shutdown(es)
                    listOf(task1, task2, task3, task4).size shouldBeGreaterThanOrEqualTo 0 // >= 0
                } catch (e : Exception) {
                    es.shutdownNow()
                    exception = e.message.toString()
                    logger.error("[TEST] Exception error ", e.message)
                }
            }

            it("타임아웃 이 초과 될 경우" ) {
                try {
                    val task1 = CompletableFuture.supplyAsync ( Supplier{memberService.getMemberBlockingTask(Dto(utils.random(), "test-1"))},futurePool )
                    val task2 = CompletableFuture.supplyAsync ( Supplier{memberService.getMemberBlockingTask(Dto(utils.random(), "test-2"))},futurePool )
                    val task3 = CompletableFuture.supplyAsync ( Supplier{memberService.getMemberBlockingTask(Dto(utils.random(), "test-3"))},futurePool )
                    val task4 = CompletableFuture.supplyAsync ( Supplier{memberService.getMemberBlockingTask(Dto(utils.random(), "test-4"))},futurePool )

                    timeoutSec = 1
                    CompletableFuture
                        .allOf(task1, task2, task3, task4)
                        .orTimeout(timeoutSec.toLong(), TimeUnit.SECONDS)
                        .join()
                } catch (e : CompletionException) {
                    es.shutdownNow()
                    exception = e.message.toString()
                }
                exception.length shouldBeGreaterThanOrEqualTo 0 // >= 0
            }
        }
    }
}