package com.example.threadtest

import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.comparables.shouldBeGreaterThanOrEqualTo
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.boot.test.context.SpringBootTest
import java.util.concurrent.*
import java.util.function.Supplier
import java.util.stream.Collectors
import java.util.stream.Collectors.toList

@SpringBootTest
class Thread_02_CompletableFutureTests : BehaviorSpec(){

    override fun isolationMode(): IsolationMode = IsolationMode.InstancePerLeaf

    companion object {
        var memberService = MemberService()
        var utils = Utils()
        val logger = LoggerFactory.getLogger(this::class.java)
        var parallelism = ForkJoinPool.commonPool().parallelism
        var futurePool = ForkJoinPool(parallelism)
        var timeoutSec = 60
        var exception = ""
    }

    init {
        this.given("JAVA 병렬 테스트 - Executors") {
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

            `when`("병렬처리 ForkJoinPool 개수 를 확인한다") {
                then("ForkJoinPool 개수가 0개 이상이 되어야 한다"){
                    futurePool.parallelism shouldBeGreaterThan 0 // >= 0
                }
            }


            `when`("모든 작업이 정상 종료 될 경우") {
                val task1 = CompletableFuture.supplyAsync ( Supplier{memberService.getMemberBlockingTask(Dto(utils.random(), "test-1"))},futurePool )
                val task2 = CompletableFuture.supplyAsync ( Supplier{memberService.getMemberBlockingTask(Dto(utils.random(), "test-2"))},futurePool )
                val task3 = CompletableFuture.supplyAsync ( Supplier{memberService.getMemberBlockingTask(Dto(utils.random(), "test-3"))},futurePool )
                val task4 = CompletableFuture.supplyAsync ( Supplier{memberService.getMemberBlockingTask(Dto(utils.random(), "test-4"))},futurePool )

                CompletableFuture
                        .allOf(task1, task2, task3, task4)
                        .orTimeout(timeoutSec.toLong(), TimeUnit.SECONDS)
                        .join()

                shutdown(futurePool)

                then("작업 목록 size 가 task 개수와 같아야 한다."){
                    listOf(task1, task2, task3, task4).size shouldBe 4 // >= 0
                }
            }

            `when`("LIST 데이터에 대한 100개 size 비동기 호출 시"){
                val uuidList = memberService.getMember().stream()
                        .map { member ->
                            CompletableFuture.supplyAsync ( Supplier{
                                logger.info("thread -> ${Thread.currentThread().name}")
                                "user -> ${member["userUuid"].toString()}"
                            },futurePool )
                        }
                        .collect(toList())
                        .stream().map { it.join() }.collect(toList())

                logger.info("[TEST] get uuid -> ${uuidList.get(0)}")

                then("LIST 데이터에 대한 사이즈 가 >= 100 이 되어야 한다."){
                    uuidList.size shouldBeGreaterThanOrEqualTo 100
                }
            }

            `when`("타임아웃을 1초로 설정하여 초과 될 경우" ) {
                timeoutSec = 1

                try {
                    val task1 = CompletableFuture.supplyAsync ( Supplier{memberService.getMemberBlockingTask(Dto(utils.random(), "test-1"))},futurePool )
                    val task2 = CompletableFuture.supplyAsync ( Supplier{memberService.getMemberBlockingTask(Dto(utils.random(), "test-2"))},futurePool )
                    val task3 = CompletableFuture.supplyAsync ( Supplier{memberService.getMemberBlockingTask(Dto(utils.random(), "test-3"))},futurePool )
                    val task4 = CompletableFuture.supplyAsync ( Supplier{memberService.getMemberBlockingTask(Dto(utils.random(), "test-4"))},futurePool )

                    CompletableFuture
                        .allOf(task1, task2, task3, task4)
                        .orTimeout(timeoutSec.toLong(), TimeUnit.SECONDS)
                        .join()

                } catch (e : CompletionException) {
                    futurePool.shutdownNow()
                    exception = e.message.toString()
                }

                then("exception message 가 정상 출력되어야 한다."){
                    logger.info("[TEST] exception message -> $exception")
                    exception.length shouldBeGreaterThan 0 // >= 0
                }
            }
        }
    }

    fun shutdown(pool : ForkJoinPool){
        pool.shutdown()
        if (!pool.awaitTermination(timeoutSec.toLong(), TimeUnit.SECONDS)) {
            pool.shutdownNow()
        }
    }
}