package cloudmetrics.server.grafana

class ElasticSearchDataSource {

    Long id
    Long orgId
    String name
    String type = "elasticsearch"
    String typeLogoUrl
    String access = "proxy"
    String url = "http://localhost:9200"
    String password
    String user
    String database
    Boolean basicAuth = false
    String basicAuthUser
    String basicAuthPassword
    Boolean withCredentials = false
    Boolean isDefault = false
    Map<String, Object> jsonData = ["timeField":"@timestamp"]
    List<String> secureJsonFields = []

}