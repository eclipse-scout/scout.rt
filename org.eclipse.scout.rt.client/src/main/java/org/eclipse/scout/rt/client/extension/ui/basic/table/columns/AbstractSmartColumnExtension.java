/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.extension.ui.basic.table.columns;

import org.eclipse.scout.rt.client.extension.ui.basic.table.columns.SmartColumnChains.SmartColumnPrepareLookupChain;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractSmartColumn;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;

public abstract class AbstractSmartColumnExtension<VALUE, OWNER extends AbstractSmartColumn<VALUE>> extends AbstractColumnExtension<VALUE, OWNER> implements ISmartColumnExtension<VALUE, OWNER> {

  public AbstractSmartColumnExtension(OWNER owner) {
    super(owner);
  }

  @Override
  public void execPrepareLookup(SmartColumnPrepareLookupChain<VALUE> chain, ILookupCall<VALUE> call, ITableRow row) {
    chain.execPrepareLookup(call, row);
  }
}
