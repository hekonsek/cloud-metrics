package cloudmetrics.server.metrics

import org.springframework.cloud.stream.annotation.Input
import org.springframework.messaging.SubscribableChannel

interface MetricsConsumer {

    String INPUT = 'metricsConsumer'

    @Input('metricsConsumer')
    SubscribableChannel metricsConsumer()

}