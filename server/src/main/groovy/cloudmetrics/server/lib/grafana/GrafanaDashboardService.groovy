package cloudmetrics.server.lib.grafana

import cloudmetrics.server.document.DocumentService
import cloudmetrics.server.metrics.Metric

import static GrafanaTemplates.emptyDashboard
import static GrafanaTemplates.rowForMetric
import static json4dummies.Json.fromJson

class GrafanaDashboardService {

    private final DocumentService documentService

    private final GrafanaService grafanaService

    GrafanaDashboardService(DocumentService documentService, GrafanaService grafanaService) {
        this.documentService = documentService
        this.grafanaService = grafanaService
    }

    Map<String, Object> dashboard() {
        def dashboard = documentService.documentGet('dashboard', 'default')
        if(dashboard == null) {
            dashboard = emptyDashboard()
            documentService.documentPut('dashboard', 'default', dashboard)
        }
        dashboard
    }

    boolean dashboardContainsMetricType(String type) {
        dashboard().dashboard.rows.find { row ->
            row.panels.find { panel ->
                panel.title == type
            } != null
        } != null
    }

    boolean dashboardContainsMetricNode(Metric metric) {
        dashboard().dashboard.rows.find { row ->
            row.panels.find { panel ->
                panel.targets.find { target ->
                    target.alias == metric.node()
                } != null
            } != null
        } != null
    }

    void plotMetric(Metric metric) {
        if (!dashboardContainsMetricType(metric.type())) {
            def dashboard = dashboard()
            dashboard.dashboard.rows << rowForMetric(metric)
            grafanaService.create('dashboards/db', dashboard)
            commit(dashboard)
        } else if(!dashboardContainsMetricNode(metric)) {
            def node = metric.node()
            def dashboard = dashboard()
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
            def target = fromJson(GrafanaTemplates.target(metric.key, refid), Map)
            targets << target
            commit(dashboard)
        }
    }

    void commit(Map<String, Object> dashboard) {
        grafanaService.create('dashboards/db', dashboard)
        documentService.documentPut('dashboard', 'default', dashboard)
    }

}
