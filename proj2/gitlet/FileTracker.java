package gitlet;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class FileTracker implements Serializable {
    public Map<String, String> trackedFiles;

    public FileTracker(Map<String, String> trackedFiles) {
        this.trackedFiles = trackedFiles;
    }

    public FileTracker() {
        this.trackedFiles = new HashMap<>();
    }

    public Map<String, String> getTrackedFiles() {
        return trackedFiles;
    }
}
