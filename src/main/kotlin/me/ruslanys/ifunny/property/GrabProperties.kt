package me.ruslanys.ifunny.property

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import java.time.Duration

@ConstructorBinding
@ConfigurationProperties(prefix = "grab")
data class GrabProperties(
        val userAgent: String = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.88 Safari/537.36",
        val retention: Retention = Retention()
) {

    data class Retention(
            val fullIndex: Duration = Duration.ofDays(1)
    )

}
