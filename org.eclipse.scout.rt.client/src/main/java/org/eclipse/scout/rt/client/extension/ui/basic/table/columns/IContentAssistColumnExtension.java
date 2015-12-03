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

import org.eclipse.scout.rt.client.extension.ui.basic.table.columns.ContentAssistColumnChains.ContentAssistColumnConvertValueToKeyChain;
import org.eclipse.scout.rt.client.extension.ui.basic.table.columns.ContentAssistColumnChains.ContentAssistColumnPrepareLookupChain;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractContentAssistColumn;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;

public interface IContentAssistColumnExtension<VALUE, LOOKUP_TYPE, OWNER extends AbstractContentAssistColumn<VALUE, LOOKUP_TYPE>> extends IColumnExtension<VALUE, OWNER> {

  LOOKUP_TYPE execConvertValueToKey(ContentAssistColumnConvertValueToKeyChain<VALUE, LOOKUP_TYPE> chain, VALUE value);

  void execPrepareLookup(ContentAssistColumnPrepareLookupChain<VALUE, LOOKUP_TYPE> chain, ILookupCall<LOOKUP_TYPE> call, ITableRow row);
}
