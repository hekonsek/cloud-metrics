package cloudmetrics.server.document

import org.apache.ignite.Ignite
import org.apache.ignite.IgniteCache
import org.apache.ignite.Ignition
import org.apache.ignite.configuration.CacheConfiguration
import org.apache.ignite.configuration.IgniteConfiguration
import org.apache.ignite.configuration.PersistentStoreConfiguration

class IgniteDocumentService implements DocumentService {

    // Constants

    private final File kimooraHome

    private Ignite ignite

    // Constructors


    IgniteDocumentService(File kimooraHome) {
        this.kimooraHome = kimooraHome
    }

    IgniteDocumentService start() {
        def persistenceConfig = new PersistentStoreConfiguration().
                setPersistentStorePath("${kimooraHome.absolutePath}/store").
                setWalStorePath("${kimooraHome.absolutePath}/wal_store").
                setWalArchivePath("${kimooraHome.absolutePath}/wal_archive")

        def igniteConfig = new IgniteConfiguration().setPersistentStoreConfiguration(persistenceConfig)
        ignite = Ignition.start(igniteConfig)
        ignite.active(true)

        this
    }

    // Operations

    private IgniteCache<String, Map<String, Object>> documentConfiguration(String collection) {
        ignite.getOrCreateCache(new CacheConfiguration<String, Map<String, Object>>().setName("document_${collection}"))
    }

    void documentPut(String collection, String key, Map<String, Object> value) {
        documentConfiguration(collection).put(key, value)
    }

    Map<String, Object> documentGet(String collection, String key) {
        documentConfiguration(collection).get(key)
    }

    void documentRemove(String collection, String key) {
        documentConfiguration(collection).remove(key)
    }

    List<String> documentsKeys(String collection) {
        def entries = documentConfiguration(collection).iterator()
        entries.inject([]) { keys, entry -> keys << entry.key; keys }
    }

}