package cloudmetrics.server

import com.fasterxml.jackson.databind.ObjectMapper
import io.vertx.core.AbstractVerticle
import io.vertx.core.Future

class TelegrafImportVerticle extends AbstractVerticle {

    @Override
    void start(Future<Void> startFuture) throws Exception {
        vertx.eventBus().consumer('telegraf.import') { telegrafImportEvent ->
            def telegrafMetric = new ObjectMapper().readValue(telegrafImportEvent.body() as String, Map)
            Metric metric
            if(telegrafMetric.name == 'cpu') {
                def key = "node.${(telegrafMetric.tags.host as String).replaceAll(/\./, '_')}.cpu"
                metric = new Metric(new Date((telegrafMetric.timestamp as long) * 1000), key, telegrafMetric.fields.usage_active)
            }
            if(metric != null) {
                vertx.eventBus().send('metrics.append', new ObjectMapper().writeValueAsString(metric)) {
                    telegrafImportEvent.reply(null)
                }
            }
        }
        startFuture.complete()
    }

}
