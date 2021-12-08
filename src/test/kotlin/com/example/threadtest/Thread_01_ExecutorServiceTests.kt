package com.example.threadtest

import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.comparables.shouldBeGreaterThanOrEqualTo
import io.kotest.matchers.shouldBe
import org.slf4j.LoggerFactory
import org.springframework.boot.test.context.SpringBootTest
import java.util.concurrent.*

@SpringBootTest
class Thread_01_ExecutorServiceTests : BehaviorSpec(){

    override fun isolationMode(): IsolationMode = IsolationMode.InstancePerLeaf

    companion object {
        var memberService = MemberService()
        var utils = Utils()
        val logger = LoggerFactory.getLogger(this::class.java)
        var parallelism = ForkJoinPool.commonPool().parallelism
        var timeoutSec = 60
        var exception = ""
    }

    init {
        this.given("JAVA 병렬 테스트 - Executors") {
            val es = Executors.newFixedThreadPool(parallelism)
            val task1 = Callable { memberService.getMemberBlockingTask(Dto(utils.random(), "test-1")) }
            val task2 = Callable { memberService.getMemberBlockingTask(Dto(utils.random(), "test-2")) }
            val task3 = Callable { memberService.getMemberBlockingTask(Dto(utils.random(), "test-3")) }
            val task4 = Callable { memberService.getMemberBlockingTask(Dto(utils.random(), "test-4")) }

            `when`("병렬처리 parallelism 개수를 commonPool 개수로 지정한다") {
                parallelism = ForkJoinPool.commonPool().parallelism
                then("parallelism 개수가 0개 이상이 되어야 한다"){
                    parallelism shouldBeGreaterThan 0 // >= 0
                }
            }

            `when`("병렬처리 parallelism 개수를 availableProcessors 개수로 지정한다") {
                parallelism = Runtime.getRuntime().availableProcessors()
                then("parallelism 개수가 0개 이상이 되어야 한다"){
                    parallelism shouldBeGreaterThan 0 // >= 0
                }
            }

            `when`("모든 작업이 정상 종료 될 경우") {
                val results = es.invokeAll(arrayListOf(task1, task2, task3, task4), timeoutSec.toLong(), TimeUnit.SECONDS)
                shutdown(es)
                for (result in results) {
                    logger.info("[TEST] member seq : ${result.get()}")
                }

                then("작업 목록 size 가 task 개수와 같아야 한다."){
                    results.size shouldBe 4 // >= 0\
                }
            }

            `when`("타임아웃을 1초로 설정하여 초과 될 경우" ) {
                timeoutSec = 1

                try {
                    val results = es.invokeAll(arrayListOf(task1, task2, task3, task4), timeoutSec.toLong(), TimeUnit.SECONDS)

                    shutdown(es)
                    for (result in results) {
                        logger.info("[TEST] member seq : ${result.get()}")
                    }
                } catch (e: CancellationException) {
                    es.shutdownNow()
                    exception = e.toString()
                }

                then("exception message 가 정상 출력되어야 한다."){
                    logger.info("[TEST] exception message -> $exception")
                    exception.length shouldBeGreaterThan 0 // >= 0
                }
            }
        }
    }

    fun shutdown(es : ExecutorService){
        es.shutdown()
        if (!es.awaitTermination(timeoutSec.toLong(), TimeUnit.SECONDS)) {
            es.shutdownNow()
        }
    }
}