/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.testing.platform.util;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.eclipse.scout.rt.platform.Bean;

/**
 * Checks for each {@link Bean} with if there exists a corresponding {@code scout.xml} file.
 */
@Bean
public class ScoutXmlTestSupport implements ITestSupportIsExcluded, ITestSupportFailOnError {

  private final List<Pattern> m_searchPatterns;
  private final List<Path> m_pathExclusions;
  private final List<String> m_errMessages;
  private final Set<Path> m_missingFiles;
  private Path m_root;

  public ScoutXmlTestSupport() {
    m_searchPatterns = new ArrayList<>();
    m_searchPatterns.add(Pattern.compile("@Bean"));
    m_searchPatterns.add(Pattern.compile("@ApplicationScoped"));

    m_pathExclusions = new ArrayList<>();

    m_errMessages = new ArrayList<>();
    m_missingFiles = new HashSet<>();
    m_root = Paths.get("..").toAbsolutePath().normalize();
  }

  public void addPathExclusion(Path path) {
    m_pathExclusions.add(path);
  }

  @Override
  public List<Path> getPathExclusions() {
    return m_pathExclusions;
  }

  public void doTest() throws IOException {
    checkPaths();
    createMissingFiles();
  }

  /**
   * Calls {@link  #checkFiles(Path)} for all paths {@code <module>/src/main} and {@code <module>/src/test}.
   */
  protected void checkPaths() throws IOException {
    try (Stream<Path> paths = Files.find(getRoot(), 3,
        (path, attrs) ->
            attrs.isDirectory()
                && (path.endsWith(Path.of("src", "main"))
                        || path.endsWith(Path.of("src", "test"))))) {
      for (Path path : paths.toList()) {
        checkFiles(path);
      }
    }
  }

  /**
   * Checks if the path contains the {@code scout.xml} file. If it's missing and there is a {@code java} folder, it calls {@link #checkFile(Path)} on each java file.
   */
  protected void checkFiles(Path path) throws IOException {
    if (Files.exists(path.resolve("resources").resolve("META-INF").resolve("scout.xml"))) {
      return; // scout.xml is present
    }
    Path java = path.resolve("java");
    if (!Files.exists(java)) {
      return; // no java folder - skip
    }

    Files.walkFileTree(java, new SimpleFileVisitor<>() {
      @Override
      public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
        if (isExcluded(dir)) {
          return FileVisitResult.SKIP_SUBTREE;
        }
        return FileVisitResult.CONTINUE;
      }

      @Override
      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        Path fileName = file.getFileName();
        if (fileName != null && fileName.toString().toLowerCase().endsWith(".java") && !isExcluded(file)) {
          checkFile(file);
        }
        return FileVisitResult.CONTINUE;
      }
    });
  }

  /**
   * If the file contains any of the search patterns, an error message is added and the missing {@code scout.xml} file is added to the missing files.
   */
  protected void checkFile(Path path) throws IOException {
    String content = Files.readString(path);
    for (Pattern pat : m_searchPatterns) {
      if (pat.matcher(content).find()) {
        Path subpath = path.subpath(getRoot().getNameCount(), path.getNameCount());
        Path scoutXmlSubpath = subpath.subpath(0, 3).resolve("resources").resolve("META-INF").resolve("scout.xml");
        m_errMessages.add("Bean annotation ('" + pat.toString().replace("\\", "") + "') found in '" + subpath + "'. Missing scout.xml in '" + scoutXmlSubpath + "'!");
        m_missingFiles.add(getRoot().resolve(scoutXmlSubpath));
      }
    }
  }

  protected void createMissingFiles() throws IOException {
    for (Path path : m_missingFiles) {
      Files.createDirectories(path.getParent());
      Files.writeString(path, getScoutXmlFileContent());
    }
  }

  protected String getScoutXmlFileContent() {
    return """
        <?xml version="1.0" encoding="UTF-8"?>
        <scout>
        </scout>
        """;
  }

  @Override
  public List<String> getErrorMessages() {
    return Collections.unmodifiableList(m_errMessages);
  }

  @Override
  public Path getRoot() {
    return m_root;
  }

  public void setRoot(Path root) {
    m_root = root;
  }
}
