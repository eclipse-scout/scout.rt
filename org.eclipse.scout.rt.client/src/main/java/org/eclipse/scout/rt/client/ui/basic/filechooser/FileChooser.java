/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.basic.filechooser;

import java.util.Collection;
import java.util.List;

import org.eclipse.scout.rt.client.job.ModelJobs;
import org.eclipse.scout.rt.client.session.ClientSessionProvider;
import org.eclipse.scout.rt.client.ui.IDisplayParent;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.form.DisplayParentResolver;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.job.IBlockingCondition;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.event.FastListenerList;
import org.eclipse.scout.rt.platform.util.event.IFastListenerList;

public class FileChooser implements IFileChooser {

  private final IFileChooserUIFacade m_uiFacade;
  private final FastListenerList<FileChooserListener> m_listenerList = new FastListenerList<>();

  private final List<String> m_fileExtensions;
  private final boolean m_multiSelect;
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
    m_blockingCondition = Jobs.newBlockingCondition(false);
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
    Assertions.assertFalse(ClientSessionProvider.currentSession().getDesktop().isShowing(this), "Property 'displayParent' cannot be changed because FileChooser is already showing [fileChooser={}]", this);

    if (displayParent == null) {
      displayParent = BEANS.get(DisplayParentResolver.class).resolve(this);
    }

    m_displayParent = Assertions.assertNotNull(displayParent, "'displayParent' must not be null");
  }

  @Override
  public IFastListenerList<FileChooserListener> fileChooserListeners() {
    return m_listenerList;
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
    // Do not exit upon ui cancel request, as the file chooser would be closed immediately otherwise.
    m_blockingCondition.waitFor(ModelJobs.EXECUTION_HINT_UI_INTERACTION_REQUIRED);
  }

  @Override
  public void setFiles(List<BinaryResource> result) {
    m_files = CollectionUtility.arrayListWithoutNullElements(result);
    m_blockingCondition.setBlocking(false);
  }

  @Override
  public void doClose() {
    setFiles(null);
  }

  @Override
  public List<BinaryResource> getFiles() {
    return CollectionUtility.arrayList(m_files);
  }

  protected void fireClosed() {
    fireFileChooserEvent(new FileChooserEvent(this, FileChooserEvent.TYPE_CLOSED));
  }

  protected void fireFileChooserEvent(FileChooserEvent e) {
    fileChooserListeners().list().forEach(listener -> listener.fileChooserChanged(e));
  }

  protected class P_UIFacade implements IFileChooserUIFacade {

    @Override
    public void setResultFromUI(List<BinaryResource> files) {
      setFiles(files);
    }
  }
}
