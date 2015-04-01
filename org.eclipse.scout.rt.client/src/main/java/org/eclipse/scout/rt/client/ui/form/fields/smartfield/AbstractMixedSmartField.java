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
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.ICallable;
import org.eclipse.scout.commons.IRunnable;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.TriState;
import org.eclipse.scout.commons.annotations.ConfigOperation;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.annotations.ScoutSdkIgnore;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.exception.VetoException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.client.extension.ui.form.fields.IFormFieldExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.smartfield.IMixedSmartFieldExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.smartfield.MixedSmartFieldChains.MixedSmartFieldConvertKeyToValueChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.smartfield.MixedSmartFieldChains.MixedSmartFieldConvertValueToKeyChain;
import org.eclipse.scout.rt.client.job.ClientJobs;
import org.eclipse.scout.rt.client.job.ModelJobs;
import org.eclipse.scout.rt.client.services.lookup.FormFieldProvisioningContext;
import org.eclipse.scout.rt.client.services.lookup.ILookupCallProvisioningService;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.platform.job.IFuture;
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
 * @param <VALUE>
 * @param <LOOKUP_KEY>
 */
@ScoutSdkIgnore
public abstract class AbstractMixedSmartField<VALUE, LOOKUP_KEY> extends AbstractContentAssistField<VALUE, LOOKUP_KEY> implements IMixedSmartField<VALUE, LOOKUP_KEY> {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractMixedSmartField.class);
  private P_UIFacadeLegacy m_uiFacadeLegacy;
  private AtomicReference<IFuture<List<ILookupRow<LOOKUP_KEY>>>> m_backgroundJobFuture;
  private IContentAssistFieldUIFacade m_uiFacade;

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
  protected VALUE execConvertKeyToValue(LOOKUP_KEY key) {
    return (VALUE) key;
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
  protected LOOKUP_KEY execConvertValueToKey(VALUE value) {
    return (LOOKUP_KEY) value;
  }

  @Override
  protected void initConfig() {
    m_backgroundJobFuture = new AtomicReference<>();
    super.initConfig();
    m_uiFacadeLegacy = new P_UIFacadeLegacy();
    m_uiFacade = new ContentAssistFieldUIFacade<LOOKUP_KEY>(this);
  }

  @Override
  @SuppressWarnings("deprecation")
  public IContentAssistFieldUIFacadeLegacy getUIFacadeLegacy() {
    return m_uiFacadeLegacy;
  }

  @Override
  public IContentAssistFieldUIFacade getUIFacade() {
    return m_uiFacade;
  }

  @Override
  public LOOKUP_KEY getValueAsLookupKey() {
    return interceptConvertValueToKey(getValue());
  }

  @Override
  protected VALUE parseValueInternal(String text) throws ProcessingException {
    if (text != null && text.length() == 0) {
      text = null;
    }
    IProposalChooser<?, LOOKUP_KEY> proposalChooser = getProposalChooser();
    ILookupRow<LOOKUP_KEY> acceptedProposalRow = null;
    if (proposalChooser != null && StringUtility.equalsIgnoreNewLines(proposalChooser.getSearchText(), toSearchText(text))) {
      acceptedProposalRow = proposalChooser.getAcceptedProposal();
    }
    //
    boolean unregister = true;
    try {
      if (acceptedProposalRow != null) {
        setCurrentLookupRow(acceptedProposalRow);
        return interceptConvertKeyToValue(acceptedProposalRow.getKey());
      }
      else if (text == null) {
        setCurrentLookupRow(EMPTY_LOOKUP_ROW);
        return null;
      }
      else {
        doSearch(text, false, true);
        IContentAssistFieldDataFetchResult<LOOKUP_KEY> fetchResult = getLookupRowFetcher().getResult();
        if (fetchResult != null && fetchResult.getLookupRows() != null && fetchResult.getLookupRows().size() == 1) {
          acceptedProposalRow = CollectionUtility.firstElement(fetchResult.getLookupRows());
        }
        if (acceptedProposalRow != null) {
          setCurrentLookupRow(acceptedProposalRow);
          return interceptConvertKeyToValue(acceptedProposalRow.getKey());
        }
        else {
          unregister = false; // prevent unregister in finally
          throw new VetoException(ScoutTexts.get("SmartFieldCannotComplete", text));
        }
      }
    }
    finally {
      if (unregister) {
        unregisterProposalChooserInternal();
      }
    }
  }

  @Override
  public void acceptProposal(ILookupRow<LOOKUP_KEY> row) {
    setCurrentLookupRow(row);
    setValue(interceptConvertKeyToValue(row.getKey()));
  }

  @Override
  public void applyLazyStyles() {
    // override: ensure that (async loading) lookup context has been set
    if (m_backgroundJobFuture.get() != null && ModelJobs.isModelThread()) {
      waitForLookupRows();
    }
  }

  @Override
  protected void installLookupRowContext(ILookupRow<LOOKUP_KEY> row) {
    setCurrentLookupRow(row);
    super.installLookupRowContext(row);
  }

  @Override
  protected String formatValueInternal(VALUE validKey) {
    if (!isCurrentLookupRowValid(validKey)) {
      setCurrentLookupRow(null);
    }

    /*
     * Ticket 76232
     */
    IFuture<List<ILookupRow<LOOKUP_KEY>>> backgroundJobFuture = m_backgroundJobFuture.get();
    if (backgroundJobFuture != null) {
      backgroundJobFuture.cancel(true);
    }

    // trivial case for null
    if (getCurrentLookupRow() == null) {
      if (validKey == null) {
        setCurrentLookupRow(EMPTY_LOOKUP_ROW);
      }
    }
    if (getCurrentLookupRow() != null) {
      installLookupRowContext(getCurrentLookupRow());
      String text = getCurrentLookupRow().getText();
      if (!isMultilineText() && text != null) {
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
            List<? extends ILookupRow<LOOKUP_KEY>> rows = callKeyLookup(interceptConvertValueToKey(validKey));
            if (rows != null && !rows.isEmpty()) {
              installLookupRowContext(rows.get(0));
            }
            else {
              installLookupRowContext(EMPTY_LOOKUP_ROW);
            }
          }
          else {
            // enqueue LookupRow fetcher
            // this will later on call installLookupRowContext()
            final ILookupCall<LOOKUP_KEY> call = SERVICES.getService(ILookupCallProvisioningService.class).newClonedInstance(getLookupCall(), new FormFieldProvisioningContext(AbstractMixedSmartField.this));
            prepareKeyLookup(call, interceptConvertValueToKey(validKey));

            m_backgroundJobFuture.set(ClientJobs.schedule(new ICallable<List<ILookupRow<LOOKUP_KEY>>>() {
              @Override
              public List<ILookupRow<LOOKUP_KEY>> call() throws Exception {
                List<ILookupRow<LOOKUP_KEY>> result = new ArrayList<>(call.getDataByKey());
                filterKeyLookup(call, result);
                return cleanupResultList(result);
              }
            }, ClientJobs.newInput(ClientRunContexts.copyCurrent()).name("Fetch smart-field data")));

            ModelJobs.schedule(new IRunnable() {
              @Override
              public void run() throws Exception {
                waitForLookupRows();
              }
            });
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
        List<? extends ILookupRow<LOOKUP_KEY>> rows = callKeyLookup(interceptConvertValueToKey(getValue()));
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
  protected IProposalChooser<?, LOOKUP_KEY> createProposalChooser() throws ProcessingException {
    return createProposalChooser(false);
  }

  @Override
  protected void handleFetchResult(IContentAssistFieldDataFetchResult<LOOKUP_KEY> result) {
    IProposalChooser<?, LOOKUP_KEY> proposalChooser = getProposalChooser();
    if (proposalChooser != null && result != null) {
      proposalChooser.dataFetchedDelegate(result, getBrowseMaxRowCount());
    }
  }

  @SuppressWarnings("deprecation")
  private class P_UIFacadeLegacy implements IContentAssistFieldUIFacadeLegacy {

    @Override
    public boolean setTextFromUI(String text) {
      String currentValidText = (getCurrentLookupRow() != null ? getCurrentLookupRow().getText() : null);
      IProposalChooser<?, LOOKUP_KEY> proposalChooser = getProposalChooser();
      // accept proposal form if either input text matches search text or
      // existing display text is valid
      try {
        if (proposalChooser != null && proposalChooser.getAcceptedProposal() != null) {
          // a proposal was selected
          return acceptProposalFromUI();
        }
        if (proposalChooser != null &&
            (StringUtility.equalsIgnoreNewLines(text, proposalChooser.getSearchText()) ||
            StringUtility.equalsIgnoreNewLines(StringUtility.emptyIfNull(text), StringUtility.emptyIfNull(currentValidText)))) {
          /*
           * empty text means null
           */
          if (text == null || text.length() == 0) {
            return parseValue(text);
          }
          else {
            // no proposal was selected...
            if (!StringUtility.equalsIgnoreNewLines(StringUtility.emptyIfNull(text), StringUtility.emptyIfNull(currentValidText))) {
              // ...and the current value is incomplete -> force proposal selection
              proposalChooser.forceProposalSelection();
              return false;
            }
            else {
              // ... and current display is unchanged from model value -> nop
              unregisterProposalChooserInternal();
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
    public void openProposalFromUI(String searchText, boolean selectCurrentValue) {
      if (searchText == null) {
        searchText = BROWSE_ALL_TEXT;
      }
      try {
        IProposalChooser<?, LOOKUP_KEY> proposalChooser = getProposalChooser();
        if (proposalChooser == null) {
          setActiveFilter(TriState.TRUE);
          proposalChooser = registerProposalChooserInternal();
          proposalChooser.dataFetchedDelegate(getLookupRowFetcher().getResult(), getConfiguredBrowseMaxRowCount());
          doSearch(searchText, selectCurrentValue, false);
        }
        else {
          if (!StringUtility.equalsIgnoreNewLines(getLookupRowFetcher().getLastSearchText(), searchText)) {
            doSearch(searchText, false, false);
          }
        }
      }
      catch (ProcessingException e) {
        SERVICES.getService(IExceptionHandlerService.class).handleException(e);
      }
    }

    @Override
    public void closeProposalFromUI() {
      unregisterProposalChooserInternal();
    }

    @Override
    public boolean acceptProposalFromUI() {
      try {
        IProposalChooser<?, LOOKUP_KEY> proposalChooser = getProposalChooser();
        if (proposalChooser != null) {
          if (proposalChooser.getAcceptedProposal() != null) {
            proposalChooser.doOk();
            return true;
          }
          else {
            // allow with null text traverse
            if (StringUtility.isNullOrEmpty(getDisplayText())) {
              return true;
            }
            else {
              // select first
              proposalChooser.forceProposalSelection();
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
  }

  private void waitForLookupRows() {
    IFuture<List<ILookupRow<LOOKUP_KEY>>> backgroundJobFuture = m_backgroundJobFuture.get();
    if (backgroundJobFuture == null) {
      return;
    }
    // here we are in the scout thread and simply need to wait until the
    // background thread finished fetching
    try {
      List<ILookupRow<LOOKUP_KEY>> rows = backgroundJobFuture.awaitDoneAndGet();
      if (CollectionUtility.hasElements(rows)) {
        installLookupRowContext(rows.get(0));
      }
      else {
        installLookupRowContext(EMPTY_LOOKUP_ROW);
      }
    }
    catch (ProcessingException e) {
      LOG.error("Error loading smartfield data.", e);
    }
    finally {
      m_backgroundJobFuture.set(null);
    }
  }

  protected static class LocalMixedSmartFieldExtension<VALUE, LOOKUP_KEY, OWNER extends AbstractMixedSmartField<VALUE, LOOKUP_KEY>> extends LocalContentAssistFieldExtension<VALUE, LOOKUP_KEY, OWNER> implements IMixedSmartFieldExtension<VALUE, LOOKUP_KEY, OWNER> {

    public LocalMixedSmartFieldExtension(OWNER owner) {
      super(owner);
    }

    @Override
    public LOOKUP_KEY execConvertValueToKey(MixedSmartFieldConvertValueToKeyChain<VALUE, LOOKUP_KEY> chain, VALUE value) {
      return getOwner().execConvertValueToKey(value);
    }

    @Override
    public VALUE execConvertKeyToValue(MixedSmartFieldConvertKeyToValueChain<VALUE, LOOKUP_KEY> chain, LOOKUP_KEY key) {
      return getOwner().execConvertKeyToValue(key);
    }
  }

  @Override
  protected IMixedSmartFieldExtension<VALUE, LOOKUP_KEY, ? extends AbstractMixedSmartField<VALUE, LOOKUP_KEY>> createLocalExtension() {
    return new LocalMixedSmartFieldExtension<VALUE, LOOKUP_KEY, AbstractMixedSmartField<VALUE, LOOKUP_KEY>>(this);
  }

  protected final LOOKUP_KEY interceptConvertValueToKey(VALUE value) {
    List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions = getAllExtensions();
    MixedSmartFieldConvertValueToKeyChain<VALUE, LOOKUP_KEY> chain = new MixedSmartFieldConvertValueToKeyChain<VALUE, LOOKUP_KEY>(extensions);
    return chain.execConvertValueToKey(value);
  }

  protected final VALUE interceptConvertKeyToValue(LOOKUP_KEY key) {
    List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions = getAllExtensions();
    MixedSmartFieldConvertKeyToValueChain<VALUE, LOOKUP_KEY> chain = new MixedSmartFieldConvertKeyToValueChain<VALUE, LOOKUP_KEY>(extensions);
    return chain.execConvertKeyToValue(key);
  }
}
