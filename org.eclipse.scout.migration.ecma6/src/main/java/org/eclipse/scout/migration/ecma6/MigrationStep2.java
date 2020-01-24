/*
 * Copyright (c) 2010-2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.migration.ecma6;

import static org.eclipse.scout.migration.ecma6.task.T30000_JsonToJsModule.JSON_MODEL_NAME_SUFFIX;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.eclipse.scout.migration.ecma6.configuration.Configuration;
import org.eclipse.scout.migration.ecma6.pathfilter.IMigrationIncludePathFilter;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MigrationStep2 implements IRunnable {
  private static final Logger LOG = LoggerFactory.getLogger(MigrationStep2.class);
  private static final String JSON_EXTENSION = "json";
  private static final String JS_EXTENSION = "js";
  Pattern JQUERY_PAT = Pattern.compile("webcontent[\\\\/]res[\\\\/]jquery-", Pattern.CASE_INSENSITIVE);
  Pattern JASMINE_PAT = Pattern.compile("webcontent[\\\\/]res[\\\\/]jasmine-", Pattern.CASE_INSENSITIVE);
  private Set<Path> m_deletedFiles = new HashSet<>();

  public static void main(String[] args) {
    new MigrationStep2().run();
  }

  @Override
  public void run() {
    moveIndexJs();
    removeJsFolder();
    fileByFileTasks();
    deleteEmptyDirectories();
    LOG.info("Migration step 2 completed.");
  }

  protected void moveIndexJs() {
    Path targetModuleDirectory = Configuration.get().getTargetModuleDirectory();
    Path indexJsFile = targetModuleDirectory.resolve(Paths.get("src/main/resources/WebContent/res/index.js"));
    if (Files.exists(indexJsFile)) {
      Path targetIndexJsFile = targetModuleDirectory.resolve(indexJsFile.getFileName());
      try {
        Files.move(indexJsFile, targetIndexJsFile, StandardCopyOption.ATOMIC_MOVE);
      }
      catch (IOException e) {
        throw new ProcessingException("Could not move index.js from '" + indexJsFile + "' to '" + targetIndexJsFile + "'.", e);
      }
    }
  }

  protected void removeJsFolder() {
    if (!Configuration.get().isRemoveJsFolder()) {
      return;
    }
    final IMigrationIncludePathFilter includeFilter = BEANS.opt(IMigrationIncludePathFilter.class);
    Path sourceFolder = Configuration.get().getTargetModuleDirectory().resolve(Paths.get("src", "main", "js", Configuration.get().getJsFolderName()));

    if (Files.exists(sourceFolder)) {
      Path targetFolder = Configuration.get().getTargetModuleDirectory().resolve(Paths.get("src", "main", "js"));
      try {
        FileUtility.moveDirectory(sourceFolder, targetFolder, includeFilter);
      }
      catch (IOException e) {
        throw new ProcessingException("Could not move src js from '" + sourceFolder + "' to '" + targetFolder + "'.", e);
      }
    }
  }

  protected void fileByFileTasks() {
    try {
      MigrationFileVisitor.visitMigrationFiles(this::visitFile);
    }
    catch (IOException e) {
      throw new ProcessingException("Could not visit all files to find model json files", e);
    }
  }

  protected void visitFile(PathInfo pathInfo) {
    try {
      try {
        removeJQueryJasmineFiles(pathInfo.getPath());
      }
      catch (IOException e) {
        throw new ProcessingException("Could not delete jqueryModule or Jasmin mod file '" + pathInfo.getPath() + "'.", e);
      }
      processModelJsonToJsFile(pathInfo);
      try {
        processLessModuleFile(pathInfo.getPath());
      }
      catch (IOException e) {
        throw new ProcessingException("Could not process less module '" + pathInfo.getPath() + "'.", e);
      }
      try {
        processModuleJsFile(pathInfo.getPath());
      }
      catch (IOException e) {
        throw new ProcessingException("Could not process js module '" + pathInfo.getPath() + "'.", e);
      }

      try {
        processSharedIndexFiles(pathInfo);
      }
      catch (IOException e) {
        throw new ProcessingException("Could not process shared .index '" + pathInfo.getPath() + "'.", e);
      }

      try {
        deleteMacroAndModuleFiles(pathInfo.getPath());
      }
      catch (IOException e) {
        throw new ProcessingException("Could not delete macro or module file '" + pathInfo.getPath() + "'.", e);
      }
    }
    catch (ProcessingException e) {
      LOG.error("exception during visiting file '" + pathInfo.getPath() + "'.", e);
      throw e;
    }
    catch (Exception e) {
      LOG.error("exception during visiting file '" + pathInfo.getPath() + "'.", e);
      throw new ProcessingException("exception during visiting file '" + pathInfo.getPath() + "'.", e);
    }
  }

  protected void removeJQueryJasmineFiles(Path path) throws IOException {
    String fullPath = path.toString();
    if (JQUERY_PAT.matcher(fullPath).find() || JASMINE_PAT.matcher(fullPath).find()) {
      if (!m_deletedFiles.contains(path)) {
        Files.delete(path);
        m_deletedFiles.add(path);
      }
    }
  }

  protected void processModelJsonToJsFile(PathInfo pathInfo) {
    if (!isModelJsonFile(pathInfo.getPath())) {
      return;
    }
    String jsonFileName = pathInfo.getPath().getFileName().toString();
    String jsFileName = jsonFileName.substring(0, jsonFileName.length() - JSON_EXTENSION.length() - 1) + JSON_MODEL_NAME_SUFFIX + '.' + JS_EXTENSION;
    Path newFileNameInSourceFolder = pathInfo.getPath().getParent().resolve(jsFileName);
    Assertions.assertFalse(Files.exists(newFileNameInSourceFolder),
        "The migration of file '{}' would be stored in '{}' but this file already exists in the source folder!", pathInfo.getPath(), newFileNameInSourceFolder);

    try {
      Files.move(pathInfo.getPath(), newFileNameInSourceFolder);
    }
    catch (IOException e) {
      throw new ProcessingException("Could not move model json to js file from '" + pathInfo.getPath() + "' to '" + newFileNameInSourceFolder + "'.", e);
    }
  }

  protected boolean isModelJsonFile(Path path) {
    if (FileUtility.hasExtension(path, "json")) {
      try {
        return FileUtility.findInFile(path, "objectType:");
      }
      catch (IOException e) {
        throw new ProcessingException("Could not parse file '' to find :objectType to determ if it is a model Json file or not.");
      }
    }
    return false;
  }

  protected void processLessModuleFile(Path path) throws IOException {
    String fileName = path.getFileName().toString();
    if (!fileName.endsWith("-module.less")) {
      return;
    }
    if (fileName.endsWith("login-module.less") || fileName.endsWith("logout-module.less")) {
      if (!m_deletedFiles.contains(path)) {
        Files.delete(path);
        m_deletedFiles.add(path);
      }
      return;
    }
    fileName = fileName.replace("-module.less", "-index.less");
    Files.move(path, path.getParent().resolve(fileName));
  }

  protected void processModuleJsFile(Path path) throws IOException {
    String fileName = path.getFileName().toString();
    if (!fileName.endsWith("-module.js")) {
      return;
    }
    String baseName = path.getFileName().toString();
    baseName = baseName.substring(0, baseName.length() - "-module.js".length());
    int firstDelim = baseName.indexOf('-');
    if (firstDelim > 0) {
      baseName = '-' + baseName.substring(firstDelim + 1);
    }
    else {
      baseName = "";
    }
    baseName = "index" + baseName + ".js";

    Path destPath = Configuration.get().getTargetModuleDirectory().resolve("src/main/js/" + baseName);
    if (Files.exists(destPath)) {
      throw new ProcessingException("File '" + destPath + "' already exists!");
    }
    Files.move(path, destPath);
  }

  protected void processSharedIndexFiles(PathInfo pathInfo) throws IOException {
    if (PathFilters.inSrcMainJs().test(pathInfo)
        && pathInfo.getPath().getFileName().toString().endsWith(".index")
        && pathInfo.getPath().toString().replace('\\', '/').contains("/_shared/")) {
      // delete
      if (!m_deletedFiles.contains(pathInfo.getPath())) {
        Files.delete(pathInfo.getPath());
        m_deletedFiles.add(pathInfo.getPath());
      }
    }
  }

  protected void deleteMacroAndModuleFiles(Path path) throws IOException {
    String fileName = path.getFileName().toString();
    if (fileName.endsWith("-module.json")
        || fileName.endsWith("-macro.json")
        || fileName.endsWith("-macro.less")
        || fileName.endsWith("-macro.js")) {
      if (!m_deletedFiles.contains(path)) {
        Files.delete(path);
        m_deletedFiles.add(path);
      }
    }
  }

  protected void deleteEmptyDirectories() {
    Path dir = Configuration.get().getTargetModuleDirectory();
    try {
      Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {
        @Override
        public FileVisitResult preVisitDirectory(Path d, BasicFileAttributes attrs) throws IOException {
          String name = d.getFileName().toString();
          if (name.startsWith(".")
              || name.equals("node_modules") ||
              name.equals("target")) {
            return FileVisitResult.SKIP_SUBTREE;
          }
          return super.preVisitDirectory(d, attrs);
        }

        @Override
        public FileVisitResult postVisitDirectory(Path d, IOException exc) throws IOException {
          //noinspection resource
          if (!Files.exists(d)) {
            return super.postVisitDirectory(d, exc);
          }
          try (Stream<Path> fileList = Files.list(d)) {
            if (fileList.count() < 1) {
              Files.delete(d);
            }
          }
          return super.postVisitDirectory(d, exc);
        }
      });
    }
    catch (IOException e) {
      throw new ProcessingException("Cannot delete empty directories in '{}'.", dir, e);
    }
  }
}
