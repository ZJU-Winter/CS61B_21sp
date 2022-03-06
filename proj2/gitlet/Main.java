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
                //TODO: Not in an initialized Gitlet directory.
                //TODO: Incorrect operands.
                Repository.add(args);
                break;
            default:
                System.out.print("No command with that name exists.");
        }
    }
}
