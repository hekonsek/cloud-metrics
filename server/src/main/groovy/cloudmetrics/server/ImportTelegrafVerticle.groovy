package cloudmetrics.server

import com.fasterxml.jackson.databind.ObjectMapper
import io.vertx.core.AbstractVerticle
import io.vertx.core.Future

class ImportTelegrafVerticle extends AbstractVerticle {

    static final INPUT = 'import.telegraf'

    @Override
    void start(Future<Void> startFuture) throws Exception {
        vertx.eventBus().consumer(INPUT) { importTelegrafEvent ->
            def telegrafMetric = new ObjectMapper().readValue(importTelegrafEvent.body() as String, Map)
            Metric metric
            if(telegrafMetric.name == 'cpu') {
                def timestamp = new Date((telegrafMetric.timestamp as long) * 1000)
                def key = "node.${(telegrafMetric.tags.host as String).replaceAll(/\./, '_')}.cpu"
                metric = new Metric(timestamp, key, telegrafMetric.fields.usage_active)
            }
            if(metric != null) {
                vertx.eventBus().send('metrics.append', new ObjectMapper().writeValueAsString(metric))
                importTelegrafEvent.reply('{"status": "OK"}')
            } else {
                importTelegrafEvent.reply('{"status": "UNKNOWN"}')
            }
        }
        startFuture.complete()
    }

}
