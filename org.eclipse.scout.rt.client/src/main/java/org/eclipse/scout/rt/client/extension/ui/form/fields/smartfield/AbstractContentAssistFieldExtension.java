package org.eclipse.scout.rt.client.extension.ui.form.fields.smartfield;

import java.util.List;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.extension.ui.form.fields.AbstractValueFieldExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.smartfield.ContentAssistFieldChains.ContentAssistFieldBrowseNewChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.smartfield.ContentAssistFieldChains.ContentAssistFieldFilterBrowseLookupResultChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.smartfield.ContentAssistFieldChains.ContentAssistFieldFilterKeyLookupResultChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.smartfield.ContentAssistFieldChains.ContentAssistFieldFilterLookupResultChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.smartfield.ContentAssistFieldChains.ContentAssistFieldFilterRecLookupResultChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.smartfield.ContentAssistFieldChains.ContentAssistFieldFilterTextLookupResultChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.smartfield.ContentAssistFieldChains.ContentAssistFieldPrepareBrowseLookupChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.smartfield.ContentAssistFieldChains.ContentAssistFieldPrepareKeyLookupChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.smartfield.ContentAssistFieldChains.ContentAssistFieldPrepareLookupChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.smartfield.ContentAssistFieldChains.ContentAssistFieldPrepareRecLookupChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.smartfield.ContentAssistFieldChains.ContentAssistFieldPrepareTextLookupChain;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.AbstractContentAssistField;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;

public abstract class AbstractContentAssistFieldExtension<VALUE, LOOKUP_KEY, OWNER extends AbstractContentAssistField<VALUE, LOOKUP_KEY>> extends AbstractValueFieldExtension<VALUE, OWNER> implements IContentAssistFieldExtension<VALUE, LOOKUP_KEY, OWNER> {

  public AbstractContentAssistFieldExtension(OWNER owner) {
    super(owner);
  }

  @Override
  public void execFilterBrowseLookupResult(ContentAssistFieldFilterBrowseLookupResultChain<VALUE, LOOKUP_KEY> chain, ILookupCall<LOOKUP_KEY> call, List<ILookupRow<LOOKUP_KEY>> result) throws ProcessingException {
    chain.execFilterBrowseLookupResult(call, result);
  }

  @Override
  public ILookupRow<LOOKUP_KEY> execBrowseNew(ContentAssistFieldBrowseNewChain<VALUE, LOOKUP_KEY> chain, String searchText) throws ProcessingException {
    return chain.execBrowseNew(searchText);
  }

  @Override
  public void execFilterKeyLookupResult(ContentAssistFieldFilterKeyLookupResultChain<VALUE, LOOKUP_KEY> chain, ILookupCall<LOOKUP_KEY> call, List<ILookupRow<LOOKUP_KEY>> result) throws ProcessingException {
    chain.execFilterKeyLookupResult(call, result);
  }

  @Override
  public void execPrepareLookup(ContentAssistFieldPrepareLookupChain<VALUE, LOOKUP_KEY> chain, ILookupCall<LOOKUP_KEY> call) throws ProcessingException {
    chain.execPrepareLookup(call);
  }

  @Override
  public void execPrepareTextLookup(ContentAssistFieldPrepareTextLookupChain<VALUE, LOOKUP_KEY> chain, ILookupCall<LOOKUP_KEY> call, String text) throws ProcessingException {
    chain.execPrepareTextLookup(call, text);
  }

  @Override
  public void execPrepareBrowseLookup(ContentAssistFieldPrepareBrowseLookupChain<VALUE, LOOKUP_KEY> chain, ILookupCall<LOOKUP_KEY> call, String browseHint) throws ProcessingException {
    chain.execPrepareBrowseLookup(call, browseHint);
  }

  @Override
  public void execFilterTextLookupResult(ContentAssistFieldFilterTextLookupResultChain<VALUE, LOOKUP_KEY> chain, ILookupCall<LOOKUP_KEY> call, List<ILookupRow<LOOKUP_KEY>> result) throws ProcessingException {
    chain.execFilterTextLookupResult(call, result);
  }

  @Override
  public void execPrepareRecLookup(ContentAssistFieldPrepareRecLookupChain<VALUE, LOOKUP_KEY> chain, ILookupCall<LOOKUP_KEY> call, LOOKUP_KEY parentKey) throws ProcessingException {
    chain.execPrepareRecLookup(call, parentKey);
  }

  @Override
  public void execFilterLookupResult(ContentAssistFieldFilterLookupResultChain<VALUE, LOOKUP_KEY> chain, ILookupCall<LOOKUP_KEY> call, List<ILookupRow<LOOKUP_KEY>> result) throws ProcessingException {
    chain.execFilterLookupResult(call, result);
  }

  @Override
  public void execFilterRecLookupResult(ContentAssistFieldFilterRecLookupResultChain<VALUE, LOOKUP_KEY> chain, ILookupCall<LOOKUP_KEY> call, List<ILookupRow<LOOKUP_KEY>> result) throws ProcessingException {
    chain.execFilterRecLookupResult(call, result);
  }

  @Override
  public void execPrepareKeyLookup(ContentAssistFieldPrepareKeyLookupChain<VALUE, LOOKUP_KEY> chain, ILookupCall<LOOKUP_KEY> call, LOOKUP_KEY key) throws ProcessingException {
    chain.execPrepareKeyLookup(call, key);
  }
}
