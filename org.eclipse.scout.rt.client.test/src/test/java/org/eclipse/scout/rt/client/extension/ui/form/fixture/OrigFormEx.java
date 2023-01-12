/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.extension.ui.form.fixture;

import org.eclipse.scout.rt.client.extension.ui.form.fixture.OrigForm.MainBox.BottomBox;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.Replace;

/**
 * Extended {@link OrigForm} with a replaced {@link BottomBox} and an additional city field.
 */
public class OrigFormEx extends OrigForm {

  @Replace
  public class BottomBoxEx extends MainBox.BottomBox {
    public BottomBoxEx(MainBox container) {
      container.super();
    }

    @Order(20)
    public class CityField extends AbstractStringField {
    }
  }
}
