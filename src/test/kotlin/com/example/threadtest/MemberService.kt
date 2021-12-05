package com.example.threadtest

import org.slf4j.LoggerFactory
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.codec.ClientCodecConfigurer
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient
import java.security.SecureRandom
import java.time.Duration

class MemberService {

    private val log = LoggerFactory.getLogger(this::class.java)

    companion object {
        const val BASE_URL = "https://api-pre-prod.raidea.io"
        const val MEMBER_API = "/user-service/v1.1/"
        const val IDRIVE_TOKEN = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdWQiOlsib2F1dGgyLXJlc291cmNlIl0sInBlcm1pc3Npb25MZXZlbCI6MCwic29JZCI6ImlEcml2ZSIsImFwaV9rZXkiOnRydWUsInVzZXJfbmFtZSI6ImlEcml2ZUFwaUtleSIsImV4cCI6Mzc1MjEyNTQ0NCwiYXV0aG9yaXRpZXMiOlsiUk9MRV9DTElFTlRfaURyaXZlQ3MiXSwianRpIjoiNWVhOTFlNDEtYmE4Ni00YmFiLTk3NjAtMmI1NmUxMjM5NTBjIiwiY2xpZW50X2lkIjoiaURyaXZlQ3MifQ.Nb8lreht9zSY9J4gjsV0RVa9TRK9Ho7_2fFXYyjoexQ"
    }

    fun getMember(page: Int, size: Int) : ArrayList<Map<String, Any?>>{
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

        return results
    }

    fun anyBlockingTask(dto : Dto): Long {
        try {
            // sleep 0 ~ 10s
            Thread.sleep(SecureRandom().nextInt(10000).toLong());
            log.info("anyBlockingTask -> ${Thread.currentThread().name}, seq -> ${dto.seq}")
        } catch (e : Exception) {
            Thread.currentThread().interrupt()
        }
        return dto.seq
    }
}