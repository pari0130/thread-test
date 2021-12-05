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
class Thread_04_ScheduersTests {

    private val log = LoggerFactory.getLogger(this::class.java)

    @Test
    @DisplayName("WebFlux 병렬 - fluxSchedulers")
    fun fluxSchedulers(){

    }
}