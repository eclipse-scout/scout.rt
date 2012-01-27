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
package org.eclipse.scout.rt.ui.swt.window.desktop.editor;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.commons.OptimisticLock;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.button.IButton;
import org.eclipse.scout.rt.ui.swt.ISwtEnvironment;
import org.eclipse.scout.rt.ui.swt.busy.AnimatedBusyImage;
import org.eclipse.scout.rt.ui.swt.form.ISwtScoutForm;
import org.eclipse.scout.rt.ui.swt.util.ScoutFormToolkit;
import org.eclipse.scout.rt.ui.swt.util.SwtUtility;
import org.eclipse.scout.rt.ui.swt.util.listener.PartListener;
import org.eclipse.scout.rt.ui.swt.window.ISwtScoutPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.ISaveablePart2;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.presentations.IPresentablePart;

public abstract class AbstractScoutEditorPart extends EditorPart implements ISwtScoutPart, ISaveablePart2 {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractScoutEditorPart.class);
  public static final String EDITOR_ID = AbstractScoutEditorPart.class.getName();

  private Form m_rootForm;
  private Composite m_rootArea;
  private P_EditorListener m_editorListener;
  private OptimisticLock m_closeLock;
  private OptimisticLock m_closeFromModel = new OptimisticLock();

  private PropertyChangeListener m_formPropertyListener;
  private OptimisticLock m_layoutLock;
  private ISwtScoutForm m_uiForm;
  private Image m_titleImageBackup;
  private AnimatedBusyImage m_busyImage;

  public AbstractScoutEditorPart() {
    m_layoutLock = new OptimisticLock();
    m_closeLock = new OptimisticLock();
    m_formPropertyListener = new P_ScoutPropertyChangeListener();
  }

  @Override
  protected void setTitleImage(Image titleImage) {
    m_titleImageBackup = titleImage;
    if (m_busyImage == null || !m_busyImage.isBusy()) {
      super.setTitleImage(titleImage);
    }
  }

  @Override
  public void setBusy(boolean b) {
    if (m_busyImage == null || m_busyImage.isBusy() == b) {
      return;
    }
    m_busyImage.setBusy(b);
    if (!b) {
      super.setTitleImage(m_titleImageBackup);
    }
  }

  protected void attachScout() {
    IForm form = getForm();
    setTitleFromScout(form.getTitle());
    setImageFromScout(form.getIconId());
    setMaximizeEnabledFromScout(form.isMaximizeEnabled());
    setMaximizedFromScout(form.isMaximized());
    setMinimizeEnabledFromScout(form.isMinimizeEnabled());
    setMinimizedFromScout(form.isMinimized());
    boolean closable = false;
    for (IFormField f : form.getAllFields()) {
      if (f.isEnabled() && f.isVisible() && (f instanceof IButton)) {
        switch (((IButton) f).getSystemType()) {
          case IButton.SYSTEM_TYPE_CLOSE:
          case IButton.SYSTEM_TYPE_CANCEL: {
            closable = true;
            break;
          }
        }
      }
      if (closable) {
        break;
      }
    }
    setCloseEnabledFromScout(closable);
    // listeners
    form.addPropertyChangeListener(m_formPropertyListener);
    if (m_editorListener == null) {
      m_editorListener = new P_EditorListener();
    }
    getSite().getPage().addPartListener(m_editorListener);
  }

  protected void detachScout() {
    if (getForm() != null) {
      // listeners
      getForm().removePropertyChangeListener(m_formPropertyListener);
    }
  }

  @Override
  public final void closePart() throws ProcessingException {
    try {
      m_closeFromModel.acquire();
      if (m_closeLock.acquire()) {
        try {
          execCloseEditor();
        }
        catch (Exception e) {
          throw new ProcessingException("could not close editor '" + getEditorSite().getId() + "'.", e);
        }
      }
    }
    finally {
      m_closeLock.release();
      m_closeFromModel.release();
    }
  }

  /**
   * @return true if the editor was successfully closed, and false if the editor is still open
   */
  protected boolean execCloseEditor() throws Exception {
    return getSite().getPage().closeEditor(this, false);
  }

  @Override
  public void dispose() {
    detachScout();
    getSite().getPage().removePartListener(m_editorListener);
    super.dispose();
  }

  protected void setImageFromScout(String iconId) {
    Image img = getSwtEnvironment().getIcon(iconId);
    setTitleImage(img);
    String sub = getForm().getSubTitle();
    if (sub != null) {
      getSwtForm().setImage(img);
    }
    else {
      getSwtForm().setImage(null);
    }
  }

  protected void setTitleFromScout(String title) {
    IForm f = getForm();
    //
    String s = f.getBasicTitle();
    setPartName(StringUtility.removeNewLines(s != null ? s : ""));
    //
    s = f.getSubTitle();
    if (s != null) {
      getSwtForm().setText(SwtUtility.escapeMnemonics(StringUtility.removeNewLines(s != null ? s : "")));
    }
    else {
      getSwtForm().setText(null);
    }
  }

  protected void setMaximizeEnabledFromScout(boolean maximizable) {
    // must be done by instantiating
  }

  protected void setMaximizedFromScout(boolean maximized) {

  }

  protected void setMinimizeEnabledFromScout(boolean minized) {
    // must be done by instantiating
  }

  protected void setMinimizedFromScout(boolean minimized) {

  }

  private void setSaveNeededFromScout() {
    firePropertyChange(IPresentablePart.PROP_DIRTY);
  }

  protected void setCloseEnabledFromScout(boolean closebale) {
    // void
  }

  /**
   * ISwtScoutEditor
   */

  protected abstract ISwtEnvironment getSwtEnvironment();

  @Override
  public void createPartControl(Composite parent) {
    m_busyImage = new AnimatedBusyImage(parent.getDisplay()) {
      @Override
      protected void notifyImage(Image image) {
        AbstractScoutEditorPart.super.setTitleImage(image);
      }
    };
    ScoutFormToolkit toolkit = getSwtEnvironment().getFormToolkit();
    m_rootForm = toolkit.createForm(parent);
    m_rootForm.setData(ISwtScoutPart.MARKER_SCOLLED_FORM, new Object());
    m_rootArea = m_rootForm.getBody();
    GridLayout gridLayout = new GridLayout();
    gridLayout.horizontalSpacing = 0;
    gridLayout.marginHeight = 2;
    gridLayout.marginWidth = 2;
    gridLayout.verticalSpacing = 0;
    m_rootArea.setLayout(gridLayout);

    // create form
    try {
      m_layoutLock.acquire();
      m_rootArea.setRedraw(false);
      m_uiForm = getSwtEnvironment().createForm(m_rootArea, getForm());
      GridData d = new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL);
      m_uiForm.getSwtContainer().setLayoutData(d);
      attachScout();
    }
    finally {
      m_layoutLock.release();
      m_rootArea.setRedraw(true);
    }
  }

  /**
   * EditorPart
   */
  @Override
  public void init(IEditorSite site, IEditorInput input) throws PartInitException {
    if (input instanceof ScoutFormEditorInput) {
      setSite(site);
      setInput(input);
    }
    else {
      throw new PartInitException("Input must be from instance ScoutFormEditorInput");
    }
  }

  @Override
  public IForm getForm() {
    return ((ScoutFormEditorInput) getEditorInput()).getScoutObject();
  }

  @Override
  public ISwtScoutForm getUiForm() {
    return m_uiForm;
  }

  @Override
  public Form getSwtForm() {
    return m_rootForm;
  }

  public Form getRootForm() {
    return m_rootForm;
  }

  @Override
  public int promptToSaveOnClose() {
    if (getForm() == null) {
      return ISaveablePart2.NO;
    }
    if (m_closeFromModel.isReleased()) {
      new ClientSyncJob("Prompt to save", getSwtEnvironment().getClientSession()) {
        @Override
        protected void runVoid(IProgressMonitor monitor) throws Throwable {
          // sle 07.08.09: Ticket 79445: Removed closeLock in
          // promtToSaveOnClose. We give the responsibility of closing to the
          // model. UI is not closing by himself.
          getForm().getUIFacade().fireFormClosingFromUI();
        }
      }.schedule();
      return ISaveablePart2.CANCEL;
    }
    return ISaveablePart2.YES;
  }

  @Override
  public void doSave(IProgressMonitor monitor) {
    // nop
  }

  @Override
  public void doSaveAs() {
    // nop
  }

  @Override
  public boolean isDirty() {
    if (getForm() != null && getForm().isAskIfNeedSave()) {
      return getForm().isSaveNeeded();
    }
    else {
      return false;
    }
  }

  @Override
  public boolean isSaveAsAllowed() {
    return false;
  }

  @Override
  public boolean isSaveOnCloseNeeded() {
    // ensure the traversal is done to write eventually changes to model
    Control focusControl = m_rootArea.getDisplay().getFocusControl();
    if (focusControl != null && !focusControl.isDisposed()) {
      focusControl.traverse(SWT.TRAVERSE_TAB_NEXT);
    }
    return isDirty();
  }

  @Override
  public void setFocus() {
    m_rootArea.setFocus();
  }

  protected void handlePartActivatedFromUI() {
    if (getSwtEnvironment().isInitialized()) {
      Runnable job = new Runnable() {
        @Override
        public void run() {
          if (getForm() != null) {
            getForm().getUIFacade().fireFormActivatedFromUI();
          }
        }
      };
      getSwtEnvironment().invokeScoutLater(job, 0);
    }
  }

  protected void handleClosedFromUI() {
    try {
      if (m_closeLock.acquire()) {
        Runnable job = new Runnable() {
          @Override
          public void run() {
            if (getForm() != null) {
              getForm().getUIFacade().fireFormKilledFromUI();
            }
          }

        };
        getSwtEnvironment().invokeScoutLater(job, 0);
      }
    }
    finally {
      m_closeLock.release();
    }
  }

  @Override
  public void activate() {
    getSite().getPage().activate(getSite().getPart());
  }

  @Override
  public boolean isActive() {
    IWorkbenchWindow w = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
    if (w == null) {
      return false;
    }
    IWorkbenchPage activePage = w.getActivePage();
    if (activePage == null) {
      return false;
    }
    return (activePage == getSite().getPage()) && (activePage.getActivePart() == this);
  }

  @Override
  public boolean isVisible() {
    return getSite().getPage().isPartVisible(getSite().getPart());
  }

  @Override
  public void setStatusLineMessage(Image image, String message) {
    getEditorSite().getActionBars().getStatusLineManager().setMessage(image, message);
  }

  protected void handleScoutPropertyChange(String name, Object newValue) {
    if (name.equals(IForm.PROP_TITLE)) {
      setTitleFromScout((String) newValue);
    }
    else if (name.equals(IForm.PROP_ICON_ID)) {
      setImageFromScout((String) newValue);
    }
    else if (name.equals(IForm.PROP_MINIMIZE_ENABLED)) {
      setMinimizeEnabledFromScout(((Boolean) newValue).booleanValue());
    }
    else if (name.equals(IForm.PROP_MAXIMIZE_ENABLED)) {
      setMaximizeEnabledFromScout(((Boolean) newValue).booleanValue());
    }
    else if (name.equals(IForm.PROP_MINIMIZED)) {
      setMinimizedFromScout(((Boolean) newValue).booleanValue());
    }
    else if (name.equals(IForm.PROP_MAXIMIZED)) {
      setMaximizedFromScout(((Boolean) newValue).booleanValue());
    }
    else if (name.equals(IForm.PROP_SAVE_NEEDED)) {
      setSaveNeededFromScout();
    }
  }

  private class P_ScoutPropertyChangeListener implements PropertyChangeListener {
    @Override
    public void propertyChange(final PropertyChangeEvent e) {
      Runnable t = new Runnable() {
        @Override
        public void run() {
          handleScoutPropertyChange(e.getPropertyName(), e.getNewValue());
        }
      };
      getSwtEnvironment().invokeSwtLater(t);
    }
  }// end private class

  private class P_EditorListener extends PartListener {
    @Override
    public void partClosed(IWorkbenchPartReference partRef) {
      IWorkbenchPart part = partRef.getPart(false);
      if (part != null && part.equals(getEditorSite().getPart())) {
        handleClosedFromUI();
      }
    }

    @Override
    public void partActivated(IWorkbenchPartReference partRef) {
      if (partRef.getPart(false).equals(getEditorSite().getPart())) {
        handlePartActivatedFromUI();

      }
    }
  } // end class P_EditorListener
}
