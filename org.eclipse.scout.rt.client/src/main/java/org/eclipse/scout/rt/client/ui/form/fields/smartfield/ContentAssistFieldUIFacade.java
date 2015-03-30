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
import org.eclipse.scout.rt.shared.services.common.exceptionhandler.IExceptionHandlerService;
import org.eclipse.scout.service.SERVICES;

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
    String searchText = toSearchText(text);
    LOG.debug("proposalTypedFromUI searchText=" + searchText);
    assert m_field.isProposalChooserRegistered();
    if (!StringUtility.equalsIgnoreNewLines(m_field.getLookupRowFetcher().getLastSearchText(), searchText)) {
      m_field.doSearch(searchText, false, false);
    }
  }

  @Override
  public void openProposalChooserFromUI(String text) { // FIXME AWE: (smart-field) ist das nicht immer browse-all? dann brauchen wir den text nie
    String searchText = IContentAssistField.BROWSE_ALL_TEXT;
    LOG.debug("openProposalChooserFromUI text=" + searchText);
    assert !m_field.isProposalChooserRegistered();
    try {
      IProposalChooser<?, LOOKUP_KEY> proposalChooser = m_field.registerProposalChooserInternal();
      proposalChooser.dataFetchedDelegate(m_field.getLookupRowFetcher().getResult(), m_field.getConfiguredBrowseMaxRowCount());
      m_field.doSearch(searchText, false, false); // FIXME AWE: (smart-field) select current value
    }
    catch (ProcessingException e) {
      SERVICES.getService(IExceptionHandlerService.class).handleException(e);
    }
  }

  @Override
  public void cancelProposalChooserFromUI() {
    System.out.println("cancelProposalChooserFromUI");
    assert m_field.isProposalChooserRegistered();
    m_field.unregisterProposalChooserInternal();
  }

  @Override
  public void acceptProposalFromUI(String text) {
    String searchText = toSearchText(text);
    if (m_field.hasAcceptedProposal()) {
      LOG.debug("acceptProposalFromUI -> acceptProposal");
      m_field.setDisplayText(text);
      m_field.acceptProposal(m_field.getProposalChooser().getAcceptedProposal());
    }
    else {
      LOG.debug("acceptProposalFromUI, no accepted proposalsearchText -> parseValue searchText=" + searchText);
      m_field.parseValue(searchText);
    }
    m_field.unregisterProposalChooserInternal();
  }

  @Override
  public void setTextFromUI(String text) {
    throw new IllegalStateException();
//    String searchText = toSearchText(text);
//    LOG.debug("setTextFromUI searchText=" + searchText);
//    m_field.parseValue(searchText);
//    m_field.unregisterProposalChooserInternal();
  }

}
