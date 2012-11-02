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

import org.eclipse.core.runtime.Platform;
import org.eclipse.scout.commons.annotations.Priority;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.shared.OfflineState;
import org.eclipse.scout.rt.shared.services.common.file.IRemoteFileService;
import org.eclipse.scout.rt.shared.services.common.file.RemoteFile;
import org.eclipse.scout.service.AbstractService;
import org.eclipse.scout.service.SERVICES;
import org.osgi.framework.Bundle;

@Priority(-1)
public class FileService extends AbstractService implements IFileService {
  private String m_rootPath = null;

  private Bundle getBundle() {
    return ClientSyncJob.getCurrentSession().getBundle();
  }

  @Override
  public File getLocalFile(String dir, String simpleName) throws ProcessingException {
    return getFileLocation(dir, simpleName, true);
  }

  @Override
  public File getRemoteFile(String dir, String simpleName) throws ProcessingException {
    return getRemoteFile(dir, simpleName, null);
  }

  @Override
  public File getRemoteFile(String dir, String simpleName, Locale locale) throws ProcessingException {
    return getRemoteFile(dir, simpleName, locale, true);
  }

  @Override
  public File getRemoteFile(String dir, String simpleName, Locale locale, boolean checkCache) throws ProcessingException {
    RemoteFile spec = null;
    File f = null;
    if (locale != null && simpleName != null && simpleName.lastIndexOf(".") != -1) {
      String filename = simpleName;
      String language = locale.toString().replaceAll("__", "_");
      String prefix = filename.substring(0, filename.lastIndexOf(".")) + "_";
      String suffix = filename.substring(filename.lastIndexOf("."));
      filename = prefix + language + suffix;
      File test = getFileLocation(dir, filename, false);
      while (!test.exists()) {
        if (language.indexOf("_") == -1) {
          filename = simpleName;
          break;
        }
        language = language.substring(0, language.lastIndexOf("_"));
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
    if (checkCache && OfflineState.isOnlineInCurrentThread()) {
      IRemoteFileService svc = SERVICES.getService(IRemoteFileService.class);
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
          spec.writeData(new FileOutputStream(f));
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

  private String[][] getFiles(String folderBase, FilenameFilter filter, boolean useServerFolderStructureOnClient) throws ProcessingException {
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
  public void syncRemoteFilesToPath(String clientFolderPath, String serverFolderPath, FilenameFilter filter) throws ProcessingException {
    setDirectPath(clientFolderPath);
    syncRemoteFilesInternal(serverFolderPath, filter, false);
    setDirectPath(null);
  }

  @Override
  public void syncRemoteFiles(String serverFolderPath, FilenameFilter filter) throws ProcessingException {
    syncRemoteFilesInternal(serverFolderPath, filter, true);
  }

  private void syncRemoteFilesInternal(String serverFolderPath, FilenameFilter filter, boolean useServerFolderStructureOnClient) throws ProcessingException {
    IRemoteFileService svc = SERVICES.getService(IRemoteFileService.class);
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
            spec.writeData(new FileOutputStream(part));
            part.setLastModified(fileDate);
            RemoteFile specPart = spec;
            while (specPart.hasMoreParts()) {
              counter++;
              part = getFileLocation(fileDirectory, spec.getName() + "." + counter, false);
              if (!part.exists() || fileDate != part.lastModified()) {
                specPart = svc.getRemoteFilePart(spec, counter);
                specPart.writeData(new FileOutputStream(part));
                part.setLastModified(fileDate);
              }
              else {
                // resuming canceled part: nothing to do
              }
            }
            // put together
            counter = 0;
            f = getFileLocation(fileDirectory, spec.getName(), false);
            OutputStream out = new FileOutputStream(f);
            part = getFileLocation(fileDirectory, spec.getName() + "." + counter, false);
            while (part.exists()) {
              InputStream in = new FileInputStream(part);
              byte[] buf = new byte[102400];
              int len;
              while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
              }
              out.flush();
              in.close();
              part.delete();
              counter++;
              part = getFileLocation(fileDirectory, spec.getName() + "." + counter, false);
            }
            out.close();
            f.setLastModified(fileDate);
          }
          else {
            // normal files
            spec.writeData(new FileOutputStream(f));
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
  public File getLocalFileLocation(String dir, String name) throws ProcessingException {
    return getFileLocation(dir, name, true);
  }

  /**
   * @since 21.10.2009
   */
  @Override
  public File getRemoteFileLocation(String dir, String name) throws ProcessingException {
    return getFileLocation(dir, name, false);
  }

  private File getFileLocation(String dir, String name, boolean local) throws ProcessingException {
    try {
      String path = m_rootPath;
      if (path == null) {
        path = Platform.getStateLocation(getBundle()).toFile().getCanonicalPath();
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
    catch (IOException e) {
      throw new ProcessingException("io error getting file", e);
    }
  }

  private void setDirectPath(String rootPath) {
    m_rootPath = rootPath;
  }

}
