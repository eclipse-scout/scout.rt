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
package org.eclipse.scout.rt.ui.swing.form.fields.checkbox;

import java.awt.Insets;

import javax.swing.JComponent;
import javax.swing.JTable;

import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.booleanfield.IBooleanField;
import org.eclipse.scout.rt.ui.swing.ISwingEnvironment;
import org.eclipse.scout.rt.ui.swing.basic.table.SwingScoutTableCellEditor;
import org.eclipse.scout.rt.ui.swing.extension.IFormFieldFactory;
import org.eclipse.scout.rt.ui.swing.form.fields.ISwingScoutFormField;

/**
 * Factory is used to instrument checkbox if being used as inline table cell editor
 */
public class CheckBoxFactory implements IFormFieldFactory {

  @Override
  public ISwingScoutFormField<?> createFormField(JComponent parent, IFormField field, ISwingEnvironment environment) {
    if (field instanceof IBooleanField) {
      IBooleanField bf = (IBooleanField) field;
      SwingScoutCheckBox ui = createCheckBox();
      if (parent instanceof JTable) {
        // instrument checkbox with table context
        ui.setTableCellContext(true);
        ui.setTableCellInsets((Insets) parent.getClientProperty(SwingScoutTableCellEditor.TABLE_CELL_INSETS));
      }
      ui.createField(bf, environment);
      return ui;
    }
    return null;
  }

  protected SwingScoutCheckBox createCheckBox() {
    return new SwingScoutCheckBox();
  }
}
