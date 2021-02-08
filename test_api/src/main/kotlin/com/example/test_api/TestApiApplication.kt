package com.example.test_api

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController


@SpringBootApplication
@RestController
class TestApiApplication {
    @GetMapping("test")
    fun test(): String {
        return "Hello, Feign!"
    }
}

fun main(args: Array<String>) {
    runApplication<TestApiApplication>(*args)
}