package cloudmetrics.server.telegraf

import cloudmetrics.server.Metric
import org.springframework.stereotype.Service

@Service
class TelegrafService {

    Metric importMetric(Map<String, Object> telegrafMetric) {
        if(telegrafMetric.name == 'cpu') {
            def timestamp = new Date((telegrafMetric.timestamp as long) * 1000)
            def key = "node.${(telegrafMetric.tags.host as String).replaceAll(/\./, '_')}.cpu"
            return new Metric(timestamp, key, telegrafMetric.fields.usage_active)
        }
        null
    }

}
