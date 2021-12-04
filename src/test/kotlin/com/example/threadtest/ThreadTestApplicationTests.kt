package com.example.threadtest

import kotlinx.coroutines.DelicateCoroutinesApi
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.slf4j.LoggerFactory
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.codec.ClientCodecConfigurer
import org.springframework.util.Assert
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient
import java.time.Duration
import java.util.concurrent.atomic.AtomicReferenceArray


/**
 * 참고 학습 자료
 * completable : https://codechacha.com/ko/java-completable-future/
 * 비동기 기술 : https://jongmin92.github.io/2019/03/31/Java/java-async-1/
 * 코루틴 : https://kotlinworld.com/139
 * 스케줄링 : https://javacan.tistory.com/entry/Reactor-Start-6-Thread-Scheduling
 * */
@SpringBootTest(classes = arrayOf(ThreadTestApplication::class))
class ThreadTestApplicationTests {

    private val log = LoggerFactory.getLogger(this::class.java)

    companion object {
        const val BASE_URL = "https://api-pre-prod.raidea.io"
        const val MEMBER_API = "/user-service/v1.1/"
        const val IDRIVE_TOKEN = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdWQiOlsib2F1dGgyLXJlc291cmNlIl0sInBlcm1pc3Npb25MZXZlbCI6MCwic29JZCI6ImlEcml2ZSIsImFwaV9rZXkiOnRydWUsInVzZXJfbmFtZSI6ImlEcml2ZUFwaUtleSIsImV4cCI6Mzc1MjEyNTQ0NCwiYXV0aG9yaXRpZXMiOlsiUk9MRV9DTElFTlRfaURyaXZlQ3MiXSwianRpIjoiNWVhOTFlNDEtYmE4Ni00YmFiLTk3NjAtMmI1NmUxMjM5NTBjIiwiY2xpZW50X2lkIjoiaURyaXZlQ3MifQ.Nb8lreht9zSY9J4gjsV0RVa9TRK9Ho7_2fFXYyjoexQ"
    }

    // 호출 시 랜덤페이지 1~40 , size 1000 호출
    @Test
    @DisplayName("JAVA 병렬")
    fun javaThread(){

    }

    @Test
    @DisplayName("JAVA 병렬")
    fun completableFuture(){

    }

    @OptIn(DelicateCoroutinesApi::class)
    @Test
    @DisplayName("KOTLIN 병렬")
    fun coroutine() {
//        GlobalScope.launch { // launch a new coroutine in background and continue
//            delay(1000L) // non-blocking delay for 1 second
//            println("World!") // print after delay
//        }
//        println("Hello") // main thread continues while coroutine is delayed
//        Thread.sleep(2000L) // block main thread for 2 seconds to keep JVM alive

        this.getObjects(1, 10000)
    }



    @Test
    @DisplayName("WebFlux 병렬")
    fun fluxSchedulers(){

    }

    fun getObjects(page:Int, size:Int){
        log.info("[TEST] objects api get before")

        val results = ArrayList<Map<String, Any?>>()
        val exchangeStrategies = ExchangeStrategies.builder()
                .codecs { configurer: ClientCodecConfigurer -> configurer.defaultCodecs().maxInMemorySize(-1) }.build()
        WebClient
                .builder()
                .exchangeStrategies(exchangeStrategies)
                .baseUrl(BASE_URL)
                .build()
                .get()
                .uri(MEMBER_API + "members?page=$page&size=$size")
                .header("Authorization", IDRIVE_TOKEN)
                .retrieve()
                .bodyToFlux(object : ParameterizedTypeReference<Map<String, Any?>>() {})
                .blockLast(Duration.ofSeconds(30))
                ?.let {
                    (it.get("result") as HashMap<*, *>).let { r ->
                        (r["data"] as? List<Map<String, Any?>>).takeIf { it != null && it.isNotEmpty() }?.let { d ->
                            results.addAll(d)
                        }
                    }
                }

        log.info("[TEST] member api get after")
        log.info("[TEST] member api get check size - ${results.size}")
        log.info("[TEST] member api results check - ${results[0]}")

        Assert.notEmpty(results, "results size check")
    }
}