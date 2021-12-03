package com.example.threadtest

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ThreadTestApplication

fun main(args: Array<String>) {
    runApplication<ThreadTestApplication>(*args)
}
