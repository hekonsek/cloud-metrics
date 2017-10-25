package cloudmetrics.server

import io.vertx.core.Vertx
import io.vertx.ext.unit.TestContext
import io.vertx.ext.unit.junit.VertxUnitRunner
import org.apache.commons.io.IOUtils
import org.junit.Test
import org.junit.runner.RunWith

import static io.vertx.core.json.Json.decodeValue
import static org.assertj.core.api.Assertions.assertThat

@RunWith(VertxUnitRunner)
class ImportTelegrafVerticleTest {

    @Test
    void shouldParseCpuMetric(TestContext ctx) {
        // Given
        def async = ctx.async()
        def vertx = Vertx.vertx()
        vertx.deployVerticle(new ImportTelegrafVerticle())
        def telegrafCpuMetric = IOUtils.toString(getClass().getResourceAsStream('/telegraf-cpu.json'))

        // Then
        vertx.eventBus().consumer('metrics.append') {
            def metric = decodeValue(it.body() as String, Metric)
            assertThat(metric.key).matches(/node\..+?.cpu/)
            assertThat(metric.value as double).isBetween(0d, 100d)
            async.complete()
        }

        // When
        vertx.eventBus().send(ImportTelegrafVerticle.INPUT, telegrafCpuMetric)
    }

    @Test
    void shouldIgnoreUnknownMetric(TestContext ctx) {
        // Given
        def async = ctx.async()
        def vertx = Vertx.vertx()
        vertx.deployVerticle(new ImportTelegrafVerticle())
        def telegrafUnknownMetric = IOUtils.toString(getClass().getResourceAsStream('/telegraf-unknown.json'))

        // When
        vertx.eventBus().send(ImportTelegrafVerticle.INPUT, telegrafUnknownMetric) {
            def status = decodeValue(it.result().body() as String, Map)
            assertThat(status.status).isEqualTo('UNKNOWN')
            async.complete()
        }
    }

}
