package cloudmetrics.server.metrics

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.support.GenericMessage
import org.springframework.stereotype.Service

@Service
class MetricsService {

    @Autowired
    MetricsProducer metricsProducer

    void appendMetric(Metric metric) {
        def headers = [:]
        headers[KafkaHeaders.MESSAGE_KEY] = metric.key
        metricsProducer.metricsProducer().send(new GenericMessage(metric, headers))
    }

}
