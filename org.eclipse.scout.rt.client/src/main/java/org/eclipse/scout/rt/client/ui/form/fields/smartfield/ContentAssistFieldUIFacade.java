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

import org.eclipse.scout.rt.platform.status.IMultiStatus;
import org.eclipse.scout.rt.platform.status.IStatus;
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

  protected AbstractContentAssistField<?, LOOKUP_KEY> getField() {
    return m_field;
  }

  private String toSearchText(String text) {
    return StringUtility.isNullOrEmpty(text) ? m_field.getWildcard() : text;
  }

  protected boolean ignoreUiEvent() {
    return !m_field.isVisible() || !m_field.isEnabled();
  }

  @Override
  public void proposalTypedFromUI(String displayText) {
    if (ignoreUiEvent()) {
      return;
    }
    LOG.debug("proposalTypedFromUI displayText={}", displayText);
    if (m_field.isProposalChooserRegistered()) {
      m_field.getProposalChooser().deselect();
    }
    m_field.setDisplayText(displayText);

    String searchText = toSearchText(displayText);
    if (!StringUtility.equalsIgnoreNewLines(m_field.getLookupRowFetcher().getLastSearchText(), searchText)) {
      if (m_field.isBrowseLoadIncremental() && m_field.getWildcard().equals(searchText)) {
        IContentAssistSearchParam<LOOKUP_KEY> searchParam = ContentAssistSearchParam.createParentParam(null, false);
        m_field.doSearch(searchParam, false);
      }
      else {
        m_field.doSearch(displayText, false, false);
      }
    }
  }

  @Override
  public void openProposalChooserFromUI(String displayText, boolean browseAll, boolean selectCurrentValue) {
    if (ignoreUiEvent()) {
      return;
    }
    LOG.debug("openProposalChooserFromUI displayText={} browseAll={} selectCurrentValue={}", displayText, browseAll, selectCurrentValue);
    if (m_field.isProposalChooserRegistered()) {
      m_field.getProposalChooser().deselect();
    }
    m_field.setDisplayText(displayText);
    String searchText = toSearchText(browseAll, displayText);
    IProposalChooser<?, LOOKUP_KEY> proposalChooser = m_field.registerProposalChooserInternal();
    IContentAssistFieldDataFetchResult<LOOKUP_KEY> newResult = m_field.getLookupRowFetcher().newResult(toSearchText(searchText), false);
    proposalChooser.dataFetchedDelegate(newResult, m_field.getConfiguredBrowseMaxRowCount());
    if (m_field.isBrowseLoadIncremental()) {
      IContentAssistSearchParam<LOOKUP_KEY> searchParam = ContentAssistSearchParam.createParentParam(null, selectCurrentValue);
      m_field.doSearch(searchParam, false);
    }
    else {
      m_field.doSearch(searchText, selectCurrentValue, false);
    }
  }

  /**
   * @return The search text used for a lookup call, depending on the state of the browseAll flag and the error status.
   */
  protected String toSearchText(boolean browseAll, String displayText) {
    // browseAll == false -> search for displayText
    if (!browseAll) {
      return toSearchText(displayText);
    }

    // browseAll == true + no errors -> search for *
    boolean valid = getField().getErrorStatus() == null;
    if (valid) {
      return toSearchText(null);
    }

    // browseAll == true + errors + error contains NOT_UNIQUE_ERROR_CODE -> search for displayText
    if (hasNotUniqueErrorCode(getField().getErrorStatus())) {
      return toSearchText(displayText);
    }

    // browseAll == true + errors + errors other than NOT_UNIQUE_ERROR_CODE -> search for *
    return toSearchText(null);
  }

  protected boolean hasNotUniqueErrorCode(IStatus errorStatus) {
    if (errorStatus.getCode() == AbstractMixedSmartField.NOT_UNIQUE_ERROR_CODE) {
      return true;
    }

    if (errorStatus instanceof IMultiStatus) {
      IMultiStatus multiStatus = (IMultiStatus) errorStatus;
      for (IStatus child : multiStatus.getChildren()) {
        if (hasNotUniqueErrorCode(child)) {
          return true;
        }
      }
    }

    return false;
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
  public void acceptProposalFromUI(String displayText, boolean chooser, boolean forceClose) {
    if (ignoreUiEvent()) {
      return;
    }
    boolean modelChooser = getField().isProposalChooserRegistered();
    LOG.debug("acceptProposalFromUI displayText={} chooser={} modelChooser={} forceClose={}", displayText, chooser, modelChooser, forceClose);

    // Info: chooser == true && modelChooser == false
    // This case happens when a lookup call (=background job) terminates and returns no
    // results at all. In that case the proposal chooser is automatically closed on the model
    // but the UI is not yet informed about that change. That's why the chooser flag sent from
    // the UI is still true.
    if (chooser && modelChooser) {
      acceptByProposalChooser(displayText, forceClose);
    }
    else {
      acceptDisplayTextAndOpenProposalChooser(displayText);
    }
  }

  protected void acceptDisplayTextAndOpenProposalChooser(String displayText) {
    // perform lookup by display text
    boolean openProposalChooser = acceptByDisplayText(displayText);
    openProposalChooserIfNotOpenYet(openProposalChooser, displayText);
  }

  protected void acceptByProposalChooser(String displayText, boolean forceClose) {
    // When the proposal chooser is open, we must check if the display-text has changed
    // since the last search. When it has changed, we cannot use the accepted proposal
    // and must perform the lookup again instead. This prevents issues as described in
    // ticket #162961:
    // - User types 'Ja' -> matches a lookup-row 'Ja'
    // - User types an additional 'x' and presses tab -> display-text is now 'Jax'
    // - the model accepts the lookup-row 'Ja', but the display-text in the UI is still 'Jax'
    //   and the field looks valid, which is wrong
    boolean acceptByLookupRow = true;
    String searchText = toSearchText(displayText);
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
        openProposalChooser = acceptByDisplayText(displayText);
        openProposalChooserIfNotOpenYet(openProposalChooser, displayText);
      }
    }
    finally {
      if (!openProposalChooser || forceClose) {
        m_field.unregisterProposalChooserInternal();
      }
    }
  }

  private void openProposalChooserIfNotOpenYet(boolean requestedOpen, String displayText) {
    if (requestedOpen) {
      if (!m_field.isProposalChooserRegistered()) {
        openProposalChooserFromUI(displayText, false, false);
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
