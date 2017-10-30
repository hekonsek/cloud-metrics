package cloudmetrics.server

import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.kafka.admin.AdminUtils
import io.vertx.kafka.client.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.BytesDeserializer
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.utils.Bytes

import static io.vertx.core.json.Json.decodeValue

class MetricsProcessorVerticle extends AbstractVerticle {

    private final GrafanaDataSourceProcessor grafanaDataSourceProcessor

    private final GrafanaDiagramProcessor grafanaDiagramProcessor

    MetricsProcessorVerticle(GrafanaDataSourceProcessor grafanaDataSourceProcessor, GrafanaDiagramProcessor grafanaDiagramProcessor) {
        this.grafanaDataSourceProcessor = grafanaDataSourceProcessor
        this.grafanaDiagramProcessor = grafanaDiagramProcessor
    }

    @Override
    void start(Future<Void> startFuture) {
        AdminUtils.create(Vertx.vertx(), 'localhost:2182').createTopic('metrics', 1, 1) {
            def consumer = KafkaConsumer.create(vertx, ['bootstrap.servers': 'localhost:9092', 'key.deserializer': StringDeserializer.name, 'value.deserializer': BytesDeserializer.name, 'group.id': 'metricsprocessor'])
            consumer.handler {
                def metricValue = decodeValue(new String((it.value() as Bytes).get()), Map)
                grafanaDataSourceProcessor.process(new Metric(new Date(metricValue.timestamp as long), it.key() as String, metricValue.value))
                grafanaDiagramProcessor.process(new Metric(new Date(metricValue.timestamp as long), it.key() as String, metricValue.value))
            }
            consumer.subscribe('metrics')
            startFuture.complete()
        }
    }

}
