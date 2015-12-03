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

import org.eclipse.scout.rt.client.ui.basic.table.columns.IBigIntegerColumn;
import org.eclipse.scout.rt.client.ui.form.fields.bigintegerfield.IBigIntegerField;

/**
 * @since 3.9.0
 */
public class BigIntegerColumnFieldPropertyDelegator extends ColumnFieldPropertyDelegator<IBigIntegerColumn, IBigIntegerField> {

  public BigIntegerColumnFieldPropertyDelegator(IBigIntegerColumn sender, IBigIntegerField receiver) {
    super(sender, receiver);
  }

  @Override
  public void init() {
    super.init();
    getReceiver().setFormat(getSender().getFormat());
    getReceiver().setGroupingUsed(getSender().isGroupingUsed());
  }

}
