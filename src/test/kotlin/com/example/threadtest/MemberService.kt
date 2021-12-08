package com.example.threadtest

import org.slf4j.LoggerFactory
import java.security.SecureRandom

class MemberService {

    private val log = LoggerFactory.getLogger(this::class.java)

    fun getMember() : ArrayList<Dto>{
        val results = ArrayList<Dto>()
        for(i in 0..99) {
            results.add(Dto((i+1).toLong(), "be14d441-79b4-4cef-8547-d18d8419bad-$i"))
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