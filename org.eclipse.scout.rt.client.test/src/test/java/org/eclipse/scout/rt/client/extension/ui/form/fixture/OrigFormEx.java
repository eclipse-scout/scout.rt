/*******************************************************************************
 * Copyright (c) 2010-2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
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
