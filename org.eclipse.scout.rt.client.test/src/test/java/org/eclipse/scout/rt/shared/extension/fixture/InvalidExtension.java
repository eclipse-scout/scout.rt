/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
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
