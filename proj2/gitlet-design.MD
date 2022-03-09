# Gitlet Design Document

**Author**: Winter Huang

## Classes and Data Structures

### Commit

#### Instance Variables

Extends `FileTracker`

* `String` author
    * always `Winter`
* `String` message
    * message for the commit
* `String` time
    * current time for the commit
        * format:`yyyy/MMM/dd HH:mm:ss`
* `String` parent
    * parent of the commit object
        * using `String` instead of `Commit` or reducing time and space complexity when serializing
* `Map<String, String>` trackedFiles inherited from `FileTracker`
    * Files that are tracked by the commit object, using map
        * `String` fileName
        * `String` file's sha1 Code

### Repository

## Algorithms

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