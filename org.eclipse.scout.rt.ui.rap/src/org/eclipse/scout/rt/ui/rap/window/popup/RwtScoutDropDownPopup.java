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
package org.eclipse.scout.rt.ui.rap.window.popup;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.ui.rap.DefaultValidateRoot;
import org.eclipse.scout.rt.ui.rap.IRwtEnvironment;
import org.eclipse.scout.rt.ui.rap.IValidateRoot;
import org.eclipse.scout.rt.ui.rap.window.RwtScoutPartEvent;
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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.widgets.SharedScrolledComposite;

/**
 * Popup window bound to a component (ownerComponent). The popup closes when
 * there is either a click outside this window or the component loses focus
 * (focusComponent), or the component becomes invisible.
 */
public class RwtScoutDropDownPopup extends RwtScoutPopup {
  private static IScoutLogger LOG = ScoutLogManager.getLogger(RwtScoutDropDownPopup.class);

  private Control m_focusComponent;
  private FocusListener m_focusComponentListener;
  private boolean m_nonFocusable;
  private P_ScrollBarListener m_scrollbarListener;

  public RwtScoutDropDownPopup() {
  }

  public void createPart(IForm scoutForm, Control ownerComponent, Control focusComponent, int style, IRwtEnvironment uiEnvironment) {
    super.createPart(scoutForm, ownerComponent, ownerComponent.getBounds(), style, uiEnvironment);
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
  protected void handleUiWindowOpened() {
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
    super.handleUiWindowOpened();
    if (m_nonFocusable) {
      Shell shell = getShell();
      Shell activeShell = shell.getDisplay().getActiveShell();
      if (activeShell == shell) {
        m_focusComponent.setFocus();
      }
      if (!m_focusComponent.isFocusControl()) {
        closePart();
        fireRwtScoutPartEvent(new RwtScoutPartEvent(RwtScoutDropDownPopup.this, RwtScoutPartEvent.TYPE_CLOSED));
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
      private static final long serialVersionUID = 1L;

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
                  private static final long serialVersionUID = 1L;

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
              fireRwtScoutPartEvent(new RwtScoutPartEvent(RwtScoutDropDownPopup.this, RwtScoutPartEvent.TYPE_CLOSED));
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
  protected void handleUiWindowClosed() {
    if (getOwnerComponent() != null && !getOwnerComponent().isDisposed() && m_scrollbarListener != null) {
      reqRemoveScrollbarListener(m_scrollbarListener, getOwnerComponent());
      if (getOwnerComponent().getShell() != null && !getOwnerComponent().getShell().isDisposed()) {
        getOwnerComponent().getShell().removeListener(SWT.Move, m_scrollbarListener);
      }
    }
    m_scrollbarListener = null;
    //
    uninstallFocusLostListener();
    super.handleUiWindowClosed();
  }

  /**
   * <h3>P_ScrollBarListener</h3> ensures the location and size of the popup in
   * case of resizing, moving.
   * 
   * @since 1.0.9 13.08.2008
   */
  private class P_ScrollBarListener implements Listener {
    private static final long serialVersionUID = 1L;

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
    if (composite instanceof SharedScrolledComposite) {
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
    if (composite instanceof SharedScrolledComposite) {
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
