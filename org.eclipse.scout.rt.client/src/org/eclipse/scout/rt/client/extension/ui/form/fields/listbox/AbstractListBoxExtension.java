package org.eclipse.scout.rt.client.extension.ui.form.fields.listbox;

import java.util.List;
import java.util.Set;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.extension.ui.form.fields.AbstractValueFieldExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.listbox.ListBoxChains.ListBoxFilterLookupResultChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.listbox.ListBoxChains.ListBoxLoadTableDataChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.listbox.ListBoxChains.ListBoxPopulateTableChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.listbox.ListBoxChains.ListBoxPrepareLookupChain;
import org.eclipse.scout.rt.client.ui.form.fields.listbox.AbstractListBox;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;

public abstract class AbstractListBoxExtension<KEY, OWNER extends AbstractListBox<KEY>> extends AbstractValueFieldExtension<Set<KEY>, OWNER> implements IListBoxExtension<KEY, OWNER> {

  public AbstractListBoxExtension(OWNER owner) {
    super(owner);
  }

  @Override
  public void execPopulateTable(ListBoxPopulateTableChain<KEY> chain) throws ProcessingException {
    chain.execPopulateTable();
  }

  @Override
  public List<? extends ILookupRow<KEY>> execLoadTableData(ListBoxLoadTableDataChain<KEY> chain) throws ProcessingException {
    return chain.execLoadTableData();
  }

  @Override
  public void execFilterLookupResult(ListBoxFilterLookupResultChain<KEY> chain, ILookupCall<KEY> call, List<ILookupRow<KEY>> result) throws ProcessingException {
    chain.execFilterLookupResult(call, result);
  }

  @Override
  public void execPrepareLookup(ListBoxPrepareLookupChain<KEY> chain, ILookupCall<KEY> call) throws ProcessingException {
    chain.execPrepareLookup(call);
  }
}
