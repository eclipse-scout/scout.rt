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
package org.eclipse.scout.rt.ui.swt.window.popup;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.ui.swt.ISwtEnvironment;
import org.eclipse.scout.rt.ui.swt.extension.UiDecorationExtensionPoint;
import org.eclipse.scout.rt.ui.swt.form.ISwtScoutForm;
import org.eclipse.scout.rt.ui.swt.util.SwtUtility;
import org.eclipse.scout.rt.ui.swt.window.ISwtScoutPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.widgets.Form;

/**
 * Popup window bound to a {@link Control}.
 */
public class SwtScoutPopup implements ISwtScoutPart {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(SwtScoutPopup.class);

  public static final String PROP_POPUP_OWNER = "propPopupOwner";

  private ISwtEnvironment m_env;
  private Control m_ownerComponent;
  private Shell m_shell;
  private Composite m_shellContentPane;
  private IForm m_scoutForm;
  private boolean m_positionBelowReferenceField;
  private boolean m_popupOnField;

  private int m_widthHint;
  private int m_heightHint;
  private int m_maxHeightHint;
  private int m_maxWidthHint;

  private ISwtScoutForm m_uiForm;

  /**
   * Flag indicating whether focus should be taken when the dialog is opened.
   */
  private boolean m_takeFocusOnOpen = false;

  /**
   * @param env
   *          {@link ISwtEnvironment}.
   * @param ownerComponent
   *          the {@link Control} the popup is bound to.
   * @param takeFocusOnOpen
   *          A boolean indicating whether focus should be taken by this popup when it opens.
   * @param style
   *          the style of control to construct
   */
  public SwtScoutPopup(ISwtEnvironment env, Control ownerComponent, boolean takeFocusOnOpen, int style) {
    m_env = env;
    m_takeFocusOnOpen = takeFocusOnOpen;

    // ensure the popup to be in front if not taking the focus when it opens. (SWT.ON_TOP is required for Mac OS X).
    if (!m_takeFocusOnOpen) {
      style |= SWT.ON_TOP | SWT.NO_FOCUS;
    }

    m_positionBelowReferenceField = true;
    m_ownerComponent = ownerComponent;

    m_widthHint = SWT.DEFAULT;
    m_heightHint = SWT.DEFAULT;
    m_maxHeightHint = SWT.DEFAULT;
    m_maxWidthHint = SWT.DEFAULT;

    m_shell = new Shell(ownerComponent.getShell(), style);
    m_shell.setData("extendedStyle", SWT.POP_UP);
    m_shell.setLayout(new FillLayout());

    // content pane
    m_shellContentPane = env.getFormToolkit().createComposite(m_shell, SWT.NONE);
    m_shellContentPane.setLayout(new FillLayout());
  }

  @Override
  public void setBusy(boolean b) {
    // NOOP
  }

  public Shell getShell() {
    return m_shell;
  }

  public void setBounds(Rectangle bounds) {
    getShell().setBounds(bounds);
    getShell().layout(true, true);
  }

  public void setPopupOnField(boolean popupOnField) {
    m_popupOnField = popupOnField;
  }

  public void setWidthHint(int widthHint) {
    if (widthHint > 0) {
      m_widthHint = widthHint;
    }
    else {
      m_widthHint = SWT.DEFAULT;
    }
  }

  public void setHeightHint(int heightHint) {
    if (heightHint > 0) {
      m_heightHint = heightHint;
    }
    else {
      m_heightHint = SWT.DEFAULT;
    }
  }

  public void setMaxHeightHint(int maxHeightHint) {
    if (maxHeightHint > 0) {
      m_maxHeightHint = maxHeightHint;
    }
    else {
      m_maxHeightHint = SWT.DEFAULT;
    }
  }

  public void setMaxWidthHint(int maxWidthHint) {
    if (maxWidthHint > 0) {
      m_maxWidthHint = maxWidthHint;
    }
    else {
      m_maxWidthHint = SWT.DEFAULT;
    }
  }

  /**
   * Opens the popup with the given {@link IForm}.
   */
  public void showForm(IForm scoutForm) {
    if (m_scoutForm != null) {
      throw new IllegalStateException("The popup is already opened.");
    }

    m_scoutForm = scoutForm;
    m_uiForm = m_env.createForm(m_shellContentPane, scoutForm);
    autoAdjustBounds();

    // open the window
    if (m_takeFocusOnOpen) {
      m_shell.open(); // open the popup, mark it visible, make it the focus owner and ask the window manager to make it the shell active.

    }
    else {
      m_shell.setVisible(true); // open the popup without making it the active shell and the focus owner.
    }

    autoAdjustBounds();
    onPopupOpened();
  }

  @Override
  public void closePart() {
    if (!m_shell.isDisposed()) {
      m_shell.dispose(); // directly dispose the Shell to not make it the focus owner while closing.
      onPopupClosed();
    }
  }

  @Override
  public IForm getForm() {
    return m_scoutForm;
  }

  @Override
  public Form getSwtForm() {
    return null;
  }

  @Override
  public ISwtScoutForm getUiForm() {
    return m_uiForm;
  }

  public void autoAdjustBounds() {
    if (getShell().isDisposed()) {
      return;
    }
    if (m_ownerComponent.isDisposed()) {
      LOG.warn("Failed to adjust popup bounds because owner component is disposed");
      return;
    }
    //invalidate all layouts
    Point dim = getShell().computeSize(m_widthHint, m_heightHint, true);

    // adjust width
    dim.x = Math.max(dim.x, UiDecorationExtensionPoint.getLookAndFeel().getLogicalGridLayoutDefaultColumnWidth());
    if (m_maxWidthHint != SWT.DEFAULT) {
      dim.x = Math.min(dim.x, m_maxWidthHint);
    }
    // adjust height
    if (m_maxHeightHint != SWT.DEFAULT) {
      dim.y = Math.min(dim.y, m_maxHeightHint);
    }

    Point p = m_ownerComponent.toDisplay(new Point(-m_ownerComponent.getBorderWidth(), 0));
    Point above = new Point(p.x, p.y);
    if (m_popupOnField) {
      above.y += m_ownerComponent.getBounds().height;
    }

    Rectangle aboveView = SwtUtility.intersectRectangleWithScreen(getShell().getDisplay(), new Rectangle(above.x, above.y - dim.y, dim.x, dim.y), false, false);
    Point below = new Point(p.x, p.y);
    if (!m_popupOnField) {
      below.y += m_ownerComponent.getBounds().height;
    }

    Rectangle belowView = SwtUtility.intersectRectangleWithScreen(getShell().getDisplay(), new Rectangle(below.x, below.y, dim.x, dim.y), false, false);
    // decide based on the preference positionBelowReferenceField
    Rectangle currentView = m_positionBelowReferenceField ? belowView : aboveView;
    Rectangle alternateView = m_positionBelowReferenceField ? aboveView : belowView;
    if (currentView.height >= alternateView.height) {
      getShell().setBounds(currentView);
    }
    else {
      getShell().setBounds(alternateView);
      // toggle preference
      m_positionBelowReferenceField = !m_positionBelowReferenceField;
    }
  }

  public Composite getShellContentPane() {
    return m_shellContentPane;
  }

  public void addShellListener(ShellListener listener) {
    m_shell.addShellListener(listener);
  }

  public void removeShellListener(ShellListener listener) {
    m_shell.removeShellListener(listener);
  }

  @Override
  public boolean isVisible() {
    return !m_shell.isDisposed() && m_shell.getVisible();
  }

  @Override
  public void activate() {
    if (!m_shell.isDisposed()) {
      m_shell.setActive();
    }
  }

  @Override
  public boolean isActive() {
    return m_shell.isDisposed() && m_shell.getDisplay().getActiveShell() == m_shell;
  }

  @Override
  public void setStatusLineMessage(Image image, String message) {
    // NOOP
  }

  /**
   * Called after the popup was opened.
   */
  protected void onPopupOpened() {
  }

  /**
   * Called after the popup was closed.
   */
  protected void onPopupClosed() {
    if (m_scoutForm != null) {
      m_env.invokeScoutLater(new Runnable() {
        @Override
        public void run() {
          m_scoutForm.getUIFacade().fireFormKilledFromUI();
        }
      }, 0);
    }
  }

  protected Control getOwnerComponent() {
    return m_ownerComponent;
  }
}
