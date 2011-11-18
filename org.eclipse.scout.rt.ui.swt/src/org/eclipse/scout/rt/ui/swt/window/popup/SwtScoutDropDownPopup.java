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
import org.eclipse.scout.rt.ui.swt.DefaultValidateRoot;
import org.eclipse.scout.rt.ui.swt.ISwtEnvironment;
import org.eclipse.scout.rt.ui.swt.IValidateRoot;
import org.eclipse.scout.rt.ui.swt.window.ISwtScoutPart;
import org.eclipse.scout.rt.ui.swt.window.SwtScoutPartEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ScrollBar;

/**
 * Popup window bound to a component (ownerComponent). The popup closes when
 * there is either a click outside this window or the component loses focus
 * (focusComponent), or the component becomes invisible.
 */
public class SwtScoutDropDownPopup extends SwtScoutPopup {
  private static IScoutLogger LOG = ScoutLogManager.getLogger(SwtScoutDropDownPopup.class);

  private Composite m_focusComponent;
  private FocusListener m_focusComponentListener;
  private boolean m_nonFocusable;
  private P_ScrollBarListener m_scrollbarListener;

  public SwtScoutDropDownPopup(ISwtEnvironment env, Control ownerComponent, Composite focusComponent, int style) {
    super(env, ownerComponent, ownerComponent.getBounds(), style);
    m_focusComponent = focusComponent;

    getShell().setData(IValidateRoot.VALIDATE_ROOT_DATA, new DefaultValidateRoot(getShell()) {
      @Override
      public void validate() {
        autoAdjustBounds();
      }
    });
  }

  public void makeNonFocusable() {
    m_nonFocusable = true;
  }

  @Override
  protected void handleSwtWindowOpened() {
    // add listener to adjust location
    if (getOwnerComponent() != null && !getOwnerComponent().isDisposed()) {
      if (m_scrollbarListener == null) {
        m_scrollbarListener = new P_ScrollBarListener();
      }
      reqAddScrollbarListener(m_scrollbarListener, getOwnerComponent());
      getOwnerComponent().getShell().addListener(SWT.Move, m_scrollbarListener);
    }

    installFocusListener();
    //
    fireSwtScoutPartEvent(new SwtScoutPartEvent(SwtScoutDropDownPopup.this, SwtScoutPartEvent.TYPE_OPENED));
    fireSwtScoutPartEvent(new SwtScoutPartEvent(SwtScoutDropDownPopup.this, SwtScoutPartEvent.TYPE_ACTIVATED));
    if (m_nonFocusable) {
      if (getShell().getDisplay().getActiveShell() == getShell()) {
        m_focusComponent.setFocus();
      }
      if (!m_focusComponent.isFocusControl()) {
        closePart();
        fireSwtScoutPartEvent(new SwtScoutPartEvent(SwtScoutDropDownPopup.this, SwtScoutPartEvent.TYPE_CLOSED));
      }
    }
  }

  protected void installFocusListener() {
    if (m_focusComponentListener != null) {
      return;
    }
    if (m_focusComponent == null || m_focusComponent.isDisposed()) {
      return;
    }

    m_focusComponentListener = new FocusAdapter() {
      @Override
      public void focusLost(FocusEvent e) {
        // defer decision until it is known who is new focus owner
        getShell().getDisplay().asyncExec(new Runnable() {
          @Override
          public void run() {
            if (getShell().isDisposed()) {
              return;
            }
            if (getShell() == getShell().getDisplay().getActiveShell()) {
              Control c = getShell().getDisplay().getFocusControl();
              if (c != null && c != getShell() && c.getShell() == getShell()) {
                c.addMouseListener(new MouseAdapter() {
                  @Override
                  public void mouseUp(MouseEvent event) {
                    if (!((Control) event.getSource()).isDisposed()) {
                      ((Control) event.getSource()).removeMouseListener(this);
                    }
                    if (!m_focusComponent.isDisposed()) {
                      m_focusComponent.setFocus();
                    }
                  }
                });
              }
              else {
                m_focusComponent.setFocus();
              }
            }
            else {
              closePart();
              fireSwtScoutPartEvent(new SwtScoutPartEvent(SwtScoutDropDownPopup.this, SwtScoutPartEvent.TYPE_CLOSED));
            }
          }
        });
      }
    };
    m_focusComponent.addFocusListener(m_focusComponentListener);
  }

  protected void uninstallFocusLostListener() {
    if (m_focusComponent == null || m_focusComponentListener == null) {
      return;
    }
    if (!m_focusComponent.isDisposed()) {
      m_focusComponent.removeFocusListener(m_focusComponentListener);
    }
    m_focusComponentListener = null;
  }

  @Override
  protected void handleSwtWindowClosed() {
    if (getOwnerComponent() != null && !getOwnerComponent().isDisposed() && m_scrollbarListener != null) {
      reqRemoveScrollbarListener(m_scrollbarListener, getOwnerComponent());
      if (getOwnerComponent().getShell() != null && !getOwnerComponent().getShell().isDisposed()) {
        getOwnerComponent().getShell().removeListener(SWT.Move, m_scrollbarListener);
      }
    }
    m_scrollbarListener = null;
    //
    uninstallFocusLostListener();
    super.handleSwtWindowClosed();
  }

  /**
   * <h3>P_ScrollBarListener</h3> ensures the location and size of the popup in
   * case of resizing, moving.
   * 
   * @since 1.0.9 13.08.2008
   */
  private class P_ScrollBarListener implements Listener {
    @Override
    public void handleEvent(Event event) {
      closePart();
    }
  } // end private class

  private void reqRemoveScrollbarListener(Listener l, Control control) {
    if (!(control instanceof Composite)) {
      return;
    }
    Composite composite = (Composite) control;
    if (composite.getData(ISwtScoutPart.MARKER_SCOLLED_FORM) != null) {
      composite.removeListener(SWT.Resize, l);
      composite.removeListener(SWT.Move, l);

      ScrollBar hBar = composite.getHorizontalBar();
      if (hBar != null) {

        hBar.removeListener(SWT.Selection, l);
      }
      ScrollBar vBar = composite.getVerticalBar();
      if (vBar != null) {
        vBar.removeListener(SWT.Selection, l);
      }
    }
    reqRemoveScrollbarListener(l, composite.getParent());
  }

  private void reqAddScrollbarListener(Listener l, Control control) {
    if (!(control instanceof Composite)) {
      return;
    }
    Composite composite = (Composite) control;
    if (control.getData(ISwtScoutPart.MARKER_SCOLLED_FORM) != null) {
      control.addListener(SWT.Resize, l);
      control.addListener(SWT.Move, l);
      ScrollBar hBar = composite.getHorizontalBar();
      if (hBar != null) {

        hBar.addListener(SWT.Selection, l);
      }
      ScrollBar vBar = composite.getVerticalBar();
      if (vBar != null) {
        vBar.addListener(SWT.Selection, l);
      }

    }
    reqAddScrollbarListener(l, control.getParent());
  }
}
