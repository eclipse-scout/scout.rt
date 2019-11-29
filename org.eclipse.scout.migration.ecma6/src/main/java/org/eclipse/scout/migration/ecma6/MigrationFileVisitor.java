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
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.function.Consumer;

import org.eclipse.scout.migration.ecma6.configuration.Configuration;
import org.eclipse.scout.migration.ecma6.pathfilter.IMigrationExcludePathFilter;
import org.eclipse.scout.migration.ecma6.pathfilter.IMigrationIncludePathFilter;
import org.eclipse.scout.rt.platform.BEANS;

public class MigrationFileVisitor {

  public static void visitMigrationFiles(Consumer<PathInfo> migrationFileConsumer) throws IOException {
    IMigrationIncludePathFilter pathFilter = BEANS.opt(IMigrationIncludePathFilter.class);
    List<IMigrationExcludePathFilter> excludeFilters = BEANS.all(IMigrationExcludePathFilter.class);
    Files.walkFileTree(Configuration.get().getTargetModuleDirectory(), new SimpleFileVisitor<Path>() {
      @Override
      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
        if (!Files.exists(file)) {
          return FileVisitResult.CONTINUE;
        }
        PathInfo info = new PathInfo(file);
        if (pathFilter != null && !pathFilter.test(info.getPath())) {
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
        if (!Files.exists(dir)) {
          return FileVisitResult.SKIP_SUBTREE;
        }
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
}
