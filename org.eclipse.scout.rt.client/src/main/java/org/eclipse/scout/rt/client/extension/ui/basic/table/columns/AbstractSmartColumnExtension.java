/*
 * Copyright (c) 2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
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
