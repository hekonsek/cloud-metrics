package cloudmetrics.server

import cloudmetrics.server.grafana.GrafanaService
import io.vertx.core.Vertx
import io.vertx.core.datagram.DatagramSocketOptions

import java.util.concurrent.LinkedBlockingQueue

class CloudMetricsServer {

    static void main(String[] args) {
        System.setProperty("es.set.netty.runtime.available.processors", "false")

        def vertx = Vertx.vertx()

        def queue = new LinkedBlockingQueue<Metric>()

        vertx.deployVerticle(new MetricsAppendVerticle(queue))
        vertx.deployVerticle(new ImportTelegrafVerticle())
        def grafanaProcessor = new GrafanaDataSourceProcessor(new GrafanaService('eyJrIjoiRlNobFE0WmF3Qmh1SE12REFkWUN0TzhTSnhrVmg3ZnUiLCJuIjoiZmRmZGYiLCJpZCI6MX0='))
        5.times {
            vertx.deployVerticle(new MetricsProcessorVerticle(queue, grafanaProcessor))
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

}