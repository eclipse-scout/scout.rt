/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
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
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import org.jboss.jandex.Index;
import org.jboss.jandex.Indexer;

public final class JandexFolderIndexer {
  private static final String CLASS_EXT = ".class";

  private JandexFolderIndexer() {
  }

  public static Index createFolderIndex(Path folderPath, Indexer indexer) throws IOException {
    scanDirectory(folderPath, indexer);
    return indexer.complete();
  }

  static void scanDirectory(Path dir, final Indexer indexer) throws IOException {
    Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {
      @Override
      public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
        if (attrs.isDirectory() && path.toString().startsWith(".")) {
          return FileVisitResult.SKIP_SUBTREE;
        }
        if (!attrs.isDirectory() && path.toString().endsWith(CLASS_EXT)) {
          try (InputStream in = Files.newInputStream(path)) {
            indexer.index(in);
          }
        }
        return FileVisitResult.CONTINUE;
      }
    });
  }
}
