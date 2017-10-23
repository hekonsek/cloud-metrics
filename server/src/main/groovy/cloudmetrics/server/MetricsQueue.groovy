package cloudmetrics.server

import org.apache.commons.lang3.RandomUtils

import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

import static java.util.concurrent.Executors.newFixedThreadPool

class MetricsQueue {

    private final queue = new LinkedBlockingQueue<Metric>()

    private final executor = newFixedThreadPool(5)

    private final elasticSearchProcessor = new ElasticSearchProcessor()

    MetricsQueue() {
        5.times {
            executor.submit(new Runnable() {
                @Override
                void run() {
                    while (true) {
                        try {
                            def metric = queue.poll(5, TimeUnit.SECONDS)
                            if (metric != null) {
                                elasticSearchProcessor.process(metric)
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
