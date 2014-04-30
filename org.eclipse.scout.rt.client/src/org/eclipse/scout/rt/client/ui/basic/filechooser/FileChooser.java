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
package org.eclipse.scout.rt.client.ui.basic.filechooser;

import java.io.File;
import java.util.List;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.commons.prefs.UserScope;
import org.eclipse.scout.rt.client.BlockingCondition;
import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.osgi.service.prefs.BackingStoreException;

public class FileChooser implements IFileChooser {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(FileChooser.class);

  private File m_directory;
  private String m_fileName;
  private List<String> m_fileExtensions;
  private boolean m_folderMode;
  private boolean m_load;
  private boolean m_multiSelect;
  // result
  private List<File> m_files;
  private final BlockingCondition m_blockingCondition = new BlockingCondition(false);

  public FileChooser() {
  }

  public FileChooser(File directory, List<String> fileExtensions, boolean load) {
    m_directory = directory;
    m_fileExtensions = CollectionUtility.arrayListWithoutNullElements(fileExtensions);
    m_load = load;
  }

  @Override
  public File getDirectory() {
    return m_directory;
  }

  public void setDirectory(File directory) {
    this.m_directory = directory;
  }

  @Override
  public List<String> getFileExtensions() {
    return CollectionUtility.arrayList(m_fileExtensions);
  }

  public void setFileExtensions(List<String> fileExtensions) {
    this.m_fileExtensions = CollectionUtility.arrayListWithoutNullElements(fileExtensions);
  }

  @Override
  public String getFileName() {
    return m_fileName;
  }

  public void setFileName(String fileName) {
    this.m_fileName = fileName;
  }

  @Override
  public boolean isFolderMode() {
    return m_folderMode;
  }

  public void setFolderMode(boolean folderMode) {
    this.m_folderMode = folderMode;
  }

  @Override
  public boolean isTypeLoad() {
    return m_load;
  }

  public void setTypeLoad(boolean load) {
    this.m_load = load;
  }

  @Override
  public boolean isMultiSelect() {
    return m_multiSelect;
  }

  public void setMultiSelect(boolean multiSelect) {
    this.m_multiSelect = multiSelect;
  }

  public static String getCurrentDirectory() {
    IClientSession session = ClientSyncJob.getCurrentSession();
    if (session == null) {
      return null;
    }
    String id = session.getBundle().getSymbolicName() + "-" + session.getUserId();
    IEclipsePreferences props = new UserScope().getNode(id);
    return props.get("current-dir", null);
  }

  public static void setCurrentDirectory(String dir) {
    IClientSession session = ClientSyncJob.getCurrentSession();
    if (session == null) {
      return;
    }
    String id = session.getBundle().getSymbolicName() + "-" + session.getUserId();
    IEclipsePreferences props = new UserScope().getNode(id);
    props.put("current-dir", dir);
    try {
      props.flush();
    }
    catch (BackingStoreException e) {
      //nop
    }
  }

  @Override
  public List<File> getFiles() {
    return CollectionUtility.arrayList(m_files);
  }

  @Override
  public void setFiles(List<File> files) {
    m_files = CollectionUtility.arrayListWithoutNullElements(files);
    m_blockingCondition.release();
  }

  @Override
  public List<File> startChooser() {
    m_files = null;
    m_blockingCondition.setBlocking(true);
    ClientSyncJob.getCurrentSession().getDesktop().addFileChooser(this);
    try {
      waitFor();
    }
    catch (ProcessingException e) {
      LOG.error(null, e);
    }
    return getFiles();
  }

  private void waitFor() throws ProcessingException {
    // check if the desktop is observing this process
    IDesktop desktop = ClientSyncJob.getCurrentSession().getDesktop();
    if (desktop == null || !desktop.isOpened()) {
      throw new ProcessingException("Cannot wait for " + getClass().getName() + ". There is no desktop or the desktop has not yet been opened in the ui");
    }
    try {
      m_blockingCondition.waitFor();
    }
    catch (InterruptedException e) {
      throw new ProcessingException(ScoutTexts.get("UserInterrupted"), e);
    }
  }
}
