package com.example.threadtest

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.boot.test.context.SpringBootTest
import java.security.SecureRandom
import java.util.concurrent.*

/**
 * 참고 학습 자료
 * completable : https://codechacha.com/ko/java-completable-future/
 * 비동기 기술 : https://jongmin92.github.io/2019/03/31/Java/java-async-1/
 * 코루틴 : https://kotlinworld.com/139
 * 스케줄링 : https://javacan.tistory.com/entry/Reactor-Start-6-Thread-Scheduling
 * 자바 executor : https://velog.io/@neity16/Java-8-4-%EC%9E%90%EB%B0%94-Concurrent-Executors-Callable-Future
 * */
@SpringBootTest(classes = arrayOf(ThreadTestApplication::class))
class Thread_01_ExecutorServiceTests {

    private val memberService = MemberService()

    private val utils = Utils()

    private val logger = LoggerFactory.getLogger(this::class.java)

    private val parallelism = ForkJoinPool.commonPool().parallelism

    // 코드 설명, 활용방안, 최종정리 순
    @Test
    @DisplayName("JAVA 병렬 - Executor")
    fun javaThread(){
        logger.info("[TEST] parallelism count -> $parallelism")

        val es = Executors.newWorkStealingPool(parallelism)

        try {
            val task1 = Callable {
                val random = utils.random()
                memberService.anyBlockingTask(Dto(random, "test-$random"))
            }
            val task2 = Callable {
                val random = utils.random()
                memberService.anyBlockingTask(Dto(random, "test-$random"))
            }
            val task3 = Callable {
                val random = utils.random()
                memberService.anyBlockingTask(Dto(random, "test-$random"))
            }
            val task4 = Callable {
                val random = utils.random()
                memberService.anyBlockingTask(Dto(random, "test-$random"))
            }

            val results = es.invokeAll(arrayListOf(task1, task2, task3, task4))
            es.shutdown()

            for(result in results) {
                logger.info("member seq : ${result.get()}")
            }

            if (!es.awaitTermination(10, TimeUnit.MINUTES)) {
                es.shutdownNow()
            }
        } catch (e: TimeoutException) {
            es.shutdownNow()
            logger.error("[TEST] TimeoutException error ", e.message)
        } catch (ie: InterruptedException) {
            es.shutdownNow()
            Thread.currentThread().interrupt()
            logger.error("[TEST] InterruptedException error ", ie.message)
        } catch (e: Exception) {
            es.shutdownNow()
            logger.error("[TEST] Exception error ", e.message)
        }
    }
}