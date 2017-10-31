package cloudmetrics.server.grafana.datasource

import cloudmetrics.server.metrics.Metric
import com.fasterxml.jackson.databind.ObjectMapper
import org.elasticsearch.common.settings.Settings
import org.elasticsearch.common.transport.InetSocketTransportAddress
import org.elasticsearch.common.xcontent.XContentType
import org.elasticsearch.transport.client.PreBuiltTransportClient
import org.springframework.cloud.stream.annotation.StreamListener
import org.springframework.stereotype.Component

import java.text.SimpleDateFormat

@Component
class GrafanaElasticSearchMaterializedView {

    def settings = Settings.builder().put("cluster.name", "cloud_metrics").build()

    def client = new PreBuiltTransportClient(settings)
            .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("localhost"), 9300))

    private final def grafanaTimestampFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")

    @StreamListener('metrics')
    void materialize(Metric metric) {
        println metric.key

        grafanaTimestampFormat.setTimeZone(TimeZone.getTimeZone('GMT'))
        def payload = [value: metric.value, '@timestamp': metric.timestamp]
        def json = new ObjectMapper().setDateFormat(grafanaTimestampFormat).writeValueAsString(payload)
        client.prepareIndex(metric.key, 'type').setSource(json, XContentType.JSON).get()
    }

}