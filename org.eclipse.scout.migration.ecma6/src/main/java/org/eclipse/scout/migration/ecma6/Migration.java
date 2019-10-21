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

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.eclipse.scout.migration.ecma6.context.Context;
import org.eclipse.scout.migration.ecma6.pathfilter.IMigrationExcludePathFilter;
import org.eclipse.scout.migration.ecma6.pathfilter.IMigrationIncludePathFilter;
import org.eclipse.scout.migration.ecma6.task.ITask;
import org.eclipse.scout.migration.ecma6.task.post.IPostMigrationTask;
import org.eclipse.scout.migration.ecma6.task.pre.IPreMigrationTask;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Migration {
  private static final Logger LOG = LoggerFactory.getLogger(Migration.class);

  private List<ITask> m_tasks;
  private Context m_context;

  public static void main(String[] args) {
    try {
      Migration migration = new Migration();
      migration.init();
      migration.run();
    }
    catch (IOException e) {
      throw new ProcessingException("Error in migration", e);
    }
  }

  protected Migration() {
  }

  public void init() throws IOException {
    Files.createDirectories(BEANS.get(Configuration.class).getTargetModuleDirectory());
    m_context = new Context();
    m_context.setup();

    m_tasks = BEANS.all(ITask.class);
    m_tasks.forEach(task -> task.setup(m_context));
  }

  public void run() throws IOException {
    List<IPreMigrationTask> preMigrationTasks = BEANS.all(IPreMigrationTask.class);
    LOG.info("Execute pre migration tasks (#: " + preMigrationTasks.size() + ")");
    preMigrationTasks.forEach(task -> task.execute(m_context));

    LOG.info("Execute migration tasks (#: " + m_tasks.size() + ")");
    visitFiles();
    writeFiles();

    List<IPostMigrationTask> postMigrationTasks = BEANS.all(IPostMigrationTask.class);
    LOG.info("Execute post migration tasks (#: " + postMigrationTasks.size() + ")");
    postMigrationTasks.forEach(task -> task.execute(m_context));
  }

  private void visitFiles() throws IOException {
    AtomicInteger totalWork = new AtomicInteger();
    visitMigrationFiles(info -> totalWork.incrementAndGet());

    AtomicInteger worked = new AtomicInteger();
    AtomicInteger pctReport = new AtomicInteger();
    visitMigrationFiles(info -> {
      processFile(info, m_context);
      int pct = 100 * worked.incrementAndGet() / totalWork.get();
      if (pctReport.compareAndSet(pct - 1, pct)) {
        System.out.print("visited " + pct + "%   \r");
      }
    });
    System.out.println();
  }

  protected void visitMigrationFiles(Consumer<PathInfo> migrationFileConsumer) throws IOException {
    IMigrationIncludePathFilter pathFilter = BEANS.opt(IMigrationIncludePathFilter.class);
    List<IMigrationExcludePathFilter> excludeFilters = BEANS.all(IMigrationExcludePathFilter.class);

    Files.walkFileTree(Configuration.get().getSourceModuleDirectory(), new SimpleFileVisitor<Path>() {
      @Override
      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
        PathInfo info = new PathInfo(file);
        if (pathFilter != null && !pathFilter.test(info)) {
          return FileVisitResult.CONTINUE;
        }
        if (excludeFilters.stream().anyMatch(filter -> filter.test(info))) {
          return FileVisitResult.CONTINUE;
        }

        migrationFileConsumer.accept(info);
        return FileVisitResult.CONTINUE;
      }

      @Override
      public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
        String dirName = dir.getFileName().toString();
        if ("target".equalsIgnoreCase(dirName)) {
          return FileVisitResult.SKIP_SUBTREE;
        }
        if (".git".equals(dirName)) {
          return FileVisitResult.SKIP_SUBTREE;
        }
        if (dir.endsWith(Paths.get("src/main/js/jquery"))) {
          return FileVisitResult.SKIP_SUBTREE;
        }
        if ("node_modules".equals(dirName)) {
          return FileVisitResult.SKIP_SUBTREE;
        }

        return FileVisitResult.CONTINUE;
      }
    });
  }

  private void processFile(PathInfo pathInfo, Context context) {
    m_tasks
        .stream()
        .filter(task -> task.accept(pathInfo, context))
        .forEach(task -> processFileWithTask(task, pathInfo, context));
  }

  private void processFileWithTask(ITask task, PathInfo pathInfo, Context context) {
    task.process(pathInfo, context);
  }

  protected void createJsImports() {
    m_context.getWorkingCopies().stream()
        .filter(wc -> wc.getPath().toString().endsWith(".js"))
        .forEach(wc -> MigrationUtility.insertImports(wc, m_context));
  }

  protected void applyManualFixes() {
    ManualFixes fixes = new ManualFixes();
    m_context.getWorkingCopies().stream().forEach(fixes::apply);
  }

  protected void writeFiles() throws IOException {
    createJsImports();
    applyManualFixes();
    cleanTarget();

    if (Configuration.get().getSourceModuleDirectory().equals(Configuration.get().getTargetModuleDirectory())) {
      // write dirty working copies first
      writeDirtyWorkingCopies();
      // wait for user input (used to keep git history, commit in
      if (!MigrationUtility.waitForUserConfirmation()) {
        return;
      }
    }

    final Path targetModule = Configuration.get().getTargetModuleDirectory();
    final Path sourceModule = Configuration.get().getSourceModuleDirectory();

    Set<WorkingCopy> allWorkingCopies = new HashSet<>(m_context.getWorkingCopies());
    Map<Path, Path> skipCopyRelativePaths = m_context.getWorkingCopies()
        .stream()
        .filter(w -> w.getRelativeTargetPath() != null)
        .collect(Collectors.toMap(WorkingCopy::getRelativeTargetPath, WorkingCopy::getPath));
    visitMigrationFiles(info -> {
      Path file = info.getPath();
      WorkingCopy workingCopy = m_context.getWorkingCopy(file);
      if (workingCopy != null) {
        allWorkingCopies.remove(workingCopy);
        writeWorkingCopy(workingCopy, skipCopyRelativePaths);
      }
      else {
        // file has not been changed or touched -> copy
        Path rel = sourceModule.relativize(file);
        if (skipCopyRelativePaths.containsKey(rel)) {
          LOG.info("Skip copying {}, the file is already written by the working copy {}", rel, skipCopyRelativePaths.get(rel));
        }
        else {
          try {
            Path dest = targetModule.resolve(rel);
            Files.createDirectories(dest.getParent());
            Files.copy(file, dest, REPLACE_EXISTING);
          }
          catch (IOException e) {
            throw new ProcessingException("Unable to copy file '{}'.", file, e);
          }
        }
      }
    });

    // writes the remaining working copies which have no corresponding source file (working copies newly created during the migration).
    allWorkingCopies.forEach(w -> writeWorkingCopy(w, skipCopyRelativePaths));
  }

  protected void writeDirtyWorkingCopies() {
    m_context.getWorkingCopies().stream().filter(WorkingCopy::isDirty).forEach(WorkingCopy::storeSource);
  }

  protected void cleanTarget() {
    if (Configuration.get().cleanTargetBeforeWriteFiles()) {
      if (Configuration.get().getSourceModuleDirectory().equals(Configuration.get().getTargetModuleDirectory())) {
        LOG.warn("Configuration 'cleanTargetBeforeWriteFiles' is ignored if source and target directory are same.");
        return;
      }
      try {
        FileUtility.deleteDirectory(Configuration.get().getTargetModuleDirectory());
      }
      catch (IOException e) {
        throw new ProcessingException("Could not delete target directory!", e);
      }
    }

  }

  private void writeWorkingCopy(WorkingCopy workingCopy, Map<Path, Path> skipCopyRelativePaths) {
    final Path sourceModule = Configuration.get().getSourceModuleDirectory();
    final Path targetModule = Configuration.get().getTargetModuleDirectory();
    final Path relativeSourcePath = sourceModule.relativize(workingCopy.getPath());
    final Path relativeTargetPath;
    try {
      if (workingCopy.getRelativeTargetPath() != null) {
        relativeTargetPath = workingCopy.getRelativeTargetPath();
      }
      else {
        relativeTargetPath = relativeSourcePath;
        if (skipCopyRelativePaths.containsKey(relativeSourcePath)) {
          LOG.info("Skip copying {}, the file is already written by the working copy {}", relativeSourcePath, skipCopyRelativePaths.get(relativeSourcePath));
          return;
        }
      }
      workingCopy.persist(targetModule.resolve(relativeTargetPath));
      if (sourceModule.equals(targetModule) && relativeTargetPath != relativeSourcePath) {
        Files.delete(workingCopy.getPath());
      }
    }
    catch (IOException e) {
      throw new ProcessingException("Cannot write working copies", e);
    }
  }
}
