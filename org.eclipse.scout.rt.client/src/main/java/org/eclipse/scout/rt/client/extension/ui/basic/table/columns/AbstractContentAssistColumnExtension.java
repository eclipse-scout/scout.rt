package org.eclipse.scout.rt.client.extension.ui.basic.table.columns;

import org.eclipse.scout.rt.client.extension.ui.basic.table.columns.ContentAssistColumnChains.ContentAssistColumnConvertValueToKeyChain;
import org.eclipse.scout.rt.client.extension.ui.basic.table.columns.ContentAssistColumnChains.ContentAssistColumnPrepareLookupChain;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractContentAssistColumn;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;

public abstract class AbstractContentAssistColumnExtension<VALUE, LOOKUP_TYPE, OWNER extends AbstractContentAssistColumn<VALUE, LOOKUP_TYPE>> extends AbstractColumnExtension<VALUE, OWNER> implements IContentAssistColumnExtension<VALUE, LOOKUP_TYPE, OWNER> {

  public AbstractContentAssistColumnExtension(OWNER owner) {
    super(owner);
  }

  @Override
  public LOOKUP_TYPE execConvertValueToKey(ContentAssistColumnConvertValueToKeyChain<VALUE, LOOKUP_TYPE> chain, VALUE value) {
    return chain.execConvertValueToKey(value);
  }

  @Override
  public void execPrepareLookup(ContentAssistColumnPrepareLookupChain<VALUE, LOOKUP_TYPE> chain, ILookupCall<LOOKUP_TYPE> call, ITableRow row) {
    chain.execPrepareLookup(call, row);
  }
}
