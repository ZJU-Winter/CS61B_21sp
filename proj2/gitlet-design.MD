# Gitlet Design Document

**Author**: Winter Huang

**Date**: 2022/03

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
    *
        1. create all needed files and directories
    *
        2. create an initial branch master
    *
        3. commit an empty commit
* `add()`
    * usage in gitlet: `gitlet add [filename]`
    *
        1. read the previous FileTracker object
    *
        2. update the object

        * if the content is different, update the content
        * if the current working version of the file is identical to the version in the current commit, delete it from
          the staging area.
    *
        3. write back to ADDITION
* `init()`
    * usage in gitlet:
* `init()`
    * usage in gitlet:
* `init()`
    * usage in gitlet:
* `init()`
    * usage in gitlet:
* `init()`
    * usage in gitlet:

* `init()`
    * usage in gitlet:
* `init()`
    * usage in gitlet:

* `init()`
    * usage in gitlet:
* `init()`
    * usage in gitlet:

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