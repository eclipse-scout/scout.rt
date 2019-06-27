package org.eclipse.scout.migration.ecma6;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Optional;

public final class FileUtility {

  private FileUtility(){}

  public static String getFileExtension(Path path){
    String filename = path.getFileName().toString();
    return Optional.ofNullable(filename)
      .filter(f -> f.contains("."))
      .map(f -> f.substring(filename.lastIndexOf(".") + 1))
      .orElse(null);
  }

  public static boolean hasExtension(Path path, String extension){
    if(extension ==  null){
      return false;
    }
    return extension.equalsIgnoreCase(getFileExtension(path));
  }

  public static boolean deleteDirectory(Path directory) throws IOException {
    if(directory == null || !Files.isDirectory(directory)){
      return false;
    }
    Files.walk(directory)
      .sorted(Comparator.reverseOrder())
      .map(Path::toFile)
      .forEach(File::delete);
    return true;
  }
}
