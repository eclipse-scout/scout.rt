package org.eclipse.scout.migration.ecma6;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.scout.rt.platform.exception.VetoException;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class FileUtility {
  private static final Logger LOG = LoggerFactory.getLogger(FileUtility.class);

  private FileUtility() {
  }

  public static String getFileExtension(Path path) {
    String filename = path.getFileName().toString();
    return Optional.ofNullable(filename)
        .filter(f -> f.contains("."))
        .map(f -> f.substring(filename.lastIndexOf(".") + 1))
        .orElse(null);
  }

  public static Path removeFileExtension(Path path) {
    String fileExtension = getFileExtension(path);
    if (fileExtension == null) {
      return path;
    }
    String pathName = path.toString();
    return Paths.get(pathName.substring(0, pathName.length() - (fileExtension.length() + 1)));
  }

  public static Path removeFileExtensionJs(Path path) {
    if ("js".equalsIgnoreCase(getFileExtension(path))) {
      return removeFileExtension(path);
    }
    return path;
  }

  public static boolean hasExtension(Path path, String... extensions) {
    if (extensions == null) {
      return false;
    }
    final Set<String> extensionSet = Arrays.stream(extensions)
        .filter(Objects::nonNull)
        .map(String::toLowerCase)
        .collect(Collectors.toSet());
    String fileExtension = Optional.ofNullable(getFileExtension(path))
        .map(String::toLowerCase)
        .orElse(null);
    return extensionSet.contains(fileExtension);
  }

  public static boolean deleteDirectory(Path directory) throws IOException {
    if (directory == null || !Files.isDirectory(directory)) {
      return false;
    }
    try (Stream<Path> resourceStream = Files.walk(directory)) {
      resourceStream
          .sorted(Comparator.reverseOrder())
          .map(Path::toFile)
          .forEach(File::delete);
    }
    return true;
  }

  public static boolean moveDirectory(Path srcDir, Path targetDir, Predicate<Path> filter) throws IOException {
    Files.createDirectories(targetDir);
    Files.walkFileTree(srcDir, new SimpleFileVisitor<Path>() {

      @Override
      public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        Path rel = srcDir.relativize(dir);
        Path target = targetDir.resolve(rel);
        if (!Files.exists(target)) {
          Files.createDirectory(target);
        }
        return FileVisitResult.CONTINUE;
      }

      @Override
      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        if (!filter.test(file)) {
          return FileVisitResult.CONTINUE;
        }
        Path rel = srcDir.relativize(file);
        Path target = targetDir.resolve(rel);
        Files.move(file, target, StandardCopyOption.ATOMIC_MOVE);
        return FileVisitResult.CONTINUE;
      }

    });

    return true;
  }

  public static Path replaceSegment(Path path, Path segments, Path replacement) {
    String p = path.toString().replace("\\", "/");
    String seg = segments.toString().replace("\\", "/");
    String rep = replacement.toString().replace("\\", "/");
    return Paths.get(p.replace(seg, rep));
  }

  public static String lineSeparator(Path path) {
    InputStream in = null;
    try {
      Assertions.assertTrue(Files.exists(path));
      in = Files.newInputStream(path, StandardOpenOption.READ);
      return Optional.ofNullable(lineSeparator(in)).filter(nl -> nl != null && nl.length() > 0).orElse("\n");
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
          lineSeparator.append((char) c);
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
    }
    finally {
      if (in != null) {
        try {
          in.close();
        }
        catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  public static boolean findInFile(Path file, String text) throws IOException {

    try (BufferedReader reader = Files.newBufferedReader(file)) {
      String line = reader.readLine();
      while (line != null) {
        if (line.contains(text)) {
          return true;
        }
        line = reader.readLine();
      }
    }
    return false;
  }

}
