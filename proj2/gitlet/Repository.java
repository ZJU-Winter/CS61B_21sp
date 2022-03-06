package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.StreamSupport;

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
        commit.commit("master");
    }

    public static void add(String[] args) {
        String[] files = new String[args.length - 1];
        System.arraycopy(args, 1, files, 0, files.length);
        for (String file : files) {
            addToStage(file);
        }
    }

    /**
     * Call the addToStage(String fileName) add a file to the STAGE directory
     */
    //TODO:: traverse the current commit object
    private static void addToStage(String fileName) {
        File file = join(CWD, fileName);
        if (!file.exists()) {
            System.out.print("File does not exist.");
            System.exit(0);
        }
        FileTracker fileTracker = readObject(ADDITION, FileTracker.class);
        updateTrackedFiles(file, fileTracker.getTrackedFiles());
        writeObject(ADDITION, fileTracker);

        /* for debug
        FileEntry newEntry = readObject(ADDITION, FileEntry.class);
        System.out.println("----------");
        System.out.println(newEntry.getFiles());
         */
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

    /**
     * update the Map<String, String>
     */
    private static void updateTrackedFiles(File fileToAdd, Map<String, String> files) {
        byte[] content = readContents(fileToAdd);
        String sha1Code = sha1(content);

        files.put(fileToAdd.getName(), sha1Code);
    }

    /**
     * get file according to the sha1Code and
     */
    private static Commit getCommit(String sha1code) {
        File commit = join(COMMITS, sha1code);
        if (!commit.exists()) {
            System.out.print("Commit file does not exist.");
            System.exit(0);
        }
        return readObject(commit, Commit.class);
    }

    public static boolean isInGit() {
        if (GITLET_DIR.exists())
            return true;
        return false;
    }

}
