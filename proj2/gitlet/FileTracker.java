package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static gitlet.Utils.*;

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

    public void updateTrackedFiles(File fileToAdd) {
        byte[] content = readContents(fileToAdd);
        String version = sha1(content);
        String fileName = fileToAdd.getName();

        Commit cur = Commit.getCurCommit();
        Map<String, String> curTrackedFiles = cur.getTrackedFiles();

        String curVersion = curTrackedFiles.getOrDefault(fileName, "");

        //If the current working version of the file is identical to the version in the current commit
        if (curVersion.equals(version)) {
            this.trackedFiles.remove(fileName);
        } else {
            this.trackedFiles.put(fileName, version);
        }
    }
}
