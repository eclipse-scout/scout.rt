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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.scout.commons.CollectionUtility;

/**
 * @since Build 202
 */

public class FileListTransferObject extends TransferObject {
  private List<File> m_files;

  public FileListTransferObject(File... files) {
    this(CollectionUtility.<File> arrayList(files));
  }

  public FileListTransferObject(Collection<? extends File> files) {
    m_files = CollectionUtility.<File> arrayListWithoutNullElements(files);
  }

  @Override
  public boolean isFileList() {
    return true;
  }

  public List<File> getFiles() {
    return CollectionUtility.unmodifiableList(m_files);
  }

  public List<String> getFilenames() {
    return getFilenameList();
  }

  public List<String> getFilenameList() {
    List<String> fileNames = new ArrayList<String>();
    if (m_files != null) {
      for (File f : m_files) {
        fileNames.add(f.getAbsolutePath());
      }
    }
    return fileNames;
  }

  @Override
  public String toString() {
    return "FileListTransferObject[files=" + CollectionUtility.format(m_files) + "]";
  }

}
