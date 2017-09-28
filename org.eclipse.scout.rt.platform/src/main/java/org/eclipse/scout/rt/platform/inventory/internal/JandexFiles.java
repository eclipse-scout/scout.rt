/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
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
