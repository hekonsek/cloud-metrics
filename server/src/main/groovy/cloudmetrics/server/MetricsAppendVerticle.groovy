package cloudmetrics.server

import io.vertx.core.AbstractVerticle
import io.vertx.core.Future

class MetricsAppendVerticle extends AbstractVerticle {

    @Override
    void start(Future<Void> startFuture) {
        vertx.eventBus().consumer('metrics.append') {
            vertx.eventBus().publish('metrics.queue', it.body())
            it.reply(null)
        }
        startFuture.complete()
    }

}
