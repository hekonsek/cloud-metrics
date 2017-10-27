package cloudmetrics.server

import io.vertx.core.Vertx
import io.vertx.core.json.Json
import org.apache.commons.lang3.RandomUtils

class Test {

    static void main(String[] args) {
        def vertx = Vertx.vertx()
        new CloudMetricsServer(vertx)
        vertx.setPeriodic(1000) {
            vertx.eventBus().send('metrics.append', Json.encode(new Metric(new Date(), 'node.node1.metric1', RandomUtils.nextDouble(80, 100))))
            vertx.eventBus().send('metrics.append', Json.encode(new Metric(new Date(), 'node.node2.metric1', RandomUtils.nextDouble(70, 90))))
        }
    }

}
