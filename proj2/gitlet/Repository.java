package gitlet;

import java.io.BufferedOutputStream;
import java.io.File;
import java.util.*;

import static gitlet.Utils.*;


/**
 * Represents a gitlet repository.
 * includes all the methods used in gitlet
 * @author winter
 */
public class Repository {
    /**
     * The current working directory
     */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /**
     * The .gitlet directory: CWD/.gitlet
     */
    public static final File GITLET_DIR = join(CWD, ".gitlet");

    /**
     * The file that records the HEAD sha1 code: CWD/.gitlet/Head.txt
     */
    public static final File HEAD = join(GITLET_DIR, "HEAD");

    /**
     * The directory for staged files: CWD/.gitlet/stage
     */
    public static final File STAGINGAREA = join(GITLET_DIR, "stagingarea");

    /**
     * The directory for all the commits: CWD/.gitlet/commits
     */
    public static final File COMMITS = join(GITLET_DIR, "commits");

    /**
     * The directory for all recorded files: CWD/.gitlet/objects
     */
    public static final File BLOBS = join(GITLET_DIR, "blobs");

    /**
     * The directory for all branches: CWS/.gitlet/branches
     */
    public static final File BRANCH = join(GITLET_DIR, "branches");

    /**
     * The file for recording the current branch's name
     */
    public static final File CURRENT = join(GITLET_DIR, "CURRENT");

    /**
     * The file for recording staged files for addition
     */
    public static final File ADDITION = join(STAGINGAREA, "addition");

    /**
     * The file for recording staged files for removal
     */
    public static final File REMOVAL = join(STAGINGAREA, "removal");


    /**
     * Call the init()
     * 1. create all needed files and directories
     * 2. create an initial branch master
     * 3. commit an empty commit
     */
    public static void init() {
        setupPersistence();
        Commit commit = new Commit("initial commit");
        commit.initCommit();
    }

    /**
     * Call the addToStage(String fileName) add a file to the STAGE directory
     * 1. read the previous FileTracker object
     * 2. update the object
     * 2.1 if the content is different, update the content
     * 2.2 If the current working version of the file is identical to the version in the current commit,
     * delete it from the staging area.
     * 3. write back to ADDITION
     */
    public static void add(String fileName) {
        File file = join(CWD, fileName);
        if (!file.exists()) {
            System.out.print("File does not exist.");
            System.exit(0);
        }
        FileTracker fileTracker = readObject(ADDITION, FileTracker.class);
        fileTracker.add(file);
        writeObject(ADDITION, fileTracker);

        writeBlob(file);

        printAddStage();
    }

    /**
     * Call the commit("message") to commit
     * 1. copy from the parent commit
     * 2. put adding stage to tracked files and clear the stage
     * 3. save the new commit to the directory
     * 4. setup HEAD and BRANCH
     */
    public static void commit(String message) {
        Commit parent = Commit.getCurCommit();
        Commit commit = new Commit(parent, message);
        commit.commit();
    }

    /**
     * Call the rm(filename) to rm
     * 1.if the file is staged, unstage it
     * 2.if the file is tracked in the current commit
     * 2.1 stage it for removal
     * 2.2 remove it from the working directory
     */
    public static void rm(String filename) {
        File file = join(CWD, filename);
        FileTracker addStage = readObject(ADDITION, FileTracker.class);
        FileTracker removeStage = readObject(REMOVAL, FileTracker.class);
        Commit commit = Commit.getCurCommit();

        if (addStage.containsFile(file)) {
            addStage.remove(file);
            writeObject(ADDITION, addStage);
        } else if (commit.containsFile(file)) {
            removeStage.put(file);
            writeObject(REMOVAL, removeStage);
            writeBlob(file);
            restrictedDelete(file);
        } else {
            System.out.print("No reason to remove the file.");
            System.exit(0);
        }

        printAddStage();
        printRemoveStage();
    }

    /**
     * Call the log() to print each commit starting from the HEAD commit
     */
    //TODO: merge!!
    public static void log() {
        Commit commit = Commit.getCurCommit();
        while (commit != null) {
            commit.printCommit();
            commit = Commit.getCommit(commit.getParent());
        }
    }

    /**
     * Call the globalLog() to print all commits in the history
     */
    //TODO:merge!!
    public static void globalLog() {
        List<String> commits = plainFilenamesIn(COMMITS);
        if (commits != null) {
            for (String sha1 : commits) {
                Commit commit = Commit.getCommit(sha1);
                commit.printCommit();
            }
        }
    }

    /**
     * Call the find to print out all the ids of commits that have given commit message
     */
    public static void find(String message) {
        List<String> commits = plainFilenamesIn(COMMITS);
        boolean found = false;
        if (commits != null) {
            for (String sha1 : commits) {
                Commit commit = Commit.getCommit(sha1);
                if (commit.getMessage().equals(message)) {
                    System.out.println(commit.getSha1());
                    found = true;
                }
            }
        }
        if (!found) {
            System.out.print("Found no commit with that message.");
            System.exit(0);
        }
    }

    /**
     * Call the status() to show information
     */
    public static void status() {
        showBranches();
        showStagedFiles();
        showRemovedFiles();
        showModifications();
        showUntrackedFiles();
    }

    /**
     * Call the checkout(args) to checkout
     */
    public static void checkout(String[] args) {
        if (args.length == 2) {
            checkoutBranch(args[1]);
        } else if (args.length == 3) {
            checkoutCurCommit(args[2]);
        } else if (args.length == 4) {
            checkoutCommit(args[1], args[3]);
        }
    }

    private static void checkoutCurCommit(String fileName) {
        String commitID = readContentsAsString(HEAD);
        checkoutCommit(commitID, fileName);
    }

    /**
     * to update all tracked files to the state of Branch head
     */
    private static void checkoutBranch(String branchName) {
        List<String> branches = plainFilenamesIn(BRANCH);
        String curBranch = readContentsAsString(CURRENT);
        File branchFile = join(BRANCH, branchName);
        String commitID = readContentsAsString(branchFile);
        Commit branchHead = Commit.getCommit(commitID);
        Map<String, String> branchTrackedFiles = branchHead.getTrackedFiles();
        Set<String> untrackedFiles = getUntrackedFiles();
        if (!branches.contains(branchName)) {
            System.out.print("No such branch exists.");
            System.exit(0);
        }
        if (curBranch.equals(branchName)) {
            System.out.print("No need to checkout the current branch.");
            System.exit(0);
        }
        if (!untrackedFiles.isEmpty()) {
            for (String file : untrackedFiles) {
                if (branchTrackedFiles.containsKey(file)) {
                    System.out.print("There is an untracked file in the way; delete it, or add and commit it first.");
                    System.exit(0);
                }
            }
        }

        updateCWDTo(branchHead);
        writeObject(ADDITION, new FileTracker());
        writeContents(HEAD, commitID);
        writeContents(CURRENT, branchName);
    }

    private static void checkoutCommit(String commitID, String filename) {
        List<String> commits = plainFilenamesIn(COMMITS);
        if (!commits.contains(commitID)) {
            System.out.print("No commit with that id exists.");
            System.exit(0);
        }
        Commit commit = Commit.getCommit(commitID);
        Map<String, String> files = commit.getTrackedFiles();
        if (!files.containsKey(filename)) {
            System.out.print("File does not exist in that commit.");
            System.exit(0);
        }

        File file = join(CWD, filename);
        File blob = join(BLOBS, files.get(filename));

        byte[] contents = readContents(blob);
        createNewFile(file);
        writeContents(file, contents);
    }

    /**
     * to create file or a directory
     */
    private static void setupFile(File file, boolean isDir) {
        if (isDir) {
            file.mkdir();
        } else {
            createNewFile(file);
        }
    }


    /**
     * to set up all needed files and directories for gitlet
     */
    private static void setupPersistence() {
        if (inGit()) {
            System.out.print("A Gitlet version-control system already exists in the current directory.");
            System.exit(0);
        }
        setupFile(GITLET_DIR, true);
        setupFile(STAGINGAREA, true);
        setupFile(COMMITS, true);
        setupFile(BLOBS, true);
        setupFile(BRANCH, true);
        setupFile(HEAD, false);
        setupFile(ADDITION, false);
        setupFile(REMOVAL, false);
        writeObject(ADDITION, new FileTracker());
        writeObject(REMOVAL, new FileTracker());
    }

    public static boolean inGit() {
        if (GITLET_DIR.exists())
            return true;
        return false;
    }

    private static void showBranches() {
        String current = readContentsAsString(CURRENT);
        List<String> files = plainFilenamesIn(BRANCH);
        List<String> branches = new LinkedList<>(files);
        branches.remove(current);
        branches.add(0, "*" + current);
        System.out.println("=== Branches ===");
        for (String branch : branches) {
            System.out.println(branch);
        }
        System.out.println();
    }

    private static void showStagedFiles() {
        FileTracker staged = readObject(ADDITION, FileTracker.class);
        Set<String> files = staged.getFiles();
        System.out.println("=== Staged Files ===");
        for (String file : files) {
            System.out.println(file);
        }
        System.out.println();
    }

    private static void showRemovedFiles() {
        FileTracker staged = readObject(REMOVAL, FileTracker.class);
        Set<String> files = staged.getFiles();
        System.out.println("=== Removed Files ===");
        for (String file : files) {
            System.out.println(file);
        }
        System.out.println();
    }

    private static void showUntrackedFiles() {
        Set<String> untrackedFiles = getUntrackedFiles();
        System.out.println("=== Untracked Files ===");
        for (String file : untrackedFiles) {
            System.out.println(file);
        }
        System.out.println();
    }

    private static Set<String> getUntrackedFiles() {
        Commit commit = Commit.getCurCommit();
        Set<String> trackedFiles = commit.getFiles();
        FileTracker staged = readObject(ADDITION, FileTracker.class);
        Set<String> stagedFiles = staged.getFiles();
        List<String> files = plainFilenamesIn(CWD);
        Set<String> untrackedFiles = new LinkedHashSet<>();
        for (String file : files) {
            if (!trackedFiles.contains(file) && !stagedFiles.contains(file)) {
                untrackedFiles.add(file);
            }
        }
        return untrackedFiles;
    }

    private static void showModifications() {
        System.out.println("=== Modifications Not Staged For Commit ===");
        showDeleted();
        showModified();
        System.out.println();
    }

    private static void showModified() {
        Commit commit = Commit.getCurCommit();
        Map<String, String> trackedFiles = commit.getTrackedFiles();
        FileTracker Addition = readObject(ADDITION, FileTracker.class);
        Map<String, String> stagedFiles = Addition.getTrackedFiles();
        Set<String> modified = new LinkedHashSet<>();
        for (String file : trackedFiles.keySet()) {
            File temp = join(CWD, file);
            if (temp.exists()) {
                String content = fileSha1(temp);
                if (differentContent(trackedFiles, file, content)
                        &&
                        differentContent(stagedFiles, file, content)) {
                    modified.add(file);
                }
            }
        }
        for (String file : stagedFiles.keySet()) {
            File temp = join(CWD, file);
            if (temp.exists()) {
                String content = fileSha1(CWD, file);
                if (differentContent(stagedFiles, file, content))
                    modified.add(file);
            }
        }

        for (String file : modified) {
            System.out.println(file + " (modified)");
        }

    }

    private static void showDeleted() {
        Commit commit = Commit.getCurCommit();
        FileTracker Addition = readObject(ADDITION, FileTracker.class);
        FileTracker Removal = readObject(REMOVAL, FileTracker.class);
        List<String> workingDir = plainFilenamesIn(CWD);
        Set<String> trackedFiles = commit.getFiles();
        Set<String> stagedFiles = Addition.getFiles();
        Set<String> removedFiles = Removal.getFiles();
        Set<String> deleted = new LinkedHashSet<>();
        for (String file : stagedFiles) {
            if (!workingDir.contains(file))
                deleted.add(file);
        }
        for (String file : trackedFiles) {
            if (!removedFiles.contains(file) && !workingDir.contains(file))
                deleted.add(file);
        }
        for (String file : deleted) {
            System.out.println(file + " (deleted)");
        }
    }

    /**
     * update all files in the CWD to the commit tracked files
     * 1. not include the untracked files
     */
    private static void updateCWDTo(Commit commit) {
        Commit curCommit = Commit.getCurCommit();
        Map<String, String> trackedFiles = commit.getTrackedFiles();
        for (String filename : curCommit.getFiles()) {
            File file = join(CWD, filename);
            restrictedDelete(file);
        }

        for (String filename : trackedFiles.keySet()) {
            File blob = join(BLOBS, trackedFiles.get(filename));
            File newFile = join(CWD, filename);
            createNewFile(newFile);
            String contents = readContentsAsString(blob);
            writeContents(newFile, contents);
        }
    }

    private static void printAddStage() {
        FileTracker tracker = readObject(ADDITION, FileTracker.class);
        System.out.println("------Addition Stage-------");
        System.out.println(tracker.getTrackedFiles());
    }

    private static void printRemoveStage() {
        FileTracker tracker = readObject(REMOVAL, FileTracker.class);
        System.out.println("-------Removed Stage--------");
        System.out.println(tracker.getTrackedFiles());
    }

    private static boolean differentContent(Map<String, String> map, String file, String content) {
        String expected = map.getOrDefault(file, "");
        return !expected.equals(content);
    }
}
