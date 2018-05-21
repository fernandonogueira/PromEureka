package promeureka.scheduled

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.cloud.client.discovery.DiscoveryClient
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import promeureka.config.AppConfig
import promeureka.model.PrometheusItem
import promeureka.model.PrometheusLabel
import promeureka.model.ServiceInstance
import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths

@Component
open class EurekaPrometheusInstanceExposeWorker
@Autowired constructor(val client: DiscoveryClient,
                       @Qualifier("prettyObjectMapper") val mapper: ObjectMapper,
                       val config: AppConfig) {

    @Scheduled(fixedDelay = 60000, initialDelay = 20000)
    fun run() {
        val pairs = getInstancesByService(client)
        val items = getPrometheusItems(pairs)

        persist(items)
    }

    private fun getInstancesByService(client: DiscoveryClient) = client
            .services
            ?.also { service -> LOGGER.info("Retrieved Services: {}", service) }
            ?.map { service ->
                Pair(
                        service,
                        client.getInstances(service).map { ServiceInstance(it.host, it.port) }
                )
            }
            ?.onEach { pair -> LOGGER.info("[{}] - Retrieved Instances: {}", pair.first, pair.second) }
            ?.map { pair -> Pair(pair.first, pair.second) }

    private fun getPrometheusItems(pairs: List<Pair<String, List<ServiceInstance>>>?) = pairs
            ?.map { pair ->
                PrometheusItem(
                        PrometheusLabel(pair.first),
                        pair.second.map { instance -> "${instance.host}:${instance.port}" }
                )
            }

    private fun persist(items: List<PrometheusItem>?) {
        val json = mapper.writeValueAsString(items)
        val destination = Paths.get(
                "${config.getAbsoluteFileName()}${File.separatorChar}eureka-instances.json"
        )

        LOGGER.info("JSON parsed: {}", json)

        Files.write(destination, json.toByteArray(StandardCharsets.UTF_8))

        LOGGER.info("File created. Path: {}", destination)
    }

    companion object {

        @JvmStatic
        private val LOGGER = LoggerFactory.getLogger(EurekaPrometheusInstanceExposeWorker::class.java)
    }
}