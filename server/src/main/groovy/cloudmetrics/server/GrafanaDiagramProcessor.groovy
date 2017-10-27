package cloudmetrics.server

import cloudmetrics.server.grafana.DashboardTemplate
import cloudmetrics.server.grafana.GrafanaService
import com.google.common.io.Files
import kafka.utils.Json
import org.apache.commons.io.IOUtils

import static io.vertx.core.json.Json.decodeValue

class GrafanaDiagramProcessor {

    private final GrafanaService grafanaService

    private Map<String, Set<String>> existingDiagrams = [:]

    GrafanaDiagramProcessor(GrafanaService grafanaService) {
        this.grafanaService = grafanaService
    }

    void process(Metric metric) {
        if (!existingDiagrams.containsKey(metric.type())) {
            def dashboard = DashboardTemplate.templateJson(metric.key)
            Files.write(dashboard.bytes, new File("/tmp/cmdashboard.json"))
            grafanaService.create('dashboards/db', dashboard)
            existingDiagrams[metric.type()] = [metric.key] as Set
        } else if(!existingDiagrams[metric.type()].contains(metric.key)) {
            def node = metric.key.replaceAll(/NODE/, metric.key.replaceFirst(/node\./, '').replaceFirst(/\..+/, ''))
            def dashboard = decodeValue(IOUtils.toString(new FileInputStream("/tmp/cmdashboard.json")), Map)
            def targets = dashboard.dashboard.rows[0].panels[0].targets as List<Map>
            def existingTarget = targets.find{ it.alias == node }
            Integer refid = -1
            if(existingTarget != null) {
                refid = existingTarget.refId
                targets.remove(existingTarget)
            } else {
                targets.each {
                    if(refid < (it.refId as int)) {
                        refid = it.refId as int
                        refid++
                    }
                }
            }
            def target = decodeValue(DashboardTemplate.target(metric.key, refid), Map)
            targets << target
            grafanaService.create('dashboards/db', dashboard)
            existingDiagrams[metric.type()] = [metric.key] as Set
        }
    }

}