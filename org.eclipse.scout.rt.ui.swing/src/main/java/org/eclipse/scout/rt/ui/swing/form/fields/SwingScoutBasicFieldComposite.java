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

import java.util.concurrent.TimeUnit;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.form.fields.IBasicField;
import org.eclipse.scout.rt.platform.job.IFuture;

/**
 * Common code for Swing fields corresponding to {@link IBasicField}.
 *
 * @since 3.10.0-M3
 */
public abstract class SwingScoutBasicFieldComposite<T extends IBasicField<?>> extends SwingScoutValueFieldComposite<T> {

  protected boolean m_updateDisplayTextOnModify;
  protected boolean m_updateDisplayTextOnModifyWasTrueSinceLastWriteDown;

  /**
   * attach Scout Model: set scout properties
   */
  @Override
  protected void attachScout() {
    IBasicField f = getScoutObject();
    setUpdateDisplayTextOnModifyFromScout(f.isUpdateDisplayTextOnModify());

    //super call must come after reading model properties:
    super.attachScout();
  }

  @Override
  public JTextComponent getSwingField() {
    return (JTextComponent) super.getSwingField();
  }

  protected void addInputListenersForBasicField(JTextComponent textField, Document doc) {
    doc.addDocumentListener(new P_SwingDocumentListener());
  }

  protected void setUpdateDisplayTextOnModifyFromScout(boolean b) {
    m_updateDisplayTextOnModify = b;
    if (b) {
      m_updateDisplayTextOnModifyWasTrueSinceLastWriteDown = true;
    }
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
    updateTextKeepCurserPosition(newText, this);
  }

  protected static void updateTextKeepCurserPosition(String newText, SwingScoutBasicFieldComposite composite) {
    try {
      composite.getUpdateSwingFromScoutLock().acquire();
      JTextComponent swingField = composite.getSwingField();
      String oldText = swingField.getText();
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
      composite.getUpdateSwingFromScoutLock().release();
    }
  }

  protected abstract void setSelectionFromSwing();

  @Override
  protected boolean handleSwingInputVerifier() {
    final String text = getSwingField().getText();
    // only handle if text has changed
    if (!m_updateDisplayTextOnModifyWasTrueSinceLastWriteDown && CompareUtility.equals(text, getScoutObject().getDisplayText()) && getScoutObject().getErrorStatus() == null) {
      return true;
    }
    // notify Scout
    Runnable t = new Runnable() {
      @Override
      public void run() {
        getScoutObject().getUIFacade().parseAndSetValueFromUI(text);
      }
    };
    IFuture<Void> job = getSwingEnvironment().invokeScoutLater(t, 0);
    try {
      job.awaitDone(2345, TimeUnit.MILLISECONDS);
    }
    catch (ProcessingException e) {
      // NOOP
    }
    // end notify
    getSwingEnvironment().dispatchImmediateSwingJobs();
    setSelectionFromSwing();
    if (m_updateDisplayTextOnModifyWasTrueSinceLastWriteDown && !m_updateDisplayTextOnModify) {
      m_updateDisplayTextOnModifyWasTrueSinceLastWriteDown = false;
    }
    return true; // continue always
  }

  @Override
  protected void handleSwingFocusGained() {
    super.handleSwingFocusGained();
    setMenuOpened(false);
  }

  /**
   * scout property handler override
   */
  @Override
  protected void handleScoutPropertyChange(String name, Object newValue) {
    super.handleScoutPropertyChange(name, newValue);
    if (name.equals(IBasicField.PROP_UPDATE_DISPLAY_TEXT_ON_MODIFY)) {
      setUpdateDisplayTextOnModifyFromScout(((Boolean) newValue).booleanValue());
    }
  }

  private class P_SwingDocumentListener implements DocumentListener {
    @Override
    public void changedUpdate(DocumentEvent e) {
      setInputDirty(true);
      setDisplayTextInScout();
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
      setInputDirty(true);
      setDisplayTextInScout();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
      setInputDirty(true);
      setDisplayTextInScout();
    }

    private void setDisplayTextInScout() {
      if (m_updateDisplayTextOnModify && getUpdateSwingFromScoutLock().isReleased()) {
        final String text = getSwingField().getText();
        // notify Scout
        Runnable t = new Runnable() {
          @Override
          public void run() {
            getScoutObject().getUIFacade().setDisplayTextFromUI(text);
          }
        };
        getSwingEnvironment().invokeScoutLater(t, 0);
      }
    }

  }// end class P_SwingDocumentListener
}
