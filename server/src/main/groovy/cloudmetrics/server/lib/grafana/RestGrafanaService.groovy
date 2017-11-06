package cloudmetrics.server.lib.grafana

import com.fasterxml.jackson.databind.ObjectMapper
import okhttp3.*

class RestGrafanaService implements GrafanaService {

    // Constants

    private static final JSON = MediaType.parse("application/json; charset=utf-8")

    // Configuration members

    private final String url

    private final String apiKey

    private final String username

    private final String password

    private final OkHttpClient client

    // Constructors

    RestGrafanaService(String url, String apiKey, String username, String password) {
        this.url = url
        this.apiKey = apiKey
        this.username = username
        this.password = password

        if (apiKey == null) {
            client = new OkHttpClient.Builder().authenticator(new Authenticator() {
                @Override
                Request authenticate(Route route, Response response) throws IOException {
                    String credential = Credentials.basic(username, password)
                    response.request().newBuilder().header("Authorization", credential).build()
                }
            }).build()
        } else {
            client = new OkHttpClient.Builder().authenticator(new Authenticator() {
                @Override
                Request authenticate(Route route, Response response) throws IOException {
                    response.request().newBuilder().header("Authorization", 'Bearer ' + apiKey).build()
                }
            }).build()
        }
    }

    RestGrafanaService(String username, String password) {
        this('http://localhost:3000/api/', null, username, password)
    }

    void create(String type, Object entity) {
        def body = entity instanceof String ? RequestBody.create(JSON, entity as String) : RequestBody.create(JSON, new ObjectMapper().writeValueAsString(entity))
        def request = new Request.Builder()
                .url(url + type).post(body)
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
            if (response != null) {
                response.close()
            }
        }
    }

}
