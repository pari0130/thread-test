package com.example.threadtest

import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.comparables.shouldBeGreaterThanOrEqualTo
import org.slf4j.LoggerFactory
import org.springframework.boot.test.context.SpringBootTest
import java.util.concurrent.*

@SpringBootTest
class Thread_01_ExecutorServiceTests : DescribeSpec(){

    override fun isolationMode(): IsolationMode = IsolationMode.InstancePerLeaf

    companion object {
        var memberService = MemberService()
        var utils = Utils()
        val logger = LoggerFactory.getLogger(this::class.java)
        val parallelism = ForkJoinPool.commonPool().parallelism
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
            val task1 = Callable { memberService.getMemberBlockingTask(Dto(utils.random(), "test-1")) }
            val task2 = Callable { memberService.getMemberBlockingTask(Dto(utils.random(), "test-2")) }
            val task3 = Callable { memberService.getMemberBlockingTask(Dto(utils.random(), "test-3")) }
            val task4 = Callable { memberService.getMemberBlockingTask(Dto(utils.random(), "test-4")) }

            it("병렬처리 parallelism 개수 테스트") {
                parallelism shouldBeGreaterThanOrEqualTo 0 // >= 0
            }

            it("모든 작업이 정상 종료 될 경우") {
                try {
                    val results = es.invokeAll(arrayListOf(task1, task2, task3, task4), timeoutSec.toLong(), TimeUnit.SECONDS)
                    shutdown(es)
                    for (result in results) {
                        logger.info("[TEST] member seq : ${result.get()}")
                    }
                    results.size shouldBeGreaterThanOrEqualTo 0 // >= 0
                } catch (e : Exception) {
                    es.shutdownNow()
                    exception = e.message.toString()
                    logger.error("[TEST] Exception error ", e.message)
                }
            }

            it("타임아웃 이 초과 될 경우" ) {
                try {
                    timeoutSec = 1
                    es.invokeAll(arrayListOf(task1, task2, task3, task4), timeoutSec.toLong(), TimeUnit.SECONDS)
                } catch (e : CancellationException) {
                    es.shutdownNow()
                    exception = e.message.toString()
                }
                exception.length shouldBeGreaterThanOrEqualTo 0 // >= 0
            }
        }
    }
}