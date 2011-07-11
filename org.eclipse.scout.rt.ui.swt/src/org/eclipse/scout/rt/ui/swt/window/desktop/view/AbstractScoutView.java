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
package org.eclipse.scout.rt.ui.swt.window.desktop.view;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.commons.OptimisticLock;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.button.IButton;
import org.eclipse.scout.rt.ui.swt.ISwtEnvironment;
import org.eclipse.scout.rt.ui.swt.form.ISwtScoutForm;
import org.eclipse.scout.rt.ui.swt.util.listener.PartListener;
import org.eclipse.scout.rt.ui.swt.window.ISwtScoutPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.ISaveablePart2;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.part.ViewPart;

/**
 * <h3>AbstractScoutView</h3> ...
 * 
 * @since 1.0.9 03.07.2008
 */
public abstract class AbstractScoutView extends ViewPart implements ISwtScoutPart, ISaveablePart2 {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractScoutView.class);
  // public static enum ViewState{OPEN, CLOSING_UI, CLOSING_MODEL, CLOSED}

  private P_ViewListener m_viewListener;
  private OptimisticLock m_closeLock;
  private OptimisticLock m_closeFromModel = new OptimisticLock();
  // private ViewState m_viewState;
  private OptimisticLock m_layoutLock = new OptimisticLock();

  private Form m_rootForm;
  private Composite m_rootArea;

  private IForm m_scoutForm;

  private PropertyChangeListener m_formPropertyListener;
  private ISwtScoutForm m_uiForm;

  public AbstractScoutView() {
    m_formPropertyListener = new P_ScoutPropertyChangeListener();
    m_closeLock = new OptimisticLock();
  }

  protected void attatchListeners() {
    if (m_viewListener == null) {
      m_viewListener = new P_ViewListener();
    }
    getSite().getPage().addPartListener(m_viewListener);
  }

  protected void detachListeners() {
    getSite().getPage().removePartListener(m_viewListener);
  }

  @Override
  public void dispose() {
    detachListeners();
    super.dispose();
  }

  public void showForm(IForm scoutForm) throws ProcessingException {
    if (m_scoutForm != null) {
      LOG.warn("The view 'ID=" + getViewSite().getId() + "' is already open. The form '" + scoutForm.getTitle() + " (" + scoutForm.getClass().getName() + ")' can not be opened!");
      detachScout(m_scoutForm);
      m_scoutForm = null;
    }
    m_scoutForm = scoutForm;
    try {
      m_layoutLock.acquire();
      getSwtContentPane().setRedraw(false);
      m_uiForm = getSwtEnvironment().createForm(getSwtContentPane(), scoutForm);
      attachScout(m_scoutForm);
    }
    finally {
      m_layoutLock.release();
      getSwtContentPane().setRedraw(true);
      getSwtContentPane().layout(true, true);
    }
  }

  @Override
  public void closePart() throws ProcessingException {
    try {
      m_closeFromModel.acquire();
      if (m_closeLock.acquire()) {
        try {
          getSite().getPage().hideView(AbstractScoutView.this);
        }
        catch (Exception e) {
          throw new ProcessingException("could not close view '" + getViewSite().getId() + "'.", e);
        }
      }
      if (m_scoutForm != null) {
        detachScout(m_scoutForm);
        m_scoutForm = null;
      }
    }
    finally {
      m_closeLock.release();
      m_closeFromModel.release();
    }
  }

  @Override
  public IForm getForm() {
    return m_scoutForm;
  }

  @Override
  public ISwtScoutForm getUiForm() {
    return m_uiForm;
  }

  protected void attachScout(IForm form) {
    setTitleFromScout(form.getTitle());
    setImageFromScout(form.getIconId());
    setMaximizeEnabledFromScout(form.isMaximizeEnabled());
    setMaximizedFromScout(form.isMaximized());
    setMinimizeEnabledFromScout(form.isMinimizeEnabled());
    setMinimizedFromScout(form.isMinimized());
    boolean closable = false;
    for (IFormField f : form.getAllFields()) {
      if (f.isEnabled() && f.isVisible() && f instanceof IButton) {
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
  }

  protected void detachScout(IForm form) {
    // listeners
    form.removePropertyChangeListener(m_formPropertyListener);
  }

  protected void setImageFromScout(String iconId) {
    setTitleImage(getSwtEnvironment().getIcon(iconId));
  }

  protected void setTitleFromScout(String title) {
    if (title == null) {
      title = "";
    }
    setPartName(title);
  }

  protected void setMaximizeEnabledFromScout(boolean maximizable) {
    // must be done by instantiating
  }

  protected void setMaximizedFromScout(boolean maximized) {
    IWorkbenchPartReference ref = getSite().getPage().getReference(getSite().getPart());
    try {
      if (maximized) {
        getSite().getPage().setPartState(ref, IWorkbenchPage.STATE_MAXIMIZED);
      }
      else {
        getSite().getPage().setPartState(ref, IWorkbenchPage.STATE_RESTORED);
      }
    }
    catch (Exception e) {
      // void
    }
  }

  protected void setMinimizeEnabledFromScout(boolean minized) {
    // must be done by instantiating
  }

  protected void setMinimizedFromScout(boolean minimized) {
    // IWorkbenchPartReference ref =
    // getSite().getPage().getReference(getSite().getPart());
    // try {
    // if (maximized) {
    // getSite().getPage().setPartState(ref, IWorkbenchPage.STATE_MAXIMIZED);
    // } else {
    // getSite().getPage().setPartState(ref, IWorkbenchPage.STATE_RESTORED);
    // }
    // } catch (Exception e) {
    // // void
    // }
  }

  protected void setCloseEnabledFromScout(boolean closebale) {
    // void
  }

  @Override
  public void createPartControl(Composite parent) {
    FormToolkit toolkit = new FormToolkit(parent.getDisplay());
    m_rootForm = toolkit.createForm(parent);

    m_rootForm.setData(ISwtScoutPart.MARKER_SCOLLED_FORM, new Object());
    m_rootArea = m_rootForm.getBody();
    m_rootArea.setLayout(new ViewStackLayout());
    attatchListeners();
  }

  protected Form getRootForm() {
    return m_rootForm;
  }

  protected Composite getRootArea() {
    return m_rootArea;
  }

  @Override
  public void setFocus() {
    m_rootArea.setFocus();
  }

  public Composite getSwtContentPane() {
    return m_rootArea;
  }

  /**
   * must be implemented by the concrete view to provide an environment
   * 
   * @return
   */
  protected abstract ISwtEnvironment getSwtEnvironment();

  @Override
  public boolean isVisible() {
    return getSite().getPage().isPartVisible(getSite().getPart());
  }

  @Override
  public void activate() {
    if (getSite().getPage().getViewStack(this) != null) {
      getSite().getPage().activate(getSite().getPart());
    }
  }

  @Override
  public boolean isActive() {
    return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage() == getSite().getPage();
  }

  @Override
  public void setStatusLineMessage(Image image, String message) {
    getViewSite().getActionBars().getStatusLineManager().setMessage(image, message);
  }

  protected void firePartActivatedFromUI() {
    Runnable job = new Runnable() {
      @Override
      public void run() {
        if (m_scoutForm != null) {
          m_scoutForm.getUIFacade().fireFormActivatedFromUI();
        }
      }
    };
    getSwtEnvironment().invokeScoutLater(job, 0);
  }

  private class P_ViewListener extends PartListener {
    @Override
    public void partActivated(IWorkbenchPartReference partRef) {
      if (partRef.getPart(false).equals(getViewSite().getPart())) {
        if (getSwtEnvironment().isInitialized()) {
          firePartActivatedFromUI();
        }
      }
    }

    @Override
    public void partClosed(IWorkbenchPartReference partRef) {
      if (getViewSite().getPart().equals(partRef.getPart(false))) {
        try {
          if (m_closeLock.acquire()) {
            Runnable job = new Runnable() {
              @Override
              public void run() {
                if (m_scoutForm != null) {
                  m_scoutForm.getUIFacade().fireFormKilledFromUI();
                }
              }
            };
            if (getSwtEnvironment().isInitialized()) {
              getSwtEnvironment().invokeScoutLater(job, 0);
            }
          }
        }
        finally {
          m_closeLock.release();
        }
      }
    }
  }

  @Override
  public int promptToSaveOnClose() {
    if (m_scoutForm == null) {
      return ISaveablePart2.NO;
    }
    if (m_closeFromModel.isReleased()) {
      new ClientSyncJob("Prompt to save", getSwtEnvironment().getClientSession()) {
        @Override
        protected void runVoid(IProgressMonitor monitor) throws Throwable {
          if (m_scoutForm != null) {
            m_scoutForm.getUIFacade().fireFormClosingFromUI();
          }
        }
      }.schedule();
      return ISaveablePart2.CANCEL;
    }
    return ISaveablePart2.YES;
  }

  @Override
  public void doSave(IProgressMonitor monitor) {
  }

  @Override
  public void doSaveAs() {
  }

  @Override
  public boolean isDirty() {
    if (m_scoutForm != null && m_scoutForm.isAskIfNeedSave()) {
      boolean saveNeeded = m_scoutForm.isSaveNeeded();
      return saveNeeded;
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
    Control focusControl = m_rootArea.getDisplay().getFocusControl();
    if (focusControl != null && !focusControl.isDisposed()) {
      focusControl.traverse(SWT.TRAVERSE_TAB_NEXT);
    }
    return isDirty();
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

}
