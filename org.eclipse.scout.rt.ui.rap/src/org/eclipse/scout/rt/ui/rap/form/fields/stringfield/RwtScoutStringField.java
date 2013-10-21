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
package org.eclipse.scout.rt.ui.rap.form.fields.stringfield;

import java.beans.PropertyChangeEvent;

import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.beans.IPropertyObserver;
import org.eclipse.scout.commons.dnd.TransferObject;
import org.eclipse.scout.commons.holders.Holder;
import org.eclipse.scout.commons.job.JobEx;
import org.eclipse.scout.rt.client.ui.IDNDSupport;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.IStringField;
import org.eclipse.scout.rt.ui.rap.LogicalGridLayout;
import org.eclipse.scout.rt.ui.rap.ext.StatusLabelEx;
import org.eclipse.scout.rt.ui.rap.ext.StyledTextEx;
import org.eclipse.scout.rt.ui.rap.ext.custom.StyledText;
import org.eclipse.scout.rt.ui.rap.extension.UiDecorationExtensionPoint;
import org.eclipse.scout.rt.ui.rap.form.fields.AbstractRwtScoutDndSupport;
import org.eclipse.scout.rt.ui.rap.form.fields.RwtScoutValueFieldComposite;
import org.eclipse.scout.rt.ui.rap.internal.TextFieldEditableSupport;
import org.eclipse.scout.rt.ui.rap.keystroke.IRwtKeyStroke;
import org.eclipse.scout.rt.ui.rap.keystroke.RwtKeyStroke;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;

public class RwtScoutStringField extends RwtScoutValueFieldComposite<IStringField> implements IRwtScoutStringField {

  public static final int DEFAULT_CASE = 0;
  public static final int UPPER_CASE = 1;
  public static final int LOWER_CASE = 2;

  // cache
  private int m_characterType = -1;
  private MouseListener m_linkTrigger;
  // private MouseListener m_linkTrigger;
  private boolean m_validateOnAnyKey;
  private boolean m_linkDecoration;
  private TextFieldEditableSupport m_editableSupport;
  private P_RwtValidateOnAnyKeyModifyListener m_validateOnAnyKeyModifyListener;
  private P_UpperLowerCaseVerifyListener m_upperLowerCaseVerifyListener;

  public RwtScoutStringField() {
  }

  @Override
  protected void initializeUi(Composite parent) {
    Composite container = getUiEnvironment().getFormToolkit().createComposite(parent);
    StatusLabelEx label = getUiEnvironment().getFormToolkit().createStatusLabel(container, getScoutObject());

    int style = SWT.BORDER;
    //Password
    if (getScoutObject().isInputMasked()) {
      style |= SWT.PASSWORD;
    }
    //Multi-Line
    if (getScoutObject().isMultilineText()) {
      style |= SWT.MULTI | SWT.V_SCROLL;
    }
    //Single-Line
    else {
      style |= SWT.SINGLE;
    }
    //Text-Wrap
    if (getScoutObject().isWrapText()) {
      style |= SWT.WRAP;
    }
    StyledText textField = getUiEnvironment().getFormToolkit().createStyledText(container, style);

    setUiContainer(container);
    setUiLabel(label);
    setUiField(textField);

    addDefaultUiListeners(textField);

    // layout
    LogicalGridLayout layout = new LogicalGridLayout(1, 0);
    getUiContainer().setLayout(layout);
  }

  protected void addDefaultUiListeners(StyledText textField) {
    textField.addSelectionListener(new P_RwtTextSelectionListener());
    attachFocusListener(textField, true);
  }

  @Override
  protected void attachScout() {
    super.attachScout();
    IStringField f = getScoutObject();
    setDecorationLinkFromScout(f.isDecorationLink());
    setFormatFromScout(f.getFormat());
    setMaxLengthFromScout(f.getMaxLength());
    setValidateOnAnyKeyFromScout(f.isValidateOnAnyKey());
    setSelectionFromScout(f.getSelectionStart(), f.getSelectionEnd());
    setTextWrapFromScout(f.isWrapText());

    attachDndSupport();
  }

  protected void attachDndSupport() {
    if (UiDecorationExtensionPoint.getLookAndFeel().isDndSupportEnabled()) {
      new P_DndSupport(getScoutObject(), getScoutObject(), getUiField());
    }
  }

  @Override
  public StyledTextEx getUiField() {
    return (StyledTextEx) super.getUiField();
  }

  @Override
  protected void setFieldEnabled(Control swtField, boolean enabled) {
    if (m_editableSupport == null) {
      m_editableSupport = new TextFieldEditableSupport(getUiField());
    }
    m_editableSupport.setEditable(enabled);
  }

  @Override
  protected IRwtKeyStroke[] getUiKeyStrokes() {
    if (getScoutObject().isMultilineText()) {
      //register CR but do not set the RWT.ACTIVE_KEYS property. This would disable typing newlines in the text field
      return new IRwtKeyStroke[]{new RwtKeyStroke(SWT.CR, SWT.NONE, false) {
        @Override
        public void handleUiAction(Event e) {
          e.doit = false;
        }
      }};
    }
    return super.getUiKeyStrokes();
  }

  protected void setDecorationLinkFromScout(boolean b) {
    if (m_linkDecoration != b) {
      m_linkDecoration = b;
      if (m_linkDecoration) {
        m_linkTrigger = new P_RwtLinkTrigger();
        getUiField().addMouseListener(m_linkTrigger);
        getUiField().setCursor(getUiField().getDisplay().getSystemCursor(SWT.CURSOR_HAND));
        setForegroundFromScout(getScoutObject().getForegroundColor());
      }
      else {
        getUiField().removeMouseListener(m_linkTrigger);
        m_linkTrigger = null;
        getUiField().setCursor(null);
        setForegroundFromScout(getScoutObject().getForegroundColor());
      }
    }
  }

  @Override
  protected void setForegroundFromScout(String scoutColor) {
    if (scoutColor == null && m_linkDecoration) {
      scoutColor = "0000FF";
    }
    super.setForegroundFromScout(scoutColor);
  }

  protected void setFormatFromScout(String s) {
    if (IStringField.FORMAT_UPPER.equals(s)) {
      m_characterType = UPPER_CASE;
      addUpperLowerCaseVerifyListener();
    }
    else if (IStringField.FORMAT_LOWER.equals(s)) {
      m_characterType = LOWER_CASE;
      addUpperLowerCaseVerifyListener();
    }
    else {
      m_characterType = DEFAULT_CASE;
      removeUpperLowerCaseVerifyListener();
    }
  }

  protected void addUpperLowerCaseVerifyListener() {
    if (m_upperLowerCaseVerifyListener == null) {
      m_upperLowerCaseVerifyListener = new P_UpperLowerCaseVerifyListener();
      getUiField().addVerifyListener(m_upperLowerCaseVerifyListener);
    }
  }

  protected void removeUpperLowerCaseVerifyListener() {
    if (m_upperLowerCaseVerifyListener != null) {
      getUiField().removeVerifyListener(m_upperLowerCaseVerifyListener);
      m_upperLowerCaseVerifyListener = null;
    }
  }

  protected void setMaxLengthFromScout(int n) {
    getUiField().setTextLimit(n);
  }

  protected void setDoInsertFromScout(String s) {
    //XXX rap
    /*
    if (s != null && s.length() > 0) {
      StyledText field = getUiField();
      int offset = field.getCaretOffset();
      int a = field.getSelection().x;
      int b = field.getSelection().y;
      String uiText = field.getText();
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
        field.setText(builder.toString());
      }
    }
    */
  }

  @Override
  protected void setDisplayTextFromScout(String newText) {
    StyledText field = getUiField();
    String oldText = field.getText();
    if (newText == null) {
      newText = "";
    }
    if (oldText == null) {
      oldText = "";
    }
    if (oldText.equals(newText)) {
      return;
    }
    try {
      getUpdateUiFromScoutLock().acquire();
      int startIndex = field.getSelection().x;
      int endIndex = field.getSelection().y;
      if (startIndex == endIndex && newText.length() != oldText.length()) {
        //No selection, just a cursor position and text length has changed.
        if (startIndex >= oldText.length()) {
          //cursor was at the end, put it at the end of the new text:
          startIndex = newText.length();
        }
        else if (newText.endsWith(oldText.substring(startIndex))) {
          //cursor was in the middle of the old text. If both end matches, the new cursor position is before the common suffix.
          startIndex = newText.length() - oldText.substring(startIndex).length();
        }
        //else: in the else case, let the startIndex as it was.
        endIndex = startIndex;
      }
      field.setText(newText);

      // restore selection and caret
      int textLength = field.getText().length();
      startIndex = Math.min(Math.max(startIndex, 0), textLength);
      endIndex = Math.min(Math.max(endIndex, 0), textLength);
      field.setSelection(startIndex, endIndex);
    }
    finally {
      getUpdateUiFromScoutLock().release();
    }
  }

  protected void setValidateOnAnyKeyFromScout(boolean b) {
    m_validateOnAnyKey = b;
    if (b) {
      addValidateOnAnyKeyModifyListener();
    }
    else {
      removeValidateOnAnyKeyModifyListener();
    }
  }

  protected void addValidateOnAnyKeyModifyListener() {
    if (m_validateOnAnyKeyModifyListener == null) {
      m_validateOnAnyKeyModifyListener = new P_RwtValidateOnAnyKeyModifyListener();
      getUiField().addModifyListener(m_validateOnAnyKeyModifyListener);
    }
  }

  protected void removeValidateOnAnyKeyModifyListener() {
    if (m_validateOnAnyKeyModifyListener != null) {
      getUiField().removeModifyListener(m_validateOnAnyKeyModifyListener);
      m_validateOnAnyKeyModifyListener = null;
    }
  }

  protected void setSelectionFromScout(int startIndex, int endIndex) {
    StyledText field = getUiField();
    int start = field.getSelection().x;
    int end = field.getSelection().y;
    if (startIndex < 0) {
      startIndex = start;
    }
    if (endIndex < 0) {
      endIndex = end;
    }
    field.setSelection(startIndex, endIndex);
  }

  protected void setTextWrapFromScout(boolean booleanValue) {
    if (getScoutObject().isMultilineText()) {
      //XXX rap       getUiField().setWordWrap(booleanValue);
    }
  }

  /**
   * scout property handler override
   */
  @Override
  protected void handleScoutPropertyChange(String name, Object newValue) {
    super.handleScoutPropertyChange(name, newValue);
    if (name.equals(IStringField.PROP_DECORATION_LINK)) {
      setDecorationLinkFromScout(((Boolean) newValue).booleanValue());
    }
    else if (name.equals(IStringField.PROP_MAX_LENGTH)) {
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
    else if (name.equals(IStringField.PROP_WRAP_TEXT)) {
      setTextWrapFromScout(((Boolean) newValue).booleanValue());
    }
  }

  protected void handleUiLinkTrigger() {
    final String text = getUiField().getText();
    // notify Scout
    Runnable t = new Runnable() {
      @Override
      public void run() {
        getScoutObject().getUIFacade().fireLinkActionFromUI(text);
      }
    };
    getUiEnvironment().invokeScoutLater(t, 0);
    // end notify
  }

  protected void setSelectionFromUi(final int startIndex, final int endIndex) {
    if (getUpdateUiFromScoutLock().isAcquired()) {
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
    getUiEnvironment().invokeScoutLater(t, 0);
  }

  @Override
  protected void handleUiInputVerifier(boolean doit) {
    if (!doit) {
      return;
    }
    final String text = getUiField().getText();
    // only handle if text has changed
    if (CompareUtility.equals(text, getScoutObject().getDisplayText()) && getScoutObject().getErrorStatus() == null) {
      return;
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
    JobEx job = getUiEnvironment().invokeScoutLater(t, 0);
    try {
      job.join(2345);
    }
    catch (InterruptedException e) {
      //nop
    }
    doit = result.getValue();
    getUiEnvironment().dispatchImmediateUiJobs();
  }

  @Override
  protected void handleUiFocusGained() {
    super.handleUiFocusGained();

    if (isSelectAllOnFocusEnabled()) {
      getUiField().setSelection(0, getUiField().getText().length());
    }
  }

  @Override
  protected boolean isSelectAllOnFocusEnabled() {
    return super.isSelectAllOnFocusEnabled() && getScoutObject().isSelectAllOnFocus();
  }

  private class P_UpperLowerCaseVerifyListener implements VerifyListener {
    private static final long serialVersionUID = 1L;

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

  private class P_RwtValidateOnAnyKeyModifyListener implements ModifyListener {
    private static final long serialVersionUID = 1L;

    /*
     * Do not call handleUiInputVerifier(), this can lead to endless loops.
     */
    @Override
    public void modifyText(ModifyEvent e) {
      if (!m_validateOnAnyKey) {
        return;
      }

      if (getUpdateUiFromScoutLock().isReleased()) {
        sendVerifyToScoutAndIgnoreResponses();
      }
    }

    /*
     * Do not call handleUiInputVerifier(), this can lead to endless loops.
     */
    private void sendVerifyToScoutAndIgnoreResponses() {
      final String text = getUiField().getText();
      // notify Scout
      Runnable t = new Runnable() {
        @Override
        public void run() {
          getScoutObject().getUIFacade().setTextFromUI(text);
        }
      };
      getUiEnvironment().invokeScoutLater(t, 0);
    }
  } // end class P_RwtTextListener

  private class P_RwtLinkTrigger extends MouseAdapter {
    private static final long serialVersionUID = 1L;

    @Override
    public void mouseDoubleClick(MouseEvent e) {
      handleUiLinkTrigger();
    }
  } // end class P_RwtLinkTrigger

  private class P_RwtTextSelectionListener extends SelectionAdapter {
    private static final long serialVersionUID = 1L;

    @Override
    public void widgetSelected(SelectionEvent e) {
      setSelectionFromUi(e.x, e.y);
    }
  } // end class P_RwtTextSelectionListener

  private class P_DndSupport extends AbstractRwtScoutDndSupport {
    public P_DndSupport(IPropertyObserver scoutObject, IDNDSupport scoutDndSupportable, Control control) {
      super(scoutObject, scoutDndSupportable, control, RwtScoutStringField.this.getUiEnvironment());
    }

    @Override
    protected TransferObject handleUiDragRequest() {
      // will never be called here, since handleDragSetData never calls super.
      final Holder<TransferObject> result = new Holder<TransferObject>(TransferObject.class, null);
      Runnable t = new Runnable() {
        @Override
        public void run() {
          TransferObject scoutTransferable = RwtScoutStringField.this.getScoutObject().getUIFacade().fireDragRequestFromUI();
          result.setValue(scoutTransferable);
        }
      };
      JobEx job = getUiEnvironment().invokeScoutLater(t, 2345);
      try {
        job.join(2345);
      }
      catch (InterruptedException e) {
        //nop
      }
      return result.getValue();
    }

    @Override
    protected void handleUiDropAction(DropTargetEvent event, final TransferObject scoutTransferObject) {
      Runnable job = new Runnable() {
        @Override
        public void run() {
          if (getScoutObject().isEnabled()) {
            getScoutObject().getUIFacade().fireDropActionFromUi(scoutTransferObject);
          }
        }
      };
      getUiEnvironment().invokeScoutLater(job, 200);
    }
  }// end class P_DndSupport

}
