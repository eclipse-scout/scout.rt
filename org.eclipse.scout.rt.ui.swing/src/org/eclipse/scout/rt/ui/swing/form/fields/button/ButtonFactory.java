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
package org.eclipse.scout.rt.ui.swing.form.fields.button;

import javax.swing.JComponent;

import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.button.IButton;
import org.eclipse.scout.rt.ui.swing.ISwingEnvironment;
import org.eclipse.scout.rt.ui.swing.extension.IFormFieldFactory;
import org.eclipse.scout.rt.ui.swing.form.fields.ISwingScoutFormField;

public class ButtonFactory implements IFormFieldFactory {
  @Override
  public ISwingScoutFormField<?> createFormField(JComponent parent, IFormField field, ISwingEnvironment environment) {
    IButton scoutField = (IButton) field;
    switch (scoutField.getDisplayStyle()) {
      case IButton.DISPLAY_STYLE_RADIO:
      case IButton.DISPLAY_STYLE_TOGGLE: {
        SwingScoutButton uiField = new SwingScoutButton();
        uiField.createField(scoutField, environment);
        return uiField;
      }
      case IButton.DISPLAY_STYLE_LINK: {
        SwingScoutLink uiField = new SwingScoutLink();
        uiField.createField(scoutField, environment);
        return uiField;
      }
      default: {
        SwingScoutButton uiField = new SwingScoutButton();
        uiField.createField(scoutField, environment);
        return uiField;
      }
    }
  }
}
