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
package org.eclipse.scout.migration.ecma6.task.post;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import org.eclipse.scout.migration.ecma6.configuration.Configuration;
import org.eclipse.scout.migration.ecma6.context.Context;
import org.eclipse.scout.rt.platform.exception.ProcessingException;

public class T99999_DeleteEmptyDirectories implements IPostMigrationTask {

  @Override
  public void execute(Context context) {
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
          if (Files.list(d).count() < 1) {
            Files.delete(d);
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
