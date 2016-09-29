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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.jboss.jandex.Index;
import org.jboss.jandex.Indexer;

public final class JandexFolderIndexer {
  private static final String CLASS_EXT = ".class";

  private JandexFolderIndexer() {
  }

  public static Index createFolderIndex(File folder, Indexer indexer) throws IOException {
    if (folder.exists()) {
      scanDirectory(folder, indexer);
    }
    return indexer.complete();
  }

  static void scanDirectory(File folder, Indexer indexer) throws IOException {
    File[] files = folder.listFiles();
    if (files == null || files.length < 1) {
      return;
    }

    for (File f : files) {
      if (f.isDirectory()) {
        if (!f.getName().startsWith(".")) {
          scanDirectory(f, indexer);
        }
      }
      else if (f.isFile() && f.getName().endsWith(CLASS_EXT)) {
        URL url = f.toURI().toURL();
        try (InputStream in = url.openStream()) {
          indexer.index(in);
        }
      }
    }
  }
}
