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

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.scout.migration.ecma6.configuration.Configuration;
import org.eclipse.scout.migration.ecma6.context.Context;
import org.eclipse.scout.migration.ecma6.task.ITask;
import org.eclipse.scout.migration.ecma6.task.post.IPostMigrationTask;
import org.eclipse.scout.migration.ecma6.task.pre.IPreMigrationTask;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Step1 is for file modifications. Do not move files in step 1. For move and delete ops step 2 is the right place
 */
public class MigrationStep1 implements Runnable {
  private static final Logger LOG = LoggerFactory.getLogger(MigrationStep1.class);

  private List<ITask> m_tasks;
  private Context m_context;

  public static void main(String[] args) {
    new MigrationStep1().run();
  }

  @Override
  public void run() {
    try {
      init();
      cleanTarget();
      List<IPreMigrationTask> preMigrationTasks = BEANS.all(IPreMigrationTask.class);
      LOG.info("Execute pre migration tasks (#: " + preMigrationTasks.size() + ")");
      preMigrationTasks.forEach(task -> task.execute(m_context));

      LOG.info("Execute migration tasks (#: " + m_tasks.size() + ")");
      visitFiles();
      writeFiles();
    }
    catch (IOException e) {
      LOG.error("Could not run Migration step 1.", e);
    }

    List<IPostMigrationTask> postMigrationTasks = BEANS.all(IPostMigrationTask.class);
    LOG.info("Execute post migration tasks (#: " + postMigrationTasks.size() + ")");
    postMigrationTasks.forEach(task -> task.execute(m_context));
    LOG.info("Migration step 1 completed.");
  }

  public void init() throws IOException {
    Files.createDirectories(BEANS.get(Configuration.class).getTargetModuleDirectory());
    m_context = new Context();
    m_context.setup();

    m_tasks = BEANS.all(ITask.class);
    m_tasks.forEach(task -> task.setup(m_context));
  }

  protected void visitFiles() throws IOException {
    AtomicInteger totalWork = new AtomicInteger();
    MigrationFileVisitor.visitMigrationFiles(info -> totalWork.incrementAndGet());

    AtomicInteger worked = new AtomicInteger();
    AtomicInteger pctReport = new AtomicInteger();
    MigrationFileVisitor.visitMigrationFiles(info -> {
      processFile(info, m_context);
      int pct = 100 * worked.incrementAndGet() / totalWork.get();
      if (pctReport.compareAndSet(pct - 1, pct)) {
        System.out.print("visited " + pct + "%   \r");
      }
    });
    System.out.println();
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

  protected void writeFiles() {
    createJsImports();
    applyManualFixes();
    writeDirtyWorkingCopies();
  }

  protected void createJsImports() {
    m_context.getWorkingCopies().stream()
        .filter(wc -> wc.getPath().toString().endsWith(".js"))
        .filter(wc -> m_context.getJsFile(wc) != null) // only working copies that have a parsed js file may add imports
        .forEach(wc -> MigrationUtility.insertImports(wc, m_context));
  }

  protected void applyManualFixes() {
    ManualFixes fixes = new ManualFixes();
    m_context.getWorkingCopies().forEach(fixes::apply);
  }

  protected void cleanTarget() {
    if (Configuration.get().cleanTargetBeforeWriteFiles()) {
      if (Configuration.get().getSourceModuleDirectory().equals(Configuration.get().getTargetModuleDirectory())) {
        LOG.info("Configuration 'cleanTargetBeforeWriteFiles' is ignored if source and target directory are same.");
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

  protected void writeDirtyWorkingCopies() {
    m_context.getWorkingCopies().stream().filter(WorkingCopy::isDirty).forEach(WorkingCopy::storeSource);
  }

}
