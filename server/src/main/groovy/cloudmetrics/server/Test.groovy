package cloudmetrics.server

import cloudmetrics.server.metrics.Metric
import cloudmetrics.server.metrics.MetricsService
import org.apache.commons.lang3.RandomUtils
import org.springframework.boot.builder.SpringApplicationBuilder

import java.util.concurrent.TimeUnit

class Test {

    static void main(String[] args) {
        def metricsService = new SpringApplicationBuilder(CloudMetricsServer).run().getBean(MetricsService)

        10000.times {
            metricsService.appendMetric(new Metric(new Date(), 'node.node1.metric1', RandomUtils.nextDouble(60, 80)))
            metricsService.appendMetric(new Metric(new Date(), 'node.node2.metric1', RandomUtils.nextDouble(70, 90)))
            metricsService.appendMetric(new Metric(new Date(), 'node.node3.metric1', RandomUtils.nextDouble(80, 100)))

            TimeUnit.SECONDS.sleep(1)
        }
    }

}
