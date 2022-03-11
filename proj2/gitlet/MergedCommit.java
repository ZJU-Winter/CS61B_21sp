package gitlet;

import java.util.HashSet;
import java.util.Set;

public class MergedCommit extends Commit {
    private String secondParent;

    MergedCommit(Commit firstParent, Commit secondParent, String message) {
        super(firstParent, message);
        this.secondParent = secondParent.getSha1();
    }

    @Override
    public String getParent() {
        return secondParent;
    }

    public Set<String> getParents() {
        Set<String> parents = new HashSet<>();
        parents.add(this.getParent());
        parents.add(this.secondParent);
        return parents;
    }

    @Override
    public String toString() {
        String info = String.format("Message: %s Time: %s Author: %s\n"
                +
                "FirstParentSha1: %s \n"
                +
                "SecondParentSha1: %s \n"
                +
                " TrackedFiles: %s", getMessage(), getTime(), "Winter", getParent(),
                secondParent, trackedFiles);
        return info;
    }

    @Override
    public int hashCode() {
        return this.toString().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (this.getClass() != o.getClass()) {
            return false;
        }
        Commit other = (Commit) o;
        return getTime().equals(other.getTime())
                &&
                getMessage().equals(other.getMessage())
                &&
                this.trackedFiles.equals(other.trackedFiles);
    }

}
