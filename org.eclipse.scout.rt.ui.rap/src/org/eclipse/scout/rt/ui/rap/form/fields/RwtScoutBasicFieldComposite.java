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
package org.eclipse.scout.rt.ui.rap.form.fields;

import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.holders.Holder;
import org.eclipse.scout.commons.job.JobEx;
import org.eclipse.scout.rt.client.ui.form.fields.IBasicField;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Text;

/**
 * Common code for RWT fields corresponding to {@link IBasicField}.
 * 
 * @since 3.10.0-M3
 */
public abstract class RwtScoutBasicFieldComposite<T extends IBasicField<?>> extends RwtScoutValueFieldComposite<T> {

  private boolean m_validateOnAnyKey;
  private P_RwtValidateOnAnyKeyModifyListener m_validateOnAnyKeyModifyListener;

  @Override
  protected void attachScout() {
    super.attachScout();
    IBasicField f = getScoutObject();
    setValidateOnAnyKeyFromScout(f.isValidateOnAnyKey());
  }

  @Override
  public Text getUiField() {
    return (Text) super.getUiField();
  }

  @Override
  protected void setDisplayTextFromScout(String newText) {
    Text field = getUiField();
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

  private void setValidateOnAnyKeyFromScout(boolean b) {
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
  } // end class P_RwtValidateOnAnyKeyModifyListener

}
