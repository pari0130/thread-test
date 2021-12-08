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

    fun getMember() : ArrayList<Map<String, Any?>>{
        val results = ArrayList<Map<String, Any?>>()
        for(i in 0..99) {
            results.add(hashMapOf("userUuid" to "be14d441-79b4-4cef-8547-d18d8419bad-$i"))
        }
        Thread.sleep(SecureRandom().nextInt(10000).toLong());
        log.info("[TEST] member api get check size - ${results.size}")
        log.info("[TEST] member api results check - ${results[0]}")
        return results
    }

    fun getMemberBlockingTask(dto : Dto): Long {
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