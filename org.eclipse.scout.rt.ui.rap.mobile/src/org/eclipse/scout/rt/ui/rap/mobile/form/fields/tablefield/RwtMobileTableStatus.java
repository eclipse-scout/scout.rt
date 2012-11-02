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
package org.eclipse.scout.rt.ui.rap.mobile.form.fields.tablefield;

import org.eclipse.scout.rt.client.mobile.ui.form.outline.IMainPageForm;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.tablefield.ITableField;
import org.eclipse.scout.rt.ui.rap.IRwtEnvironment;
import org.eclipse.scout.rt.ui.rap.form.fields.tablefield.RwtTableStatus;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

public class RwtMobileTableStatus extends RwtTableStatus {

  public RwtMobileTableStatus(Composite parent, IRwtEnvironment uiEnvironment, ITableField<?> model) {
    super(parent, uiEnvironment, model);
  }

  @Override
  protected String getVariant(ITableField<?> table) {
    IForm form = table.getForm();
    if (form instanceof IMainPageForm) {
      return VARIANT_OUTLINE_TABLE_STATUS;
    }

    return super.getVariant(table);
  }

  @Override
  protected int getLabelHorizontalAlignment() {
    return SWT.CENTER;
  }

}
