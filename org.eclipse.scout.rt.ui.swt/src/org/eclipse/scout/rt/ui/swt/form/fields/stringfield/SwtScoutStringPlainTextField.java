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
public class SwtScoutStringPlainTextField extends SwtScoutStringFieldComposite {

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
    addModifyListenerForBasicField(textField);
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

    // dnd support
    new P_DndSupport(getScoutObject(), getScoutObject(), getSwtField(), getEnvironment());
  }

  @Override
  protected void setSelection(int startIndex, int endIndex) {
    getSwtField().setSelection(startIndex, endIndex);
  }

  @Override
  protected Point getSelection() {
    return getSwtField().getSelection();
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
  protected TextFieldEditableSupport createEditableSupport() {
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

  @Override
  protected void setCaretOffset(int caretPosition) {
    //nothing to do: SWT sets the caret itself. If startIndex > endIndex it is placed at the beginning.
  }
}
