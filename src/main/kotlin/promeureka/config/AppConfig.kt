package promeureka.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.context.config.annotation.RefreshScope
import org.springframework.stereotype.Component

@Component
@RefreshScope
open class AppConfig {

    @Value("\${prometheus.jsonFolder}")
    var jsonFolder: String? = null

    fun getAbsoluteFileName(): String {
        jsonFolder?.let {
            if (it.endsWith("/") || it.endsWith("\\")) {
                return it.trimEnd('/').trimEnd('\\')
            } else {
                return it
            }
        }
        throw RuntimeException("Error! Invalid prometheus.jsonFolder! Value: " + jsonFolder)
    }

}