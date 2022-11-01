package d3e.core;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.apache.tika.Tika;

public class FileUtils {
	static Tika tika = new Tika();
    static Path getFilePath(String name, String prefix, String targetPath) {
        File file = getFile(name, prefix, targetPath);
        return file.toPath();
    }
    
    public static File getFile(String name, String prefix, String targetPath) {
    	if (name.startsWith(prefix)) {
    		name = name.substring(prefix.length() + 1);
    		File targetLocation = new File(targetPath + File.separator + name);
    		return targetLocation;
    	} else {
    		throw new RuntimeException("File not found " + name);
    	}
    }
    
    public static String detectMimeType(File file) {
    	try {
    		return tika.detect(file);
    	}catch (Exception e) {
    		return "";
    	}
    }

    static DFile storeFile(InputStream fileStream, File file, String name, String id) throws IOException {
        Path filePath = file.toPath();
        Files.copy(fileStream, filePath, StandardCopyOption.REPLACE_EXISTING);
        return createDFileFromFile(file, name, id);
    }

	public static DFile createDFileFromFile(File file, String name, String id) {
		DFile out = new DFile();
        out.setMimeType(detectMimeType(file));
        out.setId(id);
        out.setName(name);
        out.setSize(file.length());
        return out;
	}

    static String getResizedName(String name, int width, int height) {
        StringBuilder nameBuilder = new StringBuilder(name);
        String resPart = "";
        if (width != 0 && height != 0) {
            resPart = "_" + width + "_" + height;
        }
        int lastDot = name.lastIndexOf(".");
        nameBuilder.insert(lastDot, resPart);
        return nameBuilder.toString();
    }

    static String stripPrefix(String id) {
        int prefixEnd = id.indexOf(":");
        if (prefixEnd == -1) {
            return id;
        }
        return id.substring(prefixEnd + 1);
    }
    
    public static String getFileExtension(String filename) {
        int lastDot = filename.lastIndexOf(".");
        if (lastDot == -1) {
            return ".d3e";
        }
        return filename.substring(lastDot);
    }
    
    public static String getFileNameWithoutExtension(String filename) {
    	int lastDot = filename.lastIndexOf(".");
    	if (lastDot == -1) {
    		return filename;
    	}
    	return filename.substring(0, lastDot);
    }
}
