package com.example.test_feign

import feign.*
import feign.Util.*
import feign.codec.ErrorDecoder
import mu.KotlinLogging
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.TimeUnit

@Configuration
class TestConfig {
    @Bean
    fun feignRetryer(): Retryer {
        return Retryer.Default(1000, 1000, 3)
    }

    @Bean
    fun feignErrorDecoder(): ErrorDecoder {
        return FeignErrorDecoder()
    }

    @Bean
    fun feignLoggerLevel(): Logger.Level {
        return Logger.Level.FULL
    }

    @Bean
    fun feignRequestOption(): Request.Options {
        return Request.Options(1000, TimeUnit.MILLISECONDS, 5000, TimeUnit.MILLISECONDS, true)
    }

    @Bean
    fun feignRequestInterceptor(): RequestInterceptor {
        return RequestInterceptor { template -> template.header("key", "123456") }
    }

    @Bean
    fun feignLogger(): Logger {
        return FeignClientLogger()
    }
}

class FeignErrorDecoder : ErrorDecoder {
    override fun decode(methodKey: String?, response: Response?): Exception {
        return when (response?.status()) {
            in 500..599 -> RetryableException(response!!.status(), response.reason(), response.request().httpMethod(), null, response.request())
            else -> Exception("${response?.status()} Error ${response?.reason()}")
        }
    }
}

class FeignClientLogger : Logger() {
    private val logger = KotlinLogging.logger { }

    override fun logRequest(configKey: String?, logLevel: Level?, request: Request?) {
        if (request == null)
            return

        val feignRequest = FeignRequest()
        feignRequest.method = request.httpMethod().name
        feignRequest.url = request.url()
        for (field in request.headers().keys) {
            for (value in valuesOrEmpty(request.headers(), field)) {
                feignRequest.addHeader(field, value)
            }
        }

        if (request.body() != null) {
            feignRequest.body = String(request.body())
        }
        logger.info { feignRequest.toString() }
    }

    override fun logAndRebufferResponse(
        configKey: String?,
        logLevel: Level?,
        response: Response?,
        elapsedTime: Long
    ): Response? {
        if (response == null)
            return response

        val feignResponse = FeignResponse()
        val status = response.status()
        feignResponse.status = response.status()
        feignResponse.reason =
            (if (response.reason() != null && logLevel!! > Level.NONE) " " + response.reason() else "")
        feignResponse.duration = elapsedTime


        for (field in response.headers().keys) {
            for (value in valuesOrEmpty(response.headers(), field)) {
                feignResponse.addHeader(field, value)
            }
        }

        if (response.body() != null && !(status == 204 || status == 205)) {
            val bodyData: ByteArray = toByteArray(response.body().asInputStream())
            if (bodyData.isNotEmpty()) {
                feignResponse.body = decodeOrDefault(bodyData, UTF_8, "Binary data")
            }
            logger.info { feignResponse.toString() }
            return response.toBuilder().body(bodyData).build()
        } else {
            logger.info { feignResponse.toString() }
        }
        return response
    }

    override fun logRetry(configKey: String?, logLevel: Level?) {
        logger.info { "Retry $configKey" }
    }

    override fun log(p0: String?, p1: String?, vararg p2: Any?) {}
}

class FeignResponse {
    var status = 0
    var reason: String? = null
    var duration: Long = 0
    private val headers: MutableList<String> = mutableListOf()
    var body: String? = null

    fun addHeader(key: String?, value: String?) {
        headers.add("$key: $value")
    }

    override fun toString() =
        """{"type":"response","status":"$status","duration":"$duration","headers":$headers,"body":$body,"reason":"$reason"}"""
}

class FeignRequest {
    var method: String? = null
    var url: String? = null
    private val headers: MutableList<String> = mutableListOf()
    var body: String? = null

    fun addHeader(key: String?, value: String?) {
        headers.add("$key: $value")
    }

    override fun toString() =
        """{"type":"request","method":"$method","url":"$url","headers":$headers,"body":$body}"""
}