package org.eclipse.scout.rt.ui.swt.form.fields;

import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.job.JobEx;
import org.eclipse.scout.rt.client.ui.form.fields.IBasicField;
import org.eclipse.scout.rt.ui.swt.internal.TextFieldEditableSupport;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TypedListener;
import org.eclipse.swt.widgets.Widget;

/**
 * Common code for SWT fields corresponding to {@link IBasicField}.
 * 
 * @since 3.10.0-M3
 */
public abstract class SwtScoutBasicFieldComposite<T extends IBasicField<?>> extends SwtScoutValueFieldComposite<T> {

  private Point m_backupSelection = null;
  private TextFieldEditableSupport m_editableSupport;
  private boolean m_validateOnAnyKey;

  protected void addModifyListenerForBasicField(Widget inputField) {
    TypedListener typedListener = new TypedListener(new P_SwtTextModifyListener());
    inputField.addListener(SWT.Modify, typedListener);
  }

  @Override
  protected void attachScout() {
    super.attachScout();
    IBasicField f = getScoutObject();
    setValidateOnAnyKeyFromScout(f.isValidateOnAnyKey());
  }

  @Override
  protected void setDisplayTextFromScout(String newText) {
    if (newText == null) {
      newText = "";
    }
    String oldText = getText();
    if (oldText == null) {
      oldText = "";
    }
    if (oldText.equals(newText)) {
      return;
    }
    //
    try {
      getUpdateSwtFromScoutLock().acquire();
      int startIndex = getSelection().x;
      int endIndex = getSelection().y;
      int caretPosition = getCaretOffset();
      if (startIndex == endIndex && startIndex == caretPosition && newText.length() != oldText.length()) {
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
        caretPosition = startIndex;
      }
      setText(newText);
      //set selection:
      int textLength = newText.length();
      startIndex = Math.min(Math.max(startIndex, 0), textLength);
      endIndex = Math.min(Math.max(endIndex, 0), textLength);
      setCaretOffset(caretPosition);
      selectField(startIndex, endIndex);
    }
    finally {
      getUpdateSwtFromScoutLock().release();
    }
  }

  /**
   * @return getText of the swt field
   */
  protected abstract String getText();

  /**
   * set text in the swt field
   */
  protected abstract void setText(String text);

  /**
   * @return getSelection of the swt field
   */
  protected abstract Point getSelection();

  /**
   * set selection in the swt field
   */
  protected abstract void setSelection(int startIndex, int endIndex);

  /**
   * @return getCaretPosition/getCaretOffset of the swt field
   */
  protected abstract int getCaretOffset();

  /**
   * set caretOffset in the swt field
   */
  protected abstract void setCaretOffset(int caretPosition);

  /**
   * @return getEditableSupport of the swt field
   */
  protected abstract TextFieldEditableSupport createEditableSupport();

  /**
   * Backup new the selection range and set it in the swt field.
   */
  protected void selectField(int startIndex, int endIndex) {
    m_backupSelection = new Point(startIndex, endIndex);
    setSelection(startIndex, endIndex);
  }

  /**
   * restore selection, but only if there is one to not move the cursor accidentally (this is done automatically by swt)
   */
  protected void restoreSelection() {
    if (m_backupSelection != null && m_backupSelection.x != m_backupSelection.y) {
      setSelection(m_backupSelection.x, m_backupSelection.y);
    }
  }

  protected void clearSelection() {
    m_backupSelection = getSelection();
    if (m_backupSelection.y - m_backupSelection.x != 0) {
      setSelection(0, 0);
    }
  }

  protected void scheduleSelectAll() {
    getEnvironment().getDisplay().asyncExec(new Runnable() {

      @Override
      public void run() {
        if (getSwtField().isDisposed()) {
          return;
        }
        setSelection(0, getText().length());
      }
    });
  }

  @Override
  protected void setFieldEnabled(Control swtField, boolean enabled) {
    if (m_editableSupport == null) {
      m_editableSupport = createEditableSupport();
    }
    m_editableSupport.setEditable(enabled);
  }

  @Override
  protected void handleSwtFocusGained() {
    scheduleSelectAll();
  }

  @Override
  protected void handleSwtFocusLost() {
    clearSelection();
  }

  @Override
  protected boolean handleSwtInputVerifier() {
    final String text = getText();
    // only handle if text has changed
    if (CompareUtility.equals(text, getScoutObject().getDisplayText()) && getScoutObject().getErrorStatus() == null) {
      return true;
    }
    // notify Scout
    Runnable t = new Runnable() {
      @Override
      public void run() {
        getScoutObject().getUIFacade().setTextFromUI(text);
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

  private void setValidateOnAnyKeyFromScout(boolean b) {
    m_validateOnAnyKey = b;
  }

  protected class P_SwtTextModifyListener implements ModifyListener {

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
      final String text = getText();
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

}
