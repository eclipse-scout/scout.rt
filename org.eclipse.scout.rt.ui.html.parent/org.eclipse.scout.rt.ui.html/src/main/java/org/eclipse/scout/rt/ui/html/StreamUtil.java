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
package org.eclipse.scout.rt.ui.html;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public final class StreamUtil {

  private StreamUtil() {
  }

  public static byte[] compressGZIP(byte[] b) throws IOException {
    ByteArrayOutputStream buf = new ByteArrayOutputStream();
    try (GZIPOutputStream out = new GZIPOutputStream(buf)) {
      out.write(b);
    }
    return buf.toByteArray();
  }

  public static byte[] uncompressGZIP(byte[] b) throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    try (BufferedInputStream in = new BufferedInputStream(new GZIPInputStream(new ByteArrayInputStream(b)))) {
      int val;
      while ((val = in.read()) >= 0) {
        out.write(val);
      }
    }
    return out.toByteArray();
  }

  public static byte[] readResource(URL url) throws IOException {
    URLConnection uc = url.openConnection();
    int len = uc.getContentLength();
    if (len >= 0) {
      try (InputStream in = uc.getInputStream()) {
        return readStream(in, len);
      }
    }
    else {
      try (BufferedInputStream in = new BufferedInputStream(uc.getInputStream())) {
        return readStream(in, -1);
      }
    }
  }

  public static byte[] readResource(File f) throws IOException {
    try (InputStream in = new FileInputStream(f)) {
      return readStream(in, (int) f.length());
    }
  }

  public static byte[] readStream(InputStream in, int len) throws IOException {
    if (len >= 0) {
      byte[] buf = new byte[len];
      int count = 0;
      while (count < len) {
        count += in.read(buf, count, len - count);
      }
      return buf;
    }
    else {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      int b;
      while ((b = in.read()) >= 0) {
        out.write(b);
      }
      return out.toByteArray();
    }
  }
}
