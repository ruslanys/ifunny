package me.ruslanys.ifunny.property

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import java.time.Duration

@ConstructorBinding
@ConfigurationProperties(prefix = "grab")
data class GrabProperties(
        val userAgent: String = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.14; rv:72.0) Gecko/20100101 Firefox/72.0",
        val retention: Retention = Retention()
) {

    data class Retention(
            val fullIndex: Duration = Duration.ofDays(7)
    )

}
