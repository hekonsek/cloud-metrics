package cloudmetrics.server

import cloudmetrics.server.document.DocumentService
import cloudmetrics.server.document.IgniteDocumentService
import cloudmetrics.server.lib.grafana.GrafanaDashboardService
import cloudmetrics.server.lib.grafana.GrafanaService
import cloudmetrics.server.lib.grafana.RestGrafanaService
import cloudmetrics.server.metrics.MetricsConsumer
import cloudmetrics.server.metrics.MetricsProducer
import cloudmetrics.server.metrics.MetricsService
import cloudmetrics.server.telegraf.TelegrafService
import com.github.hekonsek.spring.boot.docker.spotify.NamedContainer
import com.github.hekonsek.spring.boot.docker.spotify.SpotifyDockerAutoConfiguration
import com.google.common.io.Files
import com.spotify.docker.client.messages.ContainerConfig
import com.spotify.docker.client.messages.HostConfig
import io.debezium.kafka.KafkaCluster
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.cloud.stream.annotation.EnableBinding
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import reactor.core.Environment
import reactor.io.encoding.StandardCodecs
import reactor.net.netty.udp.NettyDatagramServer
import reactor.net.udp.DatagramServer
import reactor.net.udp.spec.DatagramServerSpec
import reactor.spring.context.config.EnableReactor

import static com.spotify.docker.client.messages.HostConfig.RestartPolicy.unlessStopped
import static json4dummies.Json.fromJson

@SpringBootApplication
@EnableBinding([MetricsProducer, MetricsConsumer])
@EnableReactor
@Import(SpotifyDockerAutoConfiguration)
class CloudMetricsServer {

    @Bean(initMethod = 'startup', destroyMethod = 'shutdown')
    KafkaCluster kafkaCluster() {
        new KafkaCluster().
                withPorts(2181, 9092).
                usingDirectory(new File("/tmp/kaf1")).deleteDataPriorToStartup(true).
                addBrokers(3)
    }

    @Bean
    GrafanaService grafanaService() {
        new RestGrafanaService('admin', 'admin')
    }

    @Bean
    GrafanaDashboardService grafanaDashboardService(DocumentService documentService, GrafanaService grafanaService) {
        new GrafanaDashboardService(documentService, grafanaService)
    }

    @Bean(initMethod = 'start')
    DocumentService documentService() {
        new IgniteDocumentService(Files.createTempDir())
    }

    @Bean
    DatagramServer datagramServer(Environment environment, TelegrafService telegrafService, MetricsService metricsService) {
        final DatagramServer<byte[], byte[]> server = new DatagramServerSpec<byte[], byte[]>(NettyDatagramServer.class)
                .listen(8000)
        .env(environment)
                .codec(StandardCodecs.BYTE_ARRAY_CODEC)
                .consumeInput{
            def metric = telegrafService.importMetric(fromJson(it))
            if(metric != null) {
                metricsService.appendMetric(metric)
            }
        }.get()

        server.start().await()
        server
    }

    @Bean
    NamedContainer grafanaContainer() {
        def hostConfig = HostConfig.builder().networkMode('host').restartPolicy(unlessStopped()).build()
        new NamedContainer('grafana', ContainerConfig.builder().image('grafana/grafana').
                addVolume('cloud-metrics-grafana:/var/lib/grafana').hostConfig(hostConfig).build())
    }

    static void main(String[] args) {
        new SpringApplicationBuilder(CloudMetricsServer).run()
    }

}