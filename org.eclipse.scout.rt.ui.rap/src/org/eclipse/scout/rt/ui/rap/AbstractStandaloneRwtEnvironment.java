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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.AccessController;
import java.util.List;
import java.util.TreeMap;

import javax.security.auth.Subject;

import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.service.ServerPushSession;
import org.eclipse.scout.commons.CompositeLong;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.servicetunnel.http.IClientServiceTunnel;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.ui.rap.util.RwtUtility;
import org.eclipse.scout.rt.ui.rap.window.IRwtScoutPart;
import org.eclipse.scout.rt.ui.rap.window.desktop.RwtScoutDesktop;
import org.eclipse.scout.rt.ui.rap.window.desktop.nonmodalFormBar.RwtScoutFormButtonBar;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.osgi.framework.Bundle;

public abstract class AbstractStandaloneRwtEnvironment extends AbstractRwtEnvironment implements IRwtStandaloneEnvironment {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractStandaloneRwtEnvironment.class);

  private Display m_display;
  private RwtScoutDesktop m_uiDesktop;
  private RwtScoutFormButtonBar m_uiButtonArea;
  private ServerPushSession m_clientNotificationPushSession;

  public AbstractStandaloneRwtEnvironment(Bundle applicationBundle, Class<? extends IClientSession> clientSessionClazz) {
    super(applicationBundle, clientSessionClazz);
  }

  @Override
  public int createUI() {
    // Ensure the UI initialization request to be authenticated.
    if (getSubject() == null) {
      Subject subject = Subject.getSubject(AccessController.getContext());
      if (subject == null) {
        throw new SecurityException("/rap request is not authenticated with a Subject");
      }
      setSubject(subject);
    }

    return super.createUI();
  }

  @Override
  protected void createContents(Composite parent) {
    m_display = Display.getCurrent();
    m_display.setData(IRwtEnvironment.class.getName(), this);

    m_uiDesktop = createApplicationContent(parent);
    m_uiButtonArea = createNonmodalFormButtonArea(parent);

    // layout
    GridLayoutFactory.fillDefaults().spacing(0, 0).applyTo(parent);
    GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(m_uiDesktop.getUiContainer());
    GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).grab(true, false).exclude(true).applyTo(m_uiButtonArea.getUiContainer());

    if (needsClientNotificationServerPushSession()) {
      // Necessary for client notifications.
      m_clientNotificationPushSession = new ServerPushSession();
      m_clientNotificationPushSession.start();
    }
  }

  /**
   * Creates the controls that constitute the desktop for this application.
   *
   * @param parent
   *          the parent composite to contain the application content.
   * @return the {@link RwtScoutDesktop} that contains the desktop with a UI container which is to be laid out
   *         within the given parent composite.
   */
  protected RwtScoutDesktop createApplicationContent(final Composite parent) {
    final RwtScoutDesktop uiDesktop = createUiDesktop();
    ensureInitialized(new Runnable() {
      @Override
      public void run() {
        uiDesktop.createUiField(parent, getScoutDesktop(), AbstractStandaloneRwtEnvironment.this);
      }
    });
    if (!isInitialized()) {
      throw new SecurityException("Cannot initialize application");
    }
    getKeyStrokeManager().setGlobalKeyStrokesActivated(true);

    return uiDesktop;
  }

  /**
   * Creates the composite to keep track of non-modal forms. Whenever a non-modal form is shown, a corresponding
   * button is shown in this button bar to restore the form if being in minimized state. This button bar is visible once
   * a minimizable form is started.
   *
   * @param parent
   *          the parent composite to contain the button bar.
   * @return the {@link RwtScoutFormButtonBar} that contains the form button with a UI container which is to be laid out
   *         within the given parent composite.
   */
  protected RwtScoutFormButtonBar createNonmodalFormButtonArea(Composite parent) {
    RwtScoutFormButtonBar buttonBar = new RwtScoutFormButtonBar();
    buttonBar.createUiField(parent, m_uiDesktop.getScoutObject(), this);

    return buttonBar;
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
    // Sanity check for proper display if the calling thread is a user-interface thread.
    Display currentDisplay = Display.getCurrent();
    if (currentDisplay != null && m_display != currentDisplay) {
      LOG.error("WRONG DISPLAY: the calling user-interface thread does not belong to this environment [display_environment={0}, display_callingThread={1}, callingThread={2}]",
          new Object[]{m_display, currentDisplay, Thread.currentThread()});
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
    Display display = getDisplay();

    Shell shell = display.getActiveShell();
    if (shell != null) {
      while (RwtUtility.isPopupShell(shell) && shell.getParent() instanceof Shell) {
        shell = (Shell) shell.getParent();
      }
    }
    // traverse available shells
    if (shell == null) {
      TreeMap<CompositeLong, Shell> map = new TreeMap<CompositeLong, Shell>();
      for (Shell s : display.getShells()) {
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
      shell = getShell(); // main shell for this entrypoint.
    }

    return shell;
  }

  @Override
  protected void contributePatches(List<URL> patches) {
    super.contributePatches(patches);

    // Install patch to mark editable cells with a visual marker.
    registerEditableCellMarkerIcon();
    patches.add(Activator.class.getResource("/resources/patches/EditableCellMarkerPatch.js"));
  }

  protected void registerEditableCellMarkerIcon() {
    String icon = "editable_tablecell_marker.png";
    if (!RWT.getResourceManager().isRegistered(icon)) {
      InputStream is = Activator.class.getResourceAsStream(String.format("/resources/icons/internal/%s", icon));
      try {
        RWT.getResourceManager().register(icon, is);
      }
      finally {
        try {
          is.close();
        }
        catch (IOException e) {
          LOG.warn("Failed to close InputStream for editable cell marker.", e);
        }
      }
    }
  }
}
