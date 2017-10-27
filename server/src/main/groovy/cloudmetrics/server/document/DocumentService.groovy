package cloudmetrics.server.document

interface DocumentService {

    // Document operations

    void documentPut(String collection, String key, Map<String, Object> value)

    Map<String, Object> documentGet(String collection, String key)

    void documentRemove(String collection, String key)

    List<String> documentsKeys(String collection)

}