package cloudmetrics.server.grafana.datasource

import cloudmetrics.server.grafana.ElasticSearchDataSourceBuilder
import cloudmetrics.server.grafana.EntityAlreadyExistsException
import cloudmetrics.server.grafana.GrafanaService
import cloudmetrics.server.metrics.Metric
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.stream.annotation.StreamListener
import org.springframework.stereotype.Component

@Component
class GrafanaDataSourceMaterializedView {

    @Autowired
    private final GrafanaService grafanaService

    private Set<String> existingMetrics = new LinkedHashSet<>()

    @StreamListener('metrics')
    void process(Metric metric) {
//        println metric.key
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