/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.form.fields.smartfield;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.extension.ui.form.fields.smartfield.IContentAssistFieldProposalFormExtension;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;

public abstract class AbstractContentAssistFieldProposalForm<LOOKUP_KEY> extends AbstractForm implements IContentAssistFieldProposalForm<LOOKUP_KEY> {

  private final IContentAssistField<?, LOOKUP_KEY> m_contentAssistField;
  private final boolean m_allowCustomText;

  public AbstractContentAssistFieldProposalForm(IContentAssistField<?, LOOKUP_KEY> contentAssistField, boolean allowCustomText) throws ProcessingException {
    super(false);
    m_contentAssistField = contentAssistField;
    m_allowCustomText = allowCustomText;
    callInitializer();
  }

  @Override
  protected boolean getConfiguredAskIfNeedSave() {
    return false;
  }

  @Override
  protected boolean getConfiguredModal() {
    return false;
  }

  @Override
  protected int getConfiguredDisplayHint() {
    return DISPLAY_HINT_VIEW;
  }

  @Override
  public IContentAssistField<?, LOOKUP_KEY> getContentAssistField() {
    return m_contentAssistField;
  }

  public boolean isAllowCustomText() {
    return m_allowCustomText;
  }

  @Override
  public String getSearchText() {
    IContentAssistFieldDataFetchResult<LOOKUP_KEY> searchResult = getSearchResult();
    if (searchResult != null) {
      return searchResult.getSearchText();
    }
    return null;
  }

  private void setSearchResult(IContentAssistFieldDataFetchResult<LOOKUP_KEY> result) {
    propertySupport.setProperty(PROP_SEARCH_RESULT, result);
  }

  @SuppressWarnings("unchecked")
  @Override
  public IContentAssistFieldDataFetchResult<LOOKUP_KEY> getSearchResult() {
    return (IContentAssistFieldDataFetchResult<LOOKUP_KEY>) propertySupport.getProperty(PROP_SEARCH_RESULT);
  }

  @Override
  public final void dataFetchedDelegate(IContentAssistFieldDataFetchResult<LOOKUP_KEY> result, int maxCount) {
    dataFetchedDelegateImpl(result, maxCount);
    setSearchResult(result);
  }

  /**
   * @param result
   * @param maxCount
   */
  protected abstract void dataFetchedDelegateImpl(IContentAssistFieldDataFetchResult<LOOKUP_KEY> result, int maxCount);

  protected static class LocalContentAssistFieldProposalFormExtension<LOOKUP_KEY, OWNER extends AbstractContentAssistFieldProposalForm<LOOKUP_KEY>> extends LocalFormExtension<OWNER> implements IContentAssistFieldProposalFormExtension<LOOKUP_KEY, OWNER> {

    public LocalContentAssistFieldProposalFormExtension(OWNER owner) {
      super(owner);
    }
  }

  @Override
  protected IContentAssistFieldProposalFormExtension<LOOKUP_KEY, ? extends AbstractContentAssistFieldProposalForm<LOOKUP_KEY>> createLocalExtension() {
    return new LocalContentAssistFieldProposalFormExtension<LOOKUP_KEY, AbstractContentAssistFieldProposalForm<LOOKUP_KEY>>(this);
  }

}
