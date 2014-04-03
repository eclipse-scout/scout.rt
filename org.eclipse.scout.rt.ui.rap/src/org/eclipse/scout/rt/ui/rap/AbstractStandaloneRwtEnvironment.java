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
package org.eclipse.scout.rt.ui.rap;

import java.lang.reflect.Field;
import java.security.AccessController;
import java.util.TreeMap;

import javax.security.auth.Subject;

import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.rap.rwt.service.ServerPushSession;
import org.eclipse.scout.commons.CompositeLong;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.servicetunnel.http.IClientServiceTunnel;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.shared.services.common.exceptionhandler.IExceptionHandlerService;
import org.eclipse.scout.rt.ui.rap.util.RwtUtility;
import org.eclipse.scout.rt.ui.rap.window.IRwtScoutPart;
import org.eclipse.scout.rt.ui.rap.window.desktop.RwtScoutDesktop;
import org.eclipse.scout.rt.ui.rap.window.desktop.nonmodalFormBar.RwtScoutFormButtonBar;
import org.eclipse.scout.service.SERVICES;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.Bundle;

public abstract class AbstractStandaloneRwtEnvironment extends AbstractRwtEnvironment implements IRwtStandaloneEnvironment {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractStandaloneRwtEnvironment.class);

  private Display m_display;
  private Shell m_rootShell;
  private RwtScoutDesktop m_uiDesktop;
  private RwtScoutFormButtonBar m_uiButtonArea;
  private ServerPushSession m_clientNotificationPushSession;

  public AbstractStandaloneRwtEnvironment(Bundle applicationBundle, Class<? extends IClientSession> clientSessionClazz) {
    super(applicationBundle, clientSessionClazz);
  }

  @Override
  public int createUI() {
    if (getSubject() == null) {
      Subject subject = Subject.getSubject(AccessController.getContext());
      if (subject == null) {
        throw new SecurityException("/rap request is not authenticated with a Subject");
      }
      setSubject(subject);
    }
    m_display = Display.getDefault();
    if (m_display == null) {
      m_display = new Display();
    }
    m_display.setData(IRwtEnvironment.class.getName(), this);

    //XXX Workaround for rwt npe
    try {
      final Object wb = PlatformUI.getWorkbench();
      final Field f = wb.getClass().getDeclaredField("display");
      f.setAccessible(true);
      f.set(wb, m_display);
      m_display.addListener(SWT.Dispose, new Listener() {
        private static final long serialVersionUID = 1L;

        @Override
        public void handleEvent(Event event) {
          try {
            // WORKAROUND for memory leaks (display reference in workbench still exists after dispose)
            // workbench should be closed instead, but NPEs are thrown
            f.set(wb, null);
          }
          catch (Throwable t1) {
            // nop
          }
        }
      });
    }
    catch (Throwable t) {
      //nop
    }
    //XXX end Workaround for rwt npe

    Shell shell = new Shell(m_display, SWT.NO_TRIM);
    m_rootShell = shell;
    createApplicationContent(shell);
    createNonmodalFormButtonArea(shell);
    //layout
    GridLayout shellLayout = new GridLayout(1, true);
    shellLayout.horizontalSpacing = 0;
    shellLayout.marginHeight = 0;
    shellLayout.marginWidth = 0;
    shellLayout.verticalSpacing = 0;
    shell.setLayout(shellLayout);
    GridData desktopLayoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
    m_uiDesktop.getUiContainer().setLayoutData(desktopLayoutData);

    GridData nonmodalFormsLayoutData = new GridData(SWT.FILL, SWT.FILL, true, false);
    nonmodalFormsLayoutData.exclude = true;
    m_uiButtonArea.getUiContainer().setLayoutData(nonmodalFormsLayoutData);

    shell.setMaximized(true);
    shell.open();
    shell.layout(true, true);

    if (needsClientNotificationServerPushSession()) {
      // Necessary for client notifications.
      m_clientNotificationPushSession = new ServerPushSession();
      m_clientNotificationPushSession.start();
    }

    while (!shell.isDisposed()) {
      try {
        if (!m_display.readAndDispatch()) {
          m_display.sleep();
        }
      }
      //Catch only exception instead of throwable to allow proper uithread shutdown (see UIThread.UIThreadTerminationError
      catch (Exception e) {
        handleEventLoopException(e);
      }
    }
    return 0;
  }

  protected void handleEventLoopException(final Exception e) {
    invokeScoutLater(new Runnable() {

      @Override
      public void run() {
        ProcessingException p = new ProcessingException("", e);
        SERVICES.getService(IExceptionHandlerService.class).handleException(p);
      }

    }, 0);
  }

  protected void createApplicationContent(final Composite parent) {
    m_uiDesktop = createUiDesktop();
    ensureInitialized(new Runnable() {
      @Override
      public void run() {
        m_uiDesktop.createUiField(parent, getScoutDesktop(), AbstractStandaloneRwtEnvironment.this);
      }
    });
    if (!isInitialized()) {
      throw new SecurityException("Cannot initialize application");
    }
    getKeyStrokeManager().setGlobalKeyStrokesActivated(true);
  }

  protected void createNonmodalFormButtonArea(Composite parent) {
    m_uiButtonArea = new RwtScoutFormButtonBar();
    m_uiButtonArea.createUiField(parent, m_uiDesktop.getScoutObject(), this);
  }

  protected RwtScoutDesktop createUiDesktop() {
    return new RwtScoutDesktop();
  }

  /**
   * @return <code>boolean</code> indicating if a permanent {@link ServerPushSession} for client notifications should be
   *         established.
   */
  protected boolean needsClientNotificationServerPushSession() {
    IClientServiceTunnel serviceTunnel = getClientSession().getServiceTunnel();
    if (serviceTunnel != null) {
      // necessary if client notification polling is enabled
      return serviceTunnel.getClientNotificationPollInterval() > -1;
    }
    return false;
  }

  @Override
  public RwtScoutDesktop getUiDesktop() {
    return m_uiDesktop;
  }

  @Override
  public Display getDisplay() {
    Display current = null;
    try {
      current = Display.getCurrent();
    }
    catch (Exception e) {
      // NOP
    }
    if (current != null && m_display != current) {
      ScoutLogManager.getLogger(AbstractStandaloneRwtEnvironment.class).error(
          "Different Display.\n" +
              "m_display: {0}\n" +
              "cur_displ: {1}",
          new Object[]{m_display, current});
    }
    Display defdisp = null;
    try {
      defdisp = Display.getDefault();
    }
    catch (Exception e) {
      // NOP
    }
    if (defdisp != null && m_display != defdisp) {
      ScoutLogManager.getLogger(AbstractStandaloneRwtEnvironment.class).error(
          "Different Display.\n" +
              "m_display: {0}\n" +
              "defdisp  : {1}",
          new Object[]{m_display, defdisp});
    }
    return m_display;
  }

  @Override
  public void showFormPart(IForm form) {
    if (form == null) {
      return;
    }
    if (form.getDisplayHint() == IForm.DISPLAY_HINT_VIEW) {
      IRwtScoutPart part = m_uiDesktop.addForm(form);
      if (part != null) {
        putPart(form, part);
        part.showPart();
      }
      else {
        LOG.error("Form '" + form.getFormId() + "' cannot be displayed because no corresponding UI part could be found.");
      }
    }
    super.showFormPart(form);

    if (form.getDisplayHint() == IForm.DISPLAY_HINT_DIALOG && !form.isModal()) {
      int buttonCount = m_uiButtonArea.getFormButtonBarCount();

      m_uiButtonArea.addFormButton(form);
      if (buttonCount != m_uiButtonArea.getFormButtonBarCount()) {
        m_uiButtonArea.getUiContainer().setVisible(true);
        ((GridData) m_uiButtonArea.getUiContainer().getLayoutData()).exclude = false;
        m_uiButtonArea.getUiContainer().getParent().layout(true, true);
      }
    }
  }

  @Override
  public void hideFormPart(IForm form) {
    super.hideFormPart(form);

    if (form.getDisplayHint() == IForm.DISPLAY_HINT_DIALOG && !form.isModal()) {
      m_uiButtonArea.removeFormButton(form);
      if (m_uiButtonArea.getFormButtonBarCount() == 0) {
        m_uiButtonArea.getUiContainer().setVisible(false);
        ((GridData) m_uiButtonArea.getUiContainer().getLayoutData()).exclude = true;
        m_uiButtonArea.getUiContainer().getParent().layout(true, true);
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Shell getParentShellIgnoringPopups(int modalities) {
    Shell shell = Display.getCurrent().getActiveShell();
    if (shell != null) {
      while (RwtUtility.isPopupShell(shell) && shell.getParent() instanceof Shell) {
        shell = (Shell) shell.getParent();
      }
    }
    // traverse available shells
    if (shell == null) {
      TreeMap<CompositeLong, Shell> map = new TreeMap<CompositeLong, Shell>();
      for (Shell s : Display.getCurrent().getShells()) {
        RwtUtility.visitShellTreeRec(s, modalities, 0, map);
      }
      if (map.size() > 0) {
        shell = map.get(map.firstKey());
      }
    }

    if (shell != null && shell.getData() instanceof ProgressMonitorDialog) {
      // do also ignore the ProgressMonitorDialog, otherwise there will be some strange behaviors
      // when displaying a shell on top of the ProgressMonitorDialog-shell (f.e. when the
      // ProgressMonitorDialog-shell disappears)
      shell = (Shell) shell.getParent();
    }

    if (shell == null) {
      shell = m_rootShell;
    }

    return shell;
  }
}
