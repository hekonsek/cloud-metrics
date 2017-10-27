package cloudmetrics.server.grafana

import org.apache.commons.io.IOUtils

class DashboardTemplate {

    static String templateJson(String metric) {
        def template = IOUtils.toString(getClass().getResourceAsStream('/dashboard-with-diagram.json'))
        template = template.replaceAll(/METRIC/, metric)
        template = template.replaceAll(/TYPE/, metric.replaceFirst(/node\..+?\./, ''))
        template.replaceAll(/NODE/, metric.replaceFirst(/node\./, '').replaceFirst(/\..+/, ''))
    }

    static String target(String metric, int refid) {
        def template = IOUtils.toString(getClass().getResourceAsStream('/dashboard-target.json'))
        template = template.replaceAll(/METRIC/, metric)
        template = template.replaceAll(/REFID/,"${refid}")
        template.replaceAll(/NODE/, metric.replaceFirst(/node\./, '').replaceFirst(/\..+/, ''))
    }

}
