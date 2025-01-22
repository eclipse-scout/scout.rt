/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.dataobject.testing;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Assert;

public abstract class AbstractDataObjectTestSupport {

  protected Path m_root;

  protected final Pattern m_filePattern;
  protected final Pattern m_testFilePattern;

  protected final Pattern m_packagePattern;
  protected final Pattern m_packageNamePrefixPattern;

  /**
   * Files that should be checked for missing test and the package name they are located at.
   */
  protected final Map<Path /* file path */, String /* package name */> m_files;

  /**
   * Available test files with the package name prefixes the test is covering.
   */
  protected final Map<Path /* file path */, List<String> /* package name prefixes */> m_testFiles;

  protected final List<Path> m_pathExclusions;
  protected final List<String> m_errMessages;

  public AbstractDataObjectTestSupport() {
    m_root = Path.of("..").toAbsolutePath().normalize();

    m_filePattern = createFilePattern();
    m_testFilePattern = createTestFilePattern();

    m_packagePattern = createPackageNamePattern();
    m_packageNamePrefixPattern = createPackageNamePrefixPattern();

    m_files = new HashMap<>();
    m_testFiles = new HashMap<>();

    m_pathExclusions = new ArrayList<>();
    m_errMessages = new ArrayList<>();
  }

  public Path getRoot() {
    return m_root;
  }

  public void setRoot(Path root) {
    m_root = root;
  }

  public void addPathExclusion(Path path) {
    m_pathExclusions.add(path);
  }

  /**
   * Extracts the package name from the java file
   */
  protected Pattern createPackageNamePattern() {
    return Pattern.compile("^\\s*package\\s+([a-zA-Z_][a-zA-Z0-9_]*(?:\\.[a-zA-Z_][a-zA-Z0-9_]*)*)\\s*;", Pattern.MULTILINE);
  }

  /**
   * Pattern to detect the files to be tested
   */
  protected abstract Pattern createFilePattern();

  /**
   * Pattern to detect the test files
   */
  protected abstract Pattern createTestFilePattern();

  /**
   * Pattern to extract the package name prefixes from the test file this test is covering
   */
  protected abstract Pattern createPackageNamePrefixPattern();

  public void doTest() throws IOException {
    collectFiles();
    checkFiles();
  }

  protected void collectFiles() throws IOException {
    Files.walkFileTree(getRoot(), new SimpleFileVisitor<>() {
      @Override
      public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
        Path fileName = dir.getFileName();
        if (fileName != null && (".git".equals(fileName.toString()) || "node_modules".equals(fileName.toString())) || isExcluded(dir)) {
          return FileVisitResult.SKIP_SUBTREE;
        }
        return FileVisitResult.CONTINUE;
      }

      @Override
      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        Path fileName = file.getFileName();
        if (fileName != null && fileName.toString().toLowerCase().endsWith(".java") && !isExcluded(file)) {
          AbstractDataObjectTestSupport.this.visitFile(file);
        }
        return FileVisitResult.CONTINUE;
      }
    });
  }

  protected void visitFile(Path path) throws IOException {
    String content = Files.readString(path);
    collectFile(path, content);
    collectTestFile(path, content);
  }

  protected void collectFile(Path path, String content) {
    if (acceptFile(path, content)) {
      Matcher matcher = m_packagePattern.matcher(content);
      if (matcher.find()) {
        String packageName = matcher.group(1);
        m_files.put(path, packageName);
      }
    }
  }

  /**
   * Checks if the file should be checked for missing test
   *
   * @param path
   *          path of the file
   * @param content
   *          content of the file
   * @return <code>true</code> if the file should be checked
   */
  protected abstract boolean acceptFile(Path path, String content);

  protected void collectTestFile(Path path, String content) {
    if (acceptTestFile(path, content)) {
      Matcher matcher = m_packageNamePrefixPattern.matcher(content);
      while (matcher.find()) {
        String packageNamePrefix = matcher.group(1);
        m_testFiles.computeIfAbsent(path, k -> new ArrayList<>()).add(packageNamePrefix);
      }
    }
  }

  /**
   * Checks if the file one of the expected test files
   *
   * @param path
   *          path of the file
   * @param content
   *          content of the file
   * @return <code>true</code> if the file is an expected test file
   */
  protected abstract boolean acceptTestFile(Path path, String content);

  protected void checkFiles() {
    m_files.forEach((filePath, packageName) -> {
      if (m_testFiles.values().stream().flatMap(Collection::stream).noneMatch(packageName::startsWith)) {
        addErrorMessage(filePath.getFileName().toString());
      }
    });
  }

  protected void addErrorMessage(String message) {
    m_errMessages.add(message);
  }

  protected boolean isExcluded(Path path) {
    if (getRoot().getNameCount() >= path.getNameCount()) {
      return false;
    }
    Path subpath = path.subpath(getRoot().getNameCount(), path.getNameCount());
    for (Path pathExclusion : m_pathExclusions) {
      if (subpath.endsWith(pathExclusion)) {
        return true;
      }
    }
    return false;
  }

  public List<String> getErrorMessages() {
    return Collections.unmodifiableList(m_errMessages);
  }

  public void failOnError() {
    List<String> err = getErrorMessages();
    if (err.isEmpty()) {
      return;
    }
    StringBuilder sb = new StringBuilder();
    sb.append(getErrorTitle()).append("\n");
    sb.append(err.get(0));
    for (int i = 1; i < err.size(); i++) {
      sb.append("\n").append(err.get(i));
    }
    String message = sb.toString();
    Assert.fail(message);
  }

  /**
   * Title text for the error message when the test fails
   */
  protected abstract String getErrorTitle();
}
