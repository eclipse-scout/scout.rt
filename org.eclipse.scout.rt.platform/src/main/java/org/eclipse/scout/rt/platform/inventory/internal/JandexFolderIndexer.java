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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class JandexFolderIndexer {
  private static final Logger LOG = LoggerFactory.getLogger(JandexFolderIndexer.class);
  private static final String CLASS_EXT = ".class";

  private JandexFolderIndexer() {
  }

  public static Index createFolderIndex(File folder, Indexer indexer) {
    if (folder.exists()) {
      scanDirectory(folder, indexer);
    }
    return indexer.complete();
  }

  protected static void scanDirectory(File folder, Indexer indexer) {
    for (File f : folder.listFiles()) {
      if (f.isDirectory()) {
        if (!f.getName().startsWith(".")) {
          scanDirectory(f, indexer);
        }
      }
      else if (f.isFile()) {
        if (f.getName().endsWith(CLASS_EXT)) {
          try {
            URL url = f.toURI().toURL();
            try (InputStream in = url.openStream()) {
              indexer.index(in);
            }
          }
          catch (IOException ex) {
            LOG.error("indexing class: " + f, ex);
          }
        }
      }
    }
  }

}
