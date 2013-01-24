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
package org.eclipse.scout.rt.ui.swing.form;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.awt.Window;
import java.io.File;
import java.util.WeakHashMap;

import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.RootPaneContainer;
import javax.swing.SwingUtilities;

import org.eclipse.scout.commons.beans.IPropertyObserver;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.IEventHistory;
import org.eclipse.scout.rt.client.ui.form.FormEvent;
import org.eclipse.scout.rt.client.ui.form.FormListener;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.button.IButton;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.IGroupBox;
import org.eclipse.scout.rt.ui.swing.ISwingEnvironment;
import org.eclipse.scout.rt.ui.swing.LogicalGridData;
import org.eclipse.scout.rt.ui.swing.LogicalGridLayout;
import org.eclipse.scout.rt.ui.swing.SwingUtility;
import org.eclipse.scout.rt.ui.swing.basic.ISwingScoutComposite;
import org.eclipse.scout.rt.ui.swing.basic.SwingScoutComposite;
import org.eclipse.scout.rt.ui.swing.basic.WidgetPrinter;
import org.eclipse.scout.rt.ui.swing.ext.JPanelEx;
import org.eclipse.scout.rt.ui.swing.form.fields.SwingScoutFieldComposite;
import org.eclipse.scout.rt.ui.swing.form.fields.SwingScoutFormFieldGridData;
import org.eclipse.scout.rt.ui.swing.form.fields.groupbox.ISwingScoutGroupBox;
import org.eclipse.scout.rt.ui.swing.window.ISwingScoutView;
import org.eclipse.scout.rt.ui.swing.window.SwingScoutViewEvent;
import org.eclipse.scout.rt.ui.swing.window.SwingScoutViewListener;

public class SwingScoutForm extends SwingScoutComposite<IForm> implements ISwingScoutForm {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(SwingScoutForm.class);

  private ISwingScoutGroupBox m_mainBoxComposite;
  private FormListener m_scoutFormListener;
  private SwingScoutViewListener m_swingScoutViewListener;
  private ISwingScoutView m_viewComposite;
  private WeakHashMap<FormEvent, Object> m_consumedScoutFormEvents = new WeakHashMap<FormEvent, Object>();

  public SwingScoutForm(ISwingEnvironment env, IForm scoutForm) {
    this(env, null, scoutForm);
  }

  public SwingScoutForm(ISwingEnvironment env, ISwingScoutView targetViewComposite, IForm scoutForm) {
    m_viewComposite = targetViewComposite;
  }

  @Override
  protected void initializeSwing() {
    IGroupBox rootGroupBox = getScoutForm().getRootGroupBox();
    JComponent parent = null;
    if (m_viewComposite != null) {
      parent = m_viewComposite.getSwingContentPane();
    }
    m_mainBoxComposite = (ISwingScoutGroupBox) getSwingEnvironment().createFormField(parent, rootGroupBox);
    m_mainBoxComposite.createField(getScoutForm().getRootGroupBox(), getSwingEnvironment());
    JComponent swingContainer = m_mainBoxComposite.getSwingContainer();
    setSwingField(swingContainer);
    //
    if (m_viewComposite != null) {
      // attach to view
      m_viewComposite.getSwingContentPane().removeAll();
      // use grid layout with decent min-width
      JPanelEx optimalSizePanel = new JPanelEx(new LogicalGridLayout(getSwingEnvironment(), 0, 0));
      optimalSizePanel.setName(getScoutForm().getClass().getSimpleName() + ".optimalSizePanel");
      SwingScoutFormFieldGridData layoutData = new SwingScoutFormFieldGridData(getScoutForm().getRootGroupBox());
      getSwingFormPane().putClientProperty(LogicalGridData.CLIENT_PROPERTY_NAME, layoutData);
      optimalSizePanel.add(getSwingFormPane());
      m_viewComposite.getSwingContentPane().add(BorderLayout.CENTER, optimalSizePanel);
      attachSwingView();
    }
  }

  @Override
  protected void handleSwingShowing() {
    setInitialFocus();
  }

  @Override
  public JComponent getSwingFormPane() {
    return getSwingField();
  }

  public IForm getScoutForm() {
    return getScoutObject();
  }

  @Override
  public void detachSwingView() {
    if (m_viewComposite != null) {
      if (m_swingScoutViewListener != null) {
        m_viewComposite.removeSwingScoutViewListener(m_swingScoutViewListener);
        m_swingScoutViewListener = null;
      }
      // remove content
      m_viewComposite.getSwingContentPane().removeAll();
    }
    //force disconnect from model
    disconnectFromScout();
  }

  private void attachSwingView() {
    if (m_viewComposite != null) {
      IForm scoutForm = getScoutForm();
      m_viewComposite.setTitle(scoutForm.getTitle());
      m_viewComposite.setMaximizeEnabled(scoutForm.isMaximizeEnabled());
      m_viewComposite.setMinimizeEnabled(scoutForm.isMinimizeEnabled());
      boolean closable = false;
      for (IFormField f : scoutForm.getAllFields()) {
        if (f.isEnabled() && f.isVisible() && (f instanceof IButton)) {
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
      m_viewComposite.setCloseEnabled(closable);
      m_viewComposite.setMaximized(scoutForm.isMaximized());
      m_viewComposite.setMinimized(scoutForm.isMinimized());
      //
      m_swingScoutViewListener = new P_SwingScoutViewListener();
      m_viewComposite.addSwingScoutViewListener(m_swingScoutViewListener);
      // generate events if view is already showing or active
      if (m_viewComposite.isVisible()) {
        m_swingScoutViewListener.viewChanged(new SwingScoutViewEvent(m_viewComposite, SwingScoutViewEvent.TYPE_OPENED));
        if (m_viewComposite.isActive()) {
          m_swingScoutViewListener.viewChanged(new SwingScoutViewEvent(m_viewComposite, SwingScoutViewEvent.TYPE_ACTIVATED));
        }
      }
    }
  }

  @Override
  public ISwingScoutView getView() {
    return m_viewComposite;
  }

  @Override
  protected void attachScout() {
    super.attachScout();
    if (m_scoutFormListener == null) {
      m_scoutFormListener = new P_ScoutFormListener();
      getScoutForm().addFormListener(m_scoutFormListener);
    }
    // process all pending events, except requestFocus
    IEventHistory<FormEvent> h = getScoutObject().getEventHistory();
    if (h != null) {
      for (FormEvent e : h.getRecentEvents()) {
        switch (e.getType()) {
          case FormEvent.TYPE_TO_BACK:
          case FormEvent.TYPE_TO_FRONT:
          case FormEvent.TYPE_PRINT: {
            handleScoutFormEventInUi(e);
            break;
          }
        }
      }
    }
  }

  @Override
  protected void detachScout() {
    super.detachScout();
    if (m_scoutFormListener != null) {
      getScoutForm().removeFormListener(m_scoutFormListener);
      m_scoutFormListener = null;
    }
  }

  @Override
  public void setInitialFocus() {
    IFormField modelField = null;
    //check for request focus events in history
    IEventHistory<FormEvent> h = getScoutObject().getEventHistory();
    if (h != null) {
      for (FormEvent e : h.getRecentEvents()) {
        if (e.getType() == FormEvent.TYPE_REQUEST_FOCUS) {
          modelField = e.getFormField();
          break;
        }
      }
    }
    if (modelField != null) {
      handleRequestFocusFromScout(modelField, true);
    }
  }

  private Component findUiField(IFormField modelField) {
    if (modelField == null) {
      return null;
    }
    for (Component comp : SwingUtility.findChildComponents(getSwingContainer(), Component.class)) {
      ISwingScoutComposite<?> composite = SwingScoutFieldComposite.getCompositeOnWidget(comp);
      if (composite != null && composite.getScoutObject() == modelField) {
        return composite.getSwingField();
      }
    }
    return null;
  }

  /*
   * properties
   */
  @Override
  protected void handleScoutPropertyChange(String name, Object newValue) {
    super.handleScoutPropertyChange(name, newValue);
    if (name.equals(IForm.PROP_TITLE)) {
      if (m_viewComposite != null) {
        m_viewComposite.setTitle((String) newValue);
      }
    }
    else if (name.equals(IForm.PROP_MINIMIZE_ENABLED)) {
      if (m_viewComposite != null) {
        m_viewComposite.setMinimizeEnabled(((Boolean) newValue).booleanValue());
      }
    }
    else if (name.equals(IForm.PROP_MAXIMIZE_ENABLED)) {
      if (m_viewComposite != null) {
        m_viewComposite.setMaximizeEnabled(((Boolean) newValue).booleanValue());
      }
    }
    else if (name.equals(IForm.PROP_MINIMIZED)) {
      if (m_viewComposite != null) {
        m_viewComposite.setMinimized(((Boolean) newValue).booleanValue());
      }
    }
    else if (name.equals(IForm.PROP_MAXIMIZED)) {
      if (m_viewComposite != null) {
        m_viewComposite.setMaximized(((Boolean) newValue).booleanValue());
      }
    }
  }

  protected void handleScoutFormEventInUi(final FormEvent e) {
    if (m_consumedScoutFormEvents.containsKey(e)) {
      return;
    }
    m_consumedScoutFormEvents.put(e, Boolean.TRUE);
    //
    switch (e.getType()) {
      case FormEvent.TYPE_PRINT: {
        handlePrintFromScout(e);
        break;
      }
      case FormEvent.TYPE_TO_FRONT: {
        handleToFrontFromScout();
        break;
      }
      case FormEvent.TYPE_TO_BACK: {
        handleToBackFromScout();
        break;
      }
      case FormEvent.TYPE_REQUEST_FOCUS: {
        handleRequestFocusFromScout(e.getFormField(), false);
        break;
      }
    }
  }

  protected void handleToFrontFromScout() {
    if (getView() == null) {
      return;
    }

    Window w = SwingUtilities.getWindowAncestor(getView().getSwingContentPane());
    if (w.isShowing()) {
      w.toFront();
    }
  }

  protected void handleToBackFromScout() {
    if (getView() == null) {
      return;
    }

    Window w = SwingUtilities.getWindowAncestor(getView().getSwingContentPane());
    if (w.isShowing()) {
      w.toBack();
    }
  }

  protected void handlePrintFromScout(final FormEvent e) {
    WidgetPrinter wp = null;
    try {
      if (m_viewComposite != null) {
        if (e.getFormField() != null) {
          for (JComponent c : SwingUtility.findChildComponents(m_viewComposite.getSwingContentPane(), JComponent.class)) {
            IPropertyObserver scoutModel = SwingScoutComposite.getScoutModelOnWidget(c);
            if (scoutModel == e.getFormField()) {
              wp = new WidgetPrinter(c);
              break;
            }
          }
        }
        if (wp == null) {
          wp = new WidgetPrinter(SwingUtilities.getWindowAncestor(m_viewComposite.getSwingContentPane()));
        }
      }
      if (wp != null) {
        try {
          wp.print(e.getPrintDevice(), e.getPrintParameters());
        }
        catch (Throwable ex) {
          LOG.error(null, ex);
        }
      }

    }
    finally {
      File outputFile = null;
      if (wp != null) {
        outputFile = wp.getOutputFile();
      }
      final File outputFileFinal = outputFile;
      Runnable r = new Runnable() {
        @Override
        public void run() {
          getScoutObject().getUIFacade().fireFormPrintedFromUI(outputFileFinal);
        }
      };
      getSwingEnvironment().invokeScoutLater(r, 0);
    }
  }

  protected void handleRequestFocusFromScout(IFormField modelField, boolean force) {
    if (modelField == null) {
      return;
    }
    Component comp = findUiField(modelField);
    if (comp != null && comp.isShowing()) {
      comp.requestFocus();
    }
  }

  private class P_SwingScoutViewListener implements SwingScoutViewListener {

    @Override
    public void viewChanged(SwingScoutViewEvent e) {
      switch (e.getType()) {
        case SwingScoutViewEvent.TYPE_OPENED: {
          break;
        }
        case SwingScoutViewEvent.TYPE_ACTIVATED: {
          // notify Scout
          Runnable t = new Runnable() {
            @Override
            public void run() {
              getScoutForm().getUIFacade().fireFormActivatedFromUI();
            }
          };

          getSwingEnvironment().invokeScoutLater(t, 0);
          // end notify
          break;
        }
        case SwingScoutViewEvent.TYPE_CLOSING: {
          // notify Scout
          Runnable t = new Runnable() {
            @Override
            public void run() {
              getScoutForm().getUIFacade().fireFormClosingFromUI();
            }
          };

          getSwingEnvironment().invokeScoutLater(t, 0);
          // end notify
          break;
        }
        case SwingScoutViewEvent.TYPE_CLOSED: {
          // notify Scout
          Runnable t = new Runnable() {
            @Override
            public void run() {
              getScoutForm().getUIFacade().fireFormKilledFromUI();
            }
          };

          getSwingEnvironment().invokeScoutLater(t, 0);
          // end notify
          break;
        }
      }
    }
  }

  private class P_ScoutFormListener implements FormListener {
    private Object m_structureChangeRunnableLock = new Object();
    private Runnable m_structureChangeRunnable;

    @Override
    public void formChanged(final FormEvent e) {
      switch (e.getType()) {
        case FormEvent.TYPE_STRUCTURE_CHANGED: {
          synchronized (m_structureChangeRunnableLock) {
            if (m_structureChangeRunnable == null) {
              m_structureChangeRunnable = new Runnable() {
                @Override
                public void run() {
                  synchronized (m_structureChangeRunnableLock) {
                    m_structureChangeRunnable = null;
                  }
                  // auto correct focus owner
                  RootPaneContainer formRoot = (RootPaneContainer) SwingUtilities.getAncestorOfClass(RootPaneContainer.class, getSwingContainer());
                  if (formRoot instanceof JInternalFrame) {
                    if (!(((JInternalFrame) formRoot).isSelected())) {
                      formRoot = null;
                    }
                  }
                  else if (formRoot instanceof Window) {
                    if (!(((Window) formRoot).isActive())) {
                      formRoot = null;
                    }
                  }
                  if (formRoot != null) {
                    Component c = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
                    if (c == null || !SwingUtilities.isDescendingFrom(c, formRoot.getRootPane())) {
                      getSwingContainer().transferFocus();
                    }
                  }
                }
              };
              SwingUtilities.invokeLater(m_structureChangeRunnable);
            }
          }
          break;
        }
        case FormEvent.TYPE_PRINT:
        case FormEvent.TYPE_TO_FRONT:
        case FormEvent.TYPE_TO_BACK:
        case FormEvent.TYPE_REQUEST_FOCUS: {
          Runnable t = new Runnable() {
            @Override
            public void run() {
              handleScoutFormEventInUi(e);
            }
          };
          getSwingEnvironment().invokeSwingLater(t);
          break;
        }
      }
    }
  }// end private class

}
