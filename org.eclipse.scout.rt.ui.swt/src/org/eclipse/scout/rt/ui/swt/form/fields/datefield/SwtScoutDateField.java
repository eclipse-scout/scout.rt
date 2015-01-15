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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DateFormat;
import java.util.Date;

import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.holders.Holder;
import org.eclipse.scout.commons.job.JobEx;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.datefield.IDateField;
import org.eclipse.scout.rt.shared.AbstractIcons;
import org.eclipse.scout.rt.ui.swt.LogicalGridLayout;
import org.eclipse.scout.rt.ui.swt.action.menu.MenuPositionCorrectionListener;
import org.eclipse.scout.rt.ui.swt.action.menu.SwtContextMenuMarkerComposite;
import org.eclipse.scout.rt.ui.swt.action.menu.SwtScoutContextMenu;
import org.eclipse.scout.rt.ui.swt.action.menu.text.StyledTextAccess;
import org.eclipse.scout.rt.ui.swt.ext.StatusLabelEx;
import org.eclipse.scout.rt.ui.swt.form.fields.LogicalGridDataBuilder;
import org.eclipse.scout.rt.ui.swt.form.fields.SwtScoutBasicFieldComposite;
import org.eclipse.scout.rt.ui.swt.form.fields.datefield.chooser.DateChooserDialog;
import org.eclipse.scout.rt.ui.swt.form.fields.datefield.chooser.TimeChooserDialog;
import org.eclipse.scout.rt.ui.swt.internal.TextFieldEditableSupport;
import org.eclipse.scout.rt.ui.swt.keystroke.SwtKeyStroke;
import org.eclipse.scout.rt.ui.swt.util.SwtUtility;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;

public class SwtScoutDateField extends SwtScoutBasicFieldComposite<IDateField> implements ISwtScoutDateField {
  public static final int TYPE_TIME_CHOOSER = 1;
  public static final int TYPE_DATE_CHOOSER = 2;

  private Button m_dateChooserButton;
  private Button m_timeChooserButton;
  private boolean m_hasTime;
  private TextFieldEditableSupport m_editableSupport;

  private SwtContextMenuMarkerComposite m_menuMarkerComposite;
  private SwtScoutContextMenu m_contextMenu;
  private PropertyChangeListener m_contextMenuVisibilityListener;

  @Override
  protected void initializeSwt(Composite parent) {
    Composite container = getEnvironment().getFormToolkit().createComposite(parent);
    StatusLabelEx label = getEnvironment().getFormToolkit().createStatusLabel(container, getEnvironment(), getScoutObject());

    m_menuMarkerComposite = new SwtContextMenuMarkerComposite(container, getEnvironment());
    getEnvironment().getFormToolkit().adapt(m_menuMarkerComposite);
    m_menuMarkerComposite.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        getSwtField().setFocus();
        m_contextMenu.getSwtMenu().setVisible(true);
      }
    });

    StyledText textField = getEnvironment().getFormToolkit().createStyledText(m_menuMarkerComposite, SWT.SINGLE);
    textField.setAlignment(SwtUtility.getHorizontalAlignment(getScoutObject().getGridData().horizontalAlignment));
    textField.setWrapIndent(textField.getWrapIndent());
    textField.setMargins(2, 2, 2, 2);
    Button dateChooserButton = getEnvironment().getFormToolkit().createButton(container, SWT.PUSH);
    dateChooserButton.setImage(getEnvironment().getIcon(AbstractIcons.DateFieldDate));
    Button timeChooserButton = getEnvironment().getFormToolkit().createButton(container, SWT.PUSH);
    timeChooserButton.setImage(getEnvironment().getIcon(AbstractIcons.DateFieldTime));
    timeChooserButton.setVisible(getScoutObject().isHasTime());
    container.setTabList(new Control[]{m_menuMarkerComposite});

    addModifyListenerForBasicField(textField);

    // ui key strokes
    getEnvironment().addKeyStroke(container, new P_DateChooserOpenKeyStroke());
    getEnvironment().addKeyStroke(container, new P_DateTimeChooserOpenKeyStroke());

    // listener
    textField.addKeyListener(new P_ShiftDateListener());
    dateChooserButton.addSelectionListener(new P_SwtBrowseButtonListener(TYPE_DATE_CHOOSER));
    timeChooserButton.addSelectionListener(new P_SwtBrowseButtonListener(TYPE_TIME_CHOOSER));
    //
    setSwtContainer(container);
    setSwtLabel(label);
    setDateChooserButton(dateChooserButton);
    setTimeChooserButton(timeChooserButton);
    setSwtField(textField);
    // layout
    container.setLayout(new LogicalGridLayout(1, 0));
    m_menuMarkerComposite.setLayoutData(LogicalGridDataBuilder.createField(((IFormField) getScoutObject()).getGridData()));
    dateChooserButton.setLayoutData(LogicalGridDataBuilder.createButton1());
    timeChooserButton.setLayoutData(LogicalGridDataBuilder.createButton2());
  }

  protected void installContextMenu() {
    m_menuMarkerComposite.setMarkerVisible(getScoutObject().getContextMenu().isVisible());
    m_contextMenuVisibilityListener = new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        if (IMenu.PROP_VISIBLE.equals(evt.getPropertyName())) {
          final boolean markerVisible = getScoutObject().getContextMenu().isVisible();
          getEnvironment().invokeSwtLater(new Runnable() {
            @Override
            public void run() {
              m_menuMarkerComposite.setMarkerVisible(markerVisible);
            }
          });
        }
      }
    };
    getScoutObject().getContextMenu().addPropertyChangeListener(m_contextMenuVisibilityListener);

    m_contextMenu = new SwtScoutContextMenu(getSwtField().getShell(), getScoutObject().getContextMenu(), getEnvironment());
    if (getDateChooserButton() != null) {
      getDateChooserButton().setMenu(m_contextMenu.getSwtMenu());
    }
    if (getTimeChooserButton() != null) {
      getTimeChooserButton().setMenu(m_contextMenu.getSwtMenu());
    }

    SwtScoutContextMenu fieldMenu = new SwtScoutContextMenu(getSwtField().getShell(), getScoutObject().getContextMenu(), getEnvironment(),
        getScoutObject().isAutoAddDefaultMenus() ? new StyledTextAccess(getSwtField()) : null, getScoutObject().isAutoAddDefaultMenus() ? getSwtField() : null);
    getSwtField().setMenu(fieldMenu.getSwtMenu());
    // correction of menu position
    getSwtField().addListener(SWT.MenuDetect, new MenuPositionCorrectionListener(getSwtField()));
  }

  protected void uninstallContextMenu() {
    if (m_contextMenuVisibilityListener != null) {
      getScoutObject().getContextMenu().removePropertyChangeListener(m_contextMenuVisibilityListener);
      m_contextMenuVisibilityListener = null;
    }
  }

  @Override
  protected void attachScout() {
    super.attachScout();
    installContextMenu();
  }

  @Override
  protected void detachScout() {
    uninstallContextMenu();
    super.detachScout();
  }

  @Override
  public Button getDateChooserButton() {
    return m_dateChooserButton;
  }

  public void setDateChooserButton(Button dateChooserButton) {
    m_dateChooserButton = dateChooserButton;
  }

  @Override
  public Button getTimeChooserButton() {
    return m_timeChooserButton;
  }

  public void setTimeChooserButton(Button timeChooserButton) {
    m_timeChooserButton = timeChooserButton;
  }

  @Override
  public StyledText getSwtField() {
    return (StyledText) super.getSwtField();
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
  }

  @Override
  protected void setEnabledFromScout(boolean b) {
    super.setEnabledFromScout(b);
    m_dateChooserButton.setEnabled(b);
    m_timeChooserButton.setEnabled(b);
  }

  @Override
  protected String getText() {
    return getSwtField().getText();
  }

  @Override
  protected void setText(String text) {
    getSwtField().setText(text);
  }

  @Override
  protected Point getSelection() {
    return getSwtField().getSelection();
  }

  @Override
  protected void setSelection(int startIndex, int endIndex) {
    getSwtField().setSelection(startIndex, endIndex);
  }

  @Override
  protected int getCaretOffset() {
    return getSwtField().getCaretOffset();
  }

  @Override
  protected void setCaretOffset(int caretPosition) {
    getSwtField().setCaretOffset(caretPosition);
  }

  @Override
  protected TextFieldEditableSupport createEditableSupport() {
    return new TextFieldEditableSupport(getSwtField());
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
    scheduleSelectAll();
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
      try {
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
      }
      finally {
        if (!getSwtField().isDisposed()) {
          getSwtField().setFocus();
        }
      }
    }
  }

  private void handleSwtTimeChooserAction() {
    if (getTimeChooserButton().isVisible() && getTimeChooserButton().isEnabled()) {
      Date d = getScoutObject().getValue();
      if (d == null) {
        d = new Date();
      }
      try {
        TimeChooserDialog dialog = new TimeChooserDialog(getSwtField().getShell(), d, getEnvironment());
        Date newDate = dialog.openDateChooser(getSwtField());
        if (newDate != null) {
          getSwtField().setText(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(newDate));
          handleSwtInputVerifier();
        }
      }
      finally {
        if (!getSwtField().isDisposed()) {
          getSwtField().setFocus();
        }
      }
    }
  }

  private class P_SwtBrowseButtonListener extends SelectionAdapter {
    private int m_buttonId;

    public P_SwtBrowseButtonListener(int buttonId) {
      m_buttonId = buttonId;
    }

    @Override
    public void widgetSelected(SelectionEvent e) {
      if (m_buttonId == TYPE_DATE_CHOOSER) {
        handleSwtDateChooserAction();
      }
      else if (m_buttonId == TYPE_TIME_CHOOSER) {
        handleSwtTimeChooserAction();
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

    @Override
    public void handleSwtAction(Event e) {
      handleSwtDateChooserAction();
    }
  } // end class P_DateChooserOpenKeyStroke

  private class P_DateTimeChooserOpenKeyStroke extends SwtKeyStroke {
    public P_DateTimeChooserOpenKeyStroke() {
      super(SWT.F2, SWT.SHIFT);
    }

    @Override
    public void handleSwtAction(Event e) {
      handleSwtTimeChooserAction();
    }
  } // end class P_DateTimeChooserOpenKeyStroke
}
