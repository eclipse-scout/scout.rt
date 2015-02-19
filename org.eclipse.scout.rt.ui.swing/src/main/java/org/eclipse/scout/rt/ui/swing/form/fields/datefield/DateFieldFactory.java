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
package org.eclipse.scout.rt.ui.swing.form.fields.datefield;

import javax.swing.JComponent;

import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.datefield.IDateField;
import org.eclipse.scout.rt.ui.swing.ISwingEnvironment;
import org.eclipse.scout.rt.ui.swing.extension.IFormFieldFactory;
import org.eclipse.scout.rt.ui.swing.form.fields.ISwingScoutFormField;

public class DateFieldFactory implements IFormFieldFactory {

  @Override
  public ISwingScoutFormField<?> createFormField(JComponent parent, IFormField field, ISwingEnvironment environment) {
    if (field instanceof IDateField) {
      IDateField d = (IDateField) field;
      if (d.isHasDate() && d.isHasTime()) {
        SwingScoutDateTimeCompositeField ui = new SwingScoutDateTimeCompositeField();
        ui.createField(d, environment);
        return ui;
      }
      else if (d.isHasDate()) {
        SwingScoutDateField ui = new SwingScoutDateField();
        ui.createField(d, environment);
        return ui;
      }
      else {
        SwingScoutTimeField ui = new SwingScoutTimeField();
        ui.createField(d, environment);
        return ui;
      }
    }
    return null;
  }

}
