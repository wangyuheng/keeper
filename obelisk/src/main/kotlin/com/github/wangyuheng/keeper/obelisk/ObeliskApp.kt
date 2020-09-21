package com.github.wangyuheng.keeper.obelisk

import okhttp3.OkHttpClient
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.stereotype.Component
import java.time.Duration

@SpringBootApplication
@EnableScheduling
open class ObeliskApp

fun main(args: Array<String>) {
    runApplication<ObeliskApp>(*args)
}

@Component
open class WebConfig {
    private val timeout: Duration = Duration.ofSeconds(60)

    @Bean
    fun okHttpClient() = OkHttpClient().newBuilder()
            .callTimeout(timeout)
            .connectTimeout(timeout)
            .readTimeout(timeout)
            .writeTimeout(timeout)
            .build()
}


