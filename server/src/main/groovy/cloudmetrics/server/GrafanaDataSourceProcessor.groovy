package cloudmetrics.server

import cloudmetrics.server.grafana.ElasticSearchDataSourceBuilder
import cloudmetrics.server.grafana.EntityAlreadyExistsException
import cloudmetrics.server.grafana.RestGrafanaService

class GrafanaDataSourceProcessor {

    private final RestGrafanaService grafanaService

    private Set<String> existingMetrics = new LinkedHashSet<>()

    GrafanaDataSourceProcessor(RestGrafanaService grafanaService) {
        this.grafanaService = grafanaService
    }

    void process(Metric metric) {
        if(!existingMetrics.contains(metric.key)) {
            try {
                grafanaService.create('datasources', new ElasticSearchDataSourceBuilder(metric.key, metric.key).build())
            } catch (EntityAlreadyExistsException e) {
                existingMetrics << metric.key
            }
            existingMetrics << metric.key
        }
    }

}