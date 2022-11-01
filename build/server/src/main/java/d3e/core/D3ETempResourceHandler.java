package d3e.core;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service("temp")
public class D3ETempResourceHandler implements D3EResourceHandler {

    private static String PREFIX = "temp";

    public Resource get(String name) {
        Path filePath = getStoredFilePath(name);
        try {
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {
                return resource;
            } else {
                throw new RuntimeException("File not found " + name);
            }
        } catch (Exception e) {
            throw new RuntimeException("File not found " + name);
        }
    }
    
    @Override
    public boolean isSaved(DFile file) {
    	return false;
    }

    @Override
	public File getFile(String name) {
		return FileUtils.getFile(name, PREFIX, System.getProperty("java.io.tmpdir"));
	}

	public Path getStoredFilePath(String name) {
		return FileUtils.getFilePath(name, PREFIX, System.getProperty("java.io.tmpdir"));
	}

    private String normalizeFileName(String originalName) {
        // Normalize file name
        String fileName = StringUtils.cleanPath(originalName);
        // Check if the file's name contains invalid characters
        if (fileName.contains("..")) {
            throw new RuntimeException("Sorry! Filename contains invalid path sequence " + fileName);
        }
        return fileName;
    }

    private DFile storeNewFile(InputStream fileStream, File targetLocation, String fileName) throws IOException {
        String id = getFileId(targetLocation);
        return FileUtils.storeFile(fileStream, targetLocation, fileName, id);
    }

	private String getFileId(File file) {
		return PREFIX + ":" + file.getName();
	}
    
    public DFile save(File file, String fileName) {
    	String id = getFileId(file);
    	return FileUtils.createDFileFromFile(file, fileName, id);
    }

    public DFile save(String originalName, InputStream fileStream) {
        String fileName = normalizeFileName(originalName);
        try {
            File targetLocation = File.createTempFile(UUID.randomUUID().toString(), FileUtils.getFileExtension(originalName));
            return storeNewFile(fileStream, targetLocation, fileName);
        } catch (IOException e) {
            throw new RuntimeException("Could not store file " + fileName + ". Please try again!", e);
        }
    }

    @Override
    public DFile save(DFile file) {
        throw new RuntimeException("Cannot save to temp storage.");
    }

    @Override
    public DFile saveImage(DFile file, List<ImageDimension> resizes) {
        throw new RuntimeException("Cannot save to temp storage.");
    }
}
