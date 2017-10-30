package cloudmetrics.server

import com.fasterxml.jackson.databind.ObjectMapper
import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.kafka.admin.AdminUtils
import io.vertx.kafka.client.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.BytesDeserializer
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.utils.Bytes
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

        AdminUtils.create(Vertx.vertx(), 'localhost:2182').createTopic('metrics', 1, 1) {
            def consumer = KafkaConsumer.create(vertx, ['bootstrap.servers': 'localhost:9092', 'key.deserializer': StringDeserializer.name, 'value.deserializer': BytesDeserializer.name, 'group.id': 'grafanaprocessor'])
            consumer.handler {
                def metric = decodeValue(new String((it.value() as Bytes).get()), Map)
                def payload = [value: metric.value, '@timestamp': metric.timestamp]
                def json = new ObjectMapper().setDateFormat(grafanaTimestampFormat).writeValueAsString(payload)
                client.prepareIndex(it.key() as String, 'type').setSource(json, XContentType.JSON).get()
            }
            consumer.subscribe('metrics')
            startFuture.complete()
        }
    }

}
