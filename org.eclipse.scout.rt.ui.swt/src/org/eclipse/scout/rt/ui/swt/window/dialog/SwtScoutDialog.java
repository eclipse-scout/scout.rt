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
package org.eclipse.scout.rt.ui.swt.window.dialog;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Method;
import java.util.List;

import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.action.ActionUtility;
import org.eclipse.scout.rt.client.ui.action.tool.IToolButton;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.button.IButton;
import org.eclipse.scout.rt.ui.swt.DefaultValidateRoot;
import org.eclipse.scout.rt.ui.swt.ISwtEnvironment;
import org.eclipse.scout.rt.ui.swt.IValidateRoot;
import org.eclipse.scout.rt.ui.swt.SwtShellValidateRoot;
import org.eclipse.scout.rt.ui.swt.action.SwtScoutToolbarAction;
import org.eclipse.scout.rt.ui.swt.form.ISwtScoutForm;
import org.eclipse.scout.rt.ui.swt.util.SwtUtility;
import org.eclipse.scout.rt.ui.swt.util.VersionUtility;
import org.eclipse.scout.rt.ui.swt.window.ISwtScoutPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.widgets.Form;

/**
 * <h3>SwtScoutDialog</h3> ...
 * 
 * @since 1.0.9 18.07.2008
 */
public class SwtScoutDialog extends Dialog implements ISwtScoutPart {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(SwtScoutDialog.class);

  private final ISwtEnvironment m_environment;
  private Composite m_contentPane;
  private Form m_rootForm;
  private IForm m_scoutForm;
  private Point m_initialLocation;
  private boolean m_opened;

  private PropertyChangeListener m_formPropertyListener;

  private ISwtScoutForm m_uiForm;

  public SwtScoutDialog(Shell parentShell, ISwtEnvironment environment) {
    this(parentShell, environment, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.APPLICATION_MODAL);
  }

  public SwtScoutDialog(Shell parentShell, ISwtEnvironment environment, int style) {
    // modal
    super((style & SWT.APPLICATION_MODAL) != 0 ? parentShell : null);
    m_environment = environment;
    m_formPropertyListener = new P_ScoutPropertyChangeListener();
    setShellStyle(style);
  }

  @Override
  public void setBusy(boolean b) {
    getSwtForm().setBusy(b);
    getSwtForm().layout(true);
  }

  public void showForm(IForm scoutForm) throws ProcessingException {
    m_opened = true;
    if (m_scoutForm == null) {
      create();
      m_scoutForm = scoutForm;
      try {
        m_contentPane.setRedraw(false);
        m_uiForm = m_environment.createForm(m_contentPane, scoutForm);
        GridData d = new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL);
        m_uiForm.getSwtContainer().setLayoutData(d);
        attachScout(scoutForm);
        DefaultValidateRoot shellValidateRoot = createShellValidateRoot(getShell(), getEnvironment());
        if (shellValidateRoot != null) {
          getShell().setData(IValidateRoot.VALIDATE_ROOT_DATA, shellValidateRoot);
        }
      }
      finally {
        m_contentPane.setRedraw(true);
      }
      open();
    }
    else {
      throw new ProcessingException("The form dialog is already open. The form '" + scoutForm.getTitle() + " (" + scoutForm.getClass().getName() + ")' can not be opened!");
    }
  }

  @Override
  public void closePart() throws ProcessingException {
    if (m_scoutForm != null) {
      detachScout(m_scoutForm);
    }
    super.close();
  }

  @Override
  public IForm getForm() {
    return m_scoutForm;
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

  protected void attachScout(IForm form) {
    updateToolbarActionsFromScout();
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

  /**
  *
  */
  protected void updateToolbarActionsFromScout() {
    List<IToolButton> toolbuttons = ActionUtility.visibleNormalizedActions(getForm().getToolbuttons());
    if (!toolbuttons.isEmpty()) {
      IToolBarManager toolBarManager = getRootForm().getToolBarManager();
      for (IToolButton b : toolbuttons) {
        toolBarManager.add(new SwtScoutToolbarAction(b, toolBarManager, getEnvironment()));
      }
      toolBarManager.update(true);
    }

  }

  protected void detachScout(IForm form) {
    // listeners
    form.removePropertyChangeListener(m_formPropertyListener);
  }

  protected void setImageFromScout(String iconId) {
    Image img = m_environment.getIcon(iconId);
    getShell().setImage(img);
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
    getShell().setText(StringUtility.removeNewLines(s != null ? s : ""));
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

  protected void setCloseEnabledFromScout(boolean closebale) {
    // void
  }

  protected void setSaveNeededFromScout(boolean modified) {
    if (VersionUtility.isEclipseVersionLessThan35()) {
      return;
    }

    try {
      //Call getShell().setModified(modified);
      Method setModified = Shell.class.getMethod("setModified", boolean.class);
      setModified.invoke(getShell(), modified);
    }
    catch (Exception e) {
      LOG.warn("could not access method 'setModified' on 'Shell'.", e);
    }
  }

  @Override
  protected final Control createButtonBar(Composite parent) {
    // suppress default eclipse button bar
    return null;
  }

  public void setMinSize(Point size) {
    if (size != null) {
      getShell().setSize(size);
      getShell().setMinimumSize(size);
    }
  }

  @Override
  public Point getInitialLocation(Point initialSize) {
    if (m_initialLocation != null) {
      return m_initialLocation;
    }
    return super.getInitialLocation(initialSize);
  }

  public void setInitialLocation(Point initialLocation) {
    m_initialLocation = initialLocation;
  }

  @Override
  public int open() {
    if ((getShellStyle() & SWT.APPLICATION_MODAL) != 0) {
      //getEnvironment().interruptWaitingForSwt();
    }
    else {
      setBlockOnOpen(false);
    }
    initializeBounds();
    if (m_opened) {
      return super.open();
    }
    else {
      return CANCEL;
    }
  }

  @Override
  public boolean close() {
    m_opened = false;
    // ensure the current input is validated
    Control focusControl = getShell().getDisplay().getFocusControl();
    SwtUtility.runSwtInputVerifier(focusControl);
    Runnable job = new Runnable() {
      @Override
      public void run() {
        m_scoutForm.getUIFacade().fireFormClosingFromUI();
      }
    };
    m_environment.invokeScoutLater(job, 0);
    return false;
  }

  @Override
  protected Control createContents(Composite parent) {
    m_rootForm = getEnvironment().getFormToolkit().createForm(parent);
    m_contentPane = m_rootForm.getBody();
    GridLayout gridLayout = new GridLayout();
    gridLayout.horizontalSpacing = 0;
    gridLayout.marginHeight = 2;
    gridLayout.marginWidth = 2;
    gridLayout.verticalSpacing = 0;
    m_contentPane.setLayout(gridLayout);
    GridData d = new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL);
    m_rootForm.setLayoutData(d);
    return m_rootForm;
  }

  public ISwtEnvironment getEnvironment() {
    return m_environment;
  }

  // // scout part methods

  @Override
  public void activate() {
    super.getShell().setActive();
  }

  @Override
  public boolean isActive() {
    return getShell() == getEnvironment().getDisplay().getActiveShell();
  }

  @Override
  public boolean isVisible() {
    return getShell().isVisible();
  }

  @Override
  public void setStatusLineMessage(Image image, String message) {
    // void here
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
      setSaveNeededFromScout(((Boolean) newValue).booleanValue());
    }
  }

  protected DefaultValidateRoot createShellValidateRoot(Shell shell, ISwtEnvironment environment) {
    return new SwtShellValidateRoot(shell, environment);
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
      m_environment.invokeSwtLater(t);
    }
  }// end private class

}
