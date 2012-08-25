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
package org.eclipse.scout.rt.ui.swt.form.fields.datefield;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.holders.Holder;
import org.eclipse.scout.commons.job.JobEx;
import org.eclipse.scout.rt.client.ui.form.fields.datefield.IDateField;
import org.eclipse.scout.rt.shared.AbstractIcons;
import org.eclipse.scout.rt.ui.swt.LogicalGridLayout;
import org.eclipse.scout.rt.ui.swt.ext.ButtonEx;
import org.eclipse.scout.rt.ui.swt.ext.StatusLabelEx;
import org.eclipse.scout.rt.ui.swt.form.fields.IPopupSupport;
import org.eclipse.scout.rt.ui.swt.form.fields.LogicalGridDataBuilder;
import org.eclipse.scout.rt.ui.swt.form.fields.SwtScoutValueFieldComposite;
import org.eclipse.scout.rt.ui.swt.form.fields.datefield.chooser.TimeChooserDialog;
import org.eclipse.scout.rt.ui.swt.internal.TextFieldEditableSupport;
import org.eclipse.scout.rt.ui.swt.keystroke.SwtKeyStroke;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

public class SwtScoutTimeField extends SwtScoutValueFieldComposite<IDateField> implements ISwtScoutTimeField, IPopupSupport {
  private ButtonEx m_timeChooserButton;
  private TextFieldEditableSupport m_editableSupport;

  private Set<IPopupSupportListener> m_popupEventListeners;
  private Object m_popupEventListenerLock;

  @Override
  protected void initializeSwt(Composite parent) {
    m_popupEventListeners = new HashSet<IPopupSupportListener>();
    m_popupEventListenerLock = new Object();

    Composite container = getEnvironment().getFormToolkit().createComposite(parent);
    StatusLabelEx label = getEnvironment().getFormToolkit().createStatusLabel(container, getEnvironment(), getScoutObject());

    StyledText textField = getEnvironment().getFormToolkit().createStyledText(container, SWT.SINGLE | SWT.BORDER);
    ButtonEx timeChooserButton = getEnvironment().getFormToolkit().createButtonEx(container, SWT.PUSH);
    timeChooserButton.setImage(getEnvironment().getIcon(AbstractIcons.DateFieldTime));
    // prevent the button from grabbing focus
    container.setTabList(new Control[]{textField});

    // ui key strokes
    getEnvironment().addKeyStroke(container, new P_TimeChooserOpenKeyStroke());

    // listener
    timeChooserButton.addListener(ButtonEx.SELECTION_ACTION, new P_SwtBrowseButtonListener());
    //
    setSwtContainer(container);
    setSwtLabel(label);
    setTimeChooserButton(timeChooserButton);
    setSwtField(textField);
    // layout
    container.setLayout(new LogicalGridLayout(1, 0));
    timeChooserButton.setLayoutData(LogicalGridDataBuilder.createButton1());
  }

  @Override
  public ButtonEx getTimeChooserButton() {
    return m_timeChooserButton;
  }

  public void setTimeChooserButton(ButtonEx timeChooserButton) {
    m_timeChooserButton = timeChooserButton;
  }

  @Override
  public StyledText getSwtField() {
    return (StyledText) super.getSwtField();
  }

  @Override
  protected void setEnabledFromScout(boolean b) {
    super.setEnabledFromScout(b);
    m_timeChooserButton.setEnabled(b);
  }

  @Override
  protected void setFieldEnabled(Control swtField, boolean enabled) {
    if (m_editableSupport == null) {
      m_editableSupport = new TextFieldEditableSupport(getSwtField());
    }
    m_editableSupport.setEditable(enabled);
  }

  @Override
  protected void setDisplayTextFromScout(String s) {
    if (s == null) {
      s = "";
    }
    getSwtField().setText(s);
    super.handleSwtFocusGained();
    getSwtField().setCaretOffset(0);
  }

  @Override
  protected boolean handleSwtInputVerifier() {
    final String text = getSwtField().getText();
    // only handle if text has changed
    if (CompareUtility.equals(text, getScoutObject().getDisplayText()) && getScoutObject().getErrorStatus() == null) {
      return true;
    }
    final Holder<Boolean> result = new Holder<Boolean>(Boolean.class, false);
    // notify Scout
    Runnable t = new Runnable() {
      @Override
      public void run() {
        boolean b = getScoutObject().getUIFacade().setTimeTextFromUI(text);
        result.setValue(b);
      }
    };
    JobEx job = getEnvironment().invokeScoutLater(t, 0);
    try {
      job.join(2345);
    }
    catch (InterruptedException e) {
      //nop
    }
    getEnvironment().dispatchImmediateSwtJobs();
    // end notify
    return true;// continue always
  }

  @Override
  protected void handleSwtFocusGained() {
    super.handleSwtFocusGained();

    scheduleSelectAll();
  }

  @Override
  protected void handleSwtFocusLost() {
    getSwtField().setSelection(0, 0);
  }

  protected void scheduleSelectAll() {
    getEnvironment().getDisplay().asyncExec(new Runnable() {

      @Override
      public void run() {
        if (getSwtField().isDisposed()) {
          return;
        }

        getSwtField().setSelection(0, getSwtField().getText().length());
      }

    });

  }

  private void handleSwtTimeChooserAction() {
    if (getTimeChooserButton().isVisible() && getTimeChooserButton().isEnabled()) {
      Date d = getScoutObject().getValue();
      if (d == null) {
        d = new Date();
      }
      notifyPopupEventListeners(IPopupSupportListener.TYPE_OPENING);
      try {
        TimeChooserDialog dialog = new TimeChooserDialog(getSwtField().getShell(), d, getEnvironment());
        Date newDate = dialog.openDateChooser(getSwtField());
        if (newDate != null) {
          getSwtField().setText(DateFormat.getTimeInstance(DateFormat.SHORT).format(newDate));
          handleSwtInputVerifier();
        }
      }
      finally {
        notifyPopupEventListeners(IPopupSupportListener.TYPE_CLOSED);
        if (!getSwtField().isDisposed()) {
          getSwtField().setFocus();
        }
      }
    }
  }

  private void notifyPopupEventListeners(int eventType) {
    IPopupSupportListener[] listeners;
    synchronized (m_popupEventListenerLock) {
      listeners = m_popupEventListeners.toArray(new IPopupSupportListener[m_popupEventListeners.size()]);
    }
    for (IPopupSupportListener listener : listeners) {
      listener.handleEvent(eventType);
    }
  }

  @Override
  public void addPopupEventListener(IPopupSupportListener listener) {
    synchronized (m_popupEventListenerLock) {
      m_popupEventListeners.add(listener);
    }
  }

  @Override
  public void removePopupEventListener(IPopupSupportListener listener) {
    synchronized (m_popupEventListenerLock) {
      m_popupEventListeners.remove(listener);
    }
  }

  private class P_SwtBrowseButtonListener implements Listener {
    public P_SwtBrowseButtonListener() {
    }

    @Override
    public void handleEvent(Event event) {
      switch (event.type) {
        case ButtonEx.SELECTION_ACTION:
          handleSwtTimeChooserAction();
          break;
        default:
          break;
      }
    }
  } // end class P_SwtBrowseButtonListener

  private class P_TimeChooserOpenKeyStroke extends SwtKeyStroke {
    public P_TimeChooserOpenKeyStroke() {
      super(SWT.F2);
    }

    @Override
    public void handleSwtAction(Event e) {
      handleSwtTimeChooserAction();
    }
  }
}
