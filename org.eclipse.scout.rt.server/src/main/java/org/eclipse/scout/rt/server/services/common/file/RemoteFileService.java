/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.services.common.file;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.util.ObjectUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.server.ServerConfigProperties.RemoteFilesRootDirProperty;
import org.eclipse.scout.rt.shared.services.common.file.IRemoteFileService;
import org.eclipse.scout.rt.shared.services.common.file.RemoteFile;
import org.eclipse.scout.rt.shared.servicetunnel.RemoteServiceAccessDenied;

public class RemoteFileService implements IRemoteFileService {

  private static final String LOCALE_DELIMITER = "_";
  private static final Pattern PATTERN_LOCALE_DELIMITER = Pattern.compile(LOCALE_DELIMITER);
  private static final int LOCALE_TEXT_MAX_LENGTH = 64;
  private static final Pattern LOCALE_SECURITY_PATTERN = Pattern.compile("[-a-z0-9_#]+", Pattern.CASE_INSENSITIVE);

  private String m_rootPath;

  public RemoteFileService() {
    this(CONFIG.getPropertyValue(RemoteFilesRootDirProperty.class));
  }

  protected RemoteFileService(String rootPath) {
    m_rootPath = rootPath;
    if (m_rootPath == null) {
      throw new SecurityException("Invalid path for file service: path may not be null.");
    }
  }

  public String getRootPath() {
    return m_rootPath;
  }

  protected void setRootPath(String rootPath) {
    m_rootPath = rootPath;
  }

  @Override
  public RemoteFile getRemoteFileHeader(RemoteFile spec) {
    return getRemoteFileInternal(spec, false, 0, -1);
  }

  @Override
  public RemoteFile getRemoteFile(RemoteFile spec) {
    return getRemoteFileInternal(spec, true, 0, -1);
  }

  public RemoteFile getRemoteFile(RemoteFile spec, long maxBlockSize) {
    return getRemoteFileInternal(spec, true, 0, maxBlockSize);
  }

  @Override
  public RemoteFile getRemoteFilePart(RemoteFile spec, long blockNumber) {
    return getRemoteFileInternal(spec, true, blockNumber * RemoteFile.DEFAULT_MAX_BLOCK_SIZE, RemoteFile.DEFAULT_MAX_BLOCK_SIZE);
  }

  private RemoteFile getRemoteFileInternal(RemoteFile spec, boolean includeContent, long startPosition, long maxBlockSize) {
    File file = getFileInternal(spec);
    RemoteFile result = new RemoteFile(spec.getDirectory(), file.getName(), spec.getLocale(), -1, spec.getCharsetName());
    result.setContentType(spec.getContentType());
    if (!StringUtility.hasText(result.getContentType())) {
      int pos = result.getName().lastIndexOf('.');
      String ext = "";
      if (pos >= 0) {
        ext = result.getName().substring(pos + 1);
      }
      result.setContentTypeByExtension(ext);
    }
    //
    if (file.exists()) {
      result.setExists(true);
      result.setLastModified(file.lastModified());
      long partLength = file.length();
      if (maxBlockSize > -1 && partLength > maxBlockSize) {
        partLength = partLength - startPosition;
        if (partLength > maxBlockSize) {
          partLength = maxBlockSize;
        }
        if (partLength <= 0) {
          partLength = 0;
        }
      }
      result.setContentLength((int) partLength);
      if (!includeContent) {
        // no content requested
      }
      else if (ObjectUtility.equals(spec.getName(), result.getName()) && result.getLastModified() <= spec.getLastModified() && result.getPartStartPosition() == spec.getPartStartPosition()) {
        // no content change, keep null
      }
      else {
        try (InputStream in = new FileInputStream(file)) {
          result.readData(in, startPosition, maxBlockSize);
        }
        catch (IOException e) {
          throw new ProcessingException("error reading file: " + file.getAbsolutePath(), e);
        }
      }
    }
    return result;
  }

  private String[][] getFiles(String folderBase, FilenameFilter filter) {
    File root = new File(getRootPath());
    File path = null;
    if (folderBase == null || folderBase.isEmpty()) {
      path = new File(getRootPath());
    }
    else {
      String tmp = folderBase;
      tmp = tmp.replaceAll("\\\\", "/");
      tmp = tmp.replaceAll("//", "/");
      path = new File(getRootPath(), tmp);
    }
    String canonicalRoot;
    String canonicalFolder;
    try {
      canonicalFolder = path.getCanonicalPath();
      canonicalRoot = root.getCanonicalPath();
    }
    catch (IOException e) {
      throw new ProcessingException("invalid path for file service for file: '" + folderBase + "'", e);
    }
    if (canonicalFolder == null || !canonicalFolder.startsWith(canonicalRoot)) {
      throw new SecurityException("invalid path for file service: path outside root-path");
    }

    List<String> dirList = new ArrayList<>();
    List<String> fileList = new ArrayList<>();
    String[] dir = path.list(filter);
    if (dir != null) {
      for (String aDir : dir) {
        try {
          File file = new File(path.getCanonicalPath() + "/" + aDir);
          if (!file.isHidden()) {
            if (file.exists() && file.isDirectory()) {
              String[][] tmp = getFiles((folderBase == null ? aDir : folderBase + "/" + aDir), filter);
              for (String[] f : tmp) {
                dirList.add(f[0]);
                fileList.add(f[1]);
              }
            }
            else {
              dirList.add(folderBase);
              fileList.add(aDir);
            }
          }
        }
        catch (IOException e) {
          throw new ProcessingException("FileService.getFiles:", e);
        }
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
  public RemoteFile[] getRemoteFiles(String folderPath, FilenameFilter filter, RemoteFile[] existingFileInfoOnClient) {
    return getRemoteFiles(folderPath, filter, existingFileInfoOnClient, StandardCharsets.UTF_8.name());
  }

  public RemoteFile[] getRemoteFiles(String folderPath, FilenameFilter filter, RemoteFile[] existingFileInfoOnClient, String charsetName) {
    return getRemoteFiles(folderPath, filter, existingFileInfoOnClient, charsetName, RemoteFile.DEFAULT_MAX_BLOCK_SIZE);
  }

  public RemoteFile[] getRemoteFiles(String folderPath, FilenameFilter filter, RemoteFile[] existingFileInfoOnClient, String charsetName, long maxBlockSize) {
    Map<String, RemoteFile> fileList = new HashMap<>();
    if (existingFileInfoOnClient != null) {
      for (RemoteFile rf : existingFileInfoOnClient) {
        fileList.put((rf.getDirectory().endsWith("/") ? rf.getDirectory() : rf.getDirectory() + "/") + rf.getName(), rf);
      }
    }
    String[][] files = getFiles(folderPath, filter);
    for (String[] file : files) {
      if (!fileList.containsKey((file[0].endsWith("/") ? file[0] : file[0] + "/") + file[1])) {
        fileList.put((file[0].endsWith("/") ? file[0] : file[0] + "/") + file[1], new RemoteFile(file[0], file[1], 0L, charsetName));
      }
    }
    RemoteFile[] retVal = new RemoteFile[fileList.size()];
    int i = 0;
    for (RemoteFile rf : fileList.values()) {
      retVal[i] = getRemoteFile(rf, maxBlockSize);
      i++;
    }
    return retVal;
  }

  @Override
  @RemoteServiceAccessDenied
  @SuppressWarnings("findbugs:RV_RETURN_VALUE_IGNORED_BAD_PRACTICE")
  public void putRemoteFile(RemoteFile spec) {
    File file = getFileInternal(spec);
    file.getParentFile().mkdirs();
    try (FileOutputStream out = new FileOutputStream(file)) {
      spec.writeData(out);
      file.setLastModified(file.lastModified());
    }
    catch (Exception e) {
      throw new ProcessingException("error writing file: " + file.getAbsoluteFile(), e);
    }
  }

  protected File getFileInternal(RemoteFile spec) {
    File root = new File(getRootPath());
    File folder = null;
    if (spec.getDirectory() == null || spec.getDirectory().isEmpty()) {
      folder = new File(getRootPath());
    }
    else {
      String tmp = spec.getDirectory();
      tmp = tmp.replaceAll("\\\\", "/");
      tmp = tmp.replaceAll("//", "/");
      folder = new File(getRootPath(), tmp);
    }
    String canonicalRoot;
    String canonicalFolder;
    String canonicalSimpleName;
    try {
      canonicalRoot = root.getCanonicalPath();
      canonicalFolder = folder.getCanonicalPath();
      canonicalSimpleName = new File(canonicalFolder, spec.getName()).getName();
    }
    catch (IOException e) {
      throw new ProcessingException("invalid or unaccessible path", e);
    }
    if (canonicalFolder == null || !canonicalFolder.startsWith(canonicalRoot)) {
      throw new SecurityException("invalid or unaccessible path");
    }
    // if the remote file is requested from the RemoteFileServlet, spec.getName() will start with an "/"
    if (canonicalSimpleName == null || !canonicalSimpleName.equals(spec.getName().startsWith("/") ? spec.getName().substring(1) : spec.getName())) {
      throw new SecurityException("invalid or unaccessible path");
    }
    String filename = tryAddLocaleToFileName(canonicalSimpleName, spec.getLocale(), canonicalFolder, file -> file.exists());
    File file = new File(canonicalFolder, filename);
    return file;
  }

  protected String tryAddLocaleToFileName(String filename, Locale locale, String canonicalFolder, Predicate<File> fileExistsPredicate) {
    int suffixSeparatorIndex = filename.lastIndexOf('.');
    if (locale == null || suffixSeparatorIndex == -1) {
      return filename;
    }
    String localeText = locale.toString().replaceAll(LOCALE_DELIMITER + LOCALE_DELIMITER, LOCALE_DELIMITER);
    if (localeText.length() > LOCALE_TEXT_MAX_LENGTH) {
      return filename;
    }
    // make sure only valid locale characters are accepted.
    if (!LOCALE_SECURITY_PATTERN.matcher(localeText).matches()) {
      throw new SecurityException("invalid or inaccessible path");
    }

    String[] checkedLocaleParts = PATTERN_LOCALE_DELIMITER.splitAsStream(localeText)
        .limit(3)
        .map(s -> s.toLowerCase(Locale.ROOT))
        .map(s -> LOCALE_DELIMITER + s)
        .toArray(String[]::new);
    String prefix = filename.substring(0, suffixSeparatorIndex);
    String suffix = filename.substring(suffixSeparatorIndex);
    // Remove locale parts from the filename if and only if they are an exact match.
    for (int i = checkedLocaleParts.length - 1; i >= 0; i--) {
      if (prefix.toLowerCase(Locale.ROOT).endsWith(checkedLocaleParts[i])) {
        prefix = prefix.substring(0, prefix.length() - checkedLocaleParts[i].length());
      }
    }
    if (!prefix.endsWith(LOCALE_DELIMITER)) {
      prefix = prefix + LOCALE_DELIMITER;
    }
    // Check if a file containing the given locale name or parts of it exists.
    // Otherwise, return the original filename
    String extendedFilename = prefix + localeText + suffix;
    File test = new File(canonicalFolder, extendedFilename);
    while (!fileExistsPredicate.test(test)) {
      if (localeText.indexOf(LOCALE_DELIMITER) == -1) {
        extendedFilename = filename;
        break;
      }
      localeText = localeText.substring(0, localeText.lastIndexOf(LOCALE_DELIMITER));
      extendedFilename = prefix + localeText + suffix;
      test = new File(canonicalFolder, extendedFilename);
    }
    return extendedFilename;
  }

  @Override
  public void streamRemoteFile(RemoteFile spec, OutputStream out) {
    File file = getFileInternal(spec);
    if (!file.exists()) {
      throw new ProcessingException("remote file does not exist: " + spec.getPath());
    }
    int len = (int) file.length();
    byte[] buf = new byte[Math.min(102400, len)];
    int written = 0;
    int delta = 0;
    try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(file))) {
      while (written < len) {
        delta = in.read(buf);
        out.write(buf, 0, delta);
        written += delta;
      }
    }
    catch (IOException e) {
      throw new ProcessingException("error streaming file: " + file.getAbsolutePath(), e);
    }
  }
}
