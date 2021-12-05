package com.example.threadtest

import kotlinx.coroutines.DelicateCoroutinesApi
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.boot.test.context.SpringBootTest

/**
 * 참고 학습 자료
 * completable : https://codechacha.com/ko/java-completable-future/
 * 비동기 기술 : https://jongmin92.github.io/2019/03/31/Java/java-async-1/
 * 코루틴 : https://kotlinworld.com/139
 * 스케줄링 : https://javacan.tistory.com/entry/Reactor-Start-6-Thread-Scheduling
 * 자바 executor : https://velog.io/@neity16/Java-8-4-%EC%9E%90%EB%B0%94-Concurrent-Executors-Callable-Future
 * */
@SpringBootTest(classes = arrayOf(ThreadTestApplication::class))
class Thread_03_CoroutineTests {

    private val log = LoggerFactory.getLogger(this::class.java)

    @OptIn(DelicateCoroutinesApi::class)
    @Test
    @DisplayName("KOTLIN 병렬 - coroutine")
    fun coroutine() {
//        GlobalScope.launch { // launch a new coroutine in background and continue
//            delay(1000L) // non-blocking delay for 1 second
//            println("World!") // print after delay
//        }
//        println("Hello") // main thread continues while coroutine is delayed
//        Thread.sleep(2000L) // block main thread for 2 seconds to keep JVM alive
    }
}