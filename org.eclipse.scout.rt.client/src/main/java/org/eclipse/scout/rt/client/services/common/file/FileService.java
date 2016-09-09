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
package org.eclipse.scout.rt.client.services.common.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Locale;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.shared.services.common.file.IRemoteFileService;
import org.eclipse.scout.rt.shared.services.common.file.RemoteFile;

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
            f.delete();
          }
          f = getFileLocation(spec.getDirectory(), spec.getName(), false);
        }
        if (spec.exists() && spec.hasContent()) {
          try (OutputStream out = new FileOutputStream(f)) {
            spec.writeData(out);
          }
          f.setLastModified(spec.getLastModified());
        }
        else if (!spec.exists()) {
          f.delete();
        }
      }
      catch (IOException e) {
        throw new ProcessingException("error writing remote file in local store", e);
      }
    }
    return f;
  }

  private String[][] getFiles(String folderBase, FilenameFilter filter, boolean useServerFolderStructureOnClient) {
    File path = getFileLocation(useServerFolderStructureOnClient ? folderBase : "", null, false);
    ArrayList<String> dirList = new ArrayList<String>();
    ArrayList<String> fileList = new ArrayList<String>();
    String[] dir = path.list(filter);
    for (int i = 0; i < dir.length; i++) {
      try {
        File file = new File(path.getCanonicalPath() + "/" + dir[i]);
        if (file.exists() && file.isDirectory()) {
          String[][] tmp = getFiles((folderBase == null ? dir[i] : folderBase + "/" + dir[i]), filter, true);
          for (String[] f : tmp) {
            dirList.add(f[0]);
            fileList.add(f[1]);
          }
        }
        else {
          dirList.add(folderBase);
          fileList.add(dir[i]);
        }
      }
      catch (IOException e) {
        throw new ProcessingException("FileService.getFiles:", e);
      }
    }
    String[][] retVal = new String[dirList.size()][2];
    for (int i = 0; i < dirList.size(); i++) {
      retVal[i][0] = dirList.get(i);
      retVal[i][1] = fileList.get(i);
    }
    return retVal;
  }

  @Override
  public void syncRemoteFilesToPath(String clientFolderPath, String serverFolderPath, FilenameFilter filter) {
    setDirectPath(clientFolderPath);
    syncRemoteFilesInternal(serverFolderPath, filter, false);
    setDirectPath(null);
  }

  @Override
  public void syncRemoteFiles(String serverFolderPath, FilenameFilter filter) {
    syncRemoteFilesInternal(serverFolderPath, filter, true);
  }

  private void syncRemoteFilesInternal(String serverFolderPath, FilenameFilter filter, boolean useServerFolderStructureOnClient) {
    IRemoteFileService svc = BEANS.get(IRemoteFileService.class);
    String[][] realFiles = getFiles(serverFolderPath, filter, useServerFolderStructureOnClient);
    RemoteFile[] existingFileInfoOnClient = new RemoteFile[realFiles.length];
    for (int i = 0; i < realFiles.length; i++) {
      RemoteFile rf = new RemoteFile(realFiles[i][0], realFiles[i][1], 0);
      String dir = m_rootPath == null ? realFiles[i][0] : "";
      File f = getFileLocation(dir, realFiles[i][1], false);
      if (f.exists()) {
        rf.setLastModified(f.lastModified());
      }
      existingFileInfoOnClient[i] = rf;
    }
    existingFileInfoOnClient = svc.getRemoteFiles(serverFolderPath, filter, existingFileInfoOnClient);
    for (RemoteFile spec : existingFileInfoOnClient) {
      String fileDirectory = useServerFolderStructureOnClient ? spec.getDirectory() : null;
      File f = getFileLocation(fileDirectory, spec.getName(), false);
      if (spec.exists() && spec.hasContent()) {
        try {
          if (spec.hasMoreParts()) {
            // file is splitted - get all parts
            int counter = 0;
            long fileDate = spec.getLastModified();
            File part = getFileLocation(fileDirectory, spec.getName() + "." + counter, false);
            try (OutputStream out = new FileOutputStream(part)) {
              spec.writeData(out);
            }
            part.setLastModified(fileDate);
            RemoteFile specPart = spec;
            while (specPart.hasMoreParts()) {
              counter++;
              part = getFileLocation(fileDirectory, spec.getName() + "." + counter, false);
              if (!part.exists() || fileDate != part.lastModified()) {
                specPart = svc.getRemoteFilePart(spec, counter);
                try (OutputStream out = new FileOutputStream(part)) {
                  specPart.writeData(out);
                }
                part.setLastModified(fileDate);
              }
              else {
                // resuming canceled part: nothing to do
              }
            }
            // put together
            counter = 0;
            f = getFileLocation(fileDirectory, spec.getName(), false);
            try (OutputStream out = new FileOutputStream(f)) {
              part = getFileLocation(fileDirectory, spec.getName() + "." + counter, false);
              while (part.exists()) {
                try (InputStream in = new FileInputStream(part)) {
                  byte[] buf = new byte[102400];
                  int len;
                  while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                  }
                  out.flush();
                }
                part.delete();
                counter++;
                part = getFileLocation(fileDirectory, spec.getName() + "." + counter, false);
              }
            }
            f.setLastModified(fileDate);
          }
          else {
            // normal files
            try (OutputStream out = new FileOutputStream(f)) {
              spec.writeData(out);
            }
            f.setLastModified(spec.getLastModified());
          }
        }
        catch (IOException e) {
          throw new ProcessingException("error writing remote file in local store", e);
        }
      }
      else if (!spec.exists()) {
        f.delete();
      }
    }
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
      file.mkdirs();
    }
    if (name != null) {
      file = new File(path + name);
    }
    return file;
  }

  private void setDirectPath(String rootPath) {
    m_rootPath = rootPath;
  }

}
