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

import org.eclipse.rap.rwt.lifecycle.WidgetUtil;
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
import org.eclipse.scout.rt.ui.rap.form.fields.IPopupSupport;
import org.eclipse.scout.rt.ui.rap.form.fields.LogicalGridDataBuilder;
import org.eclipse.scout.rt.ui.rap.form.fields.RwtScoutValueFieldComposite;
import org.eclipse.scout.rt.ui.rap.form.fields.datefield.chooser.TimeChooserDialog;
import org.eclipse.scout.rt.ui.rap.internal.TextFieldEditableSupport;
import org.eclipse.scout.rt.ui.rap.keystroke.RwtKeyStroke;
import org.eclipse.scout.rt.ui.rap.util.RwtLayoutUtility;
import org.eclipse.scout.rt.ui.rap.util.RwtUtility;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

//TODO RAP 2.0 Migration
//check shell listener removel

public class RwtScoutTimeField extends RwtScoutValueFieldComposite<IDateField> implements IRwtScoutTimeField, IPopupSupport {

  private Button m_dropDownButton;
  private TextFieldEditableSupport m_editableSupport;

  private Set<IPopupSupportListener> m_popupEventListeners;
  private Object m_popupEventListenerLock;

  private boolean m_ignoreLabel = false;
  private Composite m_timeContainer;
  private boolean m_dateTimeCompositeMember;
  private String m_displayTextToVerify;
  private TimeChooserDialog m_timeChooserDialog = null;
  private FocusAdapter m_textFieldFocusAdapter = null;
  private P_TimeChooserDisposeListener m_disposeListener;

  @Override
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

  @Override
  public void setDateTimeCompositeMember(boolean dateTimeCompositeMember) {
    m_dateTimeCompositeMember = dateTimeCompositeMember;
  }

  @Override
  protected void initializeUi(Composite parent) {
    m_popupEventListeners = new HashSet<IPopupSupportListener>();
    m_popupEventListenerLock = new Object();

    Composite container = getUiEnvironment().getFormToolkit().createComposite(parent);
    StatusLabelEx label = getUiEnvironment().getFormToolkit().createStatusLabel(container, getScoutObject());

    m_timeContainer = getUiEnvironment().getFormToolkit().createComposite(container, SWT.BORDER);
    m_timeContainer.setData(WidgetUtil.CUSTOM_VARIANT, VARIANT_TIMEFIELD);

    StyledText textField = new StyledTextEx(m_timeContainer, SWT.SINGLE);
    getUiEnvironment().getFormToolkit().adapt(textField, false, false);
    textField.setData(WidgetUtil.CUSTOM_VARIANT, VARIANT_TIMEFIELD);

    ButtonEx timeChooserButton = getUiEnvironment().getFormToolkit().createButtonEx(m_timeContainer, SWT.PUSH | SWT.NO_FOCUS);
    timeChooserButton.setData(WidgetUtil.CUSTOM_VARIANT, VARIANT_TIMEFIELD);

    m_timeContainer.setTabList(new Control[]{textField});
    container.setTabList(new Control[]{m_timeContainer});

    // key strokes on container
    getUiEnvironment().addKeyStroke(container, new P_TimeChooserOpenKeyStroke(), false);

    // key strokes on field
    getUiEnvironment().addKeyStroke(textField, new P_ShiftNextQuarterHourKeyStroke(), false);
    getUiEnvironment().addKeyStroke(textField, new P_ShiftPreviousQuarterHourKeyStroke(), false);
    getUiEnvironment().addKeyStroke(textField, new P_ShiftNextHourKeyStroke(), false);
    getUiEnvironment().addKeyStroke(textField, new P_ShiftPreviousHourKeyStroke(), false);

    // listener
    timeChooserButton.addListener(ButtonEx.SELECTION_ACTION, new P_RwtBrowseButtonListener());
    attachFocusListener(textField, true);
    textField.addMouseListener(new MouseAdapter() {
      private static final long serialVersionUID = 1L;

      @Override
      public void mouseUp(MouseEvent e) {
        handleUiTimeChooserAction();
      }
    });
    //
    setUiContainer(container);
    setUiLabel(label);
    setDropDownButton(timeChooserButton);
    setUiField(textField);

    // layout
    container.setLayout(new LogicalGridLayout(1, 0));

    m_timeContainer.setLayoutData(LogicalGridDataBuilder.createField(((IFormField) getScoutObject()).getGridData()));
    m_timeContainer.setLayout(RwtLayoutUtility.createGridLayoutNoSpacing(2, false));

    GridData textLayoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
    textField.setLayoutData(textLayoutData);

    GridData buttonLayoutData = new GridData(SWT.CENTER, SWT.CENTER, false, false);
    buttonLayoutData.heightHint = 20;
    buttonLayoutData.widthHint = 20;
    timeChooserButton.setLayoutData(buttonLayoutData);
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

  public boolean isFocusInTimePicker() {
    Control focusControl = getUiEnvironment().getDisplay().getFocusControl();
    boolean isFocusInDatePicker = RwtUtility.isAncestorOf(m_timeChooserDialog.getShell(), focusControl);
    return isFocusInDatePicker;
  }

  private void installFocusListenerOnTextField() {
    if (getUiField().isDisposed()) {
      return;
    }

    getUiField().setFocus();
    if (m_textFieldFocusAdapter == null) {
      m_textFieldFocusAdapter = new FocusAdapter() {
        private static final long serialVersionUID = 1L;

        @Override
        public void focusLost(FocusEvent e) {
          handleUiFocusLostOnDatePickerPopup(e);
        }

      };
    }
    getUiField().addFocusListener(m_textFieldFocusAdapter);
  }

  /**
   * The event is fired only if the time picker popup is open.
   * <p>
   * The default sets the focus on the ui field if the new focus is inside the time picker. <br/>
   * If the new focus is outside the time picker it makes sure the time picker popup will be closed.
   * </p>
   */
  protected void handleUiFocusLostOnDatePickerPopup(FocusEvent event) {
    if (isFocusInTimePicker()) {
      getUiEnvironment().getDisplay().asyncExec(new Runnable() {

        @Override
        public void run() {
          getUiField().setFocus();
        }

      });
    }
    else {
      getUiEnvironment().getDisplay().asyncExec(new Runnable() {

        @Override
        public void run() {
          makeSureTimeChooserIsClosed();
        }

      });
    }
  }

  private void uninstallFocusListenerOnTextField() {
    if (!getUiField().isDisposed() && m_textFieldFocusAdapter != null) {
      getUiField().removeFocusListener(m_textFieldFocusAdapter);
    }
  }

  @Override
  protected void setEnabledFromScout(boolean b) {
    super.setEnabledFromScout(b);
    m_dropDownButton.setEnabled(b);
    getUiField().setEnabled(b);
    if (b) {
      m_timeContainer.setData(WidgetUtil.CUSTOM_VARIANT, VARIANT_TIMEFIELD);
    }
    else {
      m_timeContainer.setData(WidgetUtil.CUSTOM_VARIANT, VARIANT_TIMEFIELD_DISABLED);
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
    IDateField scoutField = getScoutObject();
    if (s == null) {
      s = "";
    }
    m_displayTextToVerify = s;
    Date value = scoutField.getValue();
    if (value != null) {
      DateFormat format = scoutField.getIsolatedTimeFormat();
      if (format != null) {
        m_displayTextToVerify = format.format(value);
      }
    }
    getUiField().setText(m_displayTextToVerify);
    getUiField().setCaretOffset(0);
  }

  @Override
  protected void setBackgroundFromScout(String scoutColor) {
    setBackgroundFromScout(scoutColor, m_timeContainer);
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
        boolean b = getScoutObject().getUIFacade().setTimeTextFromUI(text);
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
  }

  @Override
  protected void handleUiFocusGained() {
    if (isSelectAllOnFocusEnabled()) {
      getUiField().setSelection(0, getUiField().getText().length());
    }
  }

  protected void makeSureTimeChooserIsClosed() {
    if (m_timeChooserDialog != null
        && m_timeChooserDialog.getShell() != null
        && !m_timeChooserDialog.getShell().isDisposed()) {
      m_timeChooserDialog.getShell().close();
    }

    uninstallFocusListenerOnTextField();
  }

  private void handleUiTimeChooserAction() {
    if (!getDropDownButton().isVisible() || !getDropDownButton().isEnabled()) {
      return;
    }

    Date oldTime = getScoutObject().getValue();
    if (oldTime == null) {
      oldTime = new Date();
    }
    notifyPopupEventListeners(IPopupSupportListener.TYPE_OPENING);

    makeSureTimeChooserIsClosed();
    m_timeChooserDialog = createTimeChooserDialog(getUiField().getShell(), oldTime);
    if (m_timeChooserDialog != null) {
      m_timeChooserDialog.getShell().addDisposeListener(new DisposeListener() {
        private static final long serialVersionUID = 1L;

        @Override
        public void widgetDisposed(DisposeEvent event) {
          getTimeFromClosedDateChooserDialog();
          m_timeChooserDialog = null;
        }
      });

      m_timeChooserDialog.openTimeChooser(getUiField());
      installFocusListenerOnTextField();
    }
  }

  protected TimeChooserDialog createTimeChooserDialog(Shell parentShell, Date currentTime) {
    return new TimeChooserDialog(parentShell, currentTime);
  }

  private void getTimeFromClosedDateChooserDialog() {
    if (m_disposeListener != null) {
      m_timeChooserDialog.getShell().removeDisposeListener(m_disposeListener);
    }
    boolean setFocusToUiField = false;
    try {
      final Date newDate = m_timeChooserDialog.getReturnTime();
      if (newDate != null) {
        setFocusToUiField = true;
        // notify Scout
        Runnable t = new Runnable() {
          @Override
          public void run() {
            getScoutObject().getUIFacade().setTimeFromUI(newDate);
          }
        };
        getUiEnvironment().invokeScoutLater(t, 0);
        // end notify
      }
    }
    finally {
      notifyPopupEventListeners(IPopupSupportListener.TYPE_CLOSED);
      uninstallFocusListenerOnTextField();
      if (setFocusToUiField
          && !getUiField().isDisposed()) {
        getUiField().setFocus();
      }
    }
  }

  private void removeListenersFromTimeChooserDialog() {
    Object[] shellListeners = ShellEvent.getListeners(m_timeChooserDialog.getShell());
    for (Object object : shellListeners) {
      if (object.getClass().isInstance(this)
          || (object.getClass().getEnclosingClass() != null && object.getClass().getEnclosingClass().isInstance(this))) {
        m_timeChooserDialog.getShell().removeShellListener((ShellListener) object);
      }
    }
    Object[] disposeListeners = DisposeEvent.getListeners(m_timeChooserDialog.getShell());
    for (Object object : disposeListeners) {
      if (object.getClass().isInstance(this)
          || (object.getClass().getEnclosingClass() != null && object.getClass().getEnclosingClass().isInstance(this))) {
        m_timeChooserDialog.getShell().removeDisposeListener((DisposeListener) object);
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

  /**
   *
   */
  private final class P_TimeChooserDisposeListener implements DisposeListener {
    private static final long serialVersionUID = 1L;

    @Override
    public void widgetDisposed(DisposeEvent event) {
      getTimeFromClosedDateChooserDialog();
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
          handleUiTimeChooserAction();
          break;
        default:
          break;
      }
    }
  } // end class P_RwtBrowseButtonListener

  private void shiftTime(final int level, final int value) {
    if (getUiField().isDisposed()) {
      return;
    }
    if (getUiField().isEnabled()
        && getUiField().getEditable()
        && getUiField().isVisible()) {
      if (level >= 0) {
        // notify Scout
        Runnable t = new Runnable() {
          @Override
          public void run() {
            getScoutObject().getUIFacade().fireTimeShiftActionFromUI(level, value);
          }
        };
        getUiEnvironment().invokeScoutLater(t, 0);
        // end notify
      }
    }
  }

  private class P_ShiftNextQuarterHourKeyStroke extends RwtKeyStroke {
    public P_ShiftNextQuarterHourKeyStroke() {
      super(SWT.ARROW_UP);
    }

    @Override
    public void handleUiAction(Event e) {
      int level = 0;
      int value = 1;
      shiftTime(level, value);
    }
  }

  private class P_ShiftPreviousQuarterHourKeyStroke extends RwtKeyStroke {
    public P_ShiftPreviousQuarterHourKeyStroke() {
      super(SWT.ARROW_DOWN);
    }

    @Override
    public void handleUiAction(Event e) {
      int level = 0;
      int value = -1;
      shiftTime(level, value);
    }
  }

  private class P_ShiftNextHourKeyStroke extends RwtKeyStroke {
    public P_ShiftNextHourKeyStroke() {
      super(SWT.ARROW_UP, SWT.SHIFT);
    }

    @Override
    public void handleUiAction(Event e) {
      int level = 1;
      int value = 1;
      shiftTime(level, value);
    }
  }

  private class P_ShiftPreviousHourKeyStroke extends RwtKeyStroke {
    public P_ShiftPreviousHourKeyStroke() {
      super(SWT.ARROW_DOWN, SWT.SHIFT);
    }

    @Override
    public void handleUiAction(Event e) {
      int level = 1;
      int value = -1;
      shiftTime(level, value);
    }
  }

  private class P_TimeChooserOpenKeyStroke extends RwtKeyStroke {
    public P_TimeChooserOpenKeyStroke() {
      super(SWT.F2);
    }

    @Override
    public void handleUiAction(Event e) {
      handleUiTimeChooserAction();
    }
  }

}
