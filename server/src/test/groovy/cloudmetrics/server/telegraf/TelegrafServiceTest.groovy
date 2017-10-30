package cloudmetrics.server.telegraf

import cloudmetrics.server.CloudMetricsServer
import cloudmetrics.server.telegraf.TelegrafService
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner

import static org.assertj.core.api.Assertions.assertThat

@RunWith(SpringRunner)
@SpringBootTest(classes = CloudMetricsServer.CloudMetricsServerConfig)
class TelegrafServiceTest {

    @Autowired
    TelegrafService telegrafService

    @Test
    void shouldParseCpuMetric() {
        // Given
        def telegrafCpuMetric = new ObjectMapper().readValue(getClass().getResourceAsStream('/telegraf-cpu.json'), Map)

        // When
        def importedMetric = telegrafService.importMetric(telegrafCpuMetric)

        // Then
        assertThat(importedMetric.key).matches(/node\..+?.cpu/)
        assertThat(importedMetric.value as double).isBetween(0d, 100d)
    }

    @Test
    void shouldIgnoreUnknownMetric() {
        // Given
        def telegrafUnknownMetric = new ObjectMapper().readValue(getClass().getResourceAsStream('/telegraf-unknown.json'), Map)

        // When
        def importedMetric = telegrafService.importMetric(telegrafUnknownMetric)

        // When
        assertThat(importedMetric).isNull()
    }

}
