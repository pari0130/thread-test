package com.example.threadtest

import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.comparables.shouldBeGreaterThanOrEqualTo
import org.slf4j.LoggerFactory
import org.springframework.boot.test.context.SpringBootTest
import reactor.core.publisher.Flux
import reactor.core.scheduler.Schedulers
import java.util.concurrent.*

@SpringBootTest
class Thread_04_ScheduersTests : BehaviorSpec(){

    override fun isolationMode(): IsolationMode = IsolationMode.InstancePerLeaf

    companion object {
        var memberService = MemberService()
        var utils = Utils()
        val logger = LoggerFactory.getLogger(this::class.java)
        var parallelism = ForkJoinPool.commonPool().parallelism
        val countDownLatch = CountDownLatch(parallelism)
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

            `when`("병렬처리 countDownLatch 개수 를 확인한다") {
                then("countDownLatch 개수가 0개 이상이 되어야 한다"){
                    countDownLatch.count shouldBeGreaterThan 0L // >= 0
                }
            }

            `xwhen`("모든 작업이 정상 종료 될 경우") {
                val tastList = ArrayList<String>()

                Flux.fromIterable(memberService.getMember(1, 100))
                    .parallel(parallelism)
                    .runOn(Schedulers.elastic())
                    .map {
                        logger.info("[TEST] 스케줄러 thread -> ${Thread.currentThread().name}")
                        tastList.add(it["userUuid"].toString())
                    }.subscribe(
                        { onNext ->
                            // logger.info("[TEST] response onNext result")
                        },
                        { onError ->
                            logger.info("[TEST] flux onError")
                            logger.error("[TEST] flux error", onError)
                            countDownLatch.countDown()
                        },
                        {
                            logger.info("[TEST] flux onComplete")
                            countDownLatch.countDown()
                        }
                    )
                if (!countDownLatch.await(30, TimeUnit.MINUTES)) {
                    logger.info("[TEST] countDownLatch await timeout")
                }

                logger.info("[TEST] get uuid -> ${tastList.get(0)}")

                then("작업 목록 size 가 task 개수와 같아야 한다."){
                    tastList.size shouldBeGreaterThanOrEqualTo 99
                }
            }

            `when`("타임아웃을 1초로 설정하여 초과 될 경우" ) {
                timeoutSec = 1

                val tastList = ArrayList<String>()

                Flux.fromIterable(memberService.getMember(2, 100))
                    .parallel(parallelism)
                    .runOn(Schedulers.elastic())
                    .map {
                        logger.info("[TEST] 스케줄러 thread -> ${Thread.currentThread().name}")
                        Thread.sleep(50000)
                        tastList.add(it["userUuid"].toString())
                    }.subscribe(
                        { onNext ->
                            // logger.info("[TEST] response onNext result")
                        },
                        { onError ->
                            logger.info("[TEST] flux onError")
                            logger.error("[TEST] flux error", onError)
                            countDownLatch.countDown()
                        },
                        {
                            logger.info("[TEST] flux onComplete")
                            countDownLatch.countDown()
                        }
                    )

                try {
                    if(!countDownLatch.await(timeoutSec.toLong(), TimeUnit.MICROSECONDS)){
                        throw InterruptedException()
                    }
                } catch (e: InterruptedException) {
                    // Thread.currentThread().interrupt()
                    exception = e.toString()
                }

                then("exception message 가 정상 출력되어야 한다."){
                    logger.info("[TEST] countDownLatch await timeout -> $exception")
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