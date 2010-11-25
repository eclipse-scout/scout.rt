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
import org.eclipse.scout.commons.exception.IProcessingStatus;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.ui.swt.ISwtEnvironment;
import org.eclipse.scout.rt.ui.swt.extension.UiDecorationExtensionPoint;
import org.eclipse.scout.rt.ui.swt.form.ISwtScoutForm;
import org.eclipse.scout.rt.ui.swt.util.SwtUtility;
import org.eclipse.scout.rt.ui.swt.window.ISwtScoutPart;
import org.eclipse.scout.rt.ui.swt.window.SwtScoutPartEvent;
import org.eclipse.scout.rt.ui.swt.window.SwtScoutPartListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/**
 * Popup window bound to a component (ownerComponent). The popup closes when
 * there is either a click outside this window or the component loses focus
 * (focusComponent), or the component becomes invisible.
 */
public class SwtScoutPopup implements ISwtScoutPart {
  private static IScoutLogger LOG = ScoutLogManager.getLogger(SwtScoutPopup.class);

  private ISwtEnvironment m_env;
  private Control m_ownerComponent;
  private Rectangle m_ownerBounds;
  private Shell m_swtWindow;
  private Composite m_swtWindowContentPane;
  private EventListenerList m_listenerList;
  private IForm m_scoutForm;
  private boolean m_positionBelowReferenceField;
  private boolean m_opened;

  public SwtScoutPopup(ISwtEnvironment env, Control ownerComponent, Rectangle ownerBounds) {
    m_env = env;
    m_positionBelowReferenceField = true;
    m_ownerComponent = ownerComponent;
    m_ownerBounds = ownerBounds;
    m_listenerList = new EventListenerList();
    //
    m_swtWindow = new Shell(ownerComponent.getShell(), SWT.RESIZE);
    m_swtWindow.setData("extendedStyle", SWT.POP_UP);
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
      @SuppressWarnings("unused")
      ISwtScoutForm swtScoutForm = m_env.createForm(getSwtContentPane(), scoutForm);
      autoAdjustBounds();
      if (m_opened) {
        //open and activate, do NOT just call setVisible(true)
        m_swtWindow.open();
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

  public void closePart() {
    m_opened = false;
    try {
      if (!m_swtWindow.isDisposed()) {
        m_swtWindow.setVisible(false);
        m_swtWindow.dispose();
      }
    }
    catch (Throwable e1) {
      LOG.error("Failed closing popup for " + m_scoutForm, e1);
    }
  }

  public IForm getForm() {
    return m_scoutForm;
  }

  public void autoAdjustBounds() {
    if (!getShell().isDisposed()) {
      Point d = getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
      d.x = Math.max(d.x, UiDecorationExtensionPoint.getLookAndFeel().getLogicalGridLayoutDefaultColumnWidth());
      d.y = Math.max(UiDecorationExtensionPoint.getLookAndFeel().getLogicalGridLayoutRowHeight(), d.y);
      Point p = new Point(m_ownerBounds.x, m_ownerBounds.y);
      Point above = new Point(p.x, p.y);
      Rectangle aboveView = SwtUtility.intersectRectangleWithScreen(getShell().getDisplay(), new Rectangle(above.x, above.y - d.y, d.x, d.y), false, false);
      Point below = new Point(p.x, p.y + m_ownerBounds.height);
      Rectangle belowView = SwtUtility.intersectRectangleWithScreen(getShell().getDisplay(), new Rectangle(below.x, below.y, d.x, d.y), false, false);
      // decide based on the preference positionBelowReferenceField
      Rectangle currentView = m_positionBelowReferenceField ? belowView : aboveView;
      Rectangle alternateView = m_positionBelowReferenceField ? aboveView : belowView;
      Rectangle newView;
      if (currentView.height >= alternateView.height) {
        newView = currentView;
      }
      else {
        newView = alternateView;
        // toggle preference
        m_positionBelowReferenceField = !m_positionBelowReferenceField;
      }
      if (!newView.equals(getShell().getBounds())) {
        getShell().setBounds(newView);
        getShell().layout(true);
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

  public boolean isVisible() {
    return m_swtWindow != null && m_swtWindow.getVisible();

  }

  public void activate() {
    m_swtWindow.getShell().setActive();
  }

  public boolean isActive() {
    return m_swtWindow != null && m_swtWindow.getDisplay().getActiveShell() == m_swtWindow;
  }

  public void setStatus(IProcessingStatus newValue) {
    // void
  }

  private void handleSwtWindowOpened() {
    fireSwtScoutPartEvent(new SwtScoutPartEvent(SwtScoutPopup.this, SwtScoutPartEvent.TYPE_OPENED));
    fireSwtScoutPartEvent(new SwtScoutPartEvent(SwtScoutPopup.this, SwtScoutPartEvent.TYPE_ACTIVATED));
  }

  private void handleSwtWindowClosed() {
    fireSwtScoutPartEvent(new SwtScoutPartEvent(SwtScoutPopup.this, SwtScoutPartEvent.TYPE_CLOSED));
    Runnable job = new Runnable() {
      @Override
      public void run() {
        m_scoutForm.getUIFacade().fireFormKilledFromUI();
      }
    };
    m_env.invokeScoutLater(job, 0);
  }

  private class P_SwtWindowDisposeListener implements DisposeListener {
    public void widgetDisposed(DisposeEvent e) {
      handleSwtWindowClosed();
    }
  }// end private class

}
