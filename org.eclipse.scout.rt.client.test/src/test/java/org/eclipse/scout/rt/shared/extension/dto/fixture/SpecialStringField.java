/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.shared.extension.dto.fixture;

import org.eclipse.scout.rt.client.dto.FormData;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.platform.extension.Extends;
import org.eclipse.scout.rt.shared.extension.dto.fixture.OrigForm.MainBox;

@Extends(MainBox.class)
@FormData(value = SpecialStringFieldData.class, sdkCommand = FormData.SdkCommand.CREATE, defaultSubtypeSdkCommand = FormData.DefaultSubtypeSdkCommand.CREATE)
public class SpecialStringField extends AbstractStringField {

  public static final String INIT_VAL = "init val of special string field";

  @Override
  protected void execInitField() {
    super.execInitField();
    setValue(INIT_VAL);
  }
}
