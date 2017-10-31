package cloudmetrics.server

import cloudmetrics.server.metrics.MetricsProducer
import io.debezium.kafka.KafkaCluster
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.cloud.stream.annotation.EnableBinding
import org.springframework.context.annotation.Bean

@SpringBootApplication
@EnableBinding(MetricsProducer)
class CloudMetricsServer {

//    CloudMetricsServer(Vertx vertx) {
//        System.setProperty("es.set.netty.runtime.available.processors", "false")
//
//        vertx.deployVerticle(new MetricsAppendVerticle())
//        vertx.deployVerticle(new ImportTelegrafVerticle())
//
//        def grafanaService = new RestGrafanaService('eyJrIjoiRlNobFE0WmF3Qmh1SE12REFkWUN0TzhTSnhrVmg3ZnUiLCJuIjoiZmRmZGYiLCJpZCI6MX0=')
//        def grafanaDashboardService = new GrafanaDashboardService(new IgniteDocumentService(Files.createTempDir()).start(), grafanaService)
//        def processorVerticle = new MetricsProcessorVerticle(new GrafanaDataSourceProcessor(grafanaService), new GrafanaDiagramProcessor(grafanaDashboardService))
//        vertx.deployVerticle(processorVerticle, new DeploymentOptions().setWorker(true))
//        vertx.deployVerticle(new GrafanaElasticSearchDataSourceVerticle())
//
//        def socket = vertx.createDatagramSocket(new DatagramSocketOptions());
//        socket.listen(8000, "0.0.0.0") { asyncResult ->
//            if (asyncResult.succeeded()) {
//                socket.handler {
//                    def telegrafMetric = new ObjectMapper().readValue(it.data().bytes, Map)
//                    def importedMetric = telegrafService.importMetric(telegrafMetric)
//                    if(importedMetric != null) {
//                        vertx.eventBus().send('metrics.append', Json.encode(importedMetric))
//                    }
//                }
//            } else {
//                throw asyncResult.cause()
//            }
//        }
//    }

    @Bean(initMethod = 'startup', destroyMethod = 'shutdown')
    KafkaCluster kafkaCluster() {
        new KafkaCluster().
                withPorts(2181, 9092).
                usingDirectory(new File("/tmp/kaf1")).deleteDataPriorToStartup(true).
                addBrokers(1)
    }

    static void main(String[] args) {
        new SpringApplicationBuilder(CloudMetricsServer).run()
    }

}