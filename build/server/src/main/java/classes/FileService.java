package classes;

import d3e.core.DFile;
import d3e.core.FileHelper;

public class FileService {
  public FileService() {}

  public static DFile createTempFile(String fullNameOrExtn, boolean extnGiven, String content) {
    return FileHelper.get().createTempFile(fullNameOrExtn, extnGiven, content);
  }
}
