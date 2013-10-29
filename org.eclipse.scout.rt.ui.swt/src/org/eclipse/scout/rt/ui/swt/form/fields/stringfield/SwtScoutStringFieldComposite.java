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
import org.eclipse.scout.rt.ui.swt.form.fields.SwtScoutBasicFieldComposite;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;

/**
 *
 */
public abstract class SwtScoutStringFieldComposite extends SwtScoutBasicFieldComposite<IStringField> {

  public static final int DEFAULT_CASE = 0;
  public static final int UPPER_CASE = 1;
  public static final int LOWER_CASE = 2;

  /** one of {@link DEFAULT_CASE,UPPER_CASE, LOWER_CASE} */
  private int m_characterType = -1;

  protected Composite createContainer(Composite parent) {
    return getEnvironment().getFormToolkit().createComposite(parent);
  }

  protected StatusLabelEx createLabel(Composite container) {
    return getEnvironment().getFormToolkit().createStatusLabel(container, getEnvironment(), getScoutObject());
  }

  /**
   * @param {@link FORMAT_UPPER} or {@link FORMAT_LOWER}
   */
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

  protected int getSwtStyle(IStringField field) {
    int style = SWT.BORDER;
    if (field.isInputMasked()) {
      style |= SWT.PASSWORD;
    }
    if (field.isMultilineText()) {
      style |= SWT.MULTI | SWT.V_SCROLL;
    }
    else {
      style |= SWT.SINGLE;
    }
    if (field.isWrapText()) {
      style |= SWT.WRAP;
    }
    return style;
  }

  protected LogicalGridLayout getContainerLayout() {
    return new LogicalGridLayout(1, 0);
  }

  protected class P_TextVerifyListener implements VerifyListener {
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

  protected class P_SwtTextSelectionListener extends SelectionAdapter {
    @Override
    public void widgetSelected(SelectionEvent e) {
      setSelectionFromSwt(e.x, e.y);
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

  protected abstract void setMaxLengthFromScout(int n);

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

  protected class P_DndSupport extends AbstractSwtScoutDndSupport {
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
          TransferObject scoutTransferable = SwtScoutStringFieldComposite.this.getScoutObject().getUIFacade().fireDragRequestFromUI();
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

  protected void setDoInsertFromScout(String s) {
    if (s != null && s.length() > 0) {
      int offset = getCaretOffset();
      int a = getSelection().x;
      int b = getSelection().y;
      String uiText = getText();
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
        setText(builder.toString());
      }
    }
  }

  @Override
  protected void handleSwtFocusGained() {
    if (getScoutObject().isSelectAllOnFocus()) {
      scheduleSelectAll();
    }
    else {
      restoreSelection();
    }
  }

  protected void setSelectionFromScout(int startIndex, int endIndex) {
    if (startIndex < 0) {
      startIndex = getSelection().x;
    }
    if (endIndex < 0) {
      endIndex = getSelection().y;
    }
    selectField(startIndex, endIndex);
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
    else if (name.equals(IStringField.PROP_SELECTION_START)) {
      IStringField f = getScoutObject();
      setSelectionFromScout(f.getSelectionStart(), f.getSelectionEnd());
    }
    else if (name.equals(IStringField.PROP_SELECTION_END)) {
      IStringField f = getScoutObject();
      setSelectionFromScout(f.getSelectionStart(), f.getSelectionEnd());
    }
  }

}
