package com.example.demo

import feign.Logger
import feign.Response
import feign.Retryer
import feign.codec.ErrorDecoder
import org.springframework.context.annotation.Bean

class DemoFeignConfiguration {
    @Bean
    fun feignRetryer(): Retryer {
        return Retryer.Default(1000, 3000, 3)
    }

    @Bean
    fun feignErrorDecoder(): ErrorDecoder {
        return DemoErrorDecoder()
    }

    @Bean
    fun feignLoggerLevel(): Logger.Level {
        return Logger.Level.BASIC
    }
}

class DemoErrorDecoder : ErrorDecoder {
    override fun decode(methodKey: String?, response: Response?): Exception {
        return when (response?.status()) {
            400 -> Exception("${response.status()} Error ${response.reason()}")
            404 -> Exception("${response.status()} Error ${response.reason()}")
            500 -> Exception("${response.status()} Error ${response.reason()}")
            else -> Exception("${response?.status()} Error ${response?.reason()}")
        }
    }
}