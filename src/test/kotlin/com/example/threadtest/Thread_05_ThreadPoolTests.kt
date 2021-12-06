package com.example.threadtest

import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.comparables.shouldBeGreaterThanOrEqualTo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import org.apache.commons.lang3.ThreadUtils
import org.slf4j.LoggerFactory
import org.springframework.boot.test.context.SpringBootTest
import java.util.concurrent.*
import java.util.function.Supplier

@SpringBootTest
class Thread_05_ThreadPoolTests : DescribeSpec(){

    override fun isolationMode(): IsolationMode = IsolationMode.InstancePerLeaf

    companion object {
        var memberService = MemberService()
        var utils = Utils()
        val logger = LoggerFactory.getLogger(this::class.java)
        val parallelism = ForkJoinPool.commonPool().parallelism
        val futurePool = ForkJoinPool(parallelism)
        var timeoutSec = 60
        var exception = ""
    }

    init {
        this.describe("유휴 thread log 테스트") {
            val es = Executors.newWorkStealingPool(parallelism)

            it("CompletableFuture - thread shutdown 하지 않을 경우") {
                try {
                    logger.info("[TEST] java version ${System.getProperty("java.version")}")
                    logger.info("[TEST] ========== work.join() start ==========")

                    val task1 = CompletableFuture.supplyAsync ( Supplier{memberService.getMemberBlockingTask(Dto(utils.random(), "test-1"))}, futurePool)
                    val task2 = CompletableFuture.supplyAsync ( Supplier{memberService.getMemberBlockingTask(Dto(utils.random(), "test-2"))}, futurePool)
                    val task3 = CompletableFuture.supplyAsync ( Supplier{memberService.getMemberBlockingTask(Dto(utils.random(), "test-3"))}, futurePool)
                    val task4 = CompletableFuture.supplyAsync ( Supplier{memberService.getMemberBlockingTask(Dto(utils.random(), "test-4"))}, futurePool)

                    CompletableFuture
                        .allOf(task1, task2, task3, task4)
                        .orTimeout(timeoutSec.toLong(), TimeUnit.SECONDS)
                        .join()

                    logger.info("[TEST] ========== work.join() end ==========")

                    threadLogger()
                } catch (e : Exception) {
                    es.shutdownNow()
                    exception = e.message.toString()
                    logger.error("[TEST] Exception error ", e.message)
                }
            }

            it("CompletableFuture - thread shutdown 할 경우") {
                try {
                    logger.info("[TEST] java version ${System.getProperty("java.version")}")
                    logger.info("[TEST] ========== work.join() start ==========")

                    val task1 = CompletableFuture.supplyAsync ( Supplier{memberService.getMemberBlockingTask(Dto(utils.random(), "test-1"))}, futurePool)
                    val task2 = CompletableFuture.supplyAsync ( Supplier{memberService.getMemberBlockingTask(Dto(utils.random(), "test-2"))}, futurePool)
                    val task3 = CompletableFuture.supplyAsync ( Supplier{memberService.getMemberBlockingTask(Dto(utils.random(), "test-3"))}, futurePool)
                    val task4 = CompletableFuture.supplyAsync ( Supplier{memberService.getMemberBlockingTask(Dto(utils.random(), "test-4"))}, futurePool)

                    CompletableFuture
                        .allOf(task1, task2, task3, task4)
                        .orTimeout(timeoutSec.toLong(), TimeUnit.SECONDS)
                        .join()

                    shutdown(futurePool)

                    logger.info("[TEST] ========== work.join() end ==========")

                    threadLogger()
                } catch (e : Exception) {
                    es.shutdownNow()
                    exception = e.message.toString()
                    logger.error("[TEST] Exception error ", e.message)
                }
            }

            it("coroutine - thread check") {
                logger.info("[TEST] java version ${System.getProperty("java.version")}")
                logger.info("[TEST] ========== work.join() start ==========")

                val job1 = CoroutineScope(Dispatchers.IO).async {
                    memberService.getMemberBlockingTask(Dto(utils.random(), "test-1"))
                }
                val job2 = CoroutineScope(Dispatchers.IO).async {
                    memberService.getMemberBlockingTask(Dto(utils.random(), "test-2"))
                }
                val job3 = CoroutineScope(Dispatchers.IO).async {
                    memberService.getMemberBlockingTask(Dto(utils.random(), "test-3"))
                }
                val job4 = CoroutineScope(Dispatchers.IO).async {
                    memberService.getMemberBlockingTask(Dto(utils.random(), "test-4"))
                }
                logger.info("${job1.await()} ${job2.await()} ${job3.await()} ${job4.await()}")

                logger.info("[TEST] ========== work.join() end ==========")

                threadLogger()
            }
        }
    }

    fun shutdown(pool : ForkJoinPool){
        pool.shutdown()
        if (!pool.awaitTermination(timeoutSec.toLong(), TimeUnit.SECONDS)) {
            pool.shutdownNow()
        }
    }

    fun threadLogger(){
        Thread.sleep(10000)
        ThreadUtils.getAllThreads().stream().forEach{ thread ->
            logger.info("[TEST] after 10 sec thread pool ${thread.name}")
        }

        logger.info("[TEST] wait more ==================================================")

        Thread.sleep(30000)
        ThreadUtils.getAllThreads().stream().forEach{ thread ->
            logger.info("[TEST] after 30 sec thread pool ${thread.name}")
        }
    }
}