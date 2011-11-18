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
package org.eclipse.scout.rt.ui.swt.basic.table.celleditor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.AbstractFormHandler;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.ui.swt.basic.SwtScoutComposite;
import org.eclipse.scout.rt.ui.swt.window.SwtScoutPartEvent;
import org.eclipse.scout.rt.ui.swt.window.SwtScoutPartListener;
import org.eclipse.scout.rt.ui.swt.window.popup.SwtScoutDropDownPopup;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
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
public class SwtScoutFormFieldPopup extends SwtScoutComposite<IFormField> {

  private static IScoutLogger LOG = ScoutLogManager.getLogger(SwtScoutFormFieldPopup.class);

  private SwtScoutDropDownPopup m_swtScoutPopup;

  private Control m_owner;
  private int m_minWidth;
  private int m_prefWidth;
  private int m_minHeight;
  private int m_prefHeight;
  private int m_style;
  private SwtScoutPartListener m_popupEventListener;

  private List<IFormFieldPopupEventListener> m_eventListeners = new ArrayList<IFormFieldPopupEventListener>();
  private Object m_eventListenerLock = new Object();

  public SwtScoutFormFieldPopup(Control owner) {
    m_owner = owner;
    m_style = SWT.NO_TRIM;
    m_popupEventListener = new P_PopupEventListener();
  }

  @Override
  protected void initializeSwt(Composite parent) {
    super.initializeSwt(parent);

    m_owner.addPaintListener(new PaintListener() {

      @Override
      public void paintControl(PaintEvent e) {
        // remove listener to only be called once
        m_owner.removePaintListener(this);

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
          getEnvironment().invokeScoutLater(runnable, 2345);
          try {
            formRef.wait(2345);
          }
          catch (InterruptedException t) {
            //nop
          }
        }

        IForm form = formRef.get();
        if (form == null) {
          LOG.error("No popup form available", e);
          return;
        }

        // create popup in reference to cell editor (owner)
        m_swtScoutPopup = new P_SwtScoutDropDownPopup(m_owner, m_style);
        m_swtScoutPopup.setPopupOnField(true);
        m_swtScoutPopup.setHeightHint(m_prefHeight);
        m_swtScoutPopup.setWidthHint(m_prefWidth);
        m_swtScoutPopup.getShell().setMinimumSize(m_minWidth, m_minHeight);

        // install popup listener
        m_swtScoutPopup.addSwtScoutPartListener(m_popupEventListener);

        // open popup
        try {
          m_swtScoutPopup.showForm(form);
          // install keystrokes on inner form field
          installTraverseKeyStrokes(m_swtScoutPopup.getSwtContentPane());
        }
        catch (Throwable t) {
          LOG.error("failed to show popup form", t);
        }

      }
    });
    m_owner.addControlListener(new ControlAdapter() {

      @Override
      public void controlResized(ControlEvent e) {
        if (m_swtScoutPopup != null) {
          m_swtScoutPopup.autoAdjustBounds();
        }
      }
    });
    setSwtField(m_owner);
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
    }

    private class FormHandler extends AbstractFormHandler {
    }
  }

  /**
   * Traverse field to force the field's value written back to the model.
   * 
   * @param control
   */
  private void forceInputVerification(Control control) {
    Event event = new Event();
    event.widget = control;
    control.notifyListeners(SWT.Traverse, event);

    if (control instanceof Composite) {
      Composite composite = (Composite) control;
      for (Control child : composite.getChildren()) {
        forceInputVerification(child);
      }
    }
  }

  private void installTraverseKeyStrokes(Control control) {
    control.addTraverseListener(new TraverseListener() {
      @Override
      public void keyTraversed(TraverseEvent e) {
        switch (e.detail) {
          case SWT.TRAVERSE_ESCAPE:
            e.doit = false;
            closePopup(FormFieldPopupEvent.TYPE_CANCEL);
            break;
          case SWT.TRAVERSE_RETURN: {
            e.doit = true;
            break;
          }
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
    // update model with changed value
    forceInputVerification(m_swtScoutPopup.getSwtContentPane());
    // close popup
    m_swtScoutPopup.removeSwtScoutPartListener(m_popupEventListener);
    m_swtScoutPopup.closePart();

    // notify listeners
    notifyEventListeners(new FormFieldPopupEvent(getScoutObject(), type));
  }

  public SwtScoutDropDownPopup getPopup() {
    return m_swtScoutPopup;
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

  private class P_PopupEventListener implements SwtScoutPartListener {

    @Override
    public void partChanged(SwtScoutPartEvent e) {
      if (e.getType() == SwtScoutPartEvent.TYPE_CLOSED) {
        closePopup(FormFieldPopupEvent.TYPE_OK);
      }
    }
  }

  private class P_SwtScoutDropDownPopup extends SwtScoutDropDownPopup {

    private ShellAdapter m_shellListener;

    public P_SwtScoutDropDownPopup(Control ownerComponent, int style) {
      super(getEnvironment(), ownerComponent, null, style);
    }

    @Override
    protected void installFocusListener() {
      if (m_shellListener == null) {
        m_shellListener = new ShellAdapter() {
          @Override
          public void shellDeactivated(ShellEvent e) {
            closePart();
            fireSwtScoutPartEvent(new SwtScoutPartEvent(P_SwtScoutDropDownPopup.this, SwtScoutPartEvent.TYPE_CLOSED));
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
