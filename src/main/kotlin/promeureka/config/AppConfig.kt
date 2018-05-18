package promeureka.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.context.config.annotation.RefreshScope
import org.springframework.stereotype.Component
import java.io.File

@Component
@RefreshScope
open class AppConfig constructor(@Value("\${prom-eureka.json-file.path}") private val filePath: String) {

    fun getAbsoluteFileName(): String =
            if (filePath.endsWith(File.separatorChar)) filePath.trimEnd(File.separatorChar) else filePath
}