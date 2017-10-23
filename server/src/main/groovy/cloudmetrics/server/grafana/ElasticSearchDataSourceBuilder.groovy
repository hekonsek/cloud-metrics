package cloudmetrics.server.grafana

class ElasticSearchDataSourceBuilder {

    Long id
    Long orgId
    private final String name
    String type = "elasticsearch"
    String typeLogoUrl
    String access = "proxy"
    String url = "http://localhost:9200"
    String password
    String user
    private final String index
    Boolean basicAuth = false
    String basicAuthUser
    String basicAuthPassword
    Boolean withCredentials = false
    Boolean isDefault = false
    Map<String, Object> jsonData = ["timeField":"@timestamp"]
    List<String> secureJsonFields = []

    ElasticSearchDataSourceBuilder(String name, String index) {
        this.name = name
        this.index = index
    }

    ElasticSearchDataSource build() {
        new ElasticSearchDataSource(id: id, orgId: orgId, name: name, type: type, typeLogoUrl: typeLogoUrl,
                access: access, url: url, password: password, user: user, database: index, basicAuth: basicAuth,
        basicAuthUser: basicAuthUser, basicAuthPassword: basicAuthPassword, withCredentials: withCredentials,
        isDefault: isDefault, jsonData: jsonData, secureJsonFields: secureJsonFields)
    }

}