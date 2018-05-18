package promeureka

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient

@EnableDiscoveryClient
@SpringBootApplication
open class PrometheusEurekaApplication

fun main(args: Array<String>) {
    SpringApplication.run(PrometheusEurekaApplication::class.java, *args)
}