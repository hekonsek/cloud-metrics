package cloudmetrics.server.grafana.dashboard

import cloudmetrics.server.grafana.GrafanaDashboardService
import cloudmetrics.server.metrics.Metric
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.stream.annotation.StreamListener
import org.springframework.stereotype.Component

@Component
class GrafanaDiagramProcessor {

    @Autowired
    private final GrafanaDashboardService grafanaDashboardService

    @StreamListener('metrics')
    void process(Metric metric) {
        grafanaDashboardService.plotMetric(metric)
    }

}