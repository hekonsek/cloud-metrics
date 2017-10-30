package cloudmetrics.server

import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.core.json.Json
import io.vertx.kafka.client.producer.KafkaProducer
import io.vertx.kafka.client.producer.KafkaProducerRecord
import org.apache.kafka.common.serialization.BytesSerializer
import org.apache.kafka.common.serialization.StringSerializer
import org.apache.kafka.common.utils.Bytes

import static io.vertx.core.json.Json.decodeValue

class MetricsAppendVerticle extends AbstractVerticle {

    @Override
    void start(Future<Void> startFuture) {
        def producer = KafkaProducer.create(vertx, ['bootstrap.servers': 'localhost:9092', 'key.serializer': StringSerializer.name, 'value.serializer': BytesSerializer.name])
        vertx.eventBus().consumer('metrics.append') { metricEvent ->
            def metric = decodeValue(metricEvent.body() as String, Metric)
            producer.write(KafkaProducerRecord.create('metrics', metric.key, new Bytes(Json.encode([timestamp: metric.timestamp, value: metric.value]).bytes))) {
                if(it.succeeded()) {
                    metricEvent.reply(null)
                } else {
                    metricEvent.fail(-1, it.cause().message)
                }
            }
        }
        startFuture.complete()
    }

}
