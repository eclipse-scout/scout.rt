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
package org.eclipse.scout.rt.ui.rap.window.popup;

import java.util.EventListener;

import org.eclipse.scout.commons.EventListenerList;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.ui.rap.IRwtEnvironment;
import org.eclipse.scout.rt.ui.rap.core.form.IRwtScoutForm;
import org.eclipse.scout.rt.ui.rap.ext.table.TableEx;
import org.eclipse.scout.rt.ui.rap.ext.tree.TreeEx;
import org.eclipse.scout.rt.ui.rap.extension.UiDecorationExtensionPoint;
import org.eclipse.scout.rt.ui.rap.util.RwtUtility;
import org.eclipse.scout.rt.ui.rap.window.AbstractRwtScoutPart;
import org.eclipse.scout.rt.ui.rap.window.RwtScoutPartEvent;
import org.eclipse.scout.rt.ui.rap.window.RwtScoutPartListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.widgets.Form;

/**
 * Popup window bound to a component (ownerComponent). The popup closes when
 * there is either a click outside this window or the component loses focus
 * (focusComponent), or the component becomes invisible.
 */
public class RwtScoutPopup extends AbstractRwtScoutPart {
  private static IScoutLogger LOG = ScoutLogManager.getLogger(RwtScoutPopup.class);
  public static final String PROP_POPUP_OWNER = "propPopupOwner";

  private Control m_ownerComponent;
  private Rectangle m_ownerBounds;
  private Shell m_window;
  private Composite m_windowContentPane;
  private EventListenerList m_listenerList;
  private boolean m_positionBelowReferenceField;
  private boolean m_popupOnField;

  private int m_widthHint;
  private int m_heightHint;
  private int m_maxHeightHint;
  private int m_maxWidthHint;

  private IRwtScoutForm m_rwtScoutForm;

  public RwtScoutPopup() {
  }

  @Override
  public Form getUiForm() {
    return null;
  }

  public IRwtScoutForm getRwtScoutForm() {
    return m_rwtScoutForm;
  }

  public void createPart(IForm scoutForm, Control ownerComponent, Rectangle ownerBounds, int style, IRwtEnvironment uiEnvironment) {
    super.createPart(scoutForm, uiEnvironment);
    m_positionBelowReferenceField = true;
    m_ownerComponent = ownerComponent;
    m_ownerBounds = ownerBounds;
    m_listenerList = new EventListenerList();

    m_widthHint = SWT.DEFAULT;
    m_heightHint = SWT.DEFAULT;
    m_maxHeightHint = SWT.DEFAULT;
    m_maxWidthHint = SWT.DEFAULT;

    m_window = new Shell(ownerComponent.getShell(), style);
    m_window.setData("extendedStyle", SWT.POP_UP);
    m_window.setLayout(new FillLayout());
    //add close listener
    m_window.addDisposeListener(new P_RwtWindowDisposeListener());

    // content pane
    m_windowContentPane = getUiEnvironment().getFormToolkit().createComposite(m_window, SWT.NONE);
    m_windowContentPane.setLayout(new FillLayout());
    //
    m_rwtScoutForm = getUiEnvironment().createForm(m_windowContentPane, scoutForm);
    //
    attachScout();
  }

  public Shell getShell() {
    return m_window;
  }

  public Composite getUiContentPane() {
    return m_windowContentPane;
  }

  protected Control getOwnerComponent() {
    return m_ownerComponent;
  }

  public void setBounds(Rectangle bounds) {
    getShell().setBounds(bounds);
    getShell().layout(true, true);
  }

  public boolean isPopupOnField() {
    return m_popupOnField;
  }

  public void setPopupOnField(boolean popupOnField) {
    m_popupOnField = popupOnField;
  }

  public boolean isPopupBelow() {
    return m_positionBelowReferenceField;
  }

  public int getWidthHint() {
    return m_widthHint;
  }

  public void setWidthHint(int widthHint) {
    if (widthHint > 0) {
      m_widthHint = widthHint;
    }
    else {
      m_widthHint = SWT.DEFAULT;
    }
  }

  public int getHeightHint() {
    return m_heightHint;
  }

  public void setHeightHint(int heightHint) {
    if (heightHint > 0) {
      m_heightHint = heightHint;
    }
    else {
      m_heightHint = SWT.DEFAULT;
    }
  }

  public int getMaxHeightHint() {
    return m_maxHeightHint;
  }

  public void setMaxHeightHint(int maxHeightHint) {
    if (maxHeightHint > 0) {
      m_maxHeightHint = maxHeightHint;
    }
    else {
      m_maxHeightHint = SWT.DEFAULT;
    }
  }

  public int getMaxWidthHint() {
    return m_maxWidthHint;
  }

  public void setMaxWidthHint(int maxWidthHint) {
    if (maxWidthHint > 0) {
      m_maxWidthHint = maxWidthHint;
    }
    else {
      m_maxWidthHint = SWT.DEFAULT;
    }
  }

  @Override
  protected void showPartImpl() {
    handleUiWindowOpening();
    //open and activate, do NOT just call setVisible(true)
    autoAdjustBounds();
    m_window.open();
    handleUiWindowOpened();
  }

  @Override
  protected void closePartImpl() {
    detachScout();
    try {
      if (!m_window.isDisposed()) {
        m_window.setVisible(false);
        m_window.dispose();
      }
    }
    catch (Throwable t) {
      LOG.error("Failed closing popup " + getScoutObject(), t);
    }
  }

  public void autoAdjustBounds() {
    if (getShell().isDisposed()) {
      return;
    }
    if (m_ownerComponent.isDisposed()) {
      LOG.warn("Unexpected: Owner component of popup is disposed");
      return;
    }
    //invalidate all layouts
    Point dim = getShell().computeSize(0, m_heightHint, true);
    TableEx proposalTable = RwtUtility.findChildComponent(getShell(), TableEx.class);
    TreeEx proposalTree = RwtUtility.findChildComponent(getShell(), TreeEx.class);

    if (proposalTable != null) {
      dim = proposalTable.getSize();
    }
    else if (proposalTree != null) {
      dim = proposalTree.getSize();
    }

    // adjust width
    dim.x = Math.max(dim.x, UiDecorationExtensionPoint.getLookAndFeel().getLogicalGridLayoutDefaultPopupWidth());
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

    Rectangle aboveView = RwtUtility.intersectRectangleWithScreen(getShell().getDisplay(), new Rectangle(above.x, above.y - dim.y, dim.x, dim.y), false, false);
    Point below = new Point(p.x, p.y);
    if (!m_popupOnField) {
      below.y += m_ownerComponent.getBounds().height;
    }

    Rectangle belowView = RwtUtility.intersectRectangleWithScreen(getShell().getDisplay(), new Rectangle(below.x, below.y, dim.x, dim.y), false, false);
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

  public void addRwtScoutPartListener(RwtScoutPartListener listener) {
    m_listenerList.add(RwtScoutPartListener.class, listener);
  }

  public void removeRwtScoutPartListener(RwtScoutPartListener listener) {
    m_listenerList.remove(RwtScoutPartListener.class, listener);
  }

  protected void fireRwtScoutPartEvent(RwtScoutPartEvent e) {
    if (m_window != null) {
      EventListener[] listeners = m_listenerList.getListeners(RwtScoutPartListener.class);
      if (listeners != null && listeners.length > 0) {
        for (EventListener listener : listeners) {
          try {
            ((RwtScoutPartListener) listener).partChanged(e);
          }
          catch (Throwable t) {
            LOG.error("Unexpected:", t);
          }
        }
      }
    }
  }

  @Override
  public boolean isVisible() {
    return m_window != null && m_window.getVisible();
  }

  @Override
  public void activate() {
    m_window.getShell().setActive();
  }

  @Override
  public boolean isActive() {
    return m_window != null && m_window.getDisplay().getActiveShell() == m_window;
  }

  @Override
  public boolean setStatusLineMessage(Image image, String message) {
    // void
    return false;
  }

  protected void handleUiWindowOpening() {
    fireRwtScoutPartEvent(new RwtScoutPartEvent(RwtScoutPopup.this, RwtScoutPartEvent.TYPE_OPENING));
  }

  protected void handleUiWindowOpened() {
    fireRwtScoutPartEvent(new RwtScoutPartEvent(RwtScoutPopup.this, RwtScoutPartEvent.TYPE_OPENED));
    fireRwtScoutPartEvent(new RwtScoutPartEvent(RwtScoutPopup.this, RwtScoutPartEvent.TYPE_ACTIVATED));
  }

  protected void handleUiWindowClosed() {
    fireRwtScoutPartEvent(new RwtScoutPartEvent(RwtScoutPopup.this, RwtScoutPartEvent.TYPE_CLOSED));
    Runnable job = new Runnable() {
      @Override
      public void run() {
        getScoutObject().getUIFacade().fireFormKilledFromUI();
      }
    };
    getUiEnvironment().invokeScoutLater(job, 0);
  }

  private final class P_RwtWindowDisposeListener implements DisposeListener {
    private static final long serialVersionUID = 1L;

    @Override
    public void widgetDisposed(DisposeEvent e) {
      handleUiWindowClosed();
    }
  }// end private class
}
