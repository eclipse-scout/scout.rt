/**
 * Copyright (c) 2010-2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * <p>
 * Contributors:
 * BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.client.ui.action.menu.form.fields.fixture;

import java.math.BigDecimal;

import javax.annotation.Generated;

import org.eclipse.scout.rt.shared.data.form.AbstractFormData;
import org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData;

/**
 * <b>NOTE:</b><br>This class is auto generated by the Scout SDK. No manual modifications recommended.
 */
@Generated(value = "org.eclipse.scout.rt.client.ui.action.menu.form.fields.fixture.FormFieldMenuTestForm", comments = "This class is auto generated by the Scout SDK. No manual modifications recommended.")
public class FormFieldMenuTestFormData extends AbstractFormData {
  private static final long serialVersionUID = 1L;

  public BigDecimalInMenu getBigDecimalInMenu() {
    return getFieldByClass(BigDecimalInMenu.class);
  }

  public static class BigDecimalInMenu extends AbstractValueFieldData<BigDecimal> {
    private static final long serialVersionUID = 1L;
  }
}
