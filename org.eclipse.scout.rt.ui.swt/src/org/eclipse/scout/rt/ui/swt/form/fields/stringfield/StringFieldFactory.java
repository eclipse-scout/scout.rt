/*******************************************************************************
 * Copyright (c) 2010,2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 *     Rene Eigenheer - Patch from Bug 359677
 ******************************************************************************/
package org.eclipse.scout.rt.ui.swt.form.fields.stringfield;

import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.IStringField;
import org.eclipse.scout.rt.ui.swt.ISwtEnvironment;
import org.eclipse.scout.rt.ui.swt.extension.IFormFieldFactory;
import org.eclipse.scout.rt.ui.swt.form.fields.ISwtScoutFormField;
import org.eclipse.swt.widgets.Composite;

/**
 * 
 */
public class StringFieldFactory implements IFormFieldFactory {

  @Override
  public ISwtScoutFormField<?> createFormField(Composite parent, IFormField field, ISwtEnvironment environment) {
    if (field instanceof IStringField) {
      IStringField s = (IStringField) field;
      if (s.isInputMasked()) {
        SwtScoutStringPlainTextField ui = new SwtScoutStringPlainTextField();
        ui.createField(parent, s, environment);
        return ui;
      }
      else {
        SwtScoutStringField ui = new SwtScoutStringField();
        ui.createField(parent, s, environment);
        return ui;
      }
    }
    return null;
  }
}
