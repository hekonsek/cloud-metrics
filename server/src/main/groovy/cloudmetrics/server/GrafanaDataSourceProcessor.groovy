package cloudmetrics.server

import cloudmetrics.server.grafana.ElasticSearchDataSourceBuilder
import cloudmetrics.server.grafana.EntityAlreadyExistsException
import cloudmetrics.server.grafana.GrafanaService

class GrafanaDataSourceProcessor {

    private final GrafanaService grafanaService

    private Set<String> existingMetrics = new LinkedHashSet<>()

    GrafanaDataSourceProcessor(GrafanaService grafanaService) {
        this.grafanaService = grafanaService
    }

    void process(Metric metric) {
        if(!existingMetrics.contains(metric.key)) {
            try {
                grafanaService.create(new ElasticSearchDataSourceBuilder(metric.key, metric.key).build())
            } catch (EntityAlreadyExistsException e) {
                existingMetrics << metric.key
            }
            existingMetrics << metric.key
        }
    }

}