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
package org.eclipse.scout.rt.client.extension.ui.basic.table.columns;

import org.eclipse.scout.rt.client.extension.ui.basic.table.columns.SmartColumn2Chains.SmartColumn2PrepareLookupChain;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractSmartColumn2;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;

public abstract class AbstractSmartColumn2Extension<VALUE, OWNER extends AbstractSmartColumn2<VALUE>> extends AbstractColumnExtension<VALUE, OWNER> implements ISmartColumn2Extension<VALUE, OWNER> {

  public AbstractSmartColumn2Extension(OWNER owner) {
    super(owner);
  }

  @Override
  public void execPrepareLookup(SmartColumn2PrepareLookupChain<VALUE> chain, ILookupCall<VALUE> call, ITableRow row) {
    chain.execPrepareLookup(call, row);
  }

}
