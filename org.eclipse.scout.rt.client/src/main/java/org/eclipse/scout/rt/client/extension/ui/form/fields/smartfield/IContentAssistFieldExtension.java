package org.eclipse.scout.rt.client.extension.ui.form.fields.smartfield;

import java.util.List;

import org.eclipse.scout.rt.client.extension.ui.form.fields.IValueFieldExtension;
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

public interface IContentAssistFieldExtension<VALUE, LOOKUP_KEY, OWNER extends AbstractContentAssistField<VALUE, LOOKUP_KEY>> extends IValueFieldExtension<VALUE, OWNER> {

  void execFilterBrowseLookupResult(ContentAssistFieldFilterBrowseLookupResultChain<VALUE, LOOKUP_KEY> chain, ILookupCall<LOOKUP_KEY> call, List<ILookupRow<LOOKUP_KEY>> result);

  ILookupRow<LOOKUP_KEY> execBrowseNew(ContentAssistFieldBrowseNewChain<VALUE, LOOKUP_KEY> chain, String searchText);

  void execFilterKeyLookupResult(ContentAssistFieldFilterKeyLookupResultChain<VALUE, LOOKUP_KEY> chain, ILookupCall<LOOKUP_KEY> call, List<ILookupRow<LOOKUP_KEY>> result);

  void execPrepareLookup(ContentAssistFieldPrepareLookupChain<VALUE, LOOKUP_KEY> chain, ILookupCall<LOOKUP_KEY> call);

  void execPrepareTextLookup(ContentAssistFieldPrepareTextLookupChain<VALUE, LOOKUP_KEY> chain, ILookupCall<LOOKUP_KEY> call, String text);

  void execPrepareBrowseLookup(ContentAssistFieldPrepareBrowseLookupChain<VALUE, LOOKUP_KEY> chain, ILookupCall<LOOKUP_KEY> call, String browseHint);

  void execFilterTextLookupResult(ContentAssistFieldFilterTextLookupResultChain<VALUE, LOOKUP_KEY> chain, ILookupCall<LOOKUP_KEY> call, List<ILookupRow<LOOKUP_KEY>> result);

  void execPrepareRecLookup(ContentAssistFieldPrepareRecLookupChain<VALUE, LOOKUP_KEY> chain, ILookupCall<LOOKUP_KEY> call, LOOKUP_KEY parentKey);

  void execFilterLookupResult(ContentAssistFieldFilterLookupResultChain<VALUE, LOOKUP_KEY> chain, ILookupCall<LOOKUP_KEY> call, List<ILookupRow<LOOKUP_KEY>> result);

  void execFilterRecLookupResult(ContentAssistFieldFilterRecLookupResultChain<VALUE, LOOKUP_KEY> chain, ILookupCall<LOOKUP_KEY> call, List<ILookupRow<LOOKUP_KEY>> result);

  void execPrepareKeyLookup(ContentAssistFieldPrepareKeyLookupChain<VALUE, LOOKUP_KEY> chain, ILookupCall<LOOKUP_KEY> call, LOOKUP_KEY key);
}
