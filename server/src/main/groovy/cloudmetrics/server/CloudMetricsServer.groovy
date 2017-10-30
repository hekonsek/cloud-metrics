package cloudmetrics.server

import cloudmetrics.server.document.IgniteDocumentService
import cloudmetrics.server.grafana.GrafanaDashboardService
import cloudmetrics.server.grafana.RestGrafanaService
import com.google.common.io.Files
import io.debezium.kafka.KafkaCluster
import io.vertx.core.DeploymentOptions
import io.vertx.core.Vertx
import io.vertx.core.datagram.DatagramSocketOptions

class CloudMetricsServer {

    CloudMetricsServer() {
        this(Vertx.vertx())
    }

    CloudMetricsServer(Vertx vertx) {
        def props = new Properties()
        new KafkaCluster().usingDirectory(new File("/tmp/kaf1")).withPorts(2182, 9092).withKafkaConfiguration(props).deleteDataPriorToStartup(true).addBrokers(1).startup()

        System.setProperty("es.set.netty.runtime.available.processors", "false")

        vertx.deployVerticle(new MetricsAppendVerticle())
        vertx.deployVerticle(new ImportTelegrafVerticle())

        def grafanaService = new RestGrafanaService('eyJrIjoiRlNobFE0WmF3Qmh1SE12REFkWUN0TzhTSnhrVmg3ZnUiLCJuIjoiZmRmZGYiLCJpZCI6MX0=')
        def grafanaDashboardService = new GrafanaDashboardService(new IgniteDocumentService(Files.createTempDir()).start(), grafanaService)
        def processorVerticle = new MetricsProcessorVerticle(new GrafanaDataSourceProcessor(grafanaService), new GrafanaDiagramProcessor(grafanaDashboardService))
        vertx.deployVerticle(processorVerticle, new DeploymentOptions().setWorker(true))
        vertx.deployVerticle(new GrafanaElasticSearchDataSourceVerticle())

        def socket = vertx.createDatagramSocket(new DatagramSocketOptions());
        socket.listen(8000, "0.0.0.0") { asyncResult ->
            if (asyncResult.succeeded()) {
                socket.handler {
                    vertx.eventBus().send(ImportTelegrafVerticle.INPUT, it.data().toString())
                }
            } else {
                throw asyncResult.cause()
            }
        }
    }

    static void main(String[] args) {
        new CloudMetricsServer()
    }

}