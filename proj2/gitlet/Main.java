package gitlet;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Driver class for Gitlet, a subset of the Git version-control system.
 *
 * @author winter
 */
public class Main {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.print("Please enter a command.");
            System.exit(0);
        }
        String firstArg = args[0];
        switch (firstArg) {
            case "init":
                Repository.init();
                break;
            case "add":
                checkArguments(args, 2);
                Repository.add(args[1]);
                break;
            case "commit":
                checkArguments(args, 2);
                Repository.commit(args[1]);
                break;
            default:
                System.out.print("No command with that name exists.");
        }
    }

    private static void checkArguments(String[] args, int required) {
        if (args.length != required) {
            if (args[0].equals("commit") && args.length == 1) {
                System.out.print("Please enter a commit message.");
                System.exit(0);
            } else {
                System.out.print("Incorrect operands.");
                System.exit(0);
            }
        }
        if (!Repository.isInGit()) {
            System.out.print("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
    }
}
