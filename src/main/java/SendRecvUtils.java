import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

public class SendRecvUtils {
    public static void send(String information, PrintWriter writer){
        writer.println(information);
    }

    public static void send(String information, PrintWriter writer1, PrintWriter writer2){
        writer1.println(information);
        writer2.println(information);
    }

    public static String receive(BufferedReader reader) throws IOException {
        return reader.readLine();
    }
}
