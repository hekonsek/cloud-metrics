package cloudmetrics.server.grafana

import cloudmetrics.server.document.IgniteDocumentService
import com.google.common.io.Files
import org.junit.Test

import static org.assertj.core.api.Assertions.assertThat
import static org.mockito.Mockito.mock

class GrafanaDashboardServiceTest {

    static def dashboardService = new GrafanaDashboardService(new IgniteDocumentService(Files.createTempDir()).start(), mock(GrafanaService))

    @Test
    void shouldGetDashboard() {
        // When
        def dashboard = dashboardService.dashboard()

        // Then
        assertThat(dashboard).isNotNull()
    }

    @Test
    void shouldPersistDashboardOnCommit() {
        // Given
        def dashboard = dashboardService.dashboard()

        // When
        dashboard.foo = 'bar'
        dashboardService.commit(dashboard)
        dashboard = dashboardService.dashboard()

        // Then
        assertThat(dashboard.foo).isEqualTo('bar')
    }

}
