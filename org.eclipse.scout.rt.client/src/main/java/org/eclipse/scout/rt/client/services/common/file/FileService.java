/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.services.common.file;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Locale;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.shared.services.common.file.IRemoteFileService;
import org.eclipse.scout.rt.shared.services.common.file.RemoteFile;

@SuppressWarnings("findbugs:RV_RETURN_VALUE_IGNORED_BAD_PRACTICE")
public class FileService implements IFileService {
  private String m_rootPath = null;

  @Override
  public File getLocalFile(String dir, String simpleName) {
    return getFileLocation(dir, simpleName, true);
  }

  @Override
  public File getRemoteFile(String dir, String simpleName) {
    return getRemoteFile(dir, simpleName, null);
  }

  @Override
  public File getRemoteFile(String dir, String simpleName, Locale locale) {
    return getRemoteFile(dir, simpleName, locale, true);
  }

  @Override
  public File getRemoteFile(String dir, String simpleName, Locale locale, boolean checkCache) {
    RemoteFile spec = null;
    File f = null;
    if (locale != null && simpleName != null && simpleName.lastIndexOf('.') != -1) {
      String filename = simpleName;
      String language = locale.toString().replaceAll("__", "_");
      String prefix = filename.substring(0, filename.lastIndexOf('.')) + "_";
      String suffix = filename.substring(filename.lastIndexOf('.'));
      filename = prefix + language + suffix;
      File test = getFileLocation(dir, filename, false);
      while (!test.exists()) {
        if (language.indexOf('_') == -1) {
          filename = simpleName;
          break;
        }
        language = language.substring(0, language.lastIndexOf('_'));
        filename = prefix + language + suffix;
        test = getFileLocation(dir, filename, false);
      }
      f = getFileLocation(dir, filename, false);
      spec = new RemoteFile(dir, filename, locale, 0L);
    }
    else {
      f = getFileLocation(dir, simpleName, false);
      spec = new RemoteFile(dir, simpleName, locale, 0L);
    }
    if (f.exists()) {
      spec.setLastModified(f.lastModified());
    }
    //
    if (checkCache) {
      IRemoteFileService svc = BEANS.get(IRemoteFileService.class);
      spec = svc.getRemoteFile(spec);
      try {
        if (spec.getName() != null && !spec.getName().equalsIgnoreCase(f.getName())) {
          if (locale != null && f.getName().length() > spec.getName().length()) {
            // if local file has longer name (including locale), this means that
            // this file was deleted on the server
            //noinspection ResultOfMethodCallIgnored
            f.delete();
          }
          f = getFileLocation(spec.getDirectory(), spec.getName(), false);
        }
        if (spec.exists() && spec.hasContent()) {
          try (OutputStream out = new FileOutputStream(f)) {
            spec.writeData(out);
          }
          //noinspection ResultOfMethodCallIgnored
          f.setLastModified(spec.getLastModified());
        }
        else if (!spec.exists()) {
          //noinspection ResultOfMethodCallIgnored
          f.delete();
        }
      }
      catch (IOException e) {
        throw new ProcessingException("error writing remote file in local store", e);
      }
    }
    return f;
  }

  /**
   * @since 21.10.2009
   */
  @Override
  public File getLocalFileLocation(String dir, String name) {
    return getFileLocation(dir, name, true);
  }

  /**
   * @since 21.10.2009
   */
  @Override
  public File getRemoteFileLocation(String dir, String name) {
    return getFileLocation(dir, name, false);
  }

  private File getFileLocation(String dir, String name, boolean local) {
    String path = m_rootPath;
    if (path == null) {
      path = System.getProperty("java.io.tmpdir");
      if (!path.endsWith("/")) {
        path = path + "/";
      }
      if (local) {
        path = path + "local";
      }
      else {
        path = path + "remote";
      }
    }
    if (dir != null) {
      dir = dir.replace("\\", "/");
      if (!dir.startsWith("/")) {
        path = path + "/";
      }
      path = path + dir;
    }
    if (!path.endsWith("/")) {
      path = path + "/";
    }
    File file = new File(path);
    if (!file.exists()) {
      //noinspection ResultOfMethodCallIgnored
      file.mkdirs();
    }
    if (name != null) {
      file = new File(path + name);
    }
    return file;
  }
}
