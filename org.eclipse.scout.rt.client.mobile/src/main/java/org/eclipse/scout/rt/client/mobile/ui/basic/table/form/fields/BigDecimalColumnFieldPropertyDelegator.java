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
package org.eclipse.scout.rt.client.mobile.ui.basic.table.form.fields;

import org.eclipse.scout.rt.client.ui.basic.table.columns.IBigDecimalColumn;
import org.eclipse.scout.rt.client.ui.form.fields.bigdecimalfield.IBigDecimalField;

/**
 * @since 3.9.0
 */
public class BigDecimalColumnFieldPropertyDelegator extends ColumnFieldPropertyDelegator<IBigDecimalColumn, IBigDecimalField> {

  public BigDecimalColumnFieldPropertyDelegator(IBigDecimalColumn sender, IBigDecimalField receiver) {
    super(sender, receiver);
  }

  @Override
  public void init() {
    super.init();
    getReceiver().setFormat(getSender().getFormat());
    getReceiver().setMinFractionDigits(getSender().getMinFractionDigits());
    getReceiver().setMaxFractionDigits(getSender().getMaxFractionDigits());
    getReceiver().setGroupingUsed(getSender().isGroupingUsed());
    getReceiver().setPercent(getSender().isPercent());
    getReceiver().setMultiplier(getSender().getMultiplier());
  }

}
