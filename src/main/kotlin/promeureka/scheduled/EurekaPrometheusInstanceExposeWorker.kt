package promeureka.scheduled

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.cloud.client.ServiceInstance
import org.springframework.cloud.client.discovery.DiscoveryClient
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import promeureka.config.AppConfig
import promeureka.model.PrometheusFileSDItem
import promeureka.model.PrometheusLabel
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.HashMap
import kotlin.collections.ArrayList


@Component
open class EurekaPrometheusInstanceExposeWorker
@Autowired constructor(val discoveryClient: DiscoveryClient,
                       @Qualifier("prettyObjectMapper") val objectMapper: ObjectMapper,
                       val appConfig: AppConfig) {

    private val log = LoggerFactory.getLogger(EurekaPrometheusInstanceExposeWorker::class.java)

    @Scheduled(fixedDelay = 60000, initialDelay = 20000)
    fun saveInstances() {

        val serviceInstancesMap = HashMap<String, List<ServiceInstance>>()

        val services = discoveryClient.services

        log.info("Services: {}", services)

        if (services != null && !services.isEmpty()) {

            log.info("{} services found. Retrieving instances...", services.size)

            services.forEach {
                val serviceInstances = discoveryClient.getInstances(it)
                if (serviceInstances != null && !serviceInstances.isEmpty()) {
                    log.info("{} instances found for service: [{}]", serviceInstances.size, it)
                    serviceInstancesMap.put(it, serviceInstances)
                } else {
                    log.info("No instances found for service: [{}]", it)
                }
            }

        } else {
            log.info("No services found.")
        }


        if (!serviceInstancesMap.isEmpty()) {
            log.info("Completed discovering. Result: {}", serviceInstancesMap)


            val prometheusItems = ArrayList<PrometheusFileSDItem>()

            serviceInstancesMap.entries.forEach {

                val item = PrometheusFileSDItem()

                val urlList = ArrayList<String>()

                it.value.forEach {
                    urlList.add("${it.host}:${it.port}")
                    log.info("uri [{}], " +
                            "secure? [{}], " +
                            "host: [{}], " +
                            "port: [{}], " +
                            "serviceId: [{}], " +
                            "metadata: [{}]", it.uri,
                            it.isSecure,
                            it.host,
                            it.port,
                            it.serviceId,
                            it.metadata)
                }

                item.targets = urlList
                item.labels = PrometheusLabel(it.key)

                prometheusItems.add(item)
            }

            if (!prometheusItems.isEmpty()) {
                log.info("Items parsed... Converting to JSON...")
                val json = objectMapper.writeValueAsString(prometheusItems)
                log.info("Converted JSON: {}", json)
                val createdFilePath: Path = Files.createTempFile("prometheus-eureka-instances", ".json")
                Files.write(createdFilePath, json.toByteArray(StandardCharsets.UTF_8))
                log.info("File created. Path: {}", createdFilePath.toFile().absolutePath)

                val destination = "${appConfig.getAbsoluteFileName()}/eureka-instances.json"
                log.info("Moving file to destination: {}", destination)
                Files.move(createdFilePath,
                        Paths.get("${appConfig.getAbsoluteFileName()}/eureka-instances.json"),
                        StandardCopyOption.REPLACE_EXISTING)
                log.info("File moved.")
            }

        }

    }

}