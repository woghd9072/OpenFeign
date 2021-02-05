package com.example.demo

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@EnableFeignClients
@SpringBootApplication
class DemoApplication

fun main(args: Array<String>) {
	runApplication<DemoApplication>(*args)
}

@FeignClient(name = "feign-client", url = "http://localhost:8080")
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

	@GetMapping("/test")
	fun helloFeign(): String {
		return "Hello, Feign!"
	}
}