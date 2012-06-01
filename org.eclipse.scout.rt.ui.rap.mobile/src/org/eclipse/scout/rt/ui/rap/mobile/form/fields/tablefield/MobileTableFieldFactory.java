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
package org.eclipse.scout.rt.ui.rap.mobile.form.fields.tablefield;

import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.tablefield.ITableField;
import org.eclipse.scout.rt.ui.rap.IRwtEnvironment;
import org.eclipse.scout.rt.ui.rap.extension.IFormFieldFactory;
import org.eclipse.scout.rt.ui.rap.form.fields.IRwtScoutFormField;
import org.eclipse.scout.rt.ui.rap.form.fields.tablefield.IRwtScoutTableField;
import org.eclipse.scout.rt.ui.rap.form.fields.tablefield.RwtScoutTableField;
import org.eclipse.scout.rt.ui.rap.util.DeviceUtility;
import org.eclipse.swt.widgets.Composite;

/**
 * @since 3.8.0
 */
public class MobileTableFieldFactory implements IFormFieldFactory {

  @Override
  public IRwtScoutFormField<?> createUiFormField(Composite parent, IFormField model, IRwtEnvironment uiEnvironment) {
    IRwtScoutTableField field;

    if (DeviceUtility.isMobileOrTabletDevice()) {
      field = new RwtScoutMobileTableField();
    }
    else {
      field = new RwtScoutTableField();
    }

    ITableField<?> tableField = (ITableField) model;
    field.createUiField(parent, tableField, uiEnvironment);

    return field;
  }

}
