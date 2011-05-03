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

import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.holders.Holder;
import org.eclipse.scout.commons.job.JobEx;
import org.eclipse.scout.rt.client.ui.form.fields.datefield.IDateField;
import org.eclipse.scout.rt.shared.AbstractIcons;
import org.eclipse.scout.rt.ui.swt.LogicalGridLayout;
import org.eclipse.scout.rt.ui.swt.ext.ButtonEx;
import org.eclipse.scout.rt.ui.swt.ext.StatusLabelEx;
import org.eclipse.scout.rt.ui.swt.extension.UiDecorationExtensionPoint;
import org.eclipse.scout.rt.ui.swt.form.fields.LogicalGridDataBuilder;
import org.eclipse.scout.rt.ui.swt.form.fields.SwtScoutValueFieldComposite;
import org.eclipse.scout.rt.ui.swt.form.fields.datefield.chooser.DateChooserDialog;
import org.eclipse.scout.rt.ui.swt.form.fields.datefield.chooser.TimeChooserDialog;
import org.eclipse.scout.rt.ui.swt.internal.TextFieldEditableSupport;
import org.eclipse.scout.rt.ui.swt.keystroke.SwtKeyStroke;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

public class SwtScoutDateField extends SwtScoutValueFieldComposite<IDateField> implements ISwtScoutDateField {
  public static final int TYPE_TIME_CHOOSER = 1;
  public static final int TYPE_DATE_CHOOSER = 2;

  private ButtonEx m_dateChooserButton;
  private ButtonEx m_timeChooserButton;
  private boolean m_hasTime;
  private TextFieldEditableSupport m_editableSupport;

  @Override
  protected void initializeSwt(Composite parent) {
    Composite container = getEnvironment().getFormToolkit().createComposite(parent);
    int labelStyle = UiDecorationExtensionPoint.getLookAndFeel().getFormFieldLabelAlignment();
    StatusLabelEx label = new StatusLabelEx(container, labelStyle, getEnvironment());
    getEnvironment().getFormToolkit().getFormToolkit().adapt(label, false, false);
    StyledText textField = getEnvironment().getFormToolkit().createStyledText(container, SWT.SINGLE | SWT.BORDER);
    ButtonEx dateChooserButton = getEnvironment().getFormToolkit().createButtonEx(container, SWT.PUSH | SWT.NO_FOCUS);
    dateChooserButton.setImage(getEnvironment().getIcon(AbstractIcons.DateFieldDate));
    ButtonEx timeChooserButton = getEnvironment().getFormToolkit().createButtonEx(container, SWT.PUSH | SWT.NO_FOCUS);
    timeChooserButton.setImage(getEnvironment().getIcon(AbstractIcons.DateFieldTime));
    timeChooserButton.setVisible(getScoutObject().isHasTime());
    container.setTabList(new Control[]{textField});

    // ui key strokes
    getEnvironment().addKeyStroke(container, new P_DateChooserOpenKeyStroke());
    getEnvironment().addKeyStroke(container, new P_DateTimeChooserOpenKeyStroke());

    // listener
    textField.addKeyListener(new P_ShiftDateListener());
    dateChooserButton.addListener(ButtonEx.SELECTION_ACTION, new P_SwtBrowseButtonListener(TYPE_DATE_CHOOSER));
    timeChooserButton.addListener(ButtonEx.SELECTION_ACTION, new P_SwtBrowseButtonListener(TYPE_TIME_CHOOSER));
    //
    setSwtContainer(container);
    setSwtLabel(label);
    setDateChooserButton(dateChooserButton);
    setTimeChooserButton(timeChooserButton);
    setSwtField(textField);
    // layout
    container.setLayout(new LogicalGridLayout(1, 0));
    dateChooserButton.setLayoutData(LogicalGridDataBuilder.createButton1());
    timeChooserButton.setLayoutData(LogicalGridDataBuilder.createButton2());
  }

  public ButtonEx getDateChooserButton() {
    return m_dateChooserButton;
  }

  public void setDateChooserButton(ButtonEx dateChooserButton) {
    m_dateChooserButton = dateChooserButton;
  }

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
  protected void setFieldEnabled(Control swtField, boolean enabled) {
    if (UiDecorationExtensionPoint.getLookAndFeel().isEnabledAsReadOnly()) {
      if (m_editableSupport == null) {
        m_editableSupport = new TextFieldEditableSupport(getSwtField());
      }
      m_editableSupport.setEditable(enabled);
    }
    else {
      super.setFieldEnabled(swtField, enabled);
    }
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
  protected void setEnabledFromScout(boolean b) {
    super.setEnabledFromScout(b);
    m_dateChooserButton.setEnabled(b);
    m_timeChooserButton.setEnabled(b);
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
        boolean b = getScoutObject().getUIFacade().setDateTimeTextFromUI(text);
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
    getSwtField().setSelection(0, getSwtField().getText().length());
  }

  @Override
  protected void handleSwtFocusLost() {
    getSwtField().setSelection(0, 0);
  }

  private void handleSwtDateChooserAction() {
    if (getDateChooserButton().isVisible() && getDateChooserButton().isEnabled()) {
      Date oldDate = getScoutObject().getValue();
      if (oldDate == null) {
        oldDate = new Date();
      }
      DateChooserDialog dialog = new DateChooserDialog(getSwtField().getShell(), oldDate, getEnvironment());
      final Date newDate = dialog.openDateChooser(getSwtField());
      if (newDate != null) {
        // notify Scout
        Runnable t = new Runnable() {
          @Override
          public void run() {
            getScoutObject().getUIFacade().setDateFromUI(newDate);
          }
        };

        getEnvironment().invokeScoutLater(t, 0);
        // end notify
      }
      getSwtField().setFocus();
    }
  }

  private void handleSwtTimeChooserAction() {
    if (getTimeChooserButton().isVisible() && getTimeChooserButton().isEnabled()) {
      Date d = getScoutObject().getValue();
      if (d == null) {
        d = new Date();
      }
      TimeChooserDialog dialog = new TimeChooserDialog(getSwtField().getShell(), d, getEnvironment());
      Date newDate = dialog.openDateChooser(getSwtField());
      if (newDate != null) {
        getSwtField().setText(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(newDate));
        handleSwtInputVerifier();
      }
      getSwtField().setFocus();
    }
  }

  private class P_SwtBrowseButtonListener implements Listener {
    private int m_buttonId;

    public P_SwtBrowseButtonListener(int buttonId) {
      m_buttonId = buttonId;
    }

    public void handleEvent(Event event) {
      switch (event.type) {
        case ButtonEx.SELECTION_ACTION:
          if (m_buttonId == TYPE_DATE_CHOOSER) {
            handleSwtDateChooserAction();
          }
          else if (m_buttonId == TYPE_TIME_CHOOSER) {
            handleSwtTimeChooserAction();
          }

        default:
          break;
      }
    }
  } // end class P_SwtBrowseButtonListener

  private class P_ShiftDateListener extends KeyAdapter {
    @Override
    public void keyPressed(KeyEvent e) {
      if (getSwtField().isEnabled() && getSwtField().getEditable() && getSwtField().isVisible()) {
        int level = -1;
        int value = 0;
        switch (e.keyCode) {
          case SWT.ARROW_UP:
            if (e.stateMask == SWT.NONE) {
              // shift day up
              level = 0;
              value = 1;
            }
            else if (e.stateMask == SWT.SHIFT) {
              // shift month up
              level = 1;
              value = 1;
            }
            else if (e.stateMask == SWT.CONTROL) {
              level = 2;
              value = 1;
            }
            break;
          case SWT.ARROW_DOWN:
            if (e.stateMask == SWT.NONE) {
              // shift day down
              level = 0;
              value = -1;
            }
            else if (e.stateMask == SWT.SHIFT) {
              // shift month down
              level = 1;
              value = -1;
            }
            else if (e.stateMask == SWT.CONTROL) {
              // shift year down
              level = 2;
              value = -1;
            }
            break;
        }
        if (level >= 0) {
          final int levelFinal = level;
          final int valueFinal = value;
          final String newDisplayText = getSwtField().getText();
          // notify Scout
          Runnable t = new Runnable() {
            @Override
            public void run() {
              // store current (possibly changed) value
              if (!CompareUtility.equals(newDisplayText, getScoutObject().getDisplayText())) {
                getScoutObject().getUIFacade().setDateTimeTextFromUI(newDisplayText);
              }
              getScoutObject().getUIFacade().fireDateShiftActionFromUI(levelFinal, valueFinal);
            }
          };
          getEnvironment().invokeScoutLater(t, 2345);
          // end notify
        }
      }
    }
  }

  private class P_DateChooserOpenKeyStroke extends SwtKeyStroke {
    public P_DateChooserOpenKeyStroke() {
      super(SWT.F2);
    }

    public void handleSwtAction(Event e) {
      handleSwtDateChooserAction();
    }
  } // end class P_DateChooserOpenKeyStroke

  private class P_DateTimeChooserOpenKeyStroke extends SwtKeyStroke {
    public P_DateTimeChooserOpenKeyStroke() {
      super(SWT.F2, SWT.SHIFT);
    }

    public void handleSwtAction(Event e) {
      handleSwtTimeChooserAction();
    }
  } // end class P_DateTimeChooserOpenKeyStroke
}
