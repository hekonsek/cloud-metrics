package cloudmetrics.server

import io.vertx.core.Vertx
import io.vertx.core.datagram.DatagramSocketOptions

import java.util.concurrent.LinkedBlockingQueue

class CloudMetricsServer {

    static void main(String[] args) {
        System.setProperty("es.set.netty.runtime.available.processors", "false");
        def vertx = Vertx.vertx()

        def queue = new LinkedBlockingQueue<Metric>()

        vertx.deployVerticle(new MetricsAppendVerticle(queue))
        vertx.deployVerticle(new TelegrafImportVerticle())

        new MetricsProcessorVerticle(queue, 'eyJrIjoiRlNobFE0WmF3Qmh1SE12REFkWUN0TzhTSnhrVmg3ZnUiLCJuIjoiZmRmZGYiLCJpZCI6MX0=')

        def socket = vertx.createDatagramSocket(new DatagramSocketOptions());
        socket.listen(8000, "0.0.0.0") { asyncResult ->
            if (asyncResult.succeeded()) {
                socket.handler {
                    vertx.eventBus().send('telegraf.import', it.data().toString())
                }
            } else {
                System.out.println("Listen failed" + asyncResult.cause());
            }
        }
    }

}