package cloudmetrics.server.grafana

import cloudmetrics.server.Metric
import org.apache.commons.io.IOUtils

import static com.google.common.base.Charsets.UTF_8
import static io.vertx.core.json.Json.decodeValue

class GrafanaTemplates {

    static Map<String, Object> rowForMetric(Metric metric) {
        def template = IOUtils.toString(getClass().getResourceAsStream('/dashboard-row.json'), UTF_8)
        template = template.replaceAll(/METRIC/, metric.key)
        template = template.replaceAll(/TYPE/, metric.type())
        decodeValue(template.replaceAll(/NODE/, metric.key.replaceFirst(/node\./, '').replaceFirst(/\..+/, '')), Map)
    }

    static Map<String, Object> emptyDashboard() {
        decodeValue(IOUtils.toString(getClass().getResourceAsStream('/dashboard-empty.json'), UTF_8), Map)
    }

    static String target(String metric, int refid) {
        def template = IOUtils.toString(getClass().getResourceAsStream('/dashboard-target.json'), UTF_8)
        template = template.replaceAll(/METRIC/, metric)
        template = template.replaceAll(/REFID/,"${refid}")
        template.replaceAll(/NODE/, metric.replaceFirst(/node\./, '').replaceFirst(/\..+/, ''))
    }

}
