package gitlet;

import static gitlet.Utils.*;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * Represents a gitlet commit object.
 *
 * @author winter
 */
public class Commit extends FileTracker {

    private String message;
    private String time;
    private String parent;

    Commit(String message, String parent, Map<String, String> trackedFiles) {
        super(trackedFiles);
        this.message = message;
        this.time = new SimpleDateFormat("yyyy/MMM/dd HH:mm:ss").format(new Date());
        this.parent = parent;
        this.trackedFiles = trackedFiles;
    }

    Commit(String message) {
        super();
        this.message = message;
        this.time = new SimpleDateFormat("yyyy/MMM/dd HH:mm:ss").format(new Date());
        parent = null;
    }

    Commit(Commit commit, String message) {
        super(commit.trackedFiles);
        this.message = message;
        this.time = new SimpleDateFormat("yyyy/MMM/dd HH:mm:ss").format(new Date());
        this.parent = commit.parent;
    }

    public String getMessage() {
        return this.message;
    }

    public String getTime() {
        return this.time;
    }

    public String getParent() {
        return this.parent;
    }

    @Override
    public String toString() {
        String info = "Message: " + message + "Time: " + time + "Parent: "
                +
                parent + "TrackedFiles: " + trackedFiles;
        return info;
    }

    public void commit(String branchName) {
        String commitSha1 = getCommitSha1();
        File commitFile = join(Repository.COMMITS, commitSha1);
        writeObject(commitFile, this);
        setupHead();
        setupBranch(branchName);
    }

    public String getCommitSha1() {
        return sha1(this.toString());
    }

    private void setupHead() {
        writeContents(Repository.HEAD, getCommitSha1());
    }

    private void setupBranch(String branchName) {
        File branchFile = join(Repository.BRANCH, branchName);
        if (!branchFile.exists()) {
            try {
                branchFile.createNewFile();
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
        writeContents(branchFile, getCommitSha1());
    }

    public Commit getFromSha1(String sha1) {
        File commit = join(Repository.COMMITS, sha1);
        return readObject(commit, Commit.class);
    }

    public Commit getCurCommit() {
        String sha1 = readContentsAsString(Repository.HEAD);
        return getFromSha1(sha1);
    }

    //public String
}
