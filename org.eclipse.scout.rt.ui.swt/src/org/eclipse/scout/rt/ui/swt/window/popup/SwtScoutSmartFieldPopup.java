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

import java.util.EventListener;

import org.eclipse.scout.commons.EventListenerList;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.ui.swt.DefaultValidateRoot;
import org.eclipse.scout.rt.ui.swt.ISwtEnvironment;
import org.eclipse.scout.rt.ui.swt.IValidateRoot;
import org.eclipse.scout.rt.ui.swt.extension.UiDecorationExtensionPoint;
import org.eclipse.scout.rt.ui.swt.form.SwtScoutForm;
import org.eclipse.scout.rt.ui.swt.util.SwtUtility;
import org.eclipse.scout.rt.ui.swt.window.ISwtScoutPart;
import org.eclipse.scout.rt.ui.swt.window.SwtScoutPartEvent;
import org.eclipse.scout.rt.ui.swt.window.SwtScoutPartListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Shell;

/**
 * Popup window bound to a component (ownerComponent). The popup closes when
 * there is either a click outside this window or the component loses focus
 * (focusComponent), or the component becomes invisible.
 */
public class SwtScoutSmartFieldPopup implements ISwtScoutPart {
  private static IScoutLogger LOG = ScoutLogManager.getLogger(SwtScoutSmartFieldPopup.class);

  private ISwtEnvironment m_env;
  private EventListenerList m_listenerList;
  private Composite m_ownerComponent;
  private Composite m_focusComponent;
  private FocusListener m_focusComponentListener;
  private Shell m_swtWindow;
  private Composite m_swtWindowContentPane;
  private boolean m_nonFocusable;
  private P_ScrollBarListener m_scrollbarListener;
  private IForm m_scoutForm;
  private boolean m_positionBelowReferenceField;
  private boolean m_opened;

  public SwtScoutSmartFieldPopup(ISwtEnvironment env, Composite ownerComponent, Composite focusComponent) {
    m_env = env;
    m_ownerComponent = ownerComponent;
    m_positionBelowReferenceField = true;
    m_focusComponent = focusComponent;
    m_listenerList = new EventListenerList();
    //
    m_swtWindow = new Shell(ownerComponent.getShell(), SWT.RESIZE);
    m_swtWindow.setData("extendedStyle", SWT.POP_UP);
    m_swtWindow.setData(IValidateRoot.VALIDATE_ROOT_DATA, new DefaultValidateRoot(m_swtWindow) {
      @Override
      public void validate() {
        autoAdjustBounds();
      }
    });
    m_swtWindow.setLayout(new FillLayout());
    m_swtWindow.addDisposeListener(new P_SwtWindowDisposeListener());
    // content pane
    m_swtWindowContentPane = env.getFormToolkit().createComposite(m_swtWindow, SWT.NONE);
    m_swtWindowContentPane.setLayout(new FillLayout());

  }

  public Shell getShell() {
    return m_swtWindow;
  }

  public void showForm(IForm scoutForm) throws ProcessingException {
    m_opened = true;
    if (m_scoutForm == null) {
      m_scoutForm = scoutForm;
      SwtScoutForm swtForm = new SwtScoutForm();
      swtForm.createField(getSwtContentPane(), scoutForm, m_env);
      autoAdjustBounds();
      if (m_opened) {
        m_swtWindow.setVisible(true);
        autoAdjustBounds();
        if (m_opened) {
          handleSwtWindowOpened();
        }
      }
    }
    else {
      throw new ProcessingException("The popup is already open. The form '" + scoutForm.getTitle() + " (" + scoutForm.getClass().getName() + ")' can not be opened!");
    }

  }

  @Override
  public void closePart() throws ProcessingException {
    m_opened = false;
    if (!m_swtWindow.isDisposed()) {
      m_swtWindow.setVisible(false);
      m_swtWindow.dispose();
    }
  }

  @Override
  public IForm getForm() {
    return m_scoutForm;
  }

  private void reqRemoveScrollbarListener(Listener l, Composite comp) {

    if (comp == null) {
      return;
    }
    else if (comp.getData(ISwtScoutPart.MARKER_SCOLLED_FORM) != null) {
      comp.removeListener(SWT.Resize, l);
      comp.removeListener(SWT.Move, l);

      ScrollBar hBar = comp.getHorizontalBar();
      if (hBar != null) {

        hBar.removeListener(SWT.Selection, l);
      }
      ScrollBar vBar = comp.getVerticalBar();
      if (vBar != null) {
        vBar.removeListener(SWT.Selection, l);
      }
    }
    reqRemoveScrollbarListener(l, comp.getParent());
  }

  private void reqAddScrollbarListener(Listener l, Composite comp) {
    if (comp == null) {
      return;
    }
    else if (comp.getData(ISwtScoutPart.MARKER_SCOLLED_FORM) != null) {
      comp.addListener(SWT.Resize, l);
      comp.addListener(SWT.Move, l);
      ScrollBar hBar = comp.getHorizontalBar();
      if (hBar != null) {

        hBar.addListener(SWT.Selection, l);
      }
      ScrollBar vBar = comp.getVerticalBar();
      if (vBar != null) {
        vBar.addListener(SWT.Selection, l);
      }

    }
    reqAddScrollbarListener(l, comp.getParent());
  }

  public void makeNonFocusable() {
    m_nonFocusable = true;
  }

  public void setBounds(Rectangle bounds) {
    getShell().setBounds(bounds);
    getShell().layout(true, true);
  }

  public void autoAdjustBounds() {
    if (!getShell().isDisposed()) {
      //invalidate all layouts
      getShell().layout(true, true);
      Point d = getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
      d.x = Math.max(d.x, UiDecorationExtensionPoint.getLookAndFeel().getLogicalGridLayoutDefaultColumnWidth());
      d.y = Math.min(d.y, 280);
      d.y = Math.max(UiDecorationExtensionPoint.getLookAndFeel().getLogicalGridLayoutRowHeight(), d.y);
      Point p = m_ownerComponent.toDisplay(new Point(-m_ownerComponent.getBorderWidth(), 0));
      Point above = new Point(p.x, p.y);
      Rectangle aboveView = SwtUtility.intersectRectangleWithScreen(getShell().getDisplay(), new Rectangle(above.x, above.y - d.y, d.x, d.y), false, false);
      Point below = new Point(p.x, p.y + m_ownerComponent.getBounds().height);
      Rectangle belowView = SwtUtility.intersectRectangleWithScreen(getShell().getDisplay(), new Rectangle(below.x, below.y, d.x, d.y), false, false);
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
  }

  public Composite getSwtContentPane() {
    return m_swtWindowContentPane;
  }

  public void addSwtScoutPartListener(SwtScoutPartListener listener) {
    m_listenerList.add(SwtScoutPartListener.class, listener);
  }

  public void removeSwtScoutPartListener(SwtScoutPartListener listener) {
    m_listenerList.remove(SwtScoutPartListener.class, listener);
  }

  private void fireSwtScoutPartEvent(SwtScoutPartEvent e) {
    if (m_swtWindow != null) {
      EventListener[] listeners = m_listenerList.getListeners(SwtScoutPartListener.class);
      if (listeners != null && listeners.length > 0) {
        for (EventListener listener : listeners) {
          try {
            ((SwtScoutPartListener) listener).partChanged(e);
          }
          catch (Throwable t) {
            t.printStackTrace();
          }
        }
      }
    }
  }

  @Override
  public boolean isVisible() {
    return m_swtWindow != null && m_swtWindow.getVisible();

  }

  @Override
  public void activate() {
    m_swtWindow.getShell().setActive();
  }

  @Override
  public boolean isActive() {
    return m_swtWindow != null && m_swtWindow.getDisplay().getActiveShell() == m_swtWindow;
  }

  @Override
  public void setStatusLineMessage(Image image, String message) {
    // void
  }

  private void handleSwtWindowOpened() {
    // add listener to adjust location
    if (m_ownerComponent != null && !m_ownerComponent.isDisposed()) {
      if (m_scrollbarListener == null) {
        m_scrollbarListener = new P_ScrollBarListener();
      }
      reqAddScrollbarListener(m_scrollbarListener, m_ownerComponent);
      m_ownerComponent.getShell().addListener(SWT.Move, m_scrollbarListener);
    }
    // add listener to track focus
    if (m_focusComponent != null) {
      if (m_focusComponentListener == null) {
        m_focusComponentListener = new FocusAdapter() {
          @Override
          public void focusLost(FocusEvent e) {
            // defer decision until it is known who is new focus owner
            m_swtWindow.getDisplay().asyncExec(new Runnable() {
              @Override
              public void run() {
                if (!m_swtWindow.isDisposed()) {
                  if (m_swtWindow == m_swtWindow.getDisplay().getActiveShell()) {
                    Control c = m_swtWindow.getDisplay().getFocusControl();
                    if (c != null && c != m_swtWindow && c.getShell() == m_swtWindow) {
                      c.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseUp(MouseEvent event) {
                          if (!((Control) event.getSource()).isDisposed()) {
                            ((Control) event.getSource()).removeMouseListener(this);
                          }
                          // focus back to smartfield
                          if (!m_focusComponent.isDisposed()) {
                            m_focusComponent.setFocus();
                          }
                        }
                      });
                    }
                    else {
                      // focus back to smartfield
                      m_focusComponent.setFocus();
                    }
                  }
                  else {
                    try {
                      closePart();
                    }
                    catch (ProcessingException ex) {
                      LOG.warn("closing part", ex);
                    }
                    fireSwtScoutPartEvent(new SwtScoutPartEvent(SwtScoutSmartFieldPopup.this, SwtScoutPartEvent.TYPE_CLOSED));
                  }
                }
              }
            });
          }
        };
        m_focusComponent.addFocusListener(m_focusComponentListener);
      }
    }
    //
    fireSwtScoutPartEvent(new SwtScoutPartEvent(SwtScoutSmartFieldPopup.this, SwtScoutPartEvent.TYPE_OPENED));
    fireSwtScoutPartEvent(new SwtScoutPartEvent(SwtScoutSmartFieldPopup.this, SwtScoutPartEvent.TYPE_ACTIVATED));
    if (m_nonFocusable) {
      if (m_swtWindow.getDisplay().getActiveShell() == m_swtWindow) {
        m_focusComponent.setFocus();
      }
      if (!m_focusComponent.isFocusControl()) {
        try {
          closePart();
        }
        catch (ProcessingException e) {
          LOG.warn("closing part", e);
        }
        fireSwtScoutPartEvent(new SwtScoutPartEvent(SwtScoutSmartFieldPopup.this, SwtScoutPartEvent.TYPE_CLOSED));
      }
    }
  }

  private void handleSwtWindowClosed() {
    if (m_ownerComponent != null && !m_ownerComponent.isDisposed() && m_scrollbarListener != null) {
      reqRemoveScrollbarListener(m_scrollbarListener, m_ownerComponent);
      if (m_ownerComponent.getShell() != null && !m_ownerComponent.getShell().isDisposed()) {
        m_ownerComponent.getShell().removeListener(SWT.Move, m_scrollbarListener);
      }
    }
    m_scrollbarListener = null;
    //
    if (m_focusComponent != null) {
      if (m_focusComponentListener != null) {
        if (!m_focusComponent.isDisposed()) {
          m_focusComponent.removeFocusListener(m_focusComponentListener);
        }
        m_focusComponentListener = null;
      }
    }
    //
    fireSwtScoutPartEvent(new SwtScoutPartEvent(SwtScoutSmartFieldPopup.this, SwtScoutPartEvent.TYPE_CLOSED));
    Runnable job = new Runnable() {
      @Override
      public void run() {
        m_scoutForm.getUIFacade().fireFormKilledFromUI();
      }
    };
    m_env.invokeScoutLater(job, 0);
  }

  private class P_SwtWindowDisposeListener implements DisposeListener {
    @Override
    public void widgetDisposed(DisposeEvent e) {
      handleSwtWindowClosed();
    }
  }// end private class

  /**
   * <h3>P_ScrollBarListener</h3> ensures the location and size of the popup in
   * case of resizing, moving.
   * 
   * @since 1.0.9 13.08.2008
   */
  private class P_ScrollBarListener implements Listener {
    @Override
    public void handleEvent(Event event) {
      try {
        closePart();
      }
      catch (ProcessingException e) {
        LOG.warn("could not close popup.", e);
      }
    }
  } // end private class
}
