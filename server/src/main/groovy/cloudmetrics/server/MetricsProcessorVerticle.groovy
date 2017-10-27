package cloudmetrics.server

import io.vertx.core.AbstractVerticle
import io.vertx.core.Future

import java.util.concurrent.BlockingQueue

import static java.util.concurrent.TimeUnit.SECONDS

class MetricsProcessorVerticle extends AbstractVerticle {

    private final BlockingQueue<Metric> queue

    private final elasticSearchProcessor = new ElasticSearchProcessor()

    private final GrafanaDataSourceProcessor grafanaDataSourceProcessor

    private final GrafanaDiagramProcessor grafanaDiagramProcessor

    MetricsProcessorVerticle(BlockingQueue<Metric> queue, GrafanaDataSourceProcessor grafanaDataSourceProcessor, GrafanaDiagramProcessor grafanaDiagramProcessor) {
        this.queue = queue
        this.grafanaDataSourceProcessor = grafanaDataSourceProcessor
        this.grafanaDiagramProcessor = grafanaDiagramProcessor
    }

    @Override
    void start(Future<Void> startFuture) {
        vertx.setPeriodic(250) {
            def metric = queue.poll()
            if (metric != null) {
                elasticSearchProcessor.process(metric)
                grafanaDataSourceProcessor.process(metric)
                grafanaDiagramProcessor.process(metric)
            }
        }

        startFuture.complete()
    }

}
