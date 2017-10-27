package cloudmetrics.server

import com.fasterxml.jackson.databind.ObjectMapper
import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import org.elasticsearch.common.settings.Settings
import org.elasticsearch.common.transport.InetSocketTransportAddress
import org.elasticsearch.common.xcontent.XContentType
import org.elasticsearch.transport.client.PreBuiltTransportClient

import java.text.SimpleDateFormat

import static io.vertx.core.json.Json.decodeValue

class GrafanaElasticSearchDataSourceVerticle extends AbstractVerticle {

    def settings = Settings.builder().put("cluster.name", "cloud_metrics").build()

    def client = new PreBuiltTransportClient(settings)
            .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("localhost"), 9300))

    private final def grafanaTimestampFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")

    @Override
    void start(Future<Void> startFuture) {
        grafanaTimestampFormat.setTimeZone(TimeZone.getTimeZone('GMT'))

        vertx.eventBus().consumer('metrics.queue') {
            def metric = decodeValue(it.body() as String, Metric)
            def payload = [value: metric.value, '@timestamp': metric.timestamp]
            def json = new ObjectMapper().setDateFormat(grafanaTimestampFormat).writeValueAsString(payload)
            client.prepareIndex(metric.key, 'type').setSource(json, XContentType.JSON).get()
        }
        startFuture.complete()
    }

}
