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

import java.util.Collection;
import java.util.List;

import org.eclipse.scout.commons.Assertions;
import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.EventListenerList;
import org.eclipse.scout.commons.resource.BinaryResource;
import org.eclipse.scout.rt.client.job.ModelJobs;
import org.eclipse.scout.rt.client.session.ClientSessionProvider;
import org.eclipse.scout.rt.client.ui.IDisplayParent;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.form.DisplayParentResolver;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.job.IBlockingCondition;
import org.eclipse.scout.rt.platform.job.Jobs;

public class FileChooser implements IFileChooser {

  private final IFileChooserUIFacade m_uiFacade;
  private final EventListenerList m_listenerList = new EventListenerList();

  private List<String> m_fileExtensions;
  private boolean m_multiSelect;
  private List<BinaryResource> m_files;
  private final IBlockingCondition m_blockingCondition;
  private long m_maximumUploadSize;

  private IDisplayParent m_displayParent;

  public FileChooser() {
    this(null, false);
  }

  public FileChooser(boolean multiSelect) {
    this(null, multiSelect);
  }

  public FileChooser(Collection<String> fileExtensions) {
    this(fileExtensions, false);
  }

  public FileChooser(Collection<String> fileExtensions, boolean multiSelect) {
    m_uiFacade = new P_UIFacade();
    m_blockingCondition = Jobs.getJobManager().createBlockingCondition("block", false);
    m_fileExtensions = CollectionUtility.arrayListWithoutNullElements(fileExtensions);
    m_multiSelect = multiSelect;
    m_maximumUploadSize = DEFAULT_MAXIMUM_UPLOAD_SIZE;
    m_displayParent = BEANS.get(DisplayParentResolver.class).resolve(this);
  }

  @Override
  public IFileChooserUIFacade getUIFacade() {
    return m_uiFacade;
  }

  @Override
  public IDisplayParent getDisplayParent() {
    return m_displayParent;
  }

  @Override
  public void setDisplayParent(IDisplayParent displayParent) {
    Assertions.assertFalse(ClientSessionProvider.currentSession().getDesktop().isShowing(this), "Property 'displayParent' cannot be changed because FileChooser is already showing [fileChooser=%s]", this);

    if (displayParent == null) {
      displayParent = BEANS.get(DisplayParentResolver.class).resolve(this);
    }

    m_displayParent = Assertions.assertNotNull(displayParent, "'displayParent' must not be null");
  }

  @Override
  public void addFileChooserListener(FileChooserListener listener) {
    m_listenerList.add(FileChooserListener.class, listener);
  }

  @Override
  public void removeFileChooserListener(FileChooserListener listener) {
    m_listenerList.remove(FileChooserListener.class, listener);
  }

  @Override
  public List<String> getFileExtensions() {
    return CollectionUtility.arrayList(m_fileExtensions);
  }

  @Override
  public boolean isMultiSelect() {
    return m_multiSelect;
  }

  @Override
  public void setMaximumUploadSize(long maximumUploadSize) {
    m_maximumUploadSize = maximumUploadSize;
  }

  @Override
  public long getMaximumUploadSize() {
    return m_maximumUploadSize;
  }

  @Override
  public List<BinaryResource> startChooser() {
    m_files = null;
    m_blockingCondition.setBlocking(true);

    IDesktop desktop = ClientSessionProvider.currentSession().getDesktop();
    desktop.showFileChooser(this);
    try {
      waitFor();
    }
    finally {
      desktop.hideFileChooser(this);
      fireClosed();
    }
    return getFiles();
  }

  private void waitFor() {
    m_blockingCondition.waitFor(ModelJobs.EXECUTION_HINT_UI_INTERACTION_REQUIRED);
  }

  @Override
  public void setFiles(List<BinaryResource> result) {
    m_files = CollectionUtility.arrayListWithoutNullElements(result);
    m_blockingCondition.setBlocking(false);
  }

  @Override
  public List<BinaryResource> getFiles() {
    return CollectionUtility.arrayList(m_files);
  }

  protected void fireClosed() {
    fireFileChooserEvent(new FileChooserEvent(this, FileChooserEvent.TYPE_CLOSED));
  }

  protected void fireFileChooserEvent(FileChooserEvent e) {
    FileChooserListener[] listeners = m_listenerList.getListeners(FileChooserListener.class);
    for (FileChooserListener listener : listeners) {
      listener.fileChooserChanged(e);
    }
  }

  private class P_UIFacade implements IFileChooserUIFacade {

    @Override
    public void setResultFromUI(List<BinaryResource> files) {
      setFiles(files);
    }
  }
}
