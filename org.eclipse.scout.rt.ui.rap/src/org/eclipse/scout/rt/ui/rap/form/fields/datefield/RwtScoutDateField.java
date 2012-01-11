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
package org.eclipse.scout.rt.ui.rap.form.fields.datefield;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.rwt.lifecycle.WidgetUtil;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.holders.Holder;
import org.eclipse.scout.commons.job.JobEx;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.datefield.IDateField;
import org.eclipse.scout.rt.ui.rap.LogicalGridLayout;
import org.eclipse.scout.rt.ui.rap.ext.ButtonEx;
import org.eclipse.scout.rt.ui.rap.ext.StatusLabelEx;
import org.eclipse.scout.rt.ui.rap.ext.StyledTextEx;
import org.eclipse.scout.rt.ui.rap.ext.custom.StyledText;
import org.eclipse.scout.rt.ui.rap.extension.UiDecorationExtensionPoint;
import org.eclipse.scout.rt.ui.rap.form.fields.IPopupSupport;
import org.eclipse.scout.rt.ui.rap.form.fields.LogicalGridDataBuilder;
import org.eclipse.scout.rt.ui.rap.form.fields.RwtScoutValueFieldComposite;
import org.eclipse.scout.rt.ui.rap.form.fields.datefield.chooser.DateChooserDialog;
import org.eclipse.scout.rt.ui.rap.internal.TextFieldEditableSupport;
import org.eclipse.scout.rt.ui.rap.keystroke.RwtKeyStroke;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

public class RwtScoutDateField extends RwtScoutValueFieldComposite<IDateField> implements IRwtScoutDateField, IPopupSupport {

  private Button m_dropDownButton;
  private TextFieldEditableSupport m_editableSupport;

  private Set<IPopupSupportListener> m_popupEventListeners;
  private Object m_popupEventListenerLock;

  private boolean m_ignoreLabel = false;
  private Composite m_dateContainer;
  private boolean m_dateTimeCompositeMember;
  private String m_displayTextToVerify;

  public void setIgnoreLabel(boolean ignoreLabel) {
    m_ignoreLabel = ignoreLabel;
    if (ignoreLabel) {
      getUiLabel().setVisible(false);
    }
    else {
      getUiLabel().setVisible(getScoutObject().isLabelVisible());
    }
  }

  public boolean isIgnoreLabel() {
    return m_ignoreLabel;
  }

  public boolean isDateTimeCompositeMember() {
    return m_dateTimeCompositeMember;
  }

  public void setDateTimeCompositeMember(boolean dateTimeCompositeMember) {
    m_dateTimeCompositeMember = dateTimeCompositeMember;
  }

  @Override
  protected void initializeUi(Composite parent) {
    m_popupEventListeners = new HashSet<IPopupSupportListener>();
    m_popupEventListenerLock = new Object();
    Composite container = getUiEnvironment().getFormToolkit().createComposite(parent);
    int labelStyle = UiDecorationExtensionPoint.getLookAndFeel().getFormFieldLabelAlignment();
    StatusLabelEx label = new StatusLabelEx(container, labelStyle);
    getUiEnvironment().getFormToolkit().getFormToolkit().adapt(label, false, false);
    m_dateContainer = getUiEnvironment().getFormToolkit().createComposite(container, SWT.BORDER);
    m_dateContainer.setData(WidgetUtil.CUSTOM_VARIANT, VARIANT_DATEFIELD);
    StyledText textField = new StyledTextEx(m_dateContainer, SWT.SINGLE) {
      private static final long serialVersionUID = 1L;

      @Override
      public void setBackground(Color color) {
        super.setBackground(color);
        if (getDropDownButton() != null) {
          getDropDownButton().setBackground(color);
        }
      }

      @Override
      public void setText(String text) {
        super.setText(text);
      }
    };
    getUiEnvironment().getFormToolkit().adapt(textField, false, false);
    textField.setData(WidgetUtil.CUSTOM_VARIANT, VARIANT_DATEFIELD);
    ButtonEx dateChooserButton = getUiEnvironment().getFormToolkit().createButtonEx(m_dateContainer, SWT.PUSH | SWT.NO_FOCUS);
    dateChooserButton.setData(WidgetUtil.CUSTOM_VARIANT, VARIANT_DATEFIELD);
    m_dateContainer.setTabList(new Control[]{textField});
    container.setTabList(new Control[]{m_dateContainer});

    // ui key strokes
    getUiEnvironment().addKeyStroke(container, new P_DateChooserOpenKeyStroke(), false);
    container.addDisposeListener(new DisposeListener() {

      private static final long serialVersionUID = 1L;

      @Override
      public void widgetDisposed(DisposeEvent event) {
        getUiEnvironment().removeKeyStrokes((Control) event.getSource());
      }
    });

    // listener
    getUiEnvironment().addKeyStroke(textField, new P_ShiftDayUpKeyStroke(), false);
    getUiEnvironment().addKeyStroke(textField, new P_ShiftDayDownKeyStroke(), false);
    getUiEnvironment().addKeyStroke(textField, new P_ShiftMonthUpKeyStroke(), false);
    getUiEnvironment().addKeyStroke(textField, new P_ShiftMonthDownKeyStroke(), false);
    getUiEnvironment().addKeyStroke(textField, new P_ShiftYearUpKeyStroke(), false);
    getUiEnvironment().addKeyStroke(textField, new P_ShiftYearDownKeyStroke(), false);

    dateChooserButton.addListener(ButtonEx.SELECTION_ACTION, new P_RwtBrowseButtonListener());
    textField.addMouseListener(new MouseAdapter() {
      private static final long serialVersionUID = 1L;

      @Override
      public void mouseUp(MouseEvent e) {
        handleUiDateChooserAction();
      }
    });
    //
    setUiContainer(container);
    setUiLabel(label);
    setDropDownButton(dateChooserButton);
    setUiField(textField);
    // layout
    container.setLayout(new LogicalGridLayout(1, 0));

    m_dateContainer.setLayoutData(LogicalGridDataBuilder.createField(((IFormField) getScoutObject()).getGridData()));
    m_dateContainer.setLayout(new FormLayout());

    final FormData textLayoutData = new FormData(SWT.DEFAULT, SWT.DEFAULT);
    textLayoutData.right = new FormAttachment(100, -20);
    textLayoutData.left = new FormAttachment(0, 0);
    textLayoutData.bottom = new FormAttachment(textField, -1, SWT.BOTTOM);
    textField.setLayoutData(textLayoutData);

    final FormData buttonLayoutData = new FormData(SWT.DEFAULT, SWT.DEFAULT);
    buttonLayoutData.left = new FormAttachment(textField, 0, SWT.RIGHT);
    buttonLayoutData.bottom = new FormAttachment(dateChooserButton, 0, SWT.BOTTOM);
    dateChooserButton.setLayoutData(buttonLayoutData);
  }

  @Override
  public Button getDropDownButton() {
    return m_dropDownButton;
  }

  public void setDropDownButton(ButtonEx b) {
    m_dropDownButton = b;
  }

  @Override
  public StyledTextEx getUiField() {
    return (StyledTextEx) super.getUiField();
  }

  @Override
  protected void setEnabledFromScout(boolean b) {
    super.setEnabledFromScout(b);
    m_dropDownButton.setEnabled(b);
    if (b) {
      m_dateContainer.setData(WidgetUtil.CUSTOM_VARIANT, VARIANT_DATEFIELD);
    }
    else {
      m_dateContainer.setData(WidgetUtil.CUSTOM_VARIANT, VARIANT_DATEFIELD_DISABLED);
    }
  }

  @Override
  protected void setLabelVisibleFromScout() {
    if (!isIgnoreLabel()) {
      super.setLabelVisibleFromScout();
    }
  }

  @Override
  protected void setFieldEnabled(Control rwtField, boolean enabled) {
    if (m_editableSupport == null) {
      m_editableSupport = new TextFieldEditableSupport(getUiField());
    }
    m_editableSupport.setEditable(enabled);
  }

  @Override
  protected void setDisplayTextFromScout(String s) {
    IDateField f = getScoutObject();
    if (f.getErrorStatus() != null) {
      return;
    }
    if (s == null) {
      s = "";
    }
    m_displayTextToVerify = s;
    Date value = f.getValue();
    if (value == null) {
      //only date field
      getUiField().setText(m_displayTextToVerify);
      super.handleUiFocusGained();
      getUiField().setCaretOffset(0);
      return;
    }
    DateFormat format = f.getIsolatedDateFormat();
    if (format != null) {
      m_displayTextToVerify = format.format(value);
      getUiField().setText(m_displayTextToVerify);
      super.handleUiFocusGained();
      getUiField().setCaretOffset(0);
    }
  }

  @Override
  protected void handleUiInputVerifier(boolean doit) {
    if (!doit) {
      return;
    }
    final String text = getUiField().getText();
    // only handle if text has changed
    if (CompareUtility.equals(text, m_displayTextToVerify) && (isDateTimeCompositeMember() || getScoutObject().getErrorStatus() == null)) {
      return;
    }
    m_displayTextToVerify = text;
    final Holder<Boolean> result = new Holder<Boolean>(Boolean.class, false);
    // notify Scout
    Runnable t = new Runnable() {
      @Override
      public void run() {
        boolean b = getScoutObject().getUIFacade().setDateTextFromUI(text);
        result.setValue(b);
      }
    };
    JobEx job = getUiEnvironment().invokeScoutLater(t, 0);
    try {
      job.join(2345);
    }
    catch (InterruptedException e) {
      //nop
    }
    getUiEnvironment().dispatchImmediateUiJobs();
    // end notify
    return;
  }

  @Override
  protected void handleUiFocusGained() {
    super.handleUiFocusGained();
    getUiField().setSelection(0, getUiField().getText().length());
  }

  @Override
  protected void handleUiFocusLost() {
    getUiField().setSelection(0, 0);
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

  private void handleUiDateChooserAction() {
    if (getDropDownButton().isVisible() && getDropDownButton().isEnabled()) {
      Date oldDate = getScoutObject().getValue();
      if (oldDate == null) {
        oldDate = new Date();
      }

      notifyPopupEventListeners(IPopupSupportListener.TYPE_OPENING);
      try {
        DateChooserDialog dialog = new DateChooserDialog(getUiField().getShell(), oldDate);
        final Date newDate = dialog.openDateChooser(getUiField());
        if (newDate != null) {
          // notify Scout
          Runnable t = new Runnable() {
            @Override
            public void run() {
              getScoutObject().getUIFacade().setDateFromUI(newDate);
            }
          };

          getUiEnvironment().invokeScoutLater(t, 0);
          // end notify
        }
      }
      finally {
        notifyPopupEventListeners(IPopupSupportListener.TYPE_CLOSED);
        if (!getUiField().isDisposed()) {
          getUiField().setFocus();
        }
      }
    }
  }

  private class P_RwtBrowseButtonListener implements Listener {
    private static final long serialVersionUID = 1L;

    public P_RwtBrowseButtonListener() {
    }

    @Override
    public void handleEvent(Event event) {
      switch (event.type) {
        case ButtonEx.SELECTION_ACTION:
          getUiField().forceFocus();
          handleUiDateChooserAction();
          break;
        default:
          break;
      }
    }
  } // end class P_RwtBrowseButtonListener

  private void shiftDate(final int level, final int value) {
    if (getUiField().isDisposed()) {
      return;
    }
    if (getUiField().isEnabled()
        && getUiField().getEditable()
        && getUiField().isVisible()) {
      if (level >= 0) {
        final String newDisplayText = getUiField().getText();
        // notify Scout
        Runnable t = new Runnable() {
          @Override
          public void run() {
            // store current (possibly changed) value
            if (!CompareUtility.equals(newDisplayText, getScoutObject().getDisplayText())) {
              getScoutObject().getUIFacade().setDateTimeTextFromUI(newDisplayText);
            }
            getScoutObject().getUIFacade().fireDateShiftActionFromUI(level, value);
          }
        };
        getUiEnvironment().invokeScoutLater(t, 0);
        // end notify
      }
    }
  }

  private class P_ShiftDayUpKeyStroke extends RwtKeyStroke {
    public P_ShiftDayUpKeyStroke() {
      super(SWT.ARROW_UP);
    }

    @Override
    public void handleUiAction(Event e) {
      int level = 0;
      int value = 1;
      shiftDate(level, value);
    }
  }

  private class P_ShiftDayDownKeyStroke extends RwtKeyStroke {
    public P_ShiftDayDownKeyStroke() {
      super(SWT.ARROW_DOWN);
    }

    @Override
    public void handleUiAction(Event e) {
      int level = 0;
      int value = -1;
      shiftDate(level, value);
    }
  }

  private class P_ShiftMonthUpKeyStroke extends RwtKeyStroke {
    public P_ShiftMonthUpKeyStroke() {
      super(SWT.ARROW_UP, SWT.SHIFT);
    }

    @Override
    public void handleUiAction(Event e) {
      int level = 1;
      int value = 1;
      shiftDate(level, value);
    }
  }

  private class P_ShiftMonthDownKeyStroke extends RwtKeyStroke {
    public P_ShiftMonthDownKeyStroke() {
      super(SWT.ARROW_DOWN, SWT.SHIFT);
    }

    @Override
    public void handleUiAction(Event e) {
      int level = 1;
      int value = -1;
      shiftDate(level, value);
    }
  }

  private class P_ShiftYearUpKeyStroke extends RwtKeyStroke {
    public P_ShiftYearUpKeyStroke() {
      super(SWT.ARROW_UP, SWT.CONTROL);
    }

    @Override
    public void handleUiAction(Event e) {
      int level = 2;
      int value = 1;
      shiftDate(level, value);
    }
  }

  private class P_ShiftYearDownKeyStroke extends RwtKeyStroke {
    public P_ShiftYearDownKeyStroke() {
      super(SWT.ARROW_DOWN, SWT.CONTROL);
    }

    @Override
    public void handleUiAction(Event e) {
      int level = 2;
      int value = -1;
      shiftDate(level, value);
    }
  }

  private class P_DateChooserOpenKeyStroke extends RwtKeyStroke {
    public P_DateChooserOpenKeyStroke() {
      super(SWT.F2);
    }

    @Override
    public void handleUiAction(Event e) {
      handleUiDateChooserAction();
    }
  }
}
