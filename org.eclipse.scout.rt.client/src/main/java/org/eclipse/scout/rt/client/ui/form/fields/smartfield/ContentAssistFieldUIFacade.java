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
package org.eclipse.scout.rt.client.ui.form.fields.smartfield;

import org.eclipse.scout.rt.platform.util.CompareUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ContentAssistFieldUIFacade<LOOKUP_KEY> implements IContentAssistFieldUIFacade {

  private static final Logger LOG = LoggerFactory.getLogger(ContentAssistFieldUIFacade.class);

  private final AbstractContentAssistField<?, LOOKUP_KEY> m_field;

  ContentAssistFieldUIFacade(AbstractContentAssistField<?, LOOKUP_KEY> field) {
    m_field = field;
  }

  private String toSearchText(String text) {
    return StringUtility.isNullOrEmpty(text) ? m_field.getWildcard() : text;
  }

  private boolean ignoreUiEvent() {
    return !m_field.isVisible() || !m_field.isEnabled();
  }

  @Override
  public void proposalTypedFromUI(String text) {
    if (ignoreUiEvent()) {
      return;
    }
    LOG.debug("proposalTypedFromUI text={}", text);
    m_field.clearProposal();
    m_field.setDisplayText(text);
    if (!StringUtility.equalsIgnoreNewLines(m_field.getLookupRowFetcher().getLastSearchText(), toSearchText(text))) {
      if (m_field.isBrowseLoadIncremental() && m_field.getWildcard().equals(toSearchText(text))) {
        IContentAssistSearchParam<LOOKUP_KEY> searchParam = ContentAssistSearchParam.createParentParam(null, false);
        m_field.doSearch(searchParam, false);
      }
      else {
        m_field.doSearch(text, false, false);
      }
    }
  }

  @Override
  public void openProposalChooserFromUI(String text, boolean selectCurrentValue) {
    if (ignoreUiEvent()) {
      return;
    }
    LOG.debug("openProposalChooserFromUI text={} selectCurrentValue={}", text, selectCurrentValue);
    m_field.clearProposal();
    m_field.setDisplayText(text);
    String searchText = toSearchText(text);
    IProposalChooser<?, LOOKUP_KEY> proposalChooser = m_field.registerProposalChooserInternal();
    IContentAssistFieldDataFetchResult<LOOKUP_KEY> newResult = m_field.getLookupRowFetcher().newResult(toSearchText(searchText), false);
    proposalChooser.dataFetchedDelegate(newResult, m_field.getConfiguredBrowseMaxRowCount());
    if (m_field.isBrowseLoadIncremental()) {
      IContentAssistSearchParam<LOOKUP_KEY> searchParam = ContentAssistSearchParam.createParentParam(null, selectCurrentValue);
      m_field.doSearch(searchParam, false);
    }
    else {
      m_field.doSearch(text, selectCurrentValue, false);
    }
  }

  @Override
  public void cancelProposalChooserFromUI() {
    LOG.debug("cancelProposalChooserFromUI");
    assert m_field.isProposalChooserRegistered();
    m_field.unregisterProposalChooserInternal();
  }

  @Override
  public void deleteProposalFromUI() {
    if (ignoreUiEvent()) {
      return;
    }
    LOG.debug("deleteProposalFromUI");
    m_field.setValue(null);
    m_field.unregisterProposalChooserInternal();
  }

  @Override
  public void acceptProposalFromUI(String text, boolean chooser, boolean forceClose) {
    if (ignoreUiEvent()) {
      return;
    }
    LOG.debug("acceptProposalFromUI text={} chooser={} forceClose={}", text, chooser, forceClose);

    if (chooser) {

      // last line of defense: normally the UI prevents this kind of request, but you never know...
      if (!m_field.isProposalChooserRegistered()) {
        LOG.debug("acceptProposalFromUI: UI says chooser is open, but the chooser has been unregistered on the model");
        return;
      }

      // When the proposal chooser is open, we must check if the display-text has changed
      // since the last search. When it has changed, we cannot use the accepted proposal
      // and must perform the lookup again instead. This prevents issues as described in
      // ticket #162961:
      // - User types 'Ja' -> matches a lookup-row 'Ja'
      // - User types an additional 'x' and presses tab -> display-text is now 'Jax'
      // - the model accepts the lookup-row 'Ja', but the display-text in the UI is still 'Jax'
      //   and the field looks valid, which is wrong
      boolean acceptByLookupRow = true;
      String searchText = toSearchText(text);
      String lastSearchText = m_field.getProposalChooser().getSearchText();
      if (lastSearchText != null && !lastSearchText.equals(m_field.getWildcard())) {
        acceptByLookupRow = CompareUtility.equals(searchText, lastSearchText);
      }

      boolean openProposalChooser = false;
      try {
        // use lookup row selected in proposal chooser or perform lookup by display text?
        ILookupRow<LOOKUP_KEY> lookupRow = m_field.getProposalChooser().getAcceptedProposal();
        if (acceptByLookupRow && lookupRow != null) {
          m_field.acceptProposal(lookupRow);
        }
        else {
          openProposalChooser = acceptByDisplayText(text);
          openProposalChooserIfNotOpenYet(openProposalChooser, text);
        }
      }
      finally {
        if (!openProposalChooser || forceClose) {
          m_field.unregisterProposalChooserInternal();
        }
      }
    }
    else {
      // perform lookup by display text
      boolean openProposalChooser = acceptByDisplayText(text);
      openProposalChooserIfNotOpenYet(openProposalChooser, text);
    }
  }

  private void openProposalChooserIfNotOpenYet(boolean requestedOpen, String searchText) {
    if (requestedOpen) {
      if (!m_field.isProposalChooserRegistered()) {
        openProposalChooserFromUI(searchText, false);
      }
      m_field.getProposalChooser().forceProposalSelection();
    }
  }

  private boolean acceptByDisplayText(String text) {
    boolean openProposalChooser = false;
    if (StringUtility.hasText(text)) {
      openProposalChooser = m_field.handleAcceptByDisplayText(text);
    }
    else {
      m_field.setValue(null);
    }
    return openProposalChooser;
  }
}
