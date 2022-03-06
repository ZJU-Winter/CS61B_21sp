package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static gitlet.Utils.readContents;
import static gitlet.Utils.sha1;

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
        String sha1Code = sha1(content);

        Commit cur = Commit.getCurCommit();
        Map<String, String> curTrackedFiles = cur.getTrackedFiles();

        //the current working version of the file is identical to the version in the current commit
        if (curTrackedFiles.get(fileToAdd.getName()) != null
                && curTrackedFiles.get(fileToAdd.getName()).equals(trackedFiles.get(fileToAdd.getName()))) {
            trackedFiles.remove(fileToAdd.getName());
        } else {
            this.trackedFiles.put(fileToAdd.getName(), sha1Code);
        }
    }
}
