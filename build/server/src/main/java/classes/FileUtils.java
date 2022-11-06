package classes;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;


public class FileUtils {
    public static String readContent(String path) {
        try {
            String content = new String(Files.readAllBytes(Path.of(path)), Charset.defaultCharset());
            return content;
        } catch(Exception e) {
            return "";
        }
    }
}