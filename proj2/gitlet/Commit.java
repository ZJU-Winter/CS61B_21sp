package gitlet;

import static gitlet.Utils.*;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class Commit extends FileTracker {

    private String message;
    private String time;
    private String parent;
    private String author = "Winter";

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
        this.parent = commit.getSha1();
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
        String info = String.format("Message: %s Time: %s Author: %s\nParentSha1: %s \n" +
                "TrackedFiles: %s", message, time, author, parent, trackedFiles);
        return info;
    }

    public void commit() {
        updateTrackFiles();
        String sha1 = getSha1();
        File commitFile = join(Repository.COMMITS, sha1);
        try {
            commitFile.createNewFile();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        writeObject(commitFile, this);
        setupHead();
        setupBranch(getBranch());

        /*
        System.out.println("-------------");
        System.out.print(this);
        System.out.print("\nMySha1: " + getSha1());

         */
    }

    public void initCommit() {
        String sha1 = getSha1();
        File commit = join(Repository.COMMITS, sha1);
        createNewFile(commit);
        writeObject(commit, this);
        setupHead();
        setupBranch("master");

        /*
        System.out.println("-------------");
        System.out.print(this);
        System.out.print("\nMySha1: " + getSha1());

         */
    }

    public String getSha1() {
        return sha1(this.toString());
    }

    private void setupHead() {
        writeContents(Repository.HEAD, getSha1());
    }

    private void setupBranch(String branchName) {
        File branchFile = join(Repository.BRANCH, branchName);
        createNewFile(branchFile);
        writeContents(branchFile, getSha1());
        writeContents(Repository.CURRENT, branchName);
    }

    public static Commit getFromSha1(String sha1) {
        File commit = join(Repository.COMMITS, sha1);
        if (!commit.exists()) {
            System.out.print("Commit file does not exist.");
            System.exit(0);
        }
        return readObject(commit, Commit.class);
    }

    public static Commit getCurCommit() {
        String sha1 = readContentsAsString(Repository.HEAD);
        return getFromSha1(sha1);
    }

    public void updateTrackFiles() {
        FileTracker addStage = readObject(Repository.ADDITION, FileTracker.class);
        //clear the adding stage
        writeObject(Repository.ADDITION, new FileTracker());
        //update tracked files
        Map<String, String> files = addStage.getTrackedFiles();

        if (files.size() == 0) {
            System.out.print("No changes added to the commit.");
            System.exit(0);
        }
        for (Map.Entry<String, String> file : files.entrySet()) {
            File blob = join(Repository.BLOBS, file.getValue());
            File workFile = join(Repository.CWD, file.getKey());
            createNewFile(blob);
            byte[] content = readContents(workFile);
            writeContents(blob, content);
            trackedFiles.put(file.getKey(), file.getValue());
        }
    }

    public String getBranch() {
        String branch = readContentsAsString(Repository.CURRENT);
        return branch;
    }

}
