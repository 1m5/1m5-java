package io.onemfive.core.infovault;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Properties;

/**
 * InfoVaultDB
 *
 * Stores personal information securely while allowing access
 * by other parties with personal approval.
 *
 * @author objectorange
 */
public interface InfoVaultDB {

    enum Status {Starting,StartupFailed,Running,Stopping,Shutdown}

    void setLocation(String location);
    String getLocation();

    void setName(String name);

    void save(String label, String key, byte[] content, boolean autoCreate) throws FileNotFoundException;

    Boolean delete(String label, String key);

    byte[] load(String label, String key) throws FileNotFoundException;

    List<byte[]> loadRange(String label, int start, int numberEntries);

    List<byte[]> loadAll(String label);

    Status getStatus();

    boolean init(Properties properties);

    boolean teardown();

}
