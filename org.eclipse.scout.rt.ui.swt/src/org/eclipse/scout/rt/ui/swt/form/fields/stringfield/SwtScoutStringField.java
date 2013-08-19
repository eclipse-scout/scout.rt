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

import java.lang.reflect.Method;

import org.eclipse.core.runtime.Status;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.IStringField;
import org.eclipse.scout.rt.ui.swt.Activator;
import org.eclipse.scout.rt.ui.swt.ext.StatusLabelEx;
import org.eclipse.scout.rt.ui.swt.ext.StyledTextEx;
import org.eclipse.scout.rt.ui.swt.internal.StyledTextFieldUndoRedoSupport;
import org.eclipse.scout.rt.ui.swt.internal.TextFieldEditableSupport;
import org.eclipse.scout.rt.ui.swt.util.VersionUtility;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;

/**
 * Typical string field (see {@link SwtScoutStringPlainTextField} for masked input fields)
 */
public class SwtScoutStringField extends AbstractSwtScoutStringField implements ISwtScoutStringField {
  private MouseListener m_linkTrigger;

  private boolean m_linkDecoration;
  private StyledTextFieldUndoRedoSupport m_undoRedoSupport;

  private Point m_backupSelection = null;

  @Override
  protected void initializeSwt(Composite parent) {
    Composite container = createContainer(parent);
    StatusLabelEx label = createLabel(container);

    int style = getSwtStyle(getScoutObject());
    StyledText textField = getEnvironment().getFormToolkit().createStyledText(container, style);

    if (VersionUtility.isEclipseVersionLessThan35()) {
      //Necessary for backward compatibility to Eclipse 3.4 needed for Lotus Notes 8.5.2
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
    getSwtContainer().setLayout(getContainerLayout());
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
    setFormatFromScout(f.getFormat());
    setMaxLengthFromScout(f.getMaxLength());
    setSelectionFromScout(f.getSelectionStart(), f.getSelectionEnd());

    setDecorationLinkFromScout(f.isDecorationLink());
    setValidateOnAnyKeyFromScout(f.isValidateOnAnyKey());
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

  @Override
  protected void setMaxLengthFromScout(int n) {
    getSwtField().setTextLimit(n);
  }

  @Override
  protected void setText(String text) {
    getSwtField().setText(text);
  }

  @Override
  protected int getCaretOffset() {
    return getSwtField().getCaretOffset();
  }

  @Override
  protected void restoreSelectionAndCaret(int startIndex, int endIndex, int caretOffset) {
    int textLength = getText().length();
    if (caretOffset > 0) {
      startIndex = Math.min(Math.max(startIndex, 0), textLength);
      endIndex = Math.min(Math.max(endIndex, 0), textLength);
      getSwtField().setCaretOffset(caretOffset);
      selectField(startIndex, endIndex);
    }
  }

  /**
   * select the swt field, if it has the focus
   */
  @Override
  protected void selectField(int startIndex, int endIndex) {
    m_backupSelection = new Point(startIndex, endIndex);
    if (getSwtField().isFocusControl()) {
      getSwtField().setSelection(startIndex, endIndex);
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

  /**
   * restore selection, but only if there is one to not move the cursor accidentally (this is done automatically by swt)
   */
  @Override
  protected void restoreSelection() {
    if (m_backupSelection != null && m_backupSelection.x != m_backupSelection.y) {
      getSwtField().setSelection(m_backupSelection);
    }
  }

  @Override
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
  protected void clearSelection() {
    m_backupSelection = getSelection();
    if (getSwtField().getSelectionCount() > 0) {
      getSwtField().setSelection(0, 0);
    }
  }

  @Override
  protected String getText() {
    return getSwtField().getText();
  }

  private class P_SwtLinkTrigger extends MouseAdapter {
    @Override
    public void mouseDoubleClick(MouseEvent e) {
      handleSwtLinkTrigger();
    }
  } // end class P_SwtLinkTrigger

  @Override
  protected TextFieldEditableSupport getEditableSupport() {
    return new TextFieldEditableSupport(getSwtField());
  }

  @Override
  protected Point getSelection() {
    return getSwtField().getSelection();
  }

}
