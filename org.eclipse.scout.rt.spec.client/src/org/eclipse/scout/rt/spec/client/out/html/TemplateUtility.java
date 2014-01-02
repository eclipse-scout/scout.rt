/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.spec.client.out.html;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.scout.rt.spec.client.internal.Activator;

/**
 *
 */
public final class TemplateUtility {
  private static final String DEFAULT_CSS = "resources/style/default.css";

  private TemplateUtility() {
  }

  public static void copyDefaultCss(File destFile) throws IOException {
    ReadableByteChannel source = null;
    FileChannel destination = null;
    FileOutputStream out = null;
    try {
      Path path = new Path(DEFAULT_CSS);
      InputStream stream = FileLocator.openStream(Activator.getDefault().getBundle(), path, true);
      source = Channels.newChannel(stream);
      out = new FileOutputStream(destFile);
      destination = out.getChannel();
      long maxBytes = 1000000l;
      destination.transferFrom(source, 0, maxBytes);
    }
    finally {
      if (source != null) {
        source.close();
      }
      if (out != null) {
        out.close();
      }
      if (destination != null) {
        destination.close();
      }
    }
  }

}
