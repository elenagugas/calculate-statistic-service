package com.gugas

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class HelloFreshApplication

fun main(args: Array<String>) {
    runApplication<HelloFreshApplication>(*args)
}
