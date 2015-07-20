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

import java.util.List;

import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.form.fields.ValidationFailedStatus;
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
    m_field.clearProposal();
    m_field.setDisplayText(text);
    if (!StringUtility.equalsIgnoreNewLines(m_field.getLookupRowFetcher().getLastSearchText(), toSearchText(text))) {
      m_field.doSearch(text, false, false);
    }
  }

  @Override
  public void openProposalChooserFromUI(String text, boolean selectCurrentValue) {
    LOG.debug("openProposalChooserFromUI");
    m_field.clearProposal();
    try {
      m_field.setDisplayText(text);
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
  public void acceptProposalFromUI(String text, boolean chooser) {
    boolean openProposalChooser = false;
    if (chooser) {
      // choose from proposal chooser
      try {
        ILookupRow<LOOKUP_KEY> lookupRow = m_field.getProposalChooser().getAcceptedProposal();
        if (lookupRow == null) {
          openProposalChooser = acceptByDisplayText(text);
          openProposalChooserIfNotOpenYet(openProposalChooser, text);
        }
        else {
          m_field.acceptProposal(lookupRow);
        }
      }
      finally {
        if (!openProposalChooser) {
          m_field.unregisterProposalChooserInternal();
        }
      }
    }
    else {
      openProposalChooser = acceptByDisplayText(text);
      openProposalChooserIfNotOpenYet(openProposalChooser, text);
    }
  }

  private void openProposalChooserIfNotOpenYet(boolean requestedOpen, String searchText) {
    if (requestedOpen) {
      if (!m_field.isProposalChooserRegistered()) {
        openProposalChooserFromUI(searchText, false);
      }
      try {
        m_field.getProposalChooser().forceProposalSelection();
      }
      catch (ProcessingException e) {
        // FIXME AWE Auto-generated catch block
        e.printStackTrace();
      }
    }
  }

  private boolean acceptByDisplayText(String text) {
    if (StringUtility.hasText(text)) {
      if (m_field instanceof IProposalField) { // FIXME AWE: remove, use inheritance
        ((IProposalField) m_field).setValue(text);
      }
      else {
        // search
        try {
          String searchText = toSearchText(text);
          List<? extends ILookupRow<LOOKUP_KEY>> lookupRows = m_field.callTextLookup(searchText, 2);
          int numRows = lookupRows.size();
          if (numRows == 0) {
//            m_field.handleNoMatchFound(text);
            m_field.setEmptyLookupRow();
            m_field.setDisplayText(text);
            m_field.addErrorStatus(new ValidationFailedStatus("%Kein Ergebnis für '" + text + "'"));
          }
          else if (numRows == 1) {
            m_field.acceptProposal(lookupRows.get(0));
          }
          else if (numRows > 1) {
//            m_field.setEmptyLookupRow();
//            m_field.setDisplayText(text);
            m_field.addErrorStatus(new ValidationFailedStatus("%Mehr als ein Ergebnis für '" + text + "'"));
            return true;
          }
        }
        catch (ProcessingException e) {
          // FIXME AWE: correct error message, i18n
          m_field.setEmptyLookupRow();
          m_field.addErrorStatus(new ValidationFailedStatus("%Fehler bei der Suche nach '" + text + "'"));
        }
      }
    }
    else {
      // set to null
      m_field.setEmptyLookupRow();
    }

    return false;
  }
}
