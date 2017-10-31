package cloudmetrics.server.metrics

import org.springframework.cloud.stream.annotation.Output
import org.springframework.messaging.MessageChannel

interface MetricsProducer {

    @Output("metrics")
    MessageChannel metricsProducer()

}