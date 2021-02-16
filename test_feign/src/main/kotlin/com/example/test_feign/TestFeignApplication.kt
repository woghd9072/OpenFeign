package com.example.test_feign

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@EnableFeignClients
@SpringBootApplication
class TestFeignApplication

fun main(args: Array<String>) {
    runApplication<TestFeignApplication>(*args)
}

@FeignClient(name = "feign-client", url = "http://localhost:8081")
interface Client {
    @GetMapping("/test")
    fun test(): String
}

@RestController
class Controller(private val client: Client) {
    @GetMapping
    fun testFeign(): String {
        return client.test()
    }
}