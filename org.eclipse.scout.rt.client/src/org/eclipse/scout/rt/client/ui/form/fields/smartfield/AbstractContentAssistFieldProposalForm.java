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
import org.eclipse.scout.rt.client.ui.form.AbstractForm;

public abstract class AbstractContentAssistFieldProposalForm<KEY_TYPE> extends AbstractForm implements IContentAssistFieldProposalForm<KEY_TYPE> {

  private final IContentAssistField<?, KEY_TYPE> m_contentAssistField;
  private final boolean m_allowCustomText;

  public AbstractContentAssistFieldProposalForm(IContentAssistField<?, KEY_TYPE> contentAssistField, boolean allowCustomText) throws ProcessingException {
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
  public IContentAssistField<?, KEY_TYPE> getContentAssistField() {
    return m_contentAssistField;
  }

  public boolean isAllowCustomText() {
    return m_allowCustomText;
  }

  @Override
  public String getSearchText() {
    IContentAssistFieldDataFetchResult<KEY_TYPE> searchResult = getSearchResult();
    if (searchResult != null) {
      return searchResult.getSearchText();
    }
    return null;
  }

  private void setSearchResult(IContentAssistFieldDataFetchResult<KEY_TYPE> result) {
    propertySupport.setProperty(PROP_SEARCH_RESULT, result);
  }

  @SuppressWarnings("unchecked")
  @Override
  public IContentAssistFieldDataFetchResult<KEY_TYPE> getSearchResult() {
    return (IContentAssistFieldDataFetchResult<KEY_TYPE>) propertySupport.getProperty(PROP_SEARCH_RESULT);
  }

  @Override
  public final void dataFetchedDelegate(IContentAssistFieldDataFetchResult<KEY_TYPE> result, int maxCount) {
    dataFetchedDelegateImpl(result, maxCount);
    setSearchResult(result);
  }

  /**
   * @param result
   * @param maxCount
   */
  protected abstract void dataFetchedDelegateImpl(IContentAssistFieldDataFetchResult<KEY_TYPE> result, int maxCount);

}
