package cloudmetrics.server

import com.fasterxml.jackson.databind.ObjectMapper
import io.vertx.core.AbstractVerticle
import io.vertx.core.Future

import java.util.concurrent.BlockingQueue

class MetricsAppendVerticle extends AbstractVerticle {

    private final BlockingQueue<Metric> metricsQueue

    MetricsAppendVerticle(BlockingQueue<Metric> metricsQueue) {
        this.metricsQueue = metricsQueue
    }

    @Override
    void start(Future<Void> startFuture) {
        vertx.eventBus().consumer('metrics.append') {
            metricsQueue << new ObjectMapper().readValue(it.body() as String, Metric)
            it.reply(null)
        }
        startFuture.complete()
    }

}
