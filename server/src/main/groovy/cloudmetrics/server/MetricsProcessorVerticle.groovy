package cloudmetrics.server

import io.vertx.core.AbstractVerticle
import io.vertx.core.Future

import java.util.concurrent.BlockingQueue

import static java.util.concurrent.TimeUnit.SECONDS

class MetricsProcessorVerticle extends AbstractVerticle {

    private final BlockingQueue<Metric> queue

    private final elasticSearchProcessor = new ElasticSearchProcessor()

    private final GrafanaDataSourceProcessor grafanaDataSourceProcessor

    MetricsProcessorVerticle(BlockingQueue<Metric> queue, GrafanaDataSourceProcessor grafanaDataSourceProcessor) {
        this.queue = queue
        this.grafanaDataSourceProcessor = grafanaDataSourceProcessor
    }

    @Override
    void start(Future<Void> startFuture) {
        vertx.executeBlocking {
            while (true) {
                try {
                    def metric = queue.poll(5, SECONDS)
                    if (metric != null) {
                        elasticSearchProcessor.process(metric)
                        grafanaDataSourceProcessor.process(metric)
                    }
                } catch (Exception e) {
                    e.printStackTrace()
                }
            }
        } {}
        startFuture.complete()
    }

}
