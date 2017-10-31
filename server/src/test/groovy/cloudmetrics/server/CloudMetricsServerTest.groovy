package cloudmetrics.server

import cloudmetrics.server.metrics.Metric
import cloudmetrics.server.metrics.MetricsService
import cloudmetrics.server.telegraf.TelegrafService
import org.elasticsearch.common.settings.Settings
import org.elasticsearch.common.transport.InetSocketTransportAddress
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.transport.client.PreBuiltTransportClient
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner

import static java.lang.System.currentTimeMillis
import static json4dummies.Json.fromJson
import static org.assertj.core.api.Assertions.assertThat
import static org.awaitility.Awaitility.await

@RunWith(SpringRunner)
@SpringBootTest(classes = CloudMetricsServer)
class CloudMetricsServerTest {

    @Autowired
    TelegrafService telegrafService

    @Test
    void shouldParseCpuMetric() {
        // Given
        def telegrafCpuMetric = fromJson(getClass().getResourceAsStream('/telegraf-cpu.json').bytes, Map)

        // When
        def importedMetric = telegrafService.importMetric(telegrafCpuMetric)

        // Then
        assertThat(importedMetric.key).matches(/node\..+?.cpu/)
        assertThat(importedMetric.value as double).isBetween(0d, 100d)
    }

    @Test
    void shouldIgnoreUnknownMetric() {
        // Given
        def telegrafUnknownMetric = fromJson(getClass().getResourceAsStream('/telegraf-unknown.json').bytes, Map)

        // When
        def importedMetric = telegrafService.importMetric(telegrafUnknownMetric)

        // When
        assertThat(importedMetric).isNull()
    }

    // Metrics tests

    @Autowired
    MetricsService metricsService

    def settings = Settings.builder().put("cluster.name", "cloud_metrics").build()

    def client = new PreBuiltTransportClient(settings)
            .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("localhost"), 9300))

    @Test
    void shouldAppendMetric() {
        // When
        def metricValue = currentTimeMillis()
        metricsService.appendMetric(new Metric(new Date(), 'node.node1.cpu', metricValue))

        await().untilAsserted() {
            def resultsSize = client.prepareSearch('node.node1.cpu').setQuery(QueryBuilders.matchQuery('value', metricValue)).execute().get().hits.size()
            assertThat(resultsSize).isEqualTo(1)
        }
    }

}
