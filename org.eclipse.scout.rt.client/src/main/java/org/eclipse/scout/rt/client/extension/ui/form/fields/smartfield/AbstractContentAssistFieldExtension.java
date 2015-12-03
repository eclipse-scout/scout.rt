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
package org.eclipse.scout.rt.client.extension.ui.form.fields.smartfield;

import java.util.List;

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

public abstract class AbstractContentAssistFieldExtension<VALUE, LOOKUP_KEY, OWNER extends AbstractContentAssistField<VALUE, LOOKUP_KEY>> extends AbstractValueFieldExtension<VALUE, OWNER>
    implements IContentAssistFieldExtension<VALUE, LOOKUP_KEY, OWNER> {

  public AbstractContentAssistFieldExtension(OWNER owner) {
    super(owner);
  }

  @Override
  public void execFilterBrowseLookupResult(ContentAssistFieldFilterBrowseLookupResultChain<VALUE, LOOKUP_KEY> chain, ILookupCall<LOOKUP_KEY> call, List<ILookupRow<LOOKUP_KEY>> result) {
    chain.execFilterBrowseLookupResult(call, result);
  }

  @Override
  public ILookupRow<LOOKUP_KEY> execBrowseNew(ContentAssistFieldBrowseNewChain<VALUE, LOOKUP_KEY> chain, String searchText) {
    return chain.execBrowseNew(searchText);
  }

  @Override
  public void execFilterKeyLookupResult(ContentAssistFieldFilterKeyLookupResultChain<VALUE, LOOKUP_KEY> chain, ILookupCall<LOOKUP_KEY> call, List<ILookupRow<LOOKUP_KEY>> result) {
    chain.execFilterKeyLookupResult(call, result);
  }

  @Override
  public void execPrepareLookup(ContentAssistFieldPrepareLookupChain<VALUE, LOOKUP_KEY> chain, ILookupCall<LOOKUP_KEY> call) {
    chain.execPrepareLookup(call);
  }

  @Override
  public void execPrepareTextLookup(ContentAssistFieldPrepareTextLookupChain<VALUE, LOOKUP_KEY> chain, ILookupCall<LOOKUP_KEY> call, String text) {
    chain.execPrepareTextLookup(call, text);
  }

  @Override
  public void execPrepareBrowseLookup(ContentAssistFieldPrepareBrowseLookupChain<VALUE, LOOKUP_KEY> chain, ILookupCall<LOOKUP_KEY> call, String browseHint) {
    chain.execPrepareBrowseLookup(call, browseHint);
  }

  @Override
  public void execFilterTextLookupResult(ContentAssistFieldFilterTextLookupResultChain<VALUE, LOOKUP_KEY> chain, ILookupCall<LOOKUP_KEY> call, List<ILookupRow<LOOKUP_KEY>> result) {
    chain.execFilterTextLookupResult(call, result);
  }

  @Override
  public void execPrepareRecLookup(ContentAssistFieldPrepareRecLookupChain<VALUE, LOOKUP_KEY> chain, ILookupCall<LOOKUP_KEY> call, LOOKUP_KEY parentKey) {
    chain.execPrepareRecLookup(call, parentKey);
  }

  @Override
  public void execFilterLookupResult(ContentAssistFieldFilterLookupResultChain<VALUE, LOOKUP_KEY> chain, ILookupCall<LOOKUP_KEY> call, List<ILookupRow<LOOKUP_KEY>> result) {
    chain.execFilterLookupResult(call, result);
  }

  @Override
  public void execFilterRecLookupResult(ContentAssistFieldFilterRecLookupResultChain<VALUE, LOOKUP_KEY> chain, ILookupCall<LOOKUP_KEY> call, List<ILookupRow<LOOKUP_KEY>> result) {
    chain.execFilterRecLookupResult(call, result);
  }

  @Override
  public void execPrepareKeyLookup(ContentAssistFieldPrepareKeyLookupChain<VALUE, LOOKUP_KEY> chain, ILookupCall<LOOKUP_KEY> call, LOOKUP_KEY key) {
    chain.execPrepareKeyLookup(call, key);
  }
}
