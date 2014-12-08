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
