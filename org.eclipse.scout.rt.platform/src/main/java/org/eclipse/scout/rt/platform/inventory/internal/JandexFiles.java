/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.inventory.internal;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public final class JandexFiles {
  private static final String CLASS_EXT = ".class";

  private JandexFiles() {
  }

  public static void walkFileTree(Path dir, final IJandexFileVisitor visitor) throws IOException {
    Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {
      @Override
      public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
        if (attrs.isDirectory() && path.toString().startsWith(".")) {
          return FileVisitResult.SKIP_SUBTREE;
        }
        if (!attrs.isDirectory() && path.toString().endsWith(CLASS_EXT)) {
          visitor.visit(path, attrs);
        }
        return FileVisitResult.CONTINUE;
      }
    });
  }
}
