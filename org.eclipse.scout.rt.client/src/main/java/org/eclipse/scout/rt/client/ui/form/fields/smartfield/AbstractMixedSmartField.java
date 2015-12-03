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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.scout.rt.client.ModelContextProxy;
import org.eclipse.scout.rt.client.ModelContextProxy.ModelContext;
import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.client.extension.ui.form.fields.IFormFieldExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.smartfield.IMixedSmartFieldExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.smartfield.MixedSmartFieldChains.MixedSmartFieldConvertKeyToValueChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.smartfield.MixedSmartFieldChains.MixedSmartFieldConvertValueToKeyChain;
import org.eclipse.scout.rt.client.job.ModelJobs;
import org.eclipse.scout.rt.client.services.lookup.FormFieldProvisioningContext;
import org.eclipse.scout.rt.client.services.lookup.ILookupCallProvisioningService;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.client.ui.form.fields.ValidationFailedStatus;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigOperation;
import org.eclipse.scout.rt.platform.exception.ExceptionHandler;
import org.eclipse.scout.rt.platform.exception.VetoException;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;
import org.eclipse.scout.rt.shared.services.lookup.LocalLookupCall;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A smart field with a key type different from the value type. The default implementation of
 * {@link #convertKeyToValue(Object)} and {@link #convertValueToKey(Object)} methods works for any case where
 * <VALUE_TYPE extends LOOKUP_CALL_KEY_TYPE>. For all other cases provide your own conversion methods.
 *
 * @param <VALUE>
 * @param <LOOKUP_KEY>
 */
public abstract class AbstractMixedSmartField<VALUE, LOOKUP_KEY> extends AbstractContentAssistField<VALUE, LOOKUP_KEY> implements IMixedSmartField<VALUE, LOOKUP_KEY> {

  private static final Logger LOG = LoggerFactory.getLogger(AbstractMixedSmartField.class);

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
    m_uiFacade = BEANS.get(ModelContextProxy.class).newProxy(new ContentAssistFieldUIFacade<LOOKUP_KEY>(this), ModelContext.copyCurrent());
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
  public void acceptProposal(ILookupRow<LOOKUP_KEY> row) {
    setCurrentLookupRow(row);
    setValue(interceptConvertKeyToValue(row.getKey()));
  }

  @Override
  protected VALUE returnLookupRowAsValue(ILookupRow<LOOKUP_KEY> lookupRow) {
    return interceptConvertKeyToValue(lookupRow.getKey());
  }

  @Override
  protected VALUE handleMissingLookupRow(String text) {
    doSearch(text, false, true);
    IContentAssistFieldDataFetchResult<LOOKUP_KEY> fetchResult = getLookupRowFetcher().getResult();
    ILookupRow<LOOKUP_KEY> singleMatchLookupRow = null;
    if (fetchResult != null && fetchResult.getLookupRows() != null && fetchResult.getLookupRows().size() == 1) {
      singleMatchLookupRow = CollectionUtility.firstElement(fetchResult.getLookupRows());
    }
    if (singleMatchLookupRow != null) {
      setCurrentLookupRow(singleMatchLookupRow);
      return returnLookupRowAsValue(singleMatchLookupRow);
    }
    else {
      throw new VetoException(ScoutTexts.get("SmartFieldCannotComplete", text));
    }
  }

  @Override
  protected boolean handleAcceptByDisplayText(String text) {
    // do not change smart-field value if text of current lookup-row matches current display text
    ILookupRow<LOOKUP_KEY> lookupRow = getCurrentLookupRow();
    if (lookupRow != null && textEquals(text, lookupRow.getText())) {
      return false;
    }

    String searchText = toSearchText(text);
    getLookupRowFetcher().update(searchText, false, true);
    List<? extends ILookupRow<LOOKUP_KEY>> lookupRows = getLookupRowFetcher().getResult().getLookupRows();
    if (lookupRows == null || lookupRows.size() == 0) {
      setValidationError(text, TEXTS.get("SmartFieldCannotComplete", text));
      return true;
    }
    else if (lookupRows.size() == 1) {
      acceptProposal(lookupRows.get(0));
    }
    else if (lookupRows.size() > 1) {
      setValidationError(text, TEXTS.get("SmartFieldNotUnique", text));
      return true;
    }
    return false;
  }

  private boolean textEquals(String displayText, String lookupRowText) {
    return StringUtility.equalsIgnoreNewLines(StringUtility.emptyIfNull(displayText), StringUtility.emptyIfNull(lookupRowText));
  }

  private void setValidationError(String displayText, String errorMessage) {
    setCurrentLookupRow(null);
    setDisplayText(displayText);
    removeErrorStatus(ValidationFailedStatus.class); // remove existing validation errors first
    addErrorStatus(new ValidationFailedStatus(errorMessage));
  }

  @Override
  public void applyLazyStyles() {
    // override: ensure that (async loading) lookup context has been set
    if (m_backgroundJobFuture.get() != null && ModelJobs.isModelThread()) {
      waitForLookupRows();
    }
  }

  @Override
  protected void valueChangedInternal() {
    // When a current lookup-row is available, we don't need to perform a lookup
    // Usually this happens after the user has selected a row from the proposal-chooser (table or tree).
    if (getCurrentLookupRow() != null) {
      safeInstallLookupRowContext(Collections.singletonList(getCurrentLookupRow()));
      return;
    }

    ILookupCall<LOOKUP_KEY> lookupCall = getLookupCall();
    if (lookupCall == null) {
      return;
    }

    // When no current-lookup row is available we must perform a lookup by key (local or remote)
    try {
      if (lookupCall instanceof LocalLookupCall) {
        List<? extends ILookupRow<LOOKUP_KEY>> rows = callKeyLookup(interceptConvertValueToKey(getValue()));
        safeInstallLookupRowContext(rows);
      }
      else {
        // Enqueue LookupRow fetcher. this will later on call installLookupRowContext()
        final ILookupCall<LOOKUP_KEY> call = BEANS.get(ILookupCallProvisioningService.class).newClonedInstance(lookupCall,
            new FormFieldProvisioningContext(AbstractMixedSmartField.this));
        prepareKeyLookup(call, interceptConvertValueToKey(getValue()));

        m_backgroundJobFuture.set(Jobs.schedule(new Callable<List<ILookupRow<LOOKUP_KEY>>>() {
          @Override
          public List<ILookupRow<LOOKUP_KEY>> call() throws Exception {
            List<ILookupRow<LOOKUP_KEY>> result = new ArrayList<>(call.getDataByKey());
            filterKeyLookup(call, result);
            return cleanupResultList(result);
          }
        }, Jobs.newInput()
            .withRunContext(ClientRunContexts.copyCurrent())
            .withName("Fetching smart-field data")));

        ModelJobs.schedule(new IRunnable() {
          @Override
          public void run() throws Exception {
            waitForLookupRows();
          }
        }, ModelJobs.newInput(ClientRunContexts.copyCurrent()));
      }
    }
    catch (RuntimeException e) {
      BEANS.get(ExceptionHandler.class).handle(e);
    }
  }

  @Override
  protected String formatValueInternal(VALUE validKey) {
    if (validKey == null) {
      return "";
    }

    if (getLookupCall() == null) {
      return "";
    }

    ILookupRow<LOOKUP_KEY> currentLookupRow = getCurrentLookupRow();
    if (currentLookupRow == null) {
      try {
        List<? extends ILookupRow<LOOKUP_KEY>> lookupRows = callKeyLookup(interceptConvertValueToKey(validKey));
        if (!lookupRows.isEmpty()) {
          currentLookupRow = lookupRows.get(0);
          setCurrentLookupRow(currentLookupRow);
        }
      }
      catch (RuntimeException e) {
        BEANS.get(ExceptionHandler.class).handle(e);
      }
    }

    if (currentLookupRow != null) {
      return lookupRowAsText(currentLookupRow);
    }

    return "";
  }

  /**
   * @param currentLookupRow
   * @return
   */
  private String lookupRowAsText(ILookupRow<LOOKUP_KEY> currentLookupRow) {
    String text = currentLookupRow.getText();
    if (!isMultilineText() && text != null) {
      text = text.replaceAll("[\\n\\r]+", " ");
    }
    return text;
  }

  @Override
  public void refreshDisplayText() {
    if (getLookupCall() != null && getValue() != null) {
      try {
        List<? extends ILookupRow<LOOKUP_KEY>> rows = callKeyLookup(interceptConvertValueToKey(getValue()));
        safeInstallLookupRowContext(rows);
      }
      catch (RuntimeException e) {
        BEANS.get(ExceptionHandler.class).handle(e);
      }
    }
  }

  @Override
  protected IProposalChooser<?, LOOKUP_KEY> createProposalChooser() {
    return createProposalChooser(false);
  }

  @Override
  protected void handleFetchResult(IContentAssistFieldDataFetchResult<LOOKUP_KEY> result) {
    IProposalChooser<?, LOOKUP_KEY> proposalChooser = getProposalChooser();
    if (proposalChooser != null && result != null) {
      proposalChooser.dataFetchedDelegate(result, getBrowseMaxRowCount());
    }
  }

  protected void safeInstallLookupRowContext(List<? extends ILookupRow<LOOKUP_KEY>> lookupRows) {
    ILookupRow<LOOKUP_KEY> lookupRow = null;
    if (CollectionUtility.hasElements(lookupRows)) {
      lookupRow = lookupRows.get(0);
    }
    installLookupRowContext(lookupRow);
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
      safeInstallLookupRowContext(rows);
    }
    catch (RuntimeException e) {
      LOG.error("Error loading smartfield data.", e);
    }
    finally {
      m_backgroundJobFuture.set(null);
    }
  }

  protected static class LocalMixedSmartFieldExtension<VALUE, LOOKUP_KEY, OWNER extends AbstractMixedSmartField<VALUE, LOOKUP_KEY>> extends LocalContentAssistFieldExtension<VALUE, LOOKUP_KEY, OWNER>
      implements IMixedSmartFieldExtension<VALUE, LOOKUP_KEY, OWNER> {

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
