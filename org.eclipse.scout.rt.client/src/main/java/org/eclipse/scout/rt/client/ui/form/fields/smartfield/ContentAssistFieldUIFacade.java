/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.form.fields.smartfield;

import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.ExceptionHandler;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;

class ContentAssistFieldUIFacade<LOOKUP_KEY> implements IContentAssistFieldUIFacade {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(ContentAssistFieldUIFacade.class);

  private final AbstractContentAssistField<?, LOOKUP_KEY> m_field;

  ContentAssistFieldUIFacade(AbstractContentAssistField<?, LOOKUP_KEY> field) {
    m_field = field;
  }

  private String toSearchText(String text) {
    return StringUtility.isNullOrEmpty(text) ? IContentAssistField.BROWSE_ALL_TEXT : text;
  }

  @Override
  public void proposalTypedFromUI(String text) {
    LOG.debug("proposalTypedFromUI text=" + text);
    assert m_field.isProposalChooserRegistered();
    if (!StringUtility.equalsIgnoreNewLines(m_field.getLookupRowFetcher().getLastSearchText(), toSearchText(text))) {
      m_field.doSearch(text, false, false);
    }
    if (StringUtility.isNullOrEmpty(text)) {
      m_field.getProposalChooser().deselect();
    }
  }

  @Override
  public void openProposalChooserFromUI(String text, boolean selectCurrentValue) {
    LOG.debug("openProposalChooserFromUI");
    assert !m_field.isProposalChooserRegistered();
    try {
      String searchText = toSearchText(text);
      IProposalChooser<?, LOOKUP_KEY> proposalChooser = m_field.registerProposalChooserInternal();
      IContentAssistFieldDataFetchResult<LOOKUP_KEY> newResult = m_field.getLookupRowFetcher().newResult(toSearchText(searchText), selectCurrentValue);
      proposalChooser.dataFetchedDelegate(newResult, m_field.getConfiguredBrowseMaxRowCount());
      m_field.doSearch(text, selectCurrentValue, false);
    }
    catch (ProcessingException e) {
      BEANS.get(ExceptionHandler.class).handle(e);
    }
  }

  @Override
  public void cancelProposalChooserFromUI() {
    LOG.debug("cancelProposalChooserFromUI");
    assert m_field.isProposalChooserRegistered();
    m_field.unregisterProposalChooserInternal();
  }

  @Override
  public void acceptProposalFromUI(String text) {
    if (m_field.hasAcceptedProposal()) {
      ILookupRow<LOOKUP_KEY> acceptedProposal = m_field.getProposalChooser().getAcceptedProposal();
      LOG.debug("acceptProposalFromUI -> acceptProposal. acceptedProposal=" + acceptedProposal);
      // This line is required for the following case:
      // - Smartfield has a selected value and an accepted proposal row with text "Zoom"
      // - User changes smartfield text in UI to "Z" and presses Tab
      // - The display text on the client is still "Zoom", the accepted proposal is unchanged
      //   that's why we must set the display text from the UI (Z), so a property change
      //   event will be triggered to set the UI text back to "Zoom"
      m_field.setDisplayText(text);
      m_field.acceptProposal(acceptedProposal);
    }
    else {
      String oldDisplayText = m_field.getDisplayText();
      if (!StringUtility.equalsIgnoreCase(oldDisplayText, text)) {
        LOG.debug("acceptProposalFromUI, no accepted proposal. parseValue text=" + text);
        m_field.parseAndSetValue(text);
      }
    }
    m_field.unregisterProposalChooserInternal();
  }

}
