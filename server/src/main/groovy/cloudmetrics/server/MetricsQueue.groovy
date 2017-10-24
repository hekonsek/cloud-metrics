package cloudmetrics.server

import cloudmetrics.server.grafana.GrafanaService

import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

import static java.util.concurrent.Executors.newFixedThreadPool

class MetricsQueue {

    private final queue = new LinkedBlockingQueue<Metric>()

    private final executor = newFixedThreadPool(5)

    private final elasticSearchProcessor = new ElasticSearchProcessor()

    private final GrafanaDataSourceProcessor grafanaDataSourceProcessor

    MetricsQueue(String grafanaApiKey) {
        grafanaDataSourceProcessor = new GrafanaDataSourceProcessor(new GrafanaService(grafanaApiKey))

        5.times {
            executor.submit(new Runnable() {
                @Override
                void run() {
                    while (true) {
                        try {
                            def metric = queue.poll(5, TimeUnit.SECONDS)
                            if (metric != null) {
                                elasticSearchProcessor.process(metric)
                                grafanaDataSourceProcessor.process(metric)
                            }
                        } catch (Exception e) {
                            e.printStackTrace()
                        }
                    }
                }
            })
        }
    }

    void append(Metric metric) {
        queue.add(metric)
    }


}
