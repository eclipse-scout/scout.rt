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
package org.eclipse.scout.rt.ui.rap.mobile.form.fields.datefield;

import org.eclipse.scout.rt.client.ui.form.fields.datefield.IDateField;
import org.eclipse.scout.rt.ui.rap.form.fields.IRwtScoutFormField;
import org.eclipse.scout.rt.ui.rap.form.fields.datefield.DateFieldFactory;
import org.eclipse.scout.rt.ui.rap.form.fields.datefield.IRwtScoutDateField;
import org.eclipse.scout.rt.ui.rap.form.fields.datefield.IRwtScoutTimeField;
import org.eclipse.scout.rt.ui.rap.form.fields.datefield.RwtScoutDateField;
import org.eclipse.scout.rt.ui.rap.form.fields.datefield.RwtScoutDateTimeCompositeField;
import org.eclipse.scout.rt.ui.rap.form.fields.datefield.RwtScoutTimeField;
import org.eclipse.scout.rt.ui.rap.util.DeviceUtility;

public class MobileDateFieldFactory extends DateFieldFactory {

  @Override
  protected IRwtScoutFormField<IDateField> createRwtScoutDateTimeField() {
    if (DeviceUtility.isMobileOrTabletDevice()) {
      return new RwtScoutMobileDateTimeCompositeField();
    }
    else {
      return new RwtScoutDateTimeCompositeField();
    }
  }

  @Override
  protected IRwtScoutDateField createRwtScoutDateField() {
    if (DeviceUtility.isMobileOrTabletDevice()) {
      return new RwtScoutMobileDateField();
    }
    else {
      return new RwtScoutDateField();
    }
  }

  @Override
  protected IRwtScoutTimeField createRwtScoutTimeField() {
    if (DeviceUtility.isMobileOrTabletDevice()) {
      return new RwtScoutMobileTimeField();
    }
    else {
      return new RwtScoutTimeField();
    }
  }

}
