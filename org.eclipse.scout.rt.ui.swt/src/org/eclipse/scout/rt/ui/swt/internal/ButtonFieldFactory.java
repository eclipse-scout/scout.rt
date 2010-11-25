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
package org.eclipse.scout.rt.ui.swt.internal;

import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.button.IButton;
import org.eclipse.scout.rt.client.ui.form.fields.snapbox.ISnapBox;
import org.eclipse.scout.rt.ui.swt.ISwtEnvironment;
import org.eclipse.scout.rt.ui.swt.extension.IFormFieldFactory;
import org.eclipse.scout.rt.ui.swt.form.fields.ISwtScoutFormField;
import org.eclipse.scout.rt.ui.swt.form.fields.button.ISwtScoutButton;
import org.eclipse.scout.rt.ui.swt.form.fields.button.SwtScoutButton;
import org.eclipse.scout.rt.ui.swt.form.fields.snapbox.button.SwtScoutSnapBoxMaximizedButton;
import org.eclipse.swt.widgets.Composite;

public class ButtonFieldFactory implements IFormFieldFactory {

  public ISwtScoutFormField<?> createFormField(Composite parent, IFormField model, ISwtEnvironment environment) {
    IButton button = (IButton) model;
    ISwtScoutButton field = null;
    if (button.getParentField() instanceof ISnapBox) {
      field = new SwtScoutSnapBoxMaximizedButton();
      field.createField(parent, button, environment);
    }
    else {
      field = new SwtScoutButton();
      field.createField(parent, button, environment);
    }
    return field;
  }

}
