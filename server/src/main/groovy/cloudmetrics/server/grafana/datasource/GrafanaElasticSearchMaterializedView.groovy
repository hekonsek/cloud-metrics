package cloudmetrics.server.grafana.datasource

import cloudmetrics.server.metrics.Metric
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.kafka.common.serialization.BytesDeserializer
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.utils.Bytes
import org.elasticsearch.common.settings.Settings
import org.elasticsearch.common.transport.InetSocketTransportAddress
import org.elasticsearch.common.xcontent.XContentType
import org.elasticsearch.transport.client.PreBuiltTransportClient
import org.springframework.cloud.stream.annotation.StreamListener
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.support.GenericMessage
import org.springframework.stereotype.Component

import java.text.SimpleDateFormat

import static org.springframework.kafka.support.KafkaHeaders.MESSAGE_KEY

@Component
class GrafanaElasticSearchMaterializedView {

    def settings = Settings.builder().put("cluster.name", "cloud_metrics").build()

    def client = new PreBuiltTransportClient(settings)
            .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("localhost"), 9300))

    private final def grafanaTimestampFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")

    @StreamListener('metrics')
    void materialize(GenericMessage<Metric> metricEvent) {
        def metric = metricEvent.payload
        grafanaTimestampFormat.setTimeZone(TimeZone.getTimeZone('GMT'))
        def payload = [value: metric.value, '@timestamp': metric.timestamp]
        def json = new ObjectMapper().setDateFormat(grafanaTimestampFormat).writeValueAsString(payload)
        client.prepareIndex(metricEvent.headers.get(MESSAGE_KEY, String), 'type').setSource(json, XContentType.JSON).get()
    }

}