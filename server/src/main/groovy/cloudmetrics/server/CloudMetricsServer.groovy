package cloudmetrics.server

import cloudmetrics.server.document.DocumentService
import cloudmetrics.server.document.IgniteDocumentService
import cloudmetrics.server.grafana.GrafanaDashboardService
import cloudmetrics.server.grafana.GrafanaService
import cloudmetrics.server.grafana.RestGrafanaService
import cloudmetrics.server.metrics.MetricsConsumer
import cloudmetrics.server.metrics.MetricsProducer
import com.google.common.io.Files
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.cloud.stream.annotation.EnableBinding
import org.springframework.context.annotation.Bean

@SpringBootApplication
@EnableBinding([MetricsProducer, MetricsConsumer])
class CloudMetricsServer {

//    @Bean(initMethod = 'startup', destroyMethod = 'shutdown')
//    KafkaCluster kafkaCluster() {
//        new KafkaCluster().
//                withPorts(2181, 9092).
//                usingDirectory(new File("/tmp/kaf1")).deleteDataPriorToStartup(true).
//                addBrokers(1)
//    }

    @Bean
    GrafanaService grafanaService() {
        new RestGrafanaService('eyJrIjoiRlNobFE0WmF3Qmh1SE12REFkWUN0TzhTSnhrVmg3ZnUiLCJuIjoiZmRmZGYiLCJpZCI6MX0=')
    }

    @Bean
    GrafanaDashboardService grafanaDashboardService(DocumentService documentService, GrafanaService grafanaService) {
        new GrafanaDashboardService(documentService, grafanaService)
    }

    @Bean(initMethod = 'start')
    DocumentService documentService() {
        new IgniteDocumentService(Files.createTempDir())
    }

    static void main(String[] args) {
        new SpringApplicationBuilder(CloudMetricsServer).run()
    }

}