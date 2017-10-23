package cloudmetrics.server

import com.fasterxml.jackson.databind.ObjectMapper
import org.elasticsearch.common.settings.Settings
import org.elasticsearch.common.transport.InetSocketTransportAddress
import org.elasticsearch.common.xcontent.XContentType
import org.elasticsearch.transport.client.PreBuiltTransportClient

import java.text.SimpleDateFormat

class ElasticSearchProcessor {

    def settings = Settings.builder().put("cluster.name", "cloud_metrics").build()

    def client = new PreBuiltTransportClient(settings)
            .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("localhost"), 9300))

    void process(Metric metric) {
        def dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        def payload = [value: metric.value, '@timestamp': metric.timestamp]
        def json = new ObjectMapper().setDateFormat(dateFormat).writeValueAsString(payload)
        client.prepareIndex(metric.key, 'type').setSource(json, XContentType.JSON).get()
    }

}
