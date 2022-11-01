package d3e.core;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FileHelper {
	private static FileHelper INS;

	@Autowired
	private D3ETempResourceHandler tempHandler;

	public static FileHelper get() {
		return INS;
	}

	@PostConstruct
	public void init() {
		INS = this;
	}

	public DFile createTempFile(String fullNameOrExtn, boolean extnGiven, String content) {
		String fileName = null;
		String extn = null;
		if (extnGiven) {
			// Create file name
			if (fullNameOrExtn == null || fullNameOrExtn.isEmpty()) {
				// TODO: throw?
				return null;
			}
			extn = "." + fullNameOrExtn;
		} else {
			fileName = FileUtils.getFileNameWithoutExtension(fullNameOrExtn);
			extn = FileUtils.getFileExtension(fullNameOrExtn);
		}

		if (fileName == null) {
			// Acceptable, since createTempFile will handle it as long as the prefix has
			// length of at least 3
			fileName = "tmp";
		}
		if (extn == null || extn.isEmpty()) {
			// Something went wrong. Mostly invalid data.
			return null;
		}

		try {
			// Create file
			File file = File.createTempFile(fileName, extn);

			// Populate file
			OutputStream out = new FileOutputStream(file);
			byte[] bytes = content.getBytes();
			out.write(bytes);
			out.close();

			System.err.println("Created & wrote: " + file.getAbsolutePath());

			// Save & return DFile
			return tempHandler.save(file, fileName);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public File getStoredFile(DFile file) {
		try {
			return tempHandler.getFile(file.getId());
		} catch (RuntimeException e) {
			return null;
		}
	}
}
