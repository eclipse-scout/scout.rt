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

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.job.IBlockingCondition;
import org.eclipse.scout.rt.client.job.IModelJobManager;
import org.eclipse.scout.rt.client.session.ClientSessionProvider;
import org.eclipse.scout.rt.client.ui.ClientUIPreferences;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.platform.OBJ;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.shared.services.common.prefs.IPreferences;

public class FileChooser implements IFileChooser {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(FileChooser.class);

  private static final String LAST_DIR_PREF_KEY = "current-dir";

  private File m_directory;
  private String m_fileName;
  private List<String> m_fileExtensions;
  private boolean m_folderMode;
  private boolean m_load;
  private boolean m_multiSelect;
  // result
  private List<File> m_files;
  private final IBlockingCondition m_blockingCondition;

  public FileChooser() {
    this(null, null, false);
  }

  public FileChooser(File directory, List<String> fileExtensions, boolean load) {
    m_directory = directory;
    m_fileExtensions = CollectionUtility.arrayListWithoutNullElements(fileExtensions);
    m_load = load;
    m_blockingCondition = OBJ.get(IModelJobManager.class).createBlockingCondition("block", false);
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
    IClientSession session = ClientSessionProvider.currentSession();
    if (session == null) {
      return null;
    }

    IPreferences clientPreferences = ClientUIPreferences.getClientPreferences(session);
    if (clientPreferences == null) {
      return null;
    }

    return clientPreferences.get(LAST_DIR_PREF_KEY, null);
  }

  public static void setCurrentDirectory(String dir) {
    IClientSession session = ClientSessionProvider.currentSession();
    if (session == null) {
      return;
    }

    IPreferences clientPreferences = ClientUIPreferences.getClientPreferences(session);
    if (clientPreferences == null) {
      return;
    }

    clientPreferences.put(LAST_DIR_PREF_KEY, dir);
    try {
      clientPreferences.flush();
    }
    catch (ProcessingException t) {
      LOG.error("Unable to flush preferences.", t);
    }
  }

  @Override
  public List<File> getFiles() {
    return CollectionUtility.arrayList(m_files);
  }

  @Override
  public void setFiles(List<File> files) {
    m_files = CollectionUtility.arrayListWithoutNullElements(files);
    m_blockingCondition.setBlocking(false);
  }

  @Override
  public List<File> startChooser() {
    m_files = null;
    m_blockingCondition.setBlocking(true);
    ClientSessionProvider.currentSession().getDesktop().addFileChooser(this);
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
    IDesktop desktop = ClientSessionProvider.currentSession().getDesktop();
    if (desktop == null || !desktop.isOpened()) {
      throw new ProcessingException("Cannot wait for " + getClass().getName() + ". There is no desktop or the desktop has not yet been opened in the ui");
    }
    try {
      m_blockingCondition.waitFor();
    }
    catch (ProcessingException e) {
      throw new ProcessingException(ScoutTexts.get("UserInterrupted"), e);
    }
  }
}
