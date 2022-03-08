package gitlet;

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

        //printAddStage();
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

        //printAddStage();
        //printRemoveStage();
    }

    /**
     * Call the log() to print each commit starting from the HEAD commit
     */
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

    /**
     * create a new branch.
     */
    public static void branch(String branchName) {
        File branch = join(BRANCH, branchName);
        String head = Commit.getCurCommit().getSha1();
        if (branch.exists()) {
            System.out.print("A branch with that name already exists.");
            System.exit(0);
        }
        createNewFile(branch);
        writeContents(branch, head);
    }

    /**
     * remove the branch.
     */
    public static void removeBranch(String branchName) {
        List<String> branches = plainFilenamesIn(BRANCH);
        if (!branches.contains(branchName)) {
            System.out.print("A branch with that name does not exist.");
            System.exit(0);
        }
        String curBranch = readContentsAsString(CURRENT);
        if (curBranch.equals(branchName)) {
            System.out.print("Cannot remove the current branch.");
            System.exit(0);
        }
        File branchFile = join(BRANCH, branchName);
        branchFile.delete();
    }

    /**
     * checkout an arbitrary commit
     */
    public static void reset(String commitID) {
        Commit commit = Commit.getCommit(commitID);
        uncheckedFileOverwriteBy(commit);
        updateAllFileTo(commit);
        //move the current branch’s head to that commit node
        String curBranch = readContentsAsString(CURRENT);
        File branch = join(BRANCH, curBranch);
        writeContents(branch, commitID);
        //move head and clear stage
        writeContents(HEAD, commitID);
        writeObject(ADDITION, new FileTracker());
    }

    /**
     * merge files from the given branch into the current branch.
     */
    public static void merge(String branchName) {
        Commit spiltPoint = getSplitPoint(branchName);
        Commit curCommit = Commit.getCurCommit();
        Commit otherCommit = Commit.getCommit(readContentsAsString(join(BRANCH, branchName)));
        uncheckedFileOverwriteBy(otherCommit);

        Set<String> files = new HashSet<>();
        files.addAll(spiltPoint.getFileNames());
        files.addAll(curCommit.getFileNames());
        files.addAll(otherCommit.getFileNames());
        for (String file : files) {
            String contentInSpilt = spiltPoint.getFileContentSha1(file);
            String contentInCur = curCommit.getFileContentSha1(file);
            String contentInOther = otherCommit.getFileContentSha1(file);

            if (contentInSpilt.equals(contentInCur) && !contentInSpilt.equals(contentInOther)) {
                File newFile = join(CWD, file);
                if (contentInOther.equals("")) {
                    // remove it add to the removal
                    restrictedDelete(file);
                    FileTracker removal = readObject(REMOVAL, FileTracker.class);
                    removal.put(newFile);
                    writeObject(REMOVAL, removal);
                } else {
                    // change the CWD and add to the addition
                    String content = readBlob(contentInOther);
                    writeContents(newFile, content);
                    FileTracker addition = readObject(ADDITION, FileTracker.class);
                    addition.put(newFile);
                    writeObject(ADDITION, addition);
                }
            } else if (!contentInSpilt.equals(contentInCur)
                    &&
                    !contentInSpilt.equals(contentInOther)
                    &&
                    !contentInCur.equals(contentInOther)) {
                dealWithConflict(curCommit, otherCommit, file);
            }
        }
        commit("Merged " + branchName + " into " + readContentsAsString(CURRENT) + ".");
    }


    private static void checkoutCurCommit(String fileName) {
        String commitID = readContentsAsString(HEAD);
        checkoutCommit(commitID, fileName);
    }

    /**
     * to update all tracked files to the state of Branch head.
     */
    private static void checkoutBranch(String branchName) {
        List<String> branches = plainFilenamesIn(BRANCH);
        String curBranch = readContentsAsString(CURRENT);
        if (!branches.contains(branchName)) {
            System.out.print("No such branch exists.");
            System.exit(0);
        }

        if (curBranch.equals(branchName)) {
            System.out.print("No need to checkout the current branch.");
            System.exit(0);
        }

        File branchFile = join(BRANCH, branchName);
        String commitID = readContentsAsString(branchFile);
        Commit branchHead = Commit.getCommit(commitID);

        uncheckedFileOverwriteBy(branchHead);
        updateAllFileTo(branchHead);

        //clear the stage
        writeObject(ADDITION, new FileTracker());
        //update head and current branch
        writeContents(HEAD, commitID);
        writeContents(CURRENT, branchName);
    }

    private static void checkoutCommit(String commitID, String filename) {
        Commit commit = Commit.getCommit(commitID);
        Map<String, String> files = commit.getTrackedFiles();
        if (!files.containsKey(filename)) {
            System.out.print("File does not exist in that commit.");
            System.exit(0);
        }

        File file = join(CWD, filename);
        String contents = readBlob(files.get(filename));
        createNewFile(file);
        writeContents(file, contents);
    }

    /**
     * to create needed files for gitlet.
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
        Set<String> files = staged.getFileNames();
        System.out.println("=== Staged Files ===");
        for (String file : files) {
            System.out.println(file);
        }
        System.out.println();
    }

    private static void showRemovedFiles() {
        FileTracker staged = readObject(REMOVAL, FileTracker.class);
        Set<String> files = staged.getFileNames();
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
        //untracked files
        List<String> files = plainFilenamesIn(CWD);
        Set<String> trackedFiles = Commit.getCurCommit().getTrackedFiles().keySet();

        Set<String> untrackedFiles = new LinkedHashSet<>();
        for (String file : files) {
            if (!trackedFiles.contains(file)) {
                untrackedFiles.add(file);
            }
        }
        //removed and re-created
        List<String> removedFiles = plainFilenamesIn(REMOVAL);
        if (removedFiles != null) {
            for (String removed : removedFiles) {
                File temp = join(CWD, removed);
                if (temp.exists()) {
                    untrackedFiles.add(removed);
                }
            }
        }
        return untrackedFiles;
    }

    private static void uncheckedFileOverwriteBy(Commit commit) {
        Set<String> uncheckedFiles = getUntrackedFiles();
        for (String uncheckedFile : uncheckedFiles) {
            if (commit.getFileNames().contains(uncheckedFile)) {
                System.out.print("There is an untracked file in the way; delete it, or add and commit it first.");
                System.exit(0);
            }
        }
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
        Set<String> trackedFiles = commit.getFileNames();
        Set<String> stagedFiles = Addition.getFileNames();
        Set<String> removedFiles = Removal.getFileNames();
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
     * update files tracked in the current commit to the state of new commit.
     */
    private static void updateAllFileTo(Commit commit) {
        Commit curCommit = Commit.getCurCommit();
        Map<String, String> trackedFiles = commit.getTrackedFiles();
        for (String filename : curCommit.getFileNames()) {
            File file = join(CWD, filename);
            restrictedDelete(file);
        }

        for (String filename : trackedFiles.keySet()) {
            String contents = readBlob(trackedFiles.get(filename));
            File newFile = join(CWD, filename);
            createNewFile(newFile);
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

    private static void writeBlob(File file) {
        String sha1 = fileSha1(file);
        File folder = join(Repository.BLOBS, sha1.substring(0, 2));
        File blob = join(folder, sha1.substring(2));
        folder.mkdir();
        createNewFile(blob);
        byte[] content = readContents(file);
        writeContents(blob, content);
    }

    private static String readBlob(String sha1) {
        File blob = join(Repository.BLOBS, sha1.substring(0, 2), sha1.substring(2));
        if (!blob.exists()) {
            return "";
        }
        return readContentsAsString(blob);
    }

    /**
     * find the spiltPoint between the cur branch and the given branch.
     */
    private static Commit getSplitPoint(String branchName) {
        FileTracker fileTracker = readObject(ADDITION, FileTracker.class);
        Map<String, String> stage = fileTracker.getTrackedFiles();
        if (stage.size() != 0) {
            System.out.print("You have uncommitted changes.");
            System.exit(0);
        }

        fileTracker = readObject(REMOVAL, FileTracker.class);
        stage = fileTracker.getTrackedFiles();
        if (stage.size() != 0) {
            System.out.print("You have uncommitted changes.");
            System.exit(0);
        }
        String curBranchName = readContentsAsString(CURRENT);
        if (curBranchName.equals(branchName)) {
            System.out.print("Cannot merge a branch with itself.");
            System.exit(0);
        }
        File branchFile = join(BRANCH, Commit.curBranch());
        String branchHeadCommitSha1 = readContentsAsString(branchFile);
        File branchHeadCommitFile = join(COMMITS, branchHeadCommitSha1);
        Commit head1 = readObject(branchHeadCommitFile, Commit.class);

        branchFile = join(BRANCH, branchName);
        if (!branchFile.exists()) {
            System.out.print("A branch with that name does not exist.");
            System.exit(0);
        }
        branchHeadCommitSha1 = readContentsAsString(branchFile);
        branchHeadCommitFile = join(COMMITS, branchHeadCommitSha1);
        Commit head2 = readObject(branchHeadCommitFile, Commit.class);

        Commit ptr1 = head1, ptr2 = head2;
        while (!ptr1.equals(ptr2)) {
            ptr1 = Commit.getCommit(ptr1.getParent());
            ptr2 = Commit.getCommit(ptr2.getParent());
            if (ptr1 == null)
                ptr1 = head2;
            if (ptr2 == null)
                ptr2 = head1;
        }
        if (ptr1.equals(head2)) {
            System.out.print("Given branch is an ancestor of the current branch.");
            System.exit(0);
        }
        if (ptr1.equals(head1)) {
            checkoutBranch(branchName);
            System.out.print("Current branch fast-forwarded.");
            System.exit(0);
        }
        return ptr1;
    }

    private static void dealWithConflict(Commit cur, Commit other, String filename) {
        System.out.print("Encountered a merge conflict.");
        String curFileContentSha1 = cur.getFileContentSha1(filename);
        String otherFileContentSha1 = other.getFileContentSha1(filename);

        String contentInOther = readBlob(otherFileContentSha1);
        String contentInCur = readBlob(curFileContentSha1);
        File newVersion = join(CWD, filename);

        writeContents(newVersion, "<<<<<<< HEAD\n" + contentInCur
                +
                "=======\n" + contentInOther + "\n>>>>>>>");

        FileTracker addition = readObject(ADDITION, FileTracker.class);
        addition.put(newVersion);
        writeObject(ADDITION, addition);
    }
}
