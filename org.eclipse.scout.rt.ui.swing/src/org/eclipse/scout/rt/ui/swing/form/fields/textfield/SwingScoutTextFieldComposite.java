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
package org.eclipse.scout.rt.ui.swing.form.fields.textfield;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragGestureRecognizer;
import java.awt.dnd.DragSource;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;

import javax.swing.JComponent;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;
import javax.swing.text.JTextComponent;

import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.dnd.TransferObject;
import org.eclipse.scout.commons.holders.Holder;
import org.eclipse.scout.commons.job.JobEx;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.IStringField;
import org.eclipse.scout.rt.ui.swing.SwingUtility;
import org.eclipse.scout.rt.ui.swing.basic.document.BasicDocumentFilter;
import org.eclipse.scout.rt.ui.swing.ext.MouseClickedBugFix;
import org.eclipse.scout.rt.ui.swing.form.fields.SwingScoutValueFieldComposite;

public abstract class SwingScoutTextFieldComposite<T extends IStringField> extends SwingScoutValueFieldComposite<T> {
  private static final long serialVersionUID = 1L;
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(SwingScoutTextFieldComposite.class);

  public static final String CLIENT_PROP_INITIAL_DISABLED_TEXT_COLOR = "scoutInitialDisabledForeground";

  // cache
  private boolean m_upperCase;
  private boolean m_lowerCase;
  private MouseListener m_linkTrigger;
  private boolean m_validateOnAnyKey;
  private boolean m_decorationLink;
  private boolean m_multilineText;

  // DND
  private DragGestureRecognizer m_dragSource;
  private DropTarget m_dropTarget;

  public SwingScoutTextFieldComposite() {
  }

  @Override
  protected void initializeSwing() {
  }

  @Override
  protected void setSwingField(JComponent swingField) {
    super.setSwingField(swingField);
    if (swingField instanceof JTextComponent) {
      JTextComponent textField = (JTextComponent) swingField;
      Document doc = textField.getDocument();
      if (doc instanceof AbstractDocument) {
        ((AbstractDocument) doc).setDocumentFilter(new P_SwingDocumentFilter());
      }
      doc.addDocumentListener(new P_SwingDocumentListener());
      textField.addCaretListener(new P_SwingCaretListener());
    }
  }

  protected JTextComponent getSwingTextComponent() {
    return (JTextComponent) getSwingField();
  }

  /*
   * scout properties
   */

  @Override
  protected void attachScout() {
    IStringField f = getScoutObject();
    setDecorationLinkFromScout(f.isDecorationLink());
    setFormatFromScout(f.getFormat());
    setMaxLengthFromScout(f.getMaxLength());
    setValidateOnAnyKeyFromScout(f.isValidateOnAnyKey());
    setMultilineTextFromScout(f.isMultilineText());

    // supercall must come after reading model properties to have P_SwingDocumentFilter initialized.
    // That is because in super call, the display text is set which in turn is intercepted by document filter
    // to ensure proper text format.
    super.attachScout();

    setSelectionFromScout();
    updateDragTransferTypesFromScout();
    updateDropTransferTypesFromScout();
  }

  protected void setDecorationLinkFromScout(boolean b) {
    if (m_decorationLink != b) {
      m_decorationLink = b;
      JTextComponent swingField = getSwingTextComponent();
      if (b) {
        m_linkTrigger = new P_SwingLinkTrigger();
        swingField.addMouseListener(m_linkTrigger);
        swingField.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setForegroundFromScout(getScoutObject().getForegroundColor());
      }
      else {
        swingField.removeMouseListener(m_linkTrigger);
        m_linkTrigger = null;
        swingField.setCursor(null);
        setForegroundFromScout(getScoutObject().getForegroundColor());
      }
    }
  }

  protected void setFormatFromScout(String s) {
    m_upperCase = false;
    m_lowerCase = false;
    if (IStringField.FORMAT_UPPER.equals(s)) {
      m_upperCase = true;
    }
    else if (IStringField.FORMAT_LOWER.equals(s)) {
      m_lowerCase = true;
    }
  }

  protected void setMultilineTextFromScout(boolean multilineText) {
    m_multilineText = multilineText;
  }

  protected void setMaxLengthFromScout(int n) {
    Document doc = getSwingTextComponent().getDocument();
    if (doc instanceof AbstractDocument) {
      DocumentFilter filter = ((AbstractDocument) doc).getDocumentFilter();
      if (filter instanceof BasicDocumentFilter) {
        ((BasicDocumentFilter) filter).setMaxLength(n);
      }
    }
  }

  protected void setDoInsertFromScout(String s) {
    if (s != null && s.length() > 0) {
      JTextComponent swingField = getSwingTextComponent();
      int offset = swingField.getCaretPosition();
      int a = swingField.getSelectionStart();
      int b = swingField.getSelectionEnd();
      try {
        Document doc = swingField.getDocument();
        if (a >= 0 && b > a) {
          doc.remove(a, b - a);
          doc.insertString(a, s, null);
        }
        else if (offset >= 0) {
          doc.insertString(offset, s, null);
        }
      }
      catch (Exception e) {
        LOG.warn("inserting \"" + s + "\" into " + getScoutObject().getLabel() + " at position " + offset, e);
      }
    }
  }

  @Override
  protected void setDisplayTextFromScout(String newText) {
    JTextComponent swingField = getSwingTextComponent();
    String oldText = swingField.getText();
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
      getUpdateSwingFromScoutLock().acquire();
      //
      int startIndex = -1;
      int endIndex = -1;
      Caret caret = swingField.getCaret();
      if (caret != null) {
        startIndex = caret.getMark();
        endIndex = caret.getDot();
      }
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
      swingField.setText(newText);
      // restore selection and caret
      int textLength = swingField.getText().length();
      if (caret != null) {
        startIndex = Math.min(Math.max(startIndex, -1), textLength);
        endIndex = Math.min(Math.max(endIndex, 0), textLength);
        swingField.setCaretPosition(startIndex);
        swingField.moveCaretPosition(endIndex);
      }
    }
    finally {
      getUpdateSwingFromScoutLock().release();
    }
  }

  @Override
  protected void cacheSwingClientProperties() {
    super.cacheSwingClientProperties();
    JTextComponent fld = getSwingTextComponent();
    if (fld != null) {
      // disabled text
      if (!existsClientProperty(fld, CLIENT_PROP_INITIAL_DISABLED_TEXT_COLOR)) {
        putClientProperty(fld, CLIENT_PROP_INITIAL_DISABLED_TEXT_COLOR, fld.getDisabledTextColor());
      }
    }
  }

  @Override
  protected void setForegroundFromScout(String scoutColor) {
    if (scoutColor == null && m_decorationLink) {
      scoutColor = "445599";
    }
    super.setForegroundFromScout(scoutColor);
    JTextComponent fld = getSwingTextComponent();
    if (fld != null) {
      Color c = SwingUtility.createColor(scoutColor);
      if (c == null) {
        c = (Color) getClientProperty(fld, CLIENT_PROP_INITIAL_DISABLED_TEXT_COLOR);
      }
      else {
        c = getDisabledColor(c);
      }
      fld.setDisabledTextColor(c);
    }
  }

  protected void setValidateOnAnyKeyFromScout(boolean b) {
    m_validateOnAnyKey = b;
  }

  protected void setSelectionFromScout() {
    int startIndex = getScoutObject().getSelectionStart();
    int endIndex = getScoutObject().getSelectionEnd();
    JTextComponent swingField = getSwingTextComponent();
    Caret c = swingField.getCaret();
    int textLength = swingField.getText().length();
    if (startIndex < 0) {
      startIndex = c.getMark();
    }
    if (endIndex < 0) {
      endIndex = c.getDot();
    }
    startIndex = Math.min(Math.max(startIndex, -1), textLength);
    endIndex = Math.min(Math.max(endIndex, 0), textLength);
    if (c.getMark() != startIndex || c.getDot() != endIndex) {
      swingField.setCaretPosition(startIndex);
      swingField.moveCaretPosition(endIndex);
    }
  }

  protected void setSelectionFromSwing() {
    if (getUpdateSwingFromScoutLock().isAcquired()) {
      return;
    }
    //
    Caret c = getSwingTextComponent().getCaret();
    int textLength = StringUtility.length(getSwingTextComponent().getText());
    final int startIndex = Math.min(Math.max(c.getMark(), -1), textLength);
    final int endIndex = Math.min(Math.max(c.getDot(), 0), textLength);
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
    getSwingEnvironment().invokeScoutLater(t, 0);
    // end notify
  }

  protected void updateDragTransferTypesFromScout() {
    int scoutDragTransfer = getScoutObject().getDragType();
    if (scoutDragTransfer != 0) {
      // install
      if (m_dragSource == null) {
        // create new
        m_dragSource = DragSource.getDefaultDragSource().createDefaultDragGestureRecognizer(getSwingTextComponent(),
            DnDConstants.ACTION_COPY, new P_DragGestureListener());
      }
      m_dragSource.setComponent(getSwingTextComponent());
    }
    else {
      if (m_dragSource != null) {
        m_dragSource.setComponent(null);
      }
    }
  }

  protected void updateDropTransferTypesFromScout() {
    if (getScoutObject().getDropType() != 0) {
      // install drop support
      if (m_dropTarget == null) {
        // create new
        m_dropTarget = new DropTarget(getSwingTextComponent(), new P_DropTargetListener());
      }
      m_dropTarget.setComponent(getSwingTextComponent());
    }
    else {
      if (m_dropTarget != null) {
        m_dropTarget.setComponent(null);
      }
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
      setSelectionFromScout();
    }
    else if (name.equals(IStringField.PROP_SELECTION_END)) {
      setSelectionFromScout();
    }
    else if (name.equals(IStringField.PROP_MULTILINE_TEXT)) {
      setMultilineTextFromScout(((Boolean) newValue).booleanValue());
    }
  }

  protected void handleSwingLinkTrigger() {
    final String text = getSwingTextComponent().getText();
    // notify Scout
    Runnable t = new Runnable() {
      @Override
      public void run() {
        getScoutObject().getUIFacade().fireLinkActionFromUI(text);
      }
    };

    getSwingEnvironment().invokeScoutLater(t, 0);
    // end notify
  }

  @Override
  protected boolean handleSwingInputVerifier() {
    final String text = getSwingTextComponent().getText();
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
    JobEx job = getSwingEnvironment().invokeScoutLater(t, 0);
    try {
      job.join(2345);
    }
    catch (InterruptedException e) {
      //nop
    }
    // end notify
    getSwingEnvironment().dispatchImmediateSwingJobs();
    //if not validate any key, also update selections
    if (!m_validateOnAnyKey) {
      setSelectionFromSwing();
    }
    return true; // continue always
  }

  @Override
  protected void handleSwingFocusGained() {
    super.handleSwingFocusGained();
    if (getScoutObject().isSelectAllOnFocus()) {
      JTextComponent swingField = getSwingTextComponent();
      swingField.setCaretPosition(swingField.getDocument().getLength());
      swingField.moveCaretPosition(0);
    }
  }

  protected Transferable handleSwingDragRequest() {
    final Holder<TransferObject> result = new Holder<TransferObject>(TransferObject.class, null);
    if (getScoutObject() != null) {
      // notify Scout
      Runnable t = new Runnable() {
        @Override
        public void run() {
          TransferObject scoutTransferable = getScoutObject().getUIFacade().fireDragRequestFromUI();
          result.setValue(scoutTransferable);
        }
      };
      try {
        getSwingEnvironment().invokeScoutLater(t, 5678).join(5678);
      }
      catch (InterruptedException e) {
        //nop
      }
      // end notify
    }
    TransferObject scoutTransferable = result.getValue();
    Transferable swingTransferable = null;
    swingTransferable = SwingUtility.createSwingTransferable(scoutTransferable);
    return swingTransferable;
  }

  protected void handleSwingDropAction(Transferable swingTransferable) {
    if (getScoutObject() != null) {
      if (swingTransferable != null) {
        final TransferObject scoutTransferable = SwingUtility.createScoutTransferable(swingTransferable);
        if (scoutTransferable != null) {
          // notify Scout (asynchronous !)
          Runnable t = new Runnable() {
            @Override
            public void run() {
              getScoutObject().getUIFacade().fireDropActionFromUi(scoutTransferable);
            }
          };
          getSwingEnvironment().invokeScoutLater(t, 0);
          // end notify
        }
      }
    }
  }

  /**
   * Plain text document filtering text according to model
   */
  private class P_SwingDocumentFilter extends BasicDocumentFilter {

    @Override
    public void insertString(FilterBypass fb, int offset, String s, AttributeSet a) throws BadLocationException {
      s = ensureConfiguredTextFormat(s);
      super.insertString(fb, offset, s, a);
    }

    @Override
    public void replace(FilterBypass fb, int offset, int length, String s, AttributeSet a) throws BadLocationException {
      s = ensureConfiguredTextFormat(s);
      super.replace(fb, offset, length, s, a);
    }

    private String ensureConfiguredTextFormat(String s) {
      s = StringUtility.emptyIfNull(s);

      if (m_upperCase) {
        s = s.toUpperCase();
      }
      else if (m_lowerCase) {
        s = s.toLowerCase();
      }
      if (!m_multilineText) {
        // omit leading and trailing newlines
        s = StringUtility.trimNewLines(s);
        // replace newlines by spaces
        s = s.replaceAll("\r\n", " ").replaceAll("[\r\n]", " ");
      }
      return s;
    }

  }// end private class

  private class P_SwingLinkTrigger extends MouseAdapter {
    MouseClickedBugFix fix;

    @Override
    public void mousePressed(MouseEvent e) {
      fix = new MouseClickedBugFix(e);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
      if (fix != null) {
        fix.mouseReleased(this, e);
      }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
      if (fix.mouseClicked()) {
        return;
      }
      if (e.getClickCount() == 2) {
        handleSwingLinkTrigger();
      }
    }
  }

  private class P_SwingCaretListener implements CaretListener {
    @Override
    public void caretUpdate(CaretEvent e) {
      //only if validate any key, update selections immediately, otherwise it is done in handleSwingInputVerifier
      if (m_validateOnAnyKey) {
        setSelectionFromSwing();
      }
    }
  }// end private class

  private class P_SwingDocumentListener implements DocumentListener {
    @Override
    public void changedUpdate(DocumentEvent e) {
      setInputDirty(true);
      if (m_validateOnAnyKey) {
        if (getUpdateSwingFromScoutLock().isReleased()) {
          sendVerifyToScoutAndIgnoreResponses();
        }
      }
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
      setInputDirty(true);
      if (m_validateOnAnyKey) {
        if (getUpdateSwingFromScoutLock().isReleased()) {
          sendVerifyToScoutAndIgnoreResponses();
        }
      }
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
      setInputDirty(true);
      if (m_validateOnAnyKey) {
        if (getUpdateSwingFromScoutLock().isReleased()) {
          sendVerifyToScoutAndIgnoreResponses();
        }
      }
    }

    /*
     * Do not call handleSwingInputVerifier(), this can lead to endless loops.
     */
    private void sendVerifyToScoutAndIgnoreResponses() {
      final String text = getSwingTextComponent().getText();
      // notify Scout
      Runnable t = new Runnable() {
        @Override
        public void run() {
          getScoutObject().getUIFacade().setTextFromUI(text);
        }
      };
      getSwingEnvironment().invokeScoutLater(t, 0);
    }
  }// end private class

  private class P_DropTargetListener implements DropTargetListener {
    @Override
    public void dragEnter(DropTargetDragEvent dtde) {
      // void
    }

    @Override
    public void dragExit(DropTargetEvent dte) {
      // void
    }

    @Override
    public void dragOver(DropTargetDragEvent dtde) {
      // void
    }

    @Override
    public void drop(DropTargetDropEvent dtde) {
      dtde.acceptDrop(DnDConstants.ACTION_COPY);
      handleSwingDropAction(dtde.getTransferable());
    }

    @Override
    public void dropActionChanged(DropTargetDragEvent dtde) {
      // void
    }
  } // end class P_DropTargetListener

  private class P_DragGestureListener implements DragGestureListener {
    @Override
    public void dragGestureRecognized(DragGestureEvent dge) {
      Transferable to = handleSwingDragRequest();
      if (to != null) {
        dge.startDrag(null, to);
      }
    }
  } // end class P_DragGestureListener
}
