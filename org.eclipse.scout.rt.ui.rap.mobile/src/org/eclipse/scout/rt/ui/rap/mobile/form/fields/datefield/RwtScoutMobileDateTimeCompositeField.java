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
package org.eclipse.scout.rt.ui.rap.mobile.form.fields.datefield;

import org.eclipse.scout.rt.ui.rap.form.fields.datefield.RwtScoutDateField;
import org.eclipse.scout.rt.ui.rap.form.fields.datefield.RwtScoutDateTimeCompositeField;
import org.eclipse.scout.rt.ui.rap.form.fields.datefield.RwtScoutTimeField;

/**
 * @since 3.8.0
 */
public class RwtScoutMobileDateTimeCompositeField extends RwtScoutDateTimeCompositeField {

  @Override
  protected RwtScoutDateField createRwtScoutDateField() {
    return new RwtScoutMobileDateField();
  }

  @Override
  protected RwtScoutTimeField createRwtScoutTimeField() {
    return new RwtScoutMobileTimeField();
  }

}
