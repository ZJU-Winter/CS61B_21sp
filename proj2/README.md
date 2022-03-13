# Gitlet Design Document

**Author**: Winter Huang

## Classes and Data Structures

#### Main Methods

### Main

* the entrance of the whole program
    * mainly call functions in `Repository` (see below)
    * check input arguments

### Commit

#### Instance Variables

Extends `FileTracker`

* `String` author
    * always `Winter`
* `String` message
    * message for the commit
* `String` time
    * current time for the commit
        * format:`EEE MMM dd HH:mm:ss yyyy Z`
* `String` parent
    * parent of the commit object
        * using `String` instead of `Commit` or reducing time and space complexity when serializing
* `Map<String, String>` trackedFiles inherited from `FileTracker`
    * Files that are tracked by the commit object, using map
        * `String` fileName
        * `String` file's sha1 Code

#### Main Methods

* `public void updateTrackFiles()`
    * call the function before `commit()`
    * update `trackedfiles` according to the current stage
        * add files in the `addition` to `trackedfiles`
        * remove files in the `removal` from `trackedfiles`
        * clear stage
* `public void commit()`
    * commit a `Commit` object
        * update `trackedfiles`
        * creat commit file in the `COMMITS`
        * update `HEAD` & `CURRENT`

### Repository

#### Instance Variables

* `CWD`
    * The current working directory
* `GITLET_DIR`
    * The .gitlet directory: CWD/.gitlet
* `HEAD`
    * The file that records the HEAD sha1 code: CWD/.gitlet/Head.txt
* `STAGINGAREA`
    * The directory for staged files: CWD/.gitlet/stagearea
* `COMMITS`
    * The directory for all the commits: CWD/.gitlet/commits
* `BLOBS`
    * The directory for all recorded files: CWD/.gitlet/blobs
* `BRANCH`
    * The directory for all branches: CWD/.gitlet/branches
* `CURRENT`
    * The file for recording the current branch's name: CWD/CURRENT
* `ADDITION`
    * The file for recording staged files for addition: CWD/stagearea/addition
* `REMOVAL`
    * he file for recording staged files for removal: CWD/stagearea/removal

#### Main Methods

* `init()`
    * usage in gitlet: `gitlet init`

    1. create all needed files and directories

    2. create an initial branch master

    3. commit an empty commit
* `add()`
    * usage in gitlet: `gitlet add [filename]`

    1. read the previous FileTracker object
    2. update the object

        * if the content is different, update the content
        * if the current working version of the file is identical to the version in the current commit, delete it from
          the staging area.
    3. write back to ADDITION
* `commit(String message)`
    * usage in gitlet: `gitlet commit [message]`
    *
        1. copy from the parent commit
    *
        2. put adding stage to tracked files and clear the stage
    *
        3. save the new commit to the directory
    *
        4. setup HEAD and BRANCH
* `rm(String filename)`
    * usage in gitlet: `gitlet rm [filename]`

    1. if the file is staged, unstage it

    2. if the file is tracked in the current commit

        * stage it for removal
        * remove it from the working directory
* `log()`
    * usage in gitlet: `gitlet log`
    * show all the commits start from the head commit
* `globalLog()`
    * usage in gitlet: `gitlet global-log`
    * show all the commit regardless of order
* `find(String message)`
    * usage in gitlet: `gitlet find [message]`
    * show the information of a commit with given message

* `status()`
    * usage in gitlet: `gitlet status`
    * show current file/stage/branch status

    1. show branches
        * current branch start with *
    2. show staged files
        * include added files and removed files
    3. show modified but not staged files
        * tracked in the current commit, changed in the working directory, but not staged; or
        * staged for addition, but with different contents than in the working directory; or
        * staged for addition, but deleted in the working directory; or
        * not staged for removal, but tracked in the current commit and deleted from the working directory. 4.show
          untracked files
    4. show untracked files
        * present in the working directory but neither staged for addition nor tracked.
* `checkout(String[] args)`
    * usage in gitlet:

    1. `gitlet checkout [branchname]`
        * update tracked files to the stage of the given branch's head
    2. `gitlet checkout -- [filename]`
        * update the given file to the stage of head commit
    3. `gitlet checkout [commitID] -- [filename]`
        * update the given file to the stage of given commit

* `branch(String branchname)`
    * usage in gitlet: `gitlet branch [branchname]`
    * create a new branch named `branchname` point to the current commit
* `removeBranch(String branchname)`
    * usage in gitlet: `gitlet rm-branch [branchname]`
    * remove the branch named `branchname`
* `reset(String commitID)`
    * usage in gitlet: `gitlet reset [commitID]`
    * checkout an arbitrary commit
* `merge(String branchname)`
    * usage in gitlet: `gitlet merge [branchname]`
    * merge the given branch with the current branch

## Persistence and File System

### In the `.gitlet`

#### CURRENT

* The file contains current head's branch name
    * filename: CURRENT
    * content: current branch's name

#### HEAD

* The file contains the HEAD commit sha1Code
    * filename: HEAD
    * content: `sha1code` of the HEAD commit

#### branches

* the directory contains all branches
    * filename: branches
    * content: all branches
        * name: branch name
        * content: the `sha1code` of the commit

#### blobs

* The directory contains all versions of files
    * filename: blobs
    * content: all versions of tracked files
        * name: `sha1code` of the file content
        * content: the content of file
        * using folders with first 2 letters of the sha1code
            * like hashmap

#### commits

* the directory contains the commits history
    * filename: commits
    * content: all the commit files
        * name: the `sha1code` of the commit
        * content: the serialized commit object

#### stagingarea

* the directory contains the staging files
    * filename: stagingarea
    * content: the serialized Map of the staging files
        * Map<FileName, FileSha1Code>