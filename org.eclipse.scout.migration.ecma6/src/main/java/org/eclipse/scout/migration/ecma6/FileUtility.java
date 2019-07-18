package org.eclipse.scout.migration.ecma6;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Comparator;
import java.util.Optional;

import org.eclipse.scout.rt.platform.exception.VetoException;
import org.eclipse.scout.rt.platform.util.Assertions;

public final class FileUtility {

  private FileUtility() {
  }

  public static String getFileExtension(Path path) {
    String filename = path.getFileName().toString();
    return Optional.ofNullable(filename)
        .filter(f -> f.contains("."))
        .map(f -> f.substring(filename.lastIndexOf(".") + 1))
        .orElse(null);
  }

  public static boolean hasExtension(Path path, String extension) {
    if (extension == null) {
      return false;
    }
    return extension.equalsIgnoreCase(getFileExtension(path));
  }

  public static boolean deleteDirectory(Path directory) throws IOException {
    if (directory == null || !Files.isDirectory(directory)) {
      return false;
    }
    Files.walk(directory)
        .sorted(Comparator.reverseOrder())
        .map(Path::toFile)
        .forEach(File::delete);
    return true;
  }

  public static String lineSeparator(Path path) {
    InputStream in = null;
    try {
      Assertions.assertTrue(Files.exists(path));
      in = Files.newInputStream(path, StandardOpenOption.READ);

      return lineSeparator(in);

    }
    catch (IOException e) {
      throw new VetoException("Could not read line separator of file '" + path + "'", e);
    }
    finally {
      if (in != null) {
        try {
          in.close();
        }
        catch (IOException e) {
          // void
        }
      }
    }
  }

  private static String lineSeparator(InputStream in) throws IOException {
    StringBuilder lineSeparator = new StringBuilder();
    int c = in.read();
    while (c > -1) {
      switch ((char) c) {
        case '\n':
        case '\r':
          lineSeparator.append((char)c);
          break;
        default:
          if (lineSeparator.length() > 0) {
            return lineSeparator.toString();
          }
      }
      c = in.read();
    }
    return lineSeparator.toString();
  }

  public static String lineSeparator(String string) {
    ByteArrayInputStream in = null;
    try {
      in = new ByteArrayInputStream(string.getBytes());
      return lineSeparator(in);
    }
    catch (IOException e) {
      throw new VetoException("Could not read line separator of source.", e);
    }finally{
      if(in != null){
        try {
          in.close();
        }
        catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }
}
