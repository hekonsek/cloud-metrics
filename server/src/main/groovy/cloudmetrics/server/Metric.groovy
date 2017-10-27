package cloudmetrics.server

class Metric {

    Date timestamp

    String key

    Object value

    Metric(Date timestamp, String key, Object value) {
        this.timestamp = timestamp
        this.key = key
        this.value = value
    }

    Metric() {
    }

    String type() {
        key.replaceFirst(/node\..+?\./, '')
    }

    Date getTimestamp() {
        timestamp
    }

    void setTimestamp(Date timestamp) {
        this.timestamp = timestamp
    }

    String getKey() {
        key
    }

    void setKey(String key) {
        this.key = key
    }

    Object getValue() {
        return value
    }

    void setValue(Object value) {
        this.value = value
    }

}
