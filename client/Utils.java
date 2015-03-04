import java.io.Console;

public class Utils {
    public static char[] readPassword(String message) {
        char[] password = null;
        try {
            Console console = System.console();
            if (console != null) {
                password = console.readPassword(message);
                System.out.println("password was: " + new String(password));
            } else {
                System.err.println("Console not available, try running from a real terminal and not an IDE.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return password;
    }
}