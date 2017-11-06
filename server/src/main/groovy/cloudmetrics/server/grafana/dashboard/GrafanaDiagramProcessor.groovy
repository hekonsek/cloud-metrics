package cloudmetrics.server.grafana.dashboard

import cloudmetrics.server.lib.grafana.GrafanaDashboardService
import cloudmetrics.server.metrics.Metric
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.stream.annotation.StreamListener
import org.springframework.stereotype.Component

import static cloudmetrics.server.metrics.MetricsConsumer.INPUT

@Component
class GrafanaDiagramProcessor {

    @Autowired
    private final GrafanaDashboardService grafanaDashboardService

    @StreamListener(INPUT)
    void process(Metric metric) {
        grafanaDashboardService.plotMetric(metric)
    }

}