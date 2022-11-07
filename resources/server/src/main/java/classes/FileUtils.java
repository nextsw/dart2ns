package classes;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

import d3e.core.D3ELogger;

public class FileUtils {
    public static String readContent(String path) {
        try {
            String content = new String(Files.readAllBytes(Path.of(path)), Charset.defaultCharset());
            return content;
        } catch(Exception e) {
            return "";
        }
    }
    public static void writeFile(String path, String content) {
        try{
            Path res = Path.of(path);
            Path parent = res.getParent();
        	Files.createDirectories(parent);
            Files.write(res, content.getBytes(Charset.defaultCharset()));
        } catch(Exception e) {
        	D3ELogger.error("Unable to write file" + path);
        }
    }

    public static void deleteFolder(String path) {
		try {
			org.apache.commons.io.FileUtils.deleteDirectory(Path.of(path).toFile());
		} catch (Exception e) {
			D3ELogger.error("Unable to Delete Folder" + path);
		}
	}
}