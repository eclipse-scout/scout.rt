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
package org.eclipse.scout.rt.spec.client;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.exception.ProcessingException;

/**
 *
 */
public class SpecIOUtility {
  public static final String ENCODING = "utf-8";

  public static String[] getRelativePaths(File[] files, File baseDir) {
    List<String> pathList = new ArrayList<String>();
    for (File f : files) {
      String relative = baseDir.toURI().relativize(f.toURI()).getPath();
      pathList.add(relative);
    }
    return CollectionUtility.toArray(pathList, String.class);
  }

  public static String[] addPrefix(String[] files, String pathPrefix) {
    String[] pathList = new String[files.length];
    for (int i = 0; i < files.length; i++) {
      pathList[i] = pathPrefix + files[i];
    }
    return pathList;
  }

  /**
   * @param out
   * @param id
   * @param fileExtension
   * @return
   * @throws ProcessingException
   */
  public static File createNewFile(File out, String id, String fileExtension) throws ProcessingException {
    out.mkdirs();
    File file = new File(out, id + fileExtension);
    try {
      if (file.exists()) {
        file.delete();
      }
      file.createNewFile();
      return file;
    }
    catch (IOException e) {
      throw new ProcessingException("Error creating file.", e);
    }
  }

  public static Writer createWriter(File out, String id, String fileExtension) throws ProcessingException {
    File file = createNewFile(out, id, fileExtension);
    return createWriter(file);
  }

  public static Writer createWriter(File file) throws ProcessingException {
    try {
      FileOutputStream outputStream = new FileOutputStream(file.getPath(), true);
      return new BufferedWriter(new OutputStreamWriter(outputStream, ENCODING));
    }
    catch (IOException e) {
      throw new ProcessingException("Error writing mediawiki file.", e);
    }
  }

  public static Properties loadProperties(File file) throws ProcessingException {
    Properties prop = new Properties();
    try {
      prop.load(new FileInputStream(file));
      return prop;
    }
    catch (FileNotFoundException e) {
      throw new ProcessingException("Error loading property file", e);
    }
    catch (IOException e) {
      throw new ProcessingException("Error loading property file", e);
    }
  }

}
