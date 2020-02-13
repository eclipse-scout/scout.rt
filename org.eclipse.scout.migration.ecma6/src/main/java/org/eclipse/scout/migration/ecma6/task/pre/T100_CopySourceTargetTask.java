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
package org.eclipse.scout.migration.ecma6.task.pre;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import org.eclipse.scout.migration.ecma6.configuration.Configuration;
import org.eclipse.scout.migration.ecma6.context.Context;
import org.eclipse.scout.migration.ecma6.task.T200_HtmlScriptTags;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.util.ObjectUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Order(100)
public class T100_CopySourceTargetTask implements IPreMigrationTask {
  private static final Logger LOG = LoggerFactory.getLogger(T200_HtmlScriptTags.class);

  @Override
  public void execute(Context context) {
    Path sourceModuleDirectory = Configuration.get().getSourceModuleDirectory();
    Path targetModuleDirectory = Configuration.get().getTargetModuleDirectory();
    if (ObjectUtility.notEquals(sourceModuleDirectory, targetModuleDirectory)) {
      try {
        copyDirectory(sourceModuleDirectory, targetModuleDirectory);
      }
      catch (IOException e) {
        throw new ProcessingException("Could not copy module from: '" + sourceModuleDirectory + "' to '" + targetModuleDirectory + "'.", e);
      }
    }
  }

  public static boolean copyDirectory(Path srcDir, Path targetDir) throws IOException {
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
        Path rel = srcDir.relativize(file);
        Path target = targetDir.resolve(rel);
        Files.copy(file, target);
        return FileVisitResult.CONTINUE;
      }
    });
    return true;
  }
}
