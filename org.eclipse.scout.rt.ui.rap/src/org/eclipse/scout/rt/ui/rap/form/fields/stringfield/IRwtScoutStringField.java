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
package org.eclipse.scout.rt.ui.rap.form.fields.stringfield;

import org.eclipse.scout.rt.client.ui.form.fields.stringfield.IStringField;
import org.eclipse.scout.rt.ui.rap.ext.StyledTextEx;
import org.eclipse.scout.rt.ui.rap.form.fields.IRwtScoutFormField;

public interface IRwtScoutStringField extends IRwtScoutFormField<IStringField> {

  String VARIANT_STRINGFIELD = "stringfield";
  String VARIANT_STRINGFIELD_DISABLED = "stringfield-disabled";

  @Override
  StyledTextEx getUiField();
}
