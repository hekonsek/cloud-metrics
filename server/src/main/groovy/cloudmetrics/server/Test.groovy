package cloudmetrics.server

import org.apache.commons.lang3.RandomUtils

import java.util.concurrent.TimeUnit

class Test {

    static void main(String[] args) {
        def queue = new MetricsQueue()
        1000.times {
            queue.append(new Metric(new Date(), 'node.node10.cpu', RandomUtils.nextInt(80, 100)))
            queue.append(new Metric(new Date(), 'node.node11.cpu', RandomUtils.nextInt(60, 90)))

            TimeUnit.SECONDS.sleep(1)
        }
    }

}
