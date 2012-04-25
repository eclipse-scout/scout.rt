/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.scout.rt.ui.rap.workbench.window.editor;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.commons.OptimisticLock;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.client.ui.form.FormEvent;
import org.eclipse.scout.rt.client.ui.form.FormListener;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.button.IButton;
import org.eclipse.scout.rt.ui.rap.IRwtEnvironment;
import org.eclipse.scout.rt.ui.rap.form.IRwtScoutForm;
import org.eclipse.scout.rt.ui.rap.util.RwtUtility;
import org.eclipse.scout.rt.ui.rap.util.ScoutFormToolkit;
import org.eclipse.scout.rt.ui.rap.window.IRwtScoutPart;
import org.eclipse.scout.rt.ui.rap.workbench.util.listener.PartListener;
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

public abstract class AbstractScoutEditorPart extends EditorPart implements IRwtScoutPart, ISaveablePart2 {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractScoutEditorPart.class);
  public static final String EDITOR_ID = AbstractScoutEditorPart.class.getName();

  private final IRwtEnvironment m_uiEnvironment = createUiEnvironment();

  private Form m_rootForm;
  private Composite m_rootArea;
  private P_EditorListener m_editorListener;
  private OptimisticLock m_closeLock;
  private OptimisticLock m_closeFromModel = new OptimisticLock();

  private PropertyChangeListener m_formPropertyListener;
  private OptimisticLock m_layoutLock;

  public AbstractScoutEditorPart() {
    m_layoutLock = new OptimisticLock();
    m_closeLock = new OptimisticLock();
    m_formPropertyListener = new P_ScoutPropertyChangeListener();
  }

  @Override
  public void setBusy(boolean b) {
    //nop
  }

  protected void attachScout() {
    IForm form = getScoutObject();
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
    if (getScoutObject() != null) {
      // listeners
      getScoutObject().removePropertyChangeListener(m_formPropertyListener);
    }
  }

  @Override
  public void showPart() {
    // void
  }

  @Override
  public void closePart() {
    try {
      m_closeFromModel.acquire();
      if (m_closeLock.acquire()) {
        try {
          getSite().getPage().closeEditor(this, false);
        }
        catch (Exception e) {
          LOG.error("could not close editor '" + getEditorSite().getId() + "'.", e);
        }
      }
    }
    finally {
      m_closeLock.release();
      m_closeFromModel.release();
    }
  }

  @Override
  public void dispose() {
    detachScout();
    getSite().getPage().removePartListener(m_editorListener);
    super.dispose();
  }

  protected void setImageFromScout(String iconId) {
    Image img = getUiEnvironment().getIcon(iconId);
    setTitleImage(img);
    String sub = getScoutObject().getSubTitle();
    if (sub != null) {
      getUiForm().setImage(img);
    }
    else {
      getUiForm().setImage(null);
    }
  }

  protected void setTitleFromScout(String title) {
    IForm f = getScoutObject();
    //
    String s = f.getBasicTitle();
    setPartName(StringUtility.removeNewLines(s != null ? s : ""));
    //
    s = f.getSubTitle();
    if (s != null) {
      getUiForm().setText(RwtUtility.escapeMnemonics(StringUtility.removeNewLines(s != null ? s : "")));
    }
    else {
      getUiForm().setText(null);
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
   * IRwtScoutEditor
   * <p>
   * Client code must NOT call this method but rather the public method {@link #getUiEnvironment()}
   */
  protected abstract IRwtEnvironment createUiEnvironment();

  public final IRwtEnvironment getUiEnvironment() {
    return m_uiEnvironment;
  }

  @Override
  public void createPartControl(Composite parent) {
    ScoutFormToolkit toolkit = getUiEnvironment().getFormToolkit();
    m_rootForm = toolkit.createForm(parent);
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
      IRwtScoutForm form = getUiEnvironment().createForm(m_rootArea, getScoutObject());
      GridData d = new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL);
      form.getUiContainer().setLayoutData(d);
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
  public IForm getScoutObject() {
    return ((ScoutFormEditorInput) getEditorInput()).getScoutObject();
  }

  @Override
  public Form getUiForm() {
    return getRootForm();
  }

  protected Form getRootForm() {
    return m_rootForm;
  }

  @Override
  public int promptToSaveOnClose() {
    if (getScoutObject() == null) {
      return ISaveablePart2.NO;
    }
    if (m_closeFromModel.isReleased()) {
      new ClientSyncJob("Prompt to save", getUiEnvironment().getClientSession()) {
        @Override
        protected void runVoid(IProgressMonitor monitor) throws Throwable {
          // sle 07.08.09: Ticket 79445: Removed closeLock in
          // promtToSaveOnClose. We give the responsibility of closing to the
          // model. UI is not closing by himself.
          getScoutObject().getUIFacade().fireFormClosingFromUI();
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
    if (getScoutObject() != null && getScoutObject().isAskIfNeedSave()) {
      return getScoutObject().isSaveNeeded();
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
      //XXX rap       focusControl.traverse(SWT.TRAVERSE_TAB_NEXT);
    }
    return isDirty();
  }

  @Override
  public void setFocus() {
    m_rootArea.setFocus();
  }

  protected void handlePartActivatedFromUI() {
    if (getUiEnvironment().isInitialized()) {
      Runnable job = new Runnable() {
        @Override
        public void run() {
          if (getScoutObject() != null) {
            getScoutObject().getUIFacade().fireFormActivatedFromUI();
          }
        }
      };
      getUiEnvironment().invokeScoutLater(job, 0);
    }
  }

  protected void handleClosedFromUI() {
    Runnable job = new Runnable() {
      @Override
      public void run() {
        try {
          if (m_closeLock.acquire()) {
            if (getScoutObject() != null) {
              getScoutObject().getUIFacade().fireFormKilledFromUI();
            }
          }
        }
        finally {
          m_closeLock.release();
        }
      }
    };
    getUiEnvironment().invokeScoutLater(job, 0);
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
  public boolean setStatusLineMessage(Image image, String message) {
    getEditorSite().getActionBars().getStatusLineManager().setMessage(image, message);
    return true;
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
      getUiEnvironment().invokeUiLater(t);
    }
  }// end private class

  private class P_FormListener implements FormListener {
    @Override
    public void formChanged(FormEvent e) throws ProcessingException {
      if (e.getType() == FormEvent.TYPE_CLOSED) {
        Runnable t = new Runnable() {
          @Override
          public void run() {
            closePart();
          }
        };
        getUiEnvironment().invokeUiLater(t);
      }
    }
  }

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
