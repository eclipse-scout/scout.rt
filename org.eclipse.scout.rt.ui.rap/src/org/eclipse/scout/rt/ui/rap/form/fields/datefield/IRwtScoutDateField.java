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

import org.eclipse.scout.rt.client.ui.form.fields.datefield.IDateField;
import org.eclipse.scout.rt.ui.rap.ext.custom.StyledText;
import org.eclipse.scout.rt.ui.rap.form.fields.IRwtScoutFormField;
import org.eclipse.swt.widgets.Button;

/**
 * <h3>IRwtScoutDateField</h3> ...
 * 
 * @since 3.7.0 June 2011
 */
public interface IRwtScoutDateField extends IRwtScoutFormField<IDateField> {

  String VARIANT_DATEFIELD = "datefield";
  String VARIANT_DATEFIELD_DISABLED = "datefield-disabled";

  Button getDropDownButton();

  @Override
  StyledText getUiField();
}
