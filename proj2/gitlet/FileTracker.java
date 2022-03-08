package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static gitlet.Utils.*;

public class FileTracker implements Serializable {
    protected Map<String, String> trackedFiles;

    public FileTracker(Map<String, String> trackedFiles) {
        this.trackedFiles = trackedFiles;
    }

    public FileTracker() {
        this.trackedFiles = new HashMap<>();
    }


    /**
     * add file for "add"
     */
    public void add(File file) {
        String fileName = file.getName();
        String version = fileSha1(file);

        Commit cur = Commit.getCurCommit();
        Map<String, String> curTrackedFiles = cur.trackedFiles;

        String curVersion = curTrackedFiles.getOrDefault(fileName, "");

        //If the current working version of the file is identical
        // to the version in the current commit
        if (curVersion.equals(version)) {
            this.trackedFiles.remove(fileName);
        } else {
            this.trackedFiles.put(fileName, version);
        }
    }

    public void remove(File file) {
        this.trackedFiles.remove(file.getName());
    }

    public void put(File file) {
        this.trackedFiles.put(file.getName(), fileSha1(file));
    }

    public boolean containsFile(File file) {
        String sha1 = fileSha1(file);
        return this.trackedFiles.containsKey(file.getName())
                &&
                sha1.equals(this.trackedFiles.get(file.getName()));
    }

    public Set<String> getFileNames() {
        Set<String> files = new LinkedHashSet<>(this.trackedFiles.keySet());
        return files;
    }

    public Set<String> getFileSha1s() {
        Set<String> sha1s = new LinkedHashSet<>(this.trackedFiles.values());
        return sha1s;
    }

    public String getFileContentSha1(String filename) {
        return this.trackedFiles.getOrDefault(filename, "");
    }
}
