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
package org.eclipse.scout.rt.shared.extension.fixture;

import org.eclipse.scout.rt.client.extension.ui.form.fields.AbstractFormFieldExtension;
import org.eclipse.scout.rt.shared.extension.fixture.BasicForm.MainBox.BottomBox.FirstNameField;
import org.eclipse.scout.rt.shared.extension.fixture.BasicForm.MainBox.TopBox.NameField;

public class InvalidExtension {

  public class NameFieldExtension extends AbstractFormFieldExtension<NameField> {
    public NameFieldExtension(NameField originalField) {
      super(originalField);
    }
  }

  public class FirstNameFieldExtension extends AbstractFormFieldExtension<FirstNameField> {
    public FirstNameFieldExtension(FirstNameField originalField) {
      super(originalField);
    }
  }
}
