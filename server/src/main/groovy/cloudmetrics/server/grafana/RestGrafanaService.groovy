package cloudmetrics.server.grafana

import com.fasterxml.jackson.databind.ObjectMapper
import okhttp3.*

class RestGrafanaService implements GrafanaService {

    // Constants

    private static final JSON = MediaType.parse("application/json; charset=utf-8")

    // Configuration members

    private final String url

    private final String apiKey

    // Constructors

    RestGrafanaService(String url, String apiKey) {
        this.url = url
        this.apiKey = apiKey
    }

    RestGrafanaService(String apiKey) {
        this('http://localhost:3000/api/', apiKey)
    }

    void create(String type, Object entity) {
        def client = new OkHttpClient()

        def body = entity instanceof String ? RequestBody.create(JSON, entity as String) : RequestBody.create(JSON, new ObjectMapper().writeValueAsString(entity))
        def request = new Request.Builder()
                .url(url + type).post(body).header('Authorization', 'Bearer ' + apiKey)
                .build()

        Response response = null
        try {
            response = client.newCall(request).execute()
            if (response.code() == 200) {
            } else if (response.code() == 409) {
                throw new EntityAlreadyExistsException()
            } else if (response.code() == 422) {
                def responseMap = new ObjectMapper().readValue(response.body().bytes(), List)
                throw new IllegalArgumentException(responseMap.join('\n'))
            } else {
                throw new RuntimeException('Unknown result:' + response)
            }
        } finally {
            if(response != null) {
                response.close()
            }
        }
    }

}
