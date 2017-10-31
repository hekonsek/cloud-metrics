package cloudmetrics.server

import cloudmetrics.server.grafana.GrafanaDashboardService
import cloudmetrics.server.metrics.Metric

class GrafanaDiagramProcessor {

    private final GrafanaDashboardService grafanaDashboardService

    GrafanaDiagramProcessor(GrafanaDashboardService grafanaDashboardService) {
        this.grafanaDashboardService = grafanaDashboardService
    }

    void process(Metric metric) {
        grafanaDashboardService.plotMetric(metric)
    }

}