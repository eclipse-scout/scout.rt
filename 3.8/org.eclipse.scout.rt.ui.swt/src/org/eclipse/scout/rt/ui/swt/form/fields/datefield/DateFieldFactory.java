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
package org.eclipse.scout.rt.ui.swt.form.fields.datefield;

import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.datefield.IDateField;
import org.eclipse.scout.rt.ui.swt.ISwtEnvironment;
import org.eclipse.scout.rt.ui.swt.extension.IFormFieldFactory;
import org.eclipse.scout.rt.ui.swt.form.fields.ISwtScoutFormField;
import org.eclipse.swt.widgets.Composite;

public class DateFieldFactory implements IFormFieldFactory {

  @Override
  public ISwtScoutFormField<?> createFormField(Composite parent, IFormField field, ISwtEnvironment environment) {
    if (field instanceof IDateField) {
      IDateField d = (IDateField) field;
      if (d.isHasDate() && d.isHasTime()) {
        SwtScoutDateField ui = new SwtScoutDateField();
        ui.createField(parent, d, environment);
        return ui;
      }
      else if (d.isHasDate()) {
        SwtScoutDateField ui = new SwtScoutDateField();
        ui.createField(parent, d, environment);
        return ui;
      }
      else {
        SwtScoutTimeField ui = new SwtScoutTimeField();
        ui.createField(parent, d, environment);
        return ui;
      }
    }
    return null;
  }

}
