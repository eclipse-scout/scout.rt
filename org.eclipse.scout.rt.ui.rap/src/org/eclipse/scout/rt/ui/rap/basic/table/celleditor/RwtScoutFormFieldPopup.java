/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this tribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.rap.basic.table.celleditor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.action.keystroke.AbstractKeyStroke;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.AbstractFormHandler;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.ui.rap.IRwtEnvironment;
import org.eclipse.scout.rt.ui.rap.basic.RwtScoutComposite;
import org.eclipse.scout.rt.ui.rap.core.form.IRwtScoutForm;
import org.eclipse.scout.rt.ui.rap.window.RwtScoutPartEvent;
import org.eclipse.scout.rt.ui.rap.window.RwtScoutPartListener;
import org.eclipse.scout.rt.ui.rap.window.popup.RwtScoutDropDownPopup;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;

/**
 * Wraps a {@link IFormField} to be displayed as popup cell editor
 */
public class RwtScoutFormFieldPopup extends RwtScoutComposite<IFormField> {
  private static IScoutLogger LOG = ScoutLogManager.getLogger(RwtScoutFormFieldPopup.class);

  private P_RwtScoutDropDownPopup m_uiScoutPopup;

  private Composite m_owner;

  private int m_minWidth;
  private int m_prefWidth;
  private int m_minHeight;
  private int m_prefHeight;
  private int m_style;
  private RwtScoutPartListener m_popupEventListener;

  private List<IFormFieldPopupEventListener> m_eventListeners = new ArrayList<IFormFieldPopupEventListener>();
  private Object m_eventListenerLock = new Object();

  public RwtScoutFormFieldPopup(Composite owner) {
    m_owner = owner;
    m_style = SWT.NO_TRIM;
    m_popupEventListener = new P_PopupEventListener();
  }

  @Override
  protected void initializeUi(final Composite parent) {
    super.initializeUi(parent);

    // create form to hold the form field
    final AtomicReference<IForm> formRef = new AtomicReference<IForm>();
    Runnable runnable = new Runnable() {
      @Override
      public void run() {
        try {
          P_Form form = new P_Form();
          form.setAutoAddRemoveOnDesktop(false);
          form.startForm();
          formRef.set(form);
        }
        catch (Throwable t) {
          LOG.error("failed to start popup form", t);
        }
        synchronized (formRef) {
          formRef.notifyAll();
        }
      }
    };
    synchronized (formRef) {
      getUiEnvironment().invokeScoutLater(runnable, 2345);
      try {
        formRef.wait(2350);
      }
      catch (InterruptedException t) {
        //nop
      }
    }

    final IForm form = formRef.get();
    if (form == null) {
      LOG.error("No popup form available");
      return;
    }

    // create popup in reference to cell editor (owner)
    m_uiScoutPopup = new P_RwtScoutDropDownPopup();
    m_uiScoutPopup.createPart(form, m_owner, m_style, getUiEnvironment());
    m_uiScoutPopup.setPopupOnField(true);
    m_uiScoutPopup.setHeightHint(m_prefHeight);
    m_uiScoutPopup.setWidthHint(m_prefWidth);
    m_uiScoutPopup.getShell().setMinimumSize(m_minWidth, m_minHeight);

    // install popup listener
    m_uiScoutPopup.addRwtScoutPartListener(m_popupEventListener);

    // open popup
    try {
      m_uiScoutPopup.showPart();
      // install traversal keystrokes on inner form
      installTraverseKeyStrokes(m_uiScoutPopup.getUiContentPane());
    }
    catch (Throwable t) {
      LOG.error("failed to show popup form", t);
    }

    // add control listener to adjust popup location
    m_owner.addControlListener(new ControlAdapter() {
      private static final long serialVersionUID = 1L;

      @Override
      public void controlResized(ControlEvent e) {
        // invoke at next reasonable time to guarantee proper location
        getUiContainer().getDisplay().asyncExec(new Runnable() {

          @Override
          public void run() {
            if (m_uiScoutPopup != null) {
              m_uiScoutPopup.autoAdjustBounds();
            }
          }

        });
      }
    });
    setUiContainer(m_owner);
  }

  private class P_Form extends AbstractForm {

    public P_Form() throws ProcessingException {
      super();
    }

    @Override
    protected boolean getConfiguredModal() {
      return false;
    }

    @Override
    protected int getConfiguredDisplayHint() {
      return DISPLAY_HINT_VIEW;
    }

    @Override
    public String getDisplayViewId() {
      return IForm.VIEW_ID_CENTER;
    }

    @Override
    protected boolean getConfiguredAskIfNeedSave() {
      return false;
    }

    public void startForm() throws ProcessingException {
      startInternal(new FormHandler());
    }

    public MainBox getMainBox() {
      return (MainBox) getRootGroupBox();
    }

    @Order(10)
    public class MainBox extends AbstractGroupBox {

      @Override
      protected void injectFieldsInternal(List<IFormField> fieldList) {
        fieldList.add(getScoutObject());
      }

      @Override
      protected boolean getConfiguredBorderVisible() {
        return false;
      }

      @Override
      protected int getConfiguredGridColumnCount() {
        return 1;
      }

      @Override
      protected boolean getConfiguredGridUseUiWidth() {
        return true;
      }

      @Override
      protected boolean getConfiguredGridUseUiHeight() {
        return true;
      }

      @Order(10.0)
      public class EnterKeyStroke extends AbstractKeyStroke {

        @Override
        public void execAction() throws ProcessingException {
          getUiContainer().getDisplay().asyncExec(new Runnable() {

            @Override
            public void run() {
              closePopup(FormFieldPopupEvent.TYPE_OK);
            }
          });
        }

        @Override
        public String getConfiguredKeyStroke() {
          return "ctrl-enter";
        }
      }

      @Order(20.0)
      public class EscapeKeyStroke extends AbstractKeyStroke {

        @Override
        public void execAction() throws ProcessingException {
          getUiContainer().getDisplay().asyncExec(new Runnable() {

            @Override
            public void run() {
              closePopup(FormFieldPopupEvent.TYPE_CANCEL);
            }
          });
        }

        @Override
        public String getConfiguredKeyStroke() {
          return "escape";
        }
      }
    }

    private class FormHandler extends AbstractFormHandler {
    }
  }

  /**
   * Touch the field to write its UI value back to the model
   */
  public void touch() {
    if (m_uiScoutPopup != null) {
      touch(m_uiScoutPopup.getUiContentPane());
    }
  }

  private void touch(Control control) {
    if (control == null || control.isDisposed()) {
      return;
    }
    Event event = new Event();
    event.widget = control;
    control.notifyListeners(SWT.Traverse, event);

    if (control instanceof Composite) {
      Composite composite = (Composite) control;
      for (Control child : composite.getChildren()) {
        touch(child);
      }
    }
  }

  private void installTraverseKeyStrokes(Control control) {
    control.addTraverseListener(new TraverseListener() {
      private static final long serialVersionUID = 1L;

      @Override
      public void keyTraversed(TraverseEvent e) {
        switch (e.detail) {
          case SWT.TRAVERSE_TAB_NEXT: {
            e.doit = false;
            closePopup(FormFieldPopupEvent.TYPE_OK | FormFieldPopupEvent.TYPE_FOCUS_NEXT);
            break;
          }
          case SWT.TRAVERSE_TAB_PREVIOUS: {
            e.doit = false;
            closePopup(FormFieldPopupEvent.TYPE_OK | FormFieldPopupEvent.TYPE_FOCUS_BACK);
            break;
          }
        }
      }
    });
    if (control instanceof Composite) {
      Composite composite = (Composite) control;
      for (Control child : composite.getChildren()) {
        installTraverseKeyStrokes(child);
      }
    }
  }

  public void addEventListener(IFormFieldPopupEventListener eventListener) {
    synchronized (m_eventListenerLock) {
      m_eventListeners.add(eventListener);
    }
  }

  public void removeEventListener(IFormFieldPopupEventListener eventListener) {
    synchronized (m_eventListenerLock) {
      m_eventListeners.remove(eventListener);
    }
  }

  protected void notifyEventListeners(FormFieldPopupEvent event) {
    IFormFieldPopupEventListener[] eventListeners;
    synchronized (m_eventListenerLock) {
      eventListeners = m_eventListeners.toArray(new IFormFieldPopupEventListener[m_eventListeners.size()]);
    }
    for (IFormFieldPopupEventListener eventListener : eventListeners) {
      eventListener.handleEvent(event);
    }
  }

  public void closePopup(int type) {
    touch();
    m_uiScoutPopup.removeRwtScoutPartListener(m_popupEventListener);
    m_uiScoutPopup.closePart();
    m_uiScoutPopup = null;

    // notify listeners
    notifyEventListeners(new FormFieldPopupEvent(getScoutObject(), type));
  }

  public RwtScoutDropDownPopup getPopup() {
    return m_uiScoutPopup;
  }

  public boolean isClosed() {
    return m_uiScoutPopup == null || m_uiScoutPopup.getUiContentPane() == null || m_uiScoutPopup.getUiContentPane().isDisposed();
  }

  public IRwtScoutForm getInnerRwtScoutForm() {
    return m_uiScoutPopup.getRwtScoutForm();
  }

  public int getMinWidth() {
    return m_minWidth;
  }

  public void setMinWidth(int minWidth) {
    m_minWidth = minWidth;
  }

  public int getPrefWidth() {
    return m_prefWidth;
  }

  public void setPrefWidth(int prefWidth) {
    m_prefWidth = prefWidth;
  }

  public int getMinHeight() {
    return m_minHeight;
  }

  public void setMinHeight(int minHeight) {
    m_minHeight = minHeight;
  }

  public int getPrefHeight() {
    return m_prefHeight;
  }

  public void setPrefHeight(int prefHeight) {
    m_prefHeight = prefHeight;
  }

  public int getStyle() {
    return m_style;
  }

  public void setStyle(int style) {
    m_style = style;
  }

  private class P_PopupEventListener implements RwtScoutPartListener {

    @Override
    public void partChanged(RwtScoutPartEvent e) {
      if (e.getType() == RwtScoutPartEvent.TYPE_CLOSED) {
        closePopup(FormFieldPopupEvent.TYPE_OK);
      }
    }
  }

  private class P_RwtScoutDropDownPopup extends RwtScoutDropDownPopup {

    private ShellAdapter m_shellListener;

    public void createPart(IForm scoutForm, Composite ownerComponent, int style, IRwtEnvironment uiEnvironment) {
      super.createPart(scoutForm, ownerComponent, (Control) null, style, uiEnvironment);
    }

    @Override
    protected void installFocusListener() {
      if (m_shellListener == null) {
        m_shellListener = new ShellAdapter() {
          private static final long serialVersionUID = 1L;

          @Override
          public void shellDeactivated(ShellEvent e) {
            closePart();
            fireRwtScoutPartEvent(new RwtScoutPartEvent(P_RwtScoutDropDownPopup.this, RwtScoutPartEvent.TYPE_CLOSED));
          }
        };
        getShell().addShellListener(m_shellListener);
      }
    }

    @Override
    protected void uninstallFocusLostListener() {
      if (m_shellListener != null) {
        getShell().removeShellListener(m_shellListener);
      }
      m_shellListener = null;
    }
  }
}
