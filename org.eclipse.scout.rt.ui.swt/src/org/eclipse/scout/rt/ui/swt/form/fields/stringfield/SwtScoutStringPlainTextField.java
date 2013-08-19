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

import org.eclipse.scout.rt.client.ui.form.fields.stringfield.IStringField;
import org.eclipse.scout.rt.ui.swt.ext.StatusLabelEx;
import org.eclipse.scout.rt.ui.swt.internal.TextFieldEditableSupport;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

/**
 * A string field implementation that uses SWT Text Widget to implement masked input fields (password)
 */
public class SwtScoutStringPlainTextField extends AbstractSwtScoutStringField {

  protected Point m_backupSelection = null;

  @Override
  protected void initializeSwt(Composite parent) {
    Composite container = createContainer(parent);
    setSwtContainer(container);
    // layout
    getSwtContainer().setLayout(getContainerLayout());

    StatusLabelEx label = createLabel(container);
    setSwtLabel(label);

    int style = getSwtStyle(getScoutObject());
    Text textField = getEnvironment().getFormToolkit().createText(container, style);
    setSwtField(textField);
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
  protected void restoreSelectionAndCaret(int startIndex, int endIndex, int caretOffset) {
    int textLength = getText().length();
    if (caretOffset > 0) {
      startIndex = Math.min(Math.max(startIndex, 0), textLength);
      endIndex = Math.min(Math.max(endIndex, 0), textLength);
      selectField(startIndex, endIndex);
    }
  }

  @Override
  protected void scheduleSelectAll() {
    getSwtField().setSelection(0, getSwtField().getText().length());
  }

  @Override
  protected void restoreSelection() {
    if (m_backupSelection == null) {
      m_backupSelection = new Point(0, 0);
    }
    getSwtField().setSelection(m_backupSelection);
  }

  @Override
  protected void selectField(int startIndex, int endIndex) {
    // swt sets the caret itself. If startIndex > endIndex it is placed at the beginning.
    m_backupSelection = new Point(startIndex, endIndex);
    getSwtField().setSelection(startIndex, endIndex);
  }

  @Override
  protected Point getSelection() {
    return getSwtField().getSelection();
  }

  @Override
  protected void clearSelection() {
    m_backupSelection = getSelection();
    if (getSwtField().getSelectionCount() > 0) {
      getSwtField().setSelection(0, 0);
    }
  }

  @Override
  protected void setText(String text) {
    getSwtField().setText(text);
  }

  @Override
  protected String getText() {
    return getSwtField().getText();
  }

  @Override
  protected TextFieldEditableSupport getEditableSupport() {
    return new TextFieldEditableSupport(getSwtField());
  }

  @Override
  public Text getSwtField() {
    return (Text) super.getSwtField();
  }

  @Override
  protected void setMaxLengthFromScout(int n) {
    getSwtField().setTextLimit(n);
  }

  @Override
  protected int getCaretOffset() {
    return getSwtField().getCaretPosition();
  }

}
