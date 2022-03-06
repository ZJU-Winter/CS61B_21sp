package gitlet;

import java.io.File;

import static gitlet.Utils.*;


/**
 * Represents a gitlet repository.
 * includes all the methods used in gitlet
 *
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
    public static final File CURRENT = join(BRANCH, "current");

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
        File fileToAdd = join(CWD, fileName);
        if (!fileToAdd.exists()) {
            System.out.print("File does not exist.");
            System.exit(0);
        }
        FileTracker fileTracker = readObject(ADDITION, FileTracker.class);
        fileTracker.updateTrackedFiles(fileToAdd);
        writeObject(ADDITION, fileTracker);

/*
        FileTracker tracker = readObject(ADDITION, FileTracker.class);
        System.out.println("----------------------");
        System.out.println(tracker.getTrackedFiles());
 */

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
     * to create file or a directory
     */
    private static void createFile(File file, boolean isDir) {
        if (isDir) {
            file.mkdir();
        } else if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (Exception exception) {
                throw new GitletException("Failed to creat " + file.getName());
            }
        }
    }

    /**
     * to set up all needed files and directories for gitlet
     */
    private static void setupPersistence() {
        if (isInGit()) {
            System.out.print("A Gitlet version-control system already exists in the current directory.");
            System.exit(0);
        }
        createFile(GITLET_DIR, true);
        createFile(STAGINGAREA, true);
        createFile(COMMITS, true);
        createFile(BLOBS, true);
        createFile(BRANCH, true);
        createFile(HEAD, false);
        createFile(ADDITION, false);
        createFile(REMOVAL, false);
        writeObject(ADDITION, new FileTracker());
        writeObject(REMOVAL, new FileTracker());
    }

    public static boolean isInGit() {
        if (GITLET_DIR.exists())
            return true;
        return false;
    }

}
