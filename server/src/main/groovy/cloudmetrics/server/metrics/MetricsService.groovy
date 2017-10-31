package cloudmetrics.server.metrics

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.integration.support.MessageBuilder
import org.springframework.stereotype.Service

@Service
class MetricsService {

    @Autowired
    MetricsProducer metricsProducer

    void appendMetric(Metric metric) {
        metricsProducer.metricsProducer().send(MessageBuilder.withPayload(metric).build())
    }

}
