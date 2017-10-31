package cloudmetrics.server

import cloudmetrics.server.metrics.Metric
import cloudmetrics.server.metrics.MetricsService
import org.apache.commons.lang3.RandomUtils
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.ByteArrayDeserializer
import org.springframework.boot.builder.SpringApplicationBuilder

import java.util.concurrent.TimeUnit

class Test {

    static void main(String[] args) {
        def metricsService = new SpringApplicationBuilder(CloudMetricsServer).run().getBean(MetricsService)

        def props = new Properties()
        props['bootstrap.servers'] = 'localhost:9092'
        props['key.deserializer'] = ByteArrayDeserializer.name
        props['value.deserializer'] = ByteArrayDeserializer.name
        props['group.id'] = UUID.randomUUID().toString()
        def c = new KafkaConsumer<>(props)
        c.subscribe(['metrics'])

        new Thread(){
            @Override
            void run() {
                while (true) {
                    def res = c.poll(500)
                    println res.toList().collect{ new String(it.value) }
                }
            }
        }.start()

        1000.times {
            metricsService.appendMetric(new Metric(new Date(), 'node.node1.metric1', RandomUtils.nextDouble(80, 100)))
            metricsService.appendMetric(new Metric(new Date(), 'node.node2.metric1', RandomUtils.nextDouble(80, 100)))
            metricsService.appendMetric(new Metric(new Date(), 'node.node3.metric1', RandomUtils.nextDouble(80, 100)))

            TimeUnit.SECONDS.sleep(1)
        }
    }

}
