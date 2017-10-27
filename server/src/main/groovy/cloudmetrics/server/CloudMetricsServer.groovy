package cloudmetrics.server

import cloudmetrics.server.document.IgniteDocumentService
import cloudmetrics.server.grafana.GrafanaDashboardService
import cloudmetrics.server.grafana.RestGrafanaService
import com.google.common.io.Files
import io.vertx.core.DeploymentOptions
import io.vertx.core.Vertx
import io.vertx.core.datagram.DatagramSocketOptions

import java.util.concurrent.LinkedBlockingQueue

class CloudMetricsServer {

    CloudMetricsServer() {
        this(Vertx.vertx())
    }

    CloudMetricsServer(Vertx vertx) {
        System.setProperty("es.set.netty.runtime.available.processors", "false")

        def queue = new LinkedBlockingQueue<Metric>()

        vertx.deployVerticle(new MetricsAppendVerticle(queue))
        vertx.deployVerticle(new ImportTelegrafVerticle())

        def grafanaService = new RestGrafanaService('eyJrIjoiRlNobFE0WmF3Qmh1SE12REFkWUN0TzhTSnhrVmg3ZnUiLCJuIjoiZmRmZGYiLCJpZCI6MX0=')
        def grafanaDashboardService = new GrafanaDashboardService(new IgniteDocumentService(Files.createTempDir()).start(), grafanaService)
        def processorVerticle = new MetricsProcessorVerticle(queue, new GrafanaDataSourceProcessor(grafanaService), new GrafanaDiagramProcessor(grafanaDashboardService))
        25.times {
            vertx.deployVerticle(processorVerticle, new DeploymentOptions().setWorker(true))
        }

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