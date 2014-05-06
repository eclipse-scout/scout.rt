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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.TriState;
import org.eclipse.scout.commons.annotations.ConfigOperation;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.annotations.ScoutSdkIgnore;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.exception.VetoException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ClientAsyncJob;
import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.client.services.lookup.FormFieldProvisioningContext;
import org.eclipse.scout.rt.client.services.lookup.ILookupCallProvisioningService;
import org.eclipse.scout.rt.client.ui.form.fields.ParsingFailedStatus;
import org.eclipse.scout.rt.client.ui.form.fields.button.IButton;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.shared.services.common.exceptionhandler.IExceptionHandlerService;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;
import org.eclipse.scout.rt.shared.services.lookup.LocalLookupCall;
import org.eclipse.scout.service.SERVICES;

/**
 * A smart field with a key type different from the value type.
 * The default implementation of {@link #convertKeyToValue(Object)} and {@link #convertValueToKey(Object)} methods works
 * for any case where <VALUE_TYPE extends LOOKUP_CALL_KEY_TYPE>. For all other cases provide your own conversion
 * methods.
 * 
 * @param <VALUE_TYPE>
 * @param <LOOKUP_CALL_KEY_TYPE>
 */
@ScoutSdkIgnore
public class AbstractMixedSmartField<VALUE_TYPE, LOOKUP_CALL_KEY_TYPE> extends AbstractContentAssistField<VALUE_TYPE, LOOKUP_CALL_KEY_TYPE> {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractMixedSmartField.class);
  private P_GetLookupRowByKeyJob m_currentGetLookupRowByKeyJob;
  private P_UIFacade m_uiFacade;

  public AbstractMixedSmartField() {
    this(true);
  }

  public AbstractMixedSmartField(boolean callInitializer) {
    super(callInitializer);
  }

  /**
   * the default implementation simply casts one to the other type
   * 
   * @param key
   * @return
   */
  @SuppressWarnings("unchecked")
  @ConfigOperation
  @Order(400)
  protected VALUE_TYPE execConvertKeyToValue(LOOKUP_CALL_KEY_TYPE key) {
    return (VALUE_TYPE) key;
  }

  /**
   * the default implementation simply casts one to the other type
   * 
   * @param key
   * @return
   */
  @SuppressWarnings("unchecked")
  @ConfigOperation
  @Order(410)
  protected LOOKUP_CALL_KEY_TYPE execConvertValueToKey(VALUE_TYPE value) {
    return (LOOKUP_CALL_KEY_TYPE) value;
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
  public LOOKUP_CALL_KEY_TYPE getValueAsLookupKey() {
    return execConvertValueToKey(getValue());
  }

  @Override
  protected VALUE_TYPE parseValueInternal(String text) throws ProcessingException {
    if (text != null && text.length() == 0) {
      text = null;
    }
    IContentAssistFieldProposalForm<LOOKUP_CALL_KEY_TYPE> smartForm = getProposalForm();
    ILookupRow<LOOKUP_CALL_KEY_TYPE> acceptedProposalRow = null;
    if (smartForm != null && StringUtility.equalsIgnoreNewLines(smartForm.getSearchText(), text)) {
      acceptedProposalRow = smartForm.getAcceptedProposal();
    }
    //
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
          return execConvertKeyToValue(acceptedProposalRow.getKey());
        }
        else if (text == null) {
          setCurrentLookupRow(EMPTY_LOOKUP_ROW);
          return null;
        }
        else {
          doSearch(text, false, true);
          IContentAssistFieldDataFetchResult<LOOKUP_CALL_KEY_TYPE> fetchResult = getLookupRowFetcher().getResult();
          if (fetchResult != null && fetchResult.getLookupRows() != null && fetchResult.getLookupRows().size() == 1) {
            acceptedProposalRow = CollectionUtility.firstElement(fetchResult.getLookupRows());
          }
          if (acceptedProposalRow != null) {
            setCurrentLookupRow(acceptedProposalRow);
            return execConvertKeyToValue(acceptedProposalRow.getKey());
          }
          else {
            // no match possible and proposal is inactive; reject change
            if (smartForm == null) {
              smartForm = createProposalForm();
              smartForm.startForm();
              smartForm.dataFetchedDelegate(fetchResult, getConfiguredBrowseMaxRowCount());
            }
            registerProposalFormInternal(smartForm);
            smartForm = null;// prevent close in finally
            throw new VetoException(ScoutTexts.get("SmartFieldCannotComplete", text));
          }
        }
      }
    }
    finally {
      unregisterProposalFormInternal(smartForm);
    }
  }

  @Override
  public void acceptProposal(ILookupRow<LOOKUP_CALL_KEY_TYPE> row) {
    setCurrentLookupRow(row);
    setValue(execConvertKeyToValue(row.getKey()));
  }

  @Override
  public void applyLazyStyles() {
    // override: ensure that (async loading) lookup context has been set
    if (m_currentGetLookupRowByKeyJob != null) {
      if (m_currentGetLookupRowByKeyJob.getClientSession() == ClientSyncJob.getCurrentSession() && ClientSyncJob.isSyncClientJob()) {
        m_currentGetLookupRowByKeyJob.runNow(new NullProgressMonitor());
      }
    }
  }

  @Override
  protected void installLookupRowContext(ILookupRow<LOOKUP_CALL_KEY_TYPE> row) {
    setCurrentLookupRow(row);
    super.installLookupRowContext(row);
  }

  @Override
  protected String formatValueInternal(VALUE_TYPE validKey) {
    // check if current context row is still valid
    if (getCurrentLookupRow() != null) {
      if (validKey == getCurrentLookupRow().getKey() || (validKey != null && validKey.equals(getCurrentLookupRow().getKey()))) {
        // still valid
      }
      else {
        setCurrentLookupRow(null);
      }
    }
    /*
     * Ticket 76232
     */
    if (m_currentGetLookupRowByKeyJob != null) {
      m_currentGetLookupRowByKeyJob.cancel();
      m_currentGetLookupRowByKeyJob = null;
    }
    //
    // trivial case for null
    if (getCurrentLookupRow() == null) {
      if (validKey == null) {
        setCurrentLookupRow(EMPTY_LOOKUP_ROW);
      }
    }
    if (getCurrentLookupRow() != null) {
      installLookupRowContext(getCurrentLookupRow());
      String text = getCurrentLookupRow().getText();
      if (text != null) {
        text = text.replaceAll("[\\n\\r]+", " ");
      }
      return text;
    }
    else {
      // service lookup required
      // start a background thread that loads the text
      if (getLookupCall() != null) {
        try {
          if (getLookupCall() instanceof LocalLookupCall) {
            List<? extends ILookupRow<LOOKUP_CALL_KEY_TYPE>> rows = callKeyLookup(execConvertValueToKey(validKey));
            if (rows != null && !rows.isEmpty()) {
              installLookupRowContext(rows.get(0));
            }
            else {
              installLookupRowContext(EMPTY_LOOKUP_ROW);
            }
          }
          else {
            // enqueue LookupRow fetcher
            // this will lateron call installLookupRowContext()
            ILookupCall<LOOKUP_CALL_KEY_TYPE> call = SERVICES.getService(ILookupCallProvisioningService.class).newClonedInstance(getLookupCall(), new FormFieldProvisioningContext(AbstractMixedSmartField.this));
            prepareKeyLookup(call, execConvertValueToKey(validKey));
            m_currentGetLookupRowByKeyJob = new P_GetLookupRowByKeyJob(call);
            m_currentGetLookupRowByKeyJob.schedule();
          }
        }
        catch (ProcessingException e) {
          SERVICES.getService(IExceptionHandlerService.class).handleException(e);
        }
      }
      return propertySupport.getPropertyString(PROP_DISPLAY_TEXT);
    }
  }

  @Override
  public void refreshDisplayText() {
    if (getLookupCall() != null && getValue() != null) {
      try {
        List<? extends ILookupRow<LOOKUP_CALL_KEY_TYPE>> rows = callKeyLookup(execConvertValueToKey(getValue()));
        if (rows != null && !rows.isEmpty()) {
          installLookupRowContext(rows.get(0));
        }
      }
      catch (ProcessingException e) {
        SERVICES.getService(IExceptionHandlerService.class).handleException(e);
      }
    }
  }

  @Override
  protected IContentAssistFieldProposalForm<LOOKUP_CALL_KEY_TYPE> createProposalForm() throws ProcessingException {
    return createProposalForm(false);
  }

  @Override
  protected void handleProposalFormClosed(IContentAssistFieldProposalForm<LOOKUP_CALL_KEY_TYPE> proposalForm) throws ProcessingException {
    if (getProposalForm() == proposalForm) {
      if (proposalForm.getCloseSystemType() == IButton.SYSTEM_TYPE_OK) {
        ILookupRow<LOOKUP_CALL_KEY_TYPE> row = proposalForm.getAcceptedProposal();
        if (row != null) {
          acceptProposal(row);
        }
      }
      else {
        revertValue();
      }
      registerProposalFormInternal(null);
    }
  }

  @Override
  protected void handleFetchResult(IContentAssistFieldDataFetchResult<LOOKUP_CALL_KEY_TYPE> result) {
    IContentAssistFieldProposalForm<LOOKUP_CALL_KEY_TYPE> smartForm = getProposalForm();
    if (smartForm != null && result != null) {
      smartForm.dataFetchedDelegate(result, getBrowseMaxRowCount());
    }
  }

  private class P_UIFacade implements IContentAssistFieldUIFacade {

    @Override
    public boolean setTextFromUI(String text) {
      String currentValidText = (getCurrentLookupRow() != null ? getCurrentLookupRow().getText() : null);
      IContentAssistFieldProposalForm smartForm = getProposalForm();
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
              // ...and the current value is incomplete -> force proposal
              // selection
              smartForm.forceProposalSelection();
              return false;
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
      try {
        IContentAssistFieldProposalForm<LOOKUP_CALL_KEY_TYPE> smartForm = getProposalForm();
        if (smartForm == null) {
          setActiveFilter(TriState.TRUE);
          smartForm = createProposalForm();
          smartForm.startForm();
          smartForm.dataFetchedDelegate(getLookupRowFetcher().getResult(), getConfiguredBrowseMaxRowCount());
          if (smartForm.isFormOpen()) {
            doSearch(newText, selectCurrentValue, false);
            registerProposalFormInternal(smartForm);
          }
        }
        else {
          if (!StringUtility.equalsIgnoreNewLines(getLookupRowFetcher().getLastSearchText(), newText)) {
            doSearch(newText, false, false);
          }
        }
      }
      catch (ProcessingException e) {
        SERVICES.getService(IExceptionHandlerService.class).handleException(e);
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

  private class P_GetLookupRowByKeyJob extends ClientSyncJob {
    private List<ILookupRow<LOOKUP_CALL_KEY_TYPE>> m_rows;
    private final ClientAsyncJob m_backgroundJob;

    public P_GetLookupRowByKeyJob(final ILookupCall<LOOKUP_CALL_KEY_TYPE> call) {
      super("Fetch smartfield data for " + getLabel(), getCurrentSession());
      // immediately start a thread that fetches data async
      m_backgroundJob = new ClientAsyncJob("Fetch smartfield data", ClientSyncJob.getCurrentSession()) {
        @Override
        protected void runVoid(IProgressMonitor monitor) throws Throwable {
          List<ILookupRow<LOOKUP_CALL_KEY_TYPE>> result = new ArrayList<ILookupRow<LOOKUP_CALL_KEY_TYPE>>(call.getDataByKey());
          filterKeyLookup(call, result);
          m_rows = cleanupResultList(result);
        }
      };
      m_backgroundJob.schedule();
    }

    @Override
    protected void runVoid(IProgressMonitor monitor) throws Throwable {
      // here we are in the scout thread and simply need to wait until the
      // background thread finished fetching
      if (this == m_currentGetLookupRowByKeyJob) {
        m_currentGetLookupRowByKeyJob = null;
        try {
          m_backgroundJob.join();
        }
        catch (InterruptedException e) {
          // nop
        }
        if (m_backgroundJob.getResult() != null) {
          if (m_backgroundJob.getResult().getException() == null) {
            if (m_rows != null && !m_rows.isEmpty()) {
              installLookupRowContext(m_rows.get(0));
            }
            else {
              installLookupRowContext(EMPTY_LOOKUP_ROW);
            }
          }
          else {
            LOG.error(null, m_backgroundJob.getResult().getException());
          }
        }
      }
    }
  }
}
