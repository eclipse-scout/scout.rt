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
package org.eclipse.scout.rt.ui.swt.form.fields.stringfield;

import java.beans.PropertyChangeEvent;
import java.lang.reflect.Method;

import org.eclipse.core.runtime.Status;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.beans.IPropertyObserver;
import org.eclipse.scout.commons.dnd.TransferObject;
import org.eclipse.scout.commons.holders.Holder;
import org.eclipse.scout.commons.job.JobEx;
import org.eclipse.scout.rt.client.ui.IDNDSupport;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.IStringField;
import org.eclipse.scout.rt.ui.swt.Activator;
import org.eclipse.scout.rt.ui.swt.ISwtEnvironment;
import org.eclipse.scout.rt.ui.swt.LogicalGridLayout;
import org.eclipse.scout.rt.ui.swt.ext.StatusLabelEx;
import org.eclipse.scout.rt.ui.swt.ext.StyledTextEx;
import org.eclipse.scout.rt.ui.swt.form.fields.AbstractSwtScoutDndSupport;
import org.eclipse.scout.rt.ui.swt.form.fields.SwtScoutValueFieldComposite;
import org.eclipse.scout.rt.ui.swt.internal.StyledTextFieldUndoRedoSupport;
import org.eclipse.scout.rt.ui.swt.internal.TextFieldEditableSupport;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.osgi.framework.Version;

public class SwtScoutStringField extends SwtScoutValueFieldComposite<IStringField> implements ISwtScoutStringField {
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
  private StyledTextFieldUndoRedoSupport m_undoRedoSupport;

  public SwtScoutStringField() {
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
    StyledText textField = getEnvironment().getFormToolkit().createStyledText(container, style);

    //Necessary for backward compatibility to Eclipse 3.4 needed for Lotus Notes 8.5.2
    Version frameworkVersion = new Version(Activator.getDefault().getBundle().getBundleContext().getProperty("osgi.framework.version"));
    if (frameworkVersion.getMajor() == 3
        && frameworkVersion.getMinor() <= 5) {
      //FIXME we need a bugfix for bug 350237
    }
    else {
      try {
        //Make sure the wrap indent is the same as the indent so that the text is vertically aligned
        Method setWrapIndent = StyledText.class.getMethod("setWrapIndent", int.class);
        setWrapIndent.invoke(textField, textField.getIndent());
        //Funnily enough if style is set to multi line the margins are different.
        //In order to align text with other fields a correction is necessary.
        if ((textField.getStyle() & SWT.MULTI) != 0) {
          Method setMargins = StyledText.class.getMethod("setMargins", int.class, int.class, int.class, int.class);
          setMargins.invoke(textField, 2, 2, 2, 2);
        }
      }
      catch (Exception e) {
        Activator.getDefault().getLog().log(new Status(Status.WARNING, Activator.PLUGIN_ID, "could not access methods 'setWrapIndent' and 'setMargins' on 'StyledText'.", e));
      }
    }
    //
    textField.addDisposeListener(new DisposeListener() {
      @Override
      public void widgetDisposed(DisposeEvent e) {
        if (m_undoRedoSupport != null && !m_undoRedoSupport.isDisposed()) {
          m_undoRedoSupport.dispose();
        }
      }
    });

    setSwtContainer(container);
    setSwtLabel(label);
    setSwtField(textField);

    addDefaultUiListeners(textField);
    if (m_undoRedoSupport == null) {
      m_undoRedoSupport = new StyledTextFieldUndoRedoSupport(getSwtField());
    }

    // layout
    LogicalGridLayout layout = new LogicalGridLayout(1, 0);
    getSwtContainer().setLayout(layout);
  }

  protected void addDefaultUiListeners(StyledText textField) {
    textField.addModifyListener(new P_SwtTextListener());
    textField.addSelectionListener(new P_SwtTextSelectionListener());
    textField.addVerifyListener(new P_TextVerifyListener());
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

    // dnd support
    new P_DndSupport(getScoutObject(), getScoutObject(), getSwtField(), getEnvironment());

    //@16.03.11 sle: clear undo/redo stack. It must not be possible to undo the initial value in field
    if (m_undoRedoSupport != null) {
      m_undoRedoSupport.clearStacks();
    }
  }

  @Override
  public StyledTextEx getSwtField() {
    return (StyledTextEx) super.getSwtField();
  }

  @Override
  protected void setFieldEnabled(Control swtField, boolean enabled) {
    if (m_editableSupport == null) {
      m_editableSupport = new TextFieldEditableSupport(getSwtField());
    }
    m_editableSupport.setEditable(enabled);
  }

  protected void setDecorationLinkFromScout(boolean b) {
    if (m_linkDecoration != b) {
      m_linkDecoration = b;
      if (m_linkDecoration) {
        m_linkTrigger = new P_SwtLinkTrigger();
        getSwtField().addMouseListener(m_linkTrigger);
        getSwtField().setCursor(getSwtField().getDisplay().getSystemCursor(SWT.CURSOR_HAND));
        setForegroundFromScout(getScoutObject().getForegroundColor());
      }
      else {
        getSwtField().removeMouseListener(m_linkTrigger);
        m_linkTrigger = null;
        getSwtField().setCursor(null);
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
      StyledText swtField = getSwtField();
      int offset = swtField.getCaretOffset();
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
    StyledText swtField = getSwtField();
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
    int caretOffset = swtField.getCaretOffset();
    int endIndex = -swtField.getSelection().y;
    swtField.setText(s);
    // restore selection and caret
    int textLength = swtField.getText().length();
    if (caretOffset > 0) {
      startIndex = Math.min(Math.max(startIndex, 0), textLength);
      endIndex = Math.min(Math.max(endIndex, 0), textLength);
      swtField.setCaretOffset(caretOffset);
      m_backupSelection = new Point(startIndex, endIndex);
      if (getSwtField().isFocusControl()) {
        swtField.setSelection(startIndex, endIndex);
      }
    }
  }

  protected void setValidateOnAnyKeyFromScout(boolean b) {
    m_validateOnAnyKey = b;
  }

  protected void setSelectionFromScout(int startIndex, int endIndex) {
    StyledText swtField = getSwtField();
    int start = swtField.getSelection().x;
    int end = swtField.getSelection().y;
    if (startIndex < 0) {
      startIndex = start;
    }
    if (endIndex < 0) {
      endIndex = end;
    }
    m_backupSelection = new Point(startIndex, endIndex);
    if (getSwtField().isFocusControl()) {
      swtField.setSelection(startIndex, endIndex);
    }

  }

  protected void setTextWrapFromScout(boolean booleanValue) {
    if (getScoutObject().isMultilineText()) {
      getSwtField().setWordWrap(booleanValue);
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

  protected void handleSwtLinkTrigger() {
    final String text = getSwtField().getText();
    // notify Scout
    Runnable t = new Runnable() {
      @Override
      public void run() {
        getScoutObject().getUIFacade().fireLinkActionFromUI(text);
      }
    };
    getEnvironment().invokeScoutLater(t, 0);
    // end notify
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
    return true; // continue always
  }

  private Point m_backupSelection = null;

  @Override
  protected void handleSwtFocusGained() {
    super.handleSwtFocusGained();
    if (getScoutObject().isSelectAllOnFocus()) {
      scheduleSelectAll();
    }
    else {
      // restore selection, but only if there is one to not move the cursor accidentally (this is done automatically by swt)
      if (m_backupSelection != null && m_backupSelection.x != m_backupSelection.y) {
        getSwtField().setSelection(m_backupSelection);
      }
    }
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

  @Override
  protected void handleSwtFocusLost() {
    m_backupSelection = getSwtField().getSelection();
    if (getSwtField().getSelectionCount() > 0) {
      //Clear selection to make sure only one field at a time shows a selection
      getSwtField().setSelection(0, 0);
    }
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

  private class P_SwtLinkTrigger extends MouseAdapter {
    @Override
    public void mouseDoubleClick(MouseEvent e) {
      handleSwtLinkTrigger();
    }
  } // end class P_SwtLinkTrigger

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
          TransferObject scoutTransferable = SwtScoutStringField.this.getScoutObject().getUIFacade().fireDragRequestFromUI();
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
