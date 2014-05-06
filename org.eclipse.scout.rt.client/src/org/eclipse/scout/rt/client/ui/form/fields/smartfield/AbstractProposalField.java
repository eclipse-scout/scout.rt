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

import java.util.Collection;
import java.util.List;

import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.TriState;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.form.fields.ParsingFailedStatus;
import org.eclipse.scout.rt.client.ui.form.fields.button.IButton;
import org.eclipse.scout.rt.shared.services.common.exceptionhandler.IExceptionHandlerService;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;
import org.eclipse.scout.rt.shared.services.lookup.LookupRow;
import org.eclipse.scout.service.SERVICES;

/**
 * This field is similar to the smart field but also allows custom text. A proposal field is always of the value type
 * {@link String}. The proposals are delivered as lookup rows of any type.
 */
public abstract class AbstractProposalField<KEY_TYPE> extends AbstractContentAssistField<String, KEY_TYPE> implements IProposalField<KEY_TYPE> {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractProposalField.class);
  private P_UIFacade m_uiFacade;

  public AbstractProposalField() {
    this(true);
  }

  public AbstractProposalField(boolean callInitializer) {
    super(callInitializer);
  }

  @Override
  protected void initConfig() {
    super.initConfig();
    m_uiFacade = new P_UIFacade();

  }

  @Override
  public IContentAssistFieldUIFacade getUIFacade() {
    return m_uiFacade;
  }

  @Override
  public void applyLazyStyles() {
  }

  @Override
  public KEY_TYPE getValueAsLookupKey() {
    return null;
  }

  @Override
  public void acceptProposal(ILookupRow<KEY_TYPE> row) {
    setCurrentLookupRow(row);
    setValue(row.getText());
  }

  @Override
  protected void installLookupRowContext(ILookupRow<KEY_TYPE> row) {
    setCurrentLookupRow(row);
    super.installLookupRowContext(row);
  }

  @Override
  protected String parseValueInternal(String text) throws ProcessingException {
    if (text != null && text.length() == 0) {
      text = null;
    }
    IContentAssistFieldProposalForm<KEY_TYPE> smartForm = getProposalForm();
    ILookupRow<KEY_TYPE> acceptedProposalRow = null;
    if (smartForm != null && StringUtility.equalsIgnoreNewLines(smartForm.getSearchText(), text)) {
      acceptedProposalRow = smartForm.getAcceptedProposal();
    }
    try {
      String oldText = getDisplayText();
      boolean parsingError = (getErrorStatus() instanceof ParsingFailedStatus);
      if (acceptedProposalRow == null && (!parsingError) && getCurrentLookupRow() != null && StringUtility.equalsIgnoreNewLines(StringUtility.emptyIfNull(text), StringUtility.emptyIfNull(oldText))) {
        // no change
        return getValue();
      }
      else {
        // changed
        if (acceptedProposalRow != null) {
          setCurrentLookupRow(acceptedProposalRow);
          return acceptedProposalRow.getText();
        }
        else if (text == null) {
          setCurrentLookupRow(EMPTY_LOOKUP_ROW);
          return null;
        }
        else {
          setCurrentLookupRow(null);
          doSearch(text, false, true);
          smartForm = getProposalForm();
          if (smartForm != null) {
            acceptedProposalRow = smartForm.getAcceptedProposal();
            if (acceptedProposalRow != null) {
              setCurrentLookupRow(acceptedProposalRow);
              return acceptedProposalRow.getText();
            }
            else {
              // no match possible and proposal is inactive; reject change
              registerProposalFormInternal(smartForm);
              smartForm = null;// prevent close in finally
              setCurrentLookupRow(null);
            }
          }
          return text;
        }
      }
    }
    finally {
      unregisterProposalFormInternal(smartForm);
    }

  }

  @Override
  protected String formatValueInternal(String validKey) {
    if (getCurrentLookupRow() != null) {
      installLookupRowContext(getCurrentLookupRow());
      String text = getCurrentLookupRow().getText();
      if (text != null) {
        text = text.replaceAll("[\\n\\r]+", " ");
      }
      return text;
    }
    return validKey;
  }

  @Override
  protected IContentAssistFieldProposalForm<KEY_TYPE> createProposalForm() throws ProcessingException {
    return createProposalForm(true);
  }

  @Override
  protected void handleProposalFormClosed(IContentAssistFieldProposalForm<KEY_TYPE> proposalForm) throws ProcessingException {
    if (getProposalForm() == proposalForm) {
      if (proposalForm.getCloseSystemType() == IButton.SYSTEM_TYPE_OK) {
        ILookupRow<KEY_TYPE> row = proposalForm.getAcceptedProposal();
        if (row != null) {
          acceptProposal(row);
        }
      }
      registerProposalFormInternal(null);
    }
  }

  @Override
  protected void filterKeyLookup(ILookupCall<KEY_TYPE> call, List<ILookupRow<KEY_TYPE>> result) throws ProcessingException {
    super.filterKeyLookup(call, result);
    /*
     * ticket 79027
     */
    if (result.size() == 0) {
      String key = "" + call.getKey();
      result.add(new LookupRow<KEY_TYPE>(call.getKey(), key));
    }
  }

  @Override
  protected void handleFetchResult(IContentAssistFieldDataFetchResult<KEY_TYPE> result) {
    IContentAssistFieldProposalForm<KEY_TYPE> smartForm = getProposalForm();
    if (result == null) {
      unregisterProposalFormInternal(smartForm);
    }
    else {
      Collection<? extends ILookupRow<KEY_TYPE>> rows = result.getLookupRows();
      if (rows == null || rows.isEmpty()) {
        unregisterProposalFormInternal(smartForm);
      }
      else {
        try {
          if (smartForm == null) {
            smartForm = createProposalForm();
            smartForm.startForm();
            registerProposalFormInternal(smartForm);
          }
          smartForm.dataFetchedDelegate(result, getBrowseMaxRowCount());
        }
        catch (ProcessingException e) {
          SERVICES.getService(IExceptionHandlerService.class).handleException(e);
        }
      }
    }
  }

  private class P_UIFacade implements IContentAssistFieldUIFacade {

    @Override
    public boolean setTextFromUI(String text) {
      String currentValidText = getValue();
      IContentAssistFieldProposalForm<KEY_TYPE> smartForm = getProposalForm();
      // accept proposal form if either input text matches search text or
      // existing display text is valid
      try {
        if (smartForm != null && smartForm.getAcceptedProposal() != null) {
          // a proposal was selected
          return acceptProposalFromUI();
        }
        if (smartForm != null && (StringUtility.equalsIgnoreNewLines(text, smartForm.getSearchText()) || StringUtility.equalsIgnoreNewLines(StringUtility.emptyIfNull(text), StringUtility.emptyIfNull(currentValidText)))) {
          /*
           * empty text means null
           */
          if (text == null || text.length() == 0) {
            boolean b = parseValue(text);
            return b;
          }
          else {
            // no proposal was selected...
            if (!StringUtility.equalsIgnoreNewLines(StringUtility.emptyIfNull(text), StringUtility.emptyIfNull(currentValidText))) {
              return parseValue(text);
            }
            else {
              // ... and current display is unchanged from model value -> nop
              smartForm.doClose();
              return true;
            }
          }

        }
        else {
          /*
           * ticket 88359
           * check if changed at all
           */
          if (CompareUtility.equals(text, currentValidText)) {
            return true;
          }
          else {
            return parseValue(text);
          }
        }
      }
      catch (ProcessingException e) {
        SERVICES.getService(IExceptionHandlerService.class).handleException(e);
        return true;
      }
    }

    @Override
    public void openProposalFromUI(String newText, boolean selectCurrentValue) {
      if (newText == null) {
        newText = BROWSE_ALL_TEXT;
      }
      IContentAssistFieldProposalForm smartForm = getProposalForm();
      if (smartForm == null) {
        setActiveFilter(TriState.TRUE);
        doSearch(newText, selectCurrentValue, false);
      }
      else {
        if (!StringUtility.equalsIgnoreNewLines(getLookupRowFetcher().getLastSearchText(), newText)) {
          doSearch(newText, false, false);
        }
      }
    }

    @Override
    public boolean acceptProposalFromUI() {
      try {
        IContentAssistFieldProposalForm smartForm = getProposalForm();
        if (smartForm != null) {
          if (smartForm.getAcceptedProposal() != null) {
            smartForm.doOk();
            return true;
          }
          else {
            // allow with null text traverse
            if (StringUtility.isNullOrEmpty(getDisplayText())) {
              return true;
            }
            else {
              // select first
              smartForm.forceProposalSelection();
              return false;
            }
          }
        }
      }
      catch (ProcessingException e) {
        SERVICES.getService(IExceptionHandlerService.class).handleException(e);
      }
      return false;
    }

    @Override
    public void unregisterProposalFormFromUI(IContentAssistFieldProposalForm form) {
      unregisterProposalFormInternal(form);
    }
  }

}
