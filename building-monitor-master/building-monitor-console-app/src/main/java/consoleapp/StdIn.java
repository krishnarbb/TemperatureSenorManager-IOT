
package consoleapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class StdIn {

    public static String readLine() {
        // written to make it work in IDE as System.console() is null
        // when run inside the IDE
        try (BufferedReader in = new BufferedReader(new InputStreamReader(System.in))) {
            return in.readLine();
        } catch (final IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
