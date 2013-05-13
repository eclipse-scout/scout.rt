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
package org.eclipse.scout.rt.ui.rap.mobile.form.fields.smartfield;

import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.ISmartField;
import org.eclipse.scout.rt.ui.rap.IRwtEnvironment;
import org.eclipse.scout.rt.ui.rap.extension.IFormFieldFactory;
import org.eclipse.scout.rt.ui.rap.form.fields.IRwtScoutFormField;
import org.eclipse.scout.rt.ui.rap.form.fields.smartfield.IRwtScoutSmartField;
import org.eclipse.scout.rt.ui.rap.form.fields.smartfield.RwtScoutSmartField;
import org.eclipse.scout.rt.ui.rap.util.DeviceUtility;
import org.eclipse.swt.widgets.Composite;

/**
 * @since 3.9.0
 */
public class MobileSmartFieldFactory implements IFormFieldFactory {

  @Override
  public IRwtScoutFormField<?> createUiFormField(Composite parent, IFormField model, IRwtEnvironment uiEnvironment) {
    IRwtScoutSmartField field;

    if (DeviceUtility.isMobileOrTabletDevice()) {
      field = new RwtScoutMobileSmartField();
    }
    else {
      field = new RwtScoutSmartField();
    }

    ISmartField<?> formField = (ISmartField<?>) model;
    field.createUiField(parent, formField, uiEnvironment);

    return field;
  }

}
