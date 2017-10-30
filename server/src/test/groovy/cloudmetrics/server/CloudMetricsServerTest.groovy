package cloudmetrics.server

import cloudmetrics.server.telegraf.TelegrafService
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner

import static json4dummies.Json.fromJson
import static org.assertj.core.api.Assertions.assertThat

@RunWith(SpringRunner)
@SpringBootTest(classes = CloudMetricsServer.CloudMetricsServerConfig)
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

}
