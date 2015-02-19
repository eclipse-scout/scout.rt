/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.swing.form.fields;

import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.job.JobEx;
import org.eclipse.scout.rt.client.ui.form.fields.IBasicField;

/**
 * Common code for Swing fields corresponding to {@link IBasicField}.
 * 
 * @since 3.10.0-M3
 */
public abstract class SwingScoutBasicFieldComposite<T extends IBasicField<?>> extends SwingScoutValueFieldComposite<T> {

  private boolean m_validateOnAnyKey;

  /**
   * attach Scout Model: set scout properties
   */
  @Override
  protected void attachScout() {
    IBasicField f = getScoutObject();
    setValidateOnAnyKeyFromScout(f.isValidateOnAnyKey());

    //super call must come after reading model properties:
    super.attachScout();
  }

  @Override
  public JTextComponent getSwingField() {
    return (JTextComponent) super.getSwingField();
  }

  protected void addInputListenersForBasicField(JTextComponent textField, Document doc) {
    doc.addDocumentListener(new P_SwingDocumentListener());
    textField.addCaretListener(new P_SwingCaretListener());
  }

  protected void setValidateOnAnyKeyFromScout(boolean b) {
    m_validateOnAnyKey = b;
  }

  @Override
  protected void setDisplayTextFromScout(String newText) {
    JTextComponent swingField = getSwingField();
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

  protected abstract void setSelectionFromSwing();

  protected abstract boolean isSelectAllOnFocusInScout();

  @Override
  protected boolean handleSwingInputVerifier() {
    final String text = getSwingField().getText();
    // only handle if text has changed
    if (CompareUtility.equals(text, getScoutObject().getDisplayText()) && getScoutObject().getErrorStatus() == null) {
      return true;
    }
    // notify Scout
    Runnable t = new Runnable() {
      @Override
      public void run() {
        getScoutObject().getUIFacade().setTextFromUI(text, false);
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
    JTextComponent swingField = getSwingField();
    if (!isMenuOpened() && isSelectAllOnFocusInScout() && swingField.getDocument().getLength() > 0) {
      swingField.setCaretPosition(swingField.getDocument().getLength());
      swingField.moveCaretPosition(0);
    }
    setMenuOpened(false);
  }

  /**
   * scout property handler override
   */
  @Override
  protected void handleScoutPropertyChange(String name, Object newValue) {
    super.handleScoutPropertyChange(name, newValue);
    if (name.equals(IBasicField.PROP_VALIDATE_ON_ANY_KEY)) {
      setValidateOnAnyKeyFromScout(((Boolean) newValue).booleanValue());
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
  }// end class P_SwingCaretListener

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

    /**
     * This method notify scout, with the information that we are during "ValidateOnAnyKey".
     * Do not call handleSwingInputVerifier(), this can lead to endless loops.
     */
    private void sendVerifyToScoutAndIgnoreResponses() {
      final String text = getSwingField().getText();
      // notify Scout
      Runnable t = new Runnable() {
        @Override
        public void run() {
          getScoutObject().getUIFacade().setTextFromUI(text, true);
        }
      };
      getSwingEnvironment().invokeScoutLater(t, 0);
    }
  }// end class P_SwingDocumentListener
}
