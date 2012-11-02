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
package org.eclipse.scout.commons.dnd;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @since Build 202
 */

public class FileListTransferObject extends TransferObject {
  private File[] m_files;

  public FileListTransferObject(File... files) {
    if (files == null) {
      m_files = new File[0];
    }
    else {
      m_files = files;
    }
  }

  public FileListTransferObject(Collection<File> files) {
    if (files == null) {
      m_files = new File[0];
    }
    else {
      m_files = files.toArray(new File[0]);
    }
  }

  @Override
  public boolean isFileList() {
    return true;
  }

  public File[] getFiles() {
    return m_files;
  }

  public List<File> getFileList() {
    return Arrays.asList(m_files);
  }

  public String[] getFilenames() {
    return getFilenameList();
  }

  public String[] getFilenameList() {
    String[] list = new String[m_files.length];
    for (int i = 0; i < m_files.length; i++) {
      list[i] = m_files[i].getAbsolutePath();
    }
    return list;
  }

  @Override
  public String toString() {
    return "FileListTransferObject[files=" + Arrays.toString(m_files) + "]";
  }

}
