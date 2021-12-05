package com.example.threadtest

import java.security.SecureRandom

class Utils {
    fun random() : Long {
        return SecureRandom().nextInt(100).toLong()
    }
}