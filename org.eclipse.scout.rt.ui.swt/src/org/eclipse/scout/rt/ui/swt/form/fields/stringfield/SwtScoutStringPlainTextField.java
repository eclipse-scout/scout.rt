/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Rene Eigenheer - Patch from Bug 359677
 ******************************************************************************/
package org.eclipse.scout.rt.ui.swt.form.fields.stringfield;

import java.beans.PropertyChangeEvent;

import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.beans.IPropertyObserver;
import org.eclipse.scout.commons.dnd.TransferObject;
import org.eclipse.scout.commons.holders.Holder;
import org.eclipse.scout.commons.job.JobEx;
import org.eclipse.scout.rt.client.ui.IDNDSupport;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.IStringField;
import org.eclipse.scout.rt.ui.swt.ISwtEnvironment;
import org.eclipse.scout.rt.ui.swt.LogicalGridLayout;
import org.eclipse.scout.rt.ui.swt.ext.StatusLabelEx;
import org.eclipse.scout.rt.ui.swt.form.fields.AbstractSwtScoutDndSupport;
import org.eclipse.scout.rt.ui.swt.form.fields.SwtScoutValueFieldComposite;
import org.eclipse.scout.rt.ui.swt.internal.TextFieldEditableSupport;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Text;

/**
 * 
 */
public class SwtScoutStringPlainTextField extends SwtScoutValueFieldComposite<IStringField> {
  public static final int DEFAULT_CASE = 0;
  public static final int UPPER_CASE = 1;
  public static final int LOWER_CASE = 2;

  private int m_characterType = -1;
  private boolean m_validateOnAnyKey;
  private Point m_backupSelection = null;
  private TextFieldEditableSupport m_editableSupport;

  /**
   * uses SWT Text Widget to implement masked input fields (password)
   */
  public SwtScoutStringPlainTextField() {
  }

  @Override
  protected void initializeSwt(Composite parent) {
    Composite container = getEnvironment().getFormToolkit().createComposite(parent);
    StatusLabelEx label = getEnvironment().getFormToolkit().createStatusLabel(container, getEnvironment(), getScoutObject());

    int style = SWT.BORDER;
    if (getScoutObject().isInputMasked()) {
      style |= SWT.PASSWORD;
    }
    if (getScoutObject().isMultilineText()) {
      style |= SWT.MULTI | SWT.V_SCROLL;
    }
    else {
      style |= SWT.SINGLE;
    }
    if (getScoutObject().isWrapText()) {
      style |= SWT.WRAP;
    }
    Text textField = getEnvironment().getFormToolkit().createText(container, style);

    setSwtContainer(container);
    setSwtLabel(label);
    setSwtField(textField);

    addDefaultUiListeners(textField);

    // layout
    LogicalGridLayout layout = new LogicalGridLayout(1, 0);
    getSwtContainer().setLayout(layout);

  }

  protected void addDefaultUiListeners(Text textField) {
    textField.addModifyListener(new P_SwtTextListener());
    textField.addSelectionListener(new P_SwtTextSelectionListener());
    textField.addVerifyListener(new P_TextVerifyListener());
  }

  @Override
  protected void attachScout() {
    super.attachScout();
    IStringField f = getScoutObject();
    setFormatFromScout(f.getFormat());
    setMaxLengthFromScout(f.getMaxLength());
    setValidateOnAnyKeyFromScout(f.isValidateOnAnyKey());
    setSelectionFromScout(f.getSelectionStart(), f.getSelectionEnd());

    // dnd support
    new P_DndSupport(getScoutObject(), getScoutObject(), getSwtField(), getEnvironment());

  }

  @Override
  public Text getSwtField() {
    return (Text) super.getSwtField();
  }

  @Override
  protected void setFieldEnabled(Control swtField, boolean enabled) {
    if (m_editableSupport == null) {
      m_editableSupport = new TextFieldEditableSupport(getSwtField());
    }
    m_editableSupport.setEditable(enabled);
  }

  protected void setFormatFromScout(String s) {

    if (IStringField.FORMAT_UPPER.equals(s)) {
      m_characterType = UPPER_CASE;
    }
    else if (IStringField.FORMAT_LOWER.equals(s)) {
      m_characterType = LOWER_CASE;
    }
    else {
      m_characterType = DEFAULT_CASE;
    }
  }

  protected void setMaxLengthFromScout(int n) {
    getSwtField().setTextLimit(n);
  }

  protected void setDoInsertFromScout(String s) {
    if (s != null && s.length() > 0) {
      Text swtField = getSwtField();
      int offset = swtField.getCaretPosition();
      int a = swtField.getSelection().x;
      int b = swtField.getSelection().y;
      String uiText = swtField.getText();
      StringBuilder builder = new StringBuilder(uiText);
      if (a >= 0 && b > a) {
        builder.replace(a, b, s);
      }
      else if (offset >= 0) {
        builder.insert(offset, s);
      }
      else {
        builder = null;
      }
      if (builder != null) {
        swtField.setText(builder.toString());
      }

    }
  }

  @Override
  protected void setDisplayTextFromScout(String s) {
    //loop detection
    if (m_validateOnAnyKey && getSwtField().isFocusControl()) {
      return;
    }
    Text swtField = getSwtField();
    String oldText = swtField.getText();
    if (s == null) {
      s = "";
    }
    if (oldText == null) {
      oldText = "";
    }
    if (oldText.equals(s)) {
      return;
    }
    //
    int startIndex = swtField.getSelection().x;
    int caretOffset = swtField.getCaretPosition();
    int endIndex = -swtField.getSelection().y;
    swtField.setText(s);
    // restore selection and caret
    int textLength = swtField.getText().length();
    if (caretOffset > 0) {
      startIndex = Math.min(Math.max(startIndex, 0), textLength);
      endIndex = Math.min(Math.max(endIndex, 0), textLength);
      m_backupSelection = new Point(startIndex, endIndex);
      swtField.setSelection(startIndex, endIndex);
    }
  }

  protected void setValidateOnAnyKeyFromScout(boolean b) {
    m_validateOnAnyKey = b;
  }

  protected void setSelectionFromScout(int startIndex, int endIndex) {
    Text swtField = getSwtField();
    int start = swtField.getSelection().x;
    int end = swtField.getSelection().y;
    if (startIndex < 0) {
      startIndex = start;
    }
    if (endIndex < 0) {
      endIndex = end;
    }
    // swt sets the caret itself. If startIndex > endIndex it is placed at the beginning.
    m_backupSelection = new Point(startIndex, endIndex);
    swtField.setSelection(startIndex, endIndex);
  }

  /**
   * scout property handler override
   */
  @Override
  protected void handleScoutPropertyChange(String name, Object newValue) {
    super.handleScoutPropertyChange(name, newValue);
    if (name.equals(IStringField.PROP_MAX_LENGTH)) {
      setMaxLengthFromScout(((Number) newValue).intValue());
    }
    else if (name.equals(IStringField.PROP_INSERT_TEXT)) {
      setDoInsertFromScout((String) newValue);
    }
    else if (name.equals(IStringField.PROP_VALIDATE_ON_ANY_KEY)) {
      setValidateOnAnyKeyFromScout(((Boolean) newValue).booleanValue());
    }
    else if (name.equals(IStringField.PROP_SELECTION_START)) {
      IStringField f = getScoutObject();
      setSelectionFromScout(f.getSelectionStart(), f.getSelectionEnd());
    }
    else if (name.equals(IStringField.PROP_SELECTION_END)) {
      IStringField f = getScoutObject();
      setSelectionFromScout(f.getSelectionStart(), f.getSelectionEnd());
    }
  }

  protected void setSelectionFromSwt(final int startIndex, final int endIndex) {
    if (getUpdateSwtFromScoutLock().isAcquired()) {
      return;
    }
    Runnable t = new Runnable() {
      @Override
      public void run() {
        try {
          addIgnoredScoutEvent(PropertyChangeEvent.class, IStringField.PROP_SELECTION_START);
          addIgnoredScoutEvent(PropertyChangeEvent.class, IStringField.PROP_SELECTION_END);
          //
          getScoutObject().getUIFacade().setSelectionFromUI(startIndex, endIndex);
        }
        finally {
          removeIgnoredScoutEvent(PropertyChangeEvent.class, IStringField.PROP_SELECTION_START);
          removeIgnoredScoutEvent(PropertyChangeEvent.class, IStringField.PROP_SELECTION_END);
        }
      }
    };
    getEnvironment().invokeScoutLater(t, 0);
  }

  @Override
  protected boolean filterKeyEvent(Event e) {
    // veto for CR to ensure newline
    if (getScoutObject().isMultilineText() && (e.keyCode == SWT.CR || e.keyCode == SWT.KEYPAD_CR)) {
      return false;
    }
    else {
      return super.filterKeyEvent(e);
    }
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
        boolean b = getScoutObject().getUIFacade().setTextFromUI(text);
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
    if (getScoutObject().isSelectAllOnFocus()) {
      getSwtField().setSelection(0, getSwtField().getText().length());
    }
    else {
      // restore selction
      if (m_backupSelection == null) {
        m_backupSelection = new Point(0, 0);
      }
      getSwtField().setSelection(m_backupSelection);
    }
  }

  @Override
  protected void handleSwtFocusLost() {
    m_backupSelection = getSwtField().getSelection();
    getSwtField().setSelection(0, 0);
  }

  private class P_TextVerifyListener implements VerifyListener {
    @Override
    public void verifyText(VerifyEvent e) {
      switch (m_characterType) {
        case UPPER_CASE:
          e.text = e.text.toUpperCase();
          break;
        case LOWER_CASE:
          e.text = e.text.toLowerCase();
          break;
      }
    }
  } // end class P_TextVerifyListener

  private class P_SwtTextListener implements ModifyListener {
    /*
     * Do not call handleSwingInputVerifier(), this can lead to endless loops.
     */
    @Override
    public void modifyText(ModifyEvent e) {
      if (m_validateOnAnyKey) {
        if (getUpdateSwtFromScoutLock().isReleased()) {
          sendVerifyToScoutAndIgnoreResponses();
        }
      }
    }

    /*
     * Do not call handleSwingInputVerifier(), this can lead to endless loops.
     */
    private void sendVerifyToScoutAndIgnoreResponses() {
      final String text = getSwtField().getText();
      // notify Scout
      Runnable t = new Runnable() {
        @Override
        public void run() {
          getScoutObject().getUIFacade().setTextFromUI(text);
        }
      };
      getEnvironment().invokeScoutLater(t, 0);
    }
  } // end class P_SwtTextListener

  private class P_SwtTextSelectionListener extends SelectionAdapter {
    @Override
    public void widgetSelected(SelectionEvent e) {
      setSelectionFromSwt(e.x, e.y);
    }
  }

  private class P_DndSupport extends AbstractSwtScoutDndSupport {
    public P_DndSupport(IPropertyObserver scoutObject, IDNDSupport scoutDndSupportable, Control control, ISwtEnvironment environment) {
      super(scoutObject, scoutDndSupportable, control, environment);
    }

    @Override
    protected TransferObject handleSwtDragRequest() {
      // will never be called here, since handleDragSetData never calls super.
      final Holder<TransferObject> result = new Holder<TransferObject>(TransferObject.class, null);
      Runnable t = new Runnable() {
        @Override
        public void run() {
          TransferObject scoutTransferable = SwtScoutStringPlainTextField.this.getScoutObject().getUIFacade().fireDragRequestFromUI();
          result.setValue(scoutTransferable);
        }
      };
      JobEx job = getEnvironment().invokeScoutLater(t, 2345);
      try {
        job.join(2345);
      }
      catch (InterruptedException e) {
        //nop
      }
      return result.getValue();
    }

    @Override
    protected void handleSwtDropAction(DropTargetEvent event, final TransferObject scoutTransferObject) {
      Runnable job = new Runnable() {
        @Override
        public void run() {
          if (getScoutObject().isEnabled()) {
            getScoutObject().getUIFacade().fireDropActionFromUi(scoutTransferObject);
          }
        }
      };
      getEnvironment().invokeScoutLater(job, 200);
    }
  }// end class P_DndSupport
}
