/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.scout.rt.ui.rap.form.fields.datefield;

import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.datefield.IDateField;
import org.eclipse.scout.rt.ui.rap.IRwtEnvironment;
import org.eclipse.scout.rt.ui.rap.extension.IFormFieldFactory;
import org.eclipse.scout.rt.ui.rap.form.fields.IRwtScoutFormField;
import org.eclipse.swt.widgets.Composite;

public class DateFieldFactory implements IFormFieldFactory {

  @Override
  public IRwtScoutFormField<?> createUiFormField(Composite parent, IFormField field, IRwtEnvironment uiEnvironment) {
    if (field instanceof IDateField) {
      IDateField d = (IDateField) field;
      if (d.isHasDate() && d.isHasTime()) {
        RwtScoutDateTimeCompositeField ui = new RwtScoutDateTimeCompositeField();
        ui.createUiField(parent, d, uiEnvironment);
        return ui;
      }
      else if (d.isHasDate()) {
        RwtScoutDateField ui = new RwtScoutDateField();
        ui.createUiField(parent, d, uiEnvironment);
        return ui;
      }
      else {
        RwtScoutTimeField ui = new RwtScoutTimeField();
        ui.createUiField(parent, d, uiEnvironment);
        return ui;
      }
    }
    return null;
  }
}
