package cloudmetrics.server

import io.vertx.core.AbstractVerticle
import io.vertx.core.Future

import static io.vertx.core.json.Json.decodeValue

class MetricsProcessorVerticle extends AbstractVerticle {

    private final GrafanaDataSourceProcessor grafanaDataSourceProcessor

    private final GrafanaDiagramProcessor grafanaDiagramProcessor

    MetricsProcessorVerticle(GrafanaDataSourceProcessor grafanaDataSourceProcessor, GrafanaDiagramProcessor grafanaDiagramProcessor) {
        this.grafanaDataSourceProcessor = grafanaDataSourceProcessor
        this.grafanaDiagramProcessor = grafanaDiagramProcessor
    }

    @Override
    void start(Future<Void> startFuture) {
        vertx.eventBus().consumer('metrics.queue') {
            def metric = decodeValue(it.body() as String, Metric)
            grafanaDataSourceProcessor.process(metric)
            grafanaDiagramProcessor.process(metric)
        }
        startFuture.complete()
    }

}
