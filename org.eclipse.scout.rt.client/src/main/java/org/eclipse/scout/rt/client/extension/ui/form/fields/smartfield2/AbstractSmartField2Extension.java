package org.eclipse.scout.rt.client.extension.ui.form.fields.smartfield2;

import java.util.List;

import org.eclipse.scout.rt.client.extension.ui.form.fields.AbstractValueFieldExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.smartfield2.SmartField2Chains.SmartField2BrowseNewChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.smartfield2.SmartField2Chains.SmartField2FilterBrowseLookupResultChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.smartfield2.SmartField2Chains.SmartField2FilterKeyLookupResultChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.smartfield2.SmartField2Chains.SmartField2FilterLookupResultChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.smartfield2.SmartField2Chains.SmartField2FilterRecLookupResultChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.smartfield2.SmartField2Chains.SmartField2FilterTextLookupResultChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.smartfield2.SmartField2Chains.SmartField2PrepareBrowseLookupChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.smartfield2.SmartField2Chains.SmartField2PrepareKeyLookupChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.smartfield2.SmartField2Chains.SmartField2PrepareLookupChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.smartfield2.SmartField2Chains.SmartField2PrepareRecLookupChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.smartfield2.SmartField2Chains.SmartField2PrepareTextLookupChain;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield2.AbstractSmartField2;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;

public abstract class AbstractSmartField2Extension<VALUE, OWNER extends AbstractSmartField2<VALUE>> extends AbstractValueFieldExtension<VALUE, OWNER> implements ISmartField2Extension<VALUE, OWNER> {

  public AbstractSmartField2Extension(OWNER owner) {
    super(owner);
  }

  @Override
  public void execFilterBrowseLookupResult(SmartField2FilterBrowseLookupResultChain<VALUE> chain, ILookupCall<VALUE> call, List<ILookupRow<VALUE>> result) {
    chain.execFilterBrowseLookupResult(call, result);
  }

  @Override
  public ILookupRow<VALUE> execBrowseNew(SmartField2BrowseNewChain<VALUE> chain, String searchText) {
    return chain.execBrowseNew(searchText);
  }

  @Override
  public void execFilterKeyLookupResult(SmartField2FilterKeyLookupResultChain<VALUE> chain, ILookupCall<VALUE> call, List<ILookupRow<VALUE>> result) {
    chain.execFilterKeyLookupResult(call, result);
  }

  @Override
  public void execPrepareLookup(SmartField2PrepareLookupChain<VALUE> chain, ILookupCall<VALUE> call) {
    chain.execPrepareLookup(call);
  }

  @Override
  public void execPrepareTextLookup(SmartField2PrepareTextLookupChain<VALUE> chain, ILookupCall<VALUE> call, String text) {
    chain.execPrepareTextLookup(call, text);
  }

  @Override
  public void execPrepareBrowseLookup(SmartField2PrepareBrowseLookupChain<VALUE> chain, ILookupCall<VALUE> call, String browseHint) {
    chain.execPrepareBrowseLookup(call, browseHint);
  }

  @Override
  public void execFilterTextLookupResult(SmartField2FilterTextLookupResultChain<VALUE> chain, ILookupCall<VALUE> call, List<ILookupRow<VALUE>> result) {
    chain.execFilterTextLookupResult(call, result);
  }

  @Override
  public void execPrepareRecLookup(SmartField2PrepareRecLookupChain<VALUE> chain, ILookupCall<VALUE> call, VALUE parentKey) {
    chain.execPrepareRecLookup(call, parentKey);
  }

  @Override
  public void execFilterLookupResult(SmartField2FilterLookupResultChain<VALUE> chain, ILookupCall<VALUE> call, List<ILookupRow<VALUE>> result) {
    chain.execFilterLookupResult(call, result);
  }

  @Override
  public void execFilterRecLookupResult(SmartField2FilterRecLookupResultChain<VALUE> chain, ILookupCall<VALUE> call, List<ILookupRow<VALUE>> result) {
    chain.execFilterRecLookupResult(call, result);
  }

  @Override
  public void execPrepareKeyLookup(SmartField2PrepareKeyLookupChain<VALUE> chain, ILookupCall<VALUE> call, VALUE key) {
    chain.execPrepareKeyLookup(call, key);
  }
}
