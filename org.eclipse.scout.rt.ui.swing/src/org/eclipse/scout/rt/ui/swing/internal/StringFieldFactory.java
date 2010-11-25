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
package org.eclipse.scout.rt.ui.swing.internal;

import javax.swing.JComponent;

import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.IStringField;
import org.eclipse.scout.rt.ui.swing.ISwingEnvironment;
import org.eclipse.scout.rt.ui.swing.extension.IFormFieldFactory;
import org.eclipse.scout.rt.ui.swing.form.fields.ISwingScoutFormField;
import org.eclipse.scout.rt.ui.swing.form.fields.textfield.SwingScoutPasswordField;
import org.eclipse.scout.rt.ui.swing.form.fields.textfield.SwingScoutTextArea;
import org.eclipse.scout.rt.ui.swing.form.fields.textfield.SwingScoutTextField;

public class StringFieldFactory implements IFormFieldFactory {
  @Override
  public ISwingScoutFormField<?> createFormField(JComponent parent, IFormField field, ISwingEnvironment environment) {
    IStringField scoutTextField = (IStringField) field;
    if (scoutTextField.isInputMasked()) {
      SwingScoutPasswordField uiField = new SwingScoutPasswordField();
      uiField.createField(scoutTextField, environment);
      return uiField;
    }
    else if (scoutTextField.isMultilineText()) {
      SwingScoutTextArea uiField = new SwingScoutTextArea();
      uiField.createField(scoutTextField, environment);
      return uiField;
    }
    else {
      SwingScoutTextField uiField = new SwingScoutTextField();
      uiField.createField(scoutTextField, environment);
      return uiField;
    }
  }
}
