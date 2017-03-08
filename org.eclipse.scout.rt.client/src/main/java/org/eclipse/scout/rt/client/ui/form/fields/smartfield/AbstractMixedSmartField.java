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

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.scout.rt.client.ModelContextProxy;
import org.eclipse.scout.rt.client.ModelContextProxy.ModelContext;
import org.eclipse.scout.rt.client.extension.ui.form.fields.IFormFieldExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.smartfield.IMixedSmartFieldExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.smartfield.MixedSmartFieldChains.MixedSmartFieldConvertKeyToValueChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.smartfield.MixedSmartFieldChains.MixedSmartFieldConvertValueToKeyChain;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.client.ui.form.fields.ValidationFailedStatus;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigOperation;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.exception.ExceptionHandler;
import org.eclipse.scout.rt.platform.exception.PlatformError;
import org.eclipse.scout.rt.platform.exception.VetoException;
import org.eclipse.scout.rt.platform.job.DoneEvent;
import org.eclipse.scout.rt.platform.job.IBlockingCondition;
import org.eclipse.scout.rt.platform.job.IDoneHandler;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.platform.util.TriState;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRowFetchedCallback;

/**
 * A smart field with a key type different from the value type. The default implementation of
 * {@link #convertKeyToValue(Object)} and {@link #convertValueToKey(Object)} methods works for any case where
 * <VALUE_TYPE extends LOOKUP_CALL_KEY_TYPE>. For all other cases provide your own conversion methods.
 *
 * @param <VALUE>
 * @param <LOOKUP_KEY>
 */
@ClassId("25035a5d-55ea-4e91-a5a4-8c216e0a3ffb")
public abstract class AbstractMixedSmartField<VALUE, LOOKUP_KEY> extends AbstractContentAssistField<VALUE, LOOKUP_KEY> implements IMixedSmartField<VALUE, LOOKUP_KEY> {

  public static final int NOT_UNIQUE_ERROR_CODE = 1;
  public static final int NO_RESULTS_ERROR_CODE = 2;

  private final IContentAssistFieldUIFacade m_uiFacade = BEANS.get(ModelContextProxy.class).newProxy(new ContentAssistFieldUIFacade<LOOKUP_KEY>(this), ModelContext.copyCurrent());
  private final IBlockingCondition m_contextInstalledCondition = Jobs.newBlockingCondition(false);
  private final AtomicInteger m_valueChangedLookupCounter = new AtomicInteger();

  public AbstractMixedSmartField() {
    this(true);
  }

  public AbstractMixedSmartField(boolean callInitializer) {
    super(false); // do not auto-initialize via super constructor, because final members of this class would not be set yet.
    if (callInitializer) {
      callInitializer();
    }
  }

  /**
   * the default implementation simply casts one to the other type
   */
  @SuppressWarnings("unchecked")
  @ConfigOperation
  @Order(400)
  protected VALUE execConvertKeyToValue(LOOKUP_KEY key) {
    return (VALUE) key;
  }

  /**
   * the default implementation simply casts one to the other type
   */
  @SuppressWarnings("unchecked")
  @ConfigOperation
  @Order(410)
  protected LOOKUP_KEY execConvertValueToKey(VALUE value) {
    return (LOOKUP_KEY) value;
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

    int numResults = 0;
    if (fetchResult != null && fetchResult.getLookupRows() != null) {
      numResults = fetchResult.getLookupRows().size();
      if (numResults == 1) {
        ILookupRow<LOOKUP_KEY> singleMatchLookupRow = CollectionUtility.firstElement(fetchResult.getLookupRows());
        setCurrentLookupRow(singleMatchLookupRow);
        return returnLookupRowAsValue(singleMatchLookupRow);
      }
    }

    VetoException veto = new VetoException(ScoutTexts.get("SmartFieldCannotComplete", text));
    veto.withCode(numResults > 1 ? NOT_UNIQUE_ERROR_CODE : NO_RESULTS_ERROR_CODE);
    throw veto;
  }

  @Override
  protected boolean handleAcceptByDisplayText(String text) {
    // do not change smart-field value if text of current lookup-row matches current display text
    ILookupRow<LOOKUP_KEY> lookupRow = getCurrentLookupRow();
    if (lookupRow != null && textEquals(text, lookupRow.getText())) {
      return false;
    }

    String searchText = toSearchText(text);
    IContentAssistSearchParam<LOOKUP_KEY> param = ContentAssistSearchParam.createTextParam(searchText, false);
    getLookupRowFetcher().update(param, true);
    List<? extends ILookupRow<LOOKUP_KEY>> lookupRows = getLookupRowFetcher().getResult().getLookupRows();
    if (lookupRows == null || lookupRows.size() == 0) {
      setValidationError(text, TEXTS.get("SmartFieldCannotComplete", text), NO_RESULTS_ERROR_CODE);
      return true;
    }
    else if (lookupRows.size() == 1) {
      lookupRow = lookupRows.get(0);
      if (lookupRowAccepted(lookupRow)) {
        acceptProposal(lookupRow);
      }
      else {
        setValidationError(text, TEXTS.get("SmartFieldInactiveRow", text), NO_RESULTS_ERROR_CODE);
        return true;
      }
    }
    else if (lookupRows.size() > 1) {
      setValidationError(text, TEXTS.get("SmartFieldNotUnique", text), NOT_UNIQUE_ERROR_CODE);
      return true;
    }
    return false;
  }

  private boolean lookupRowAccepted(ILookupRow<LOOKUP_KEY> lookupRow) {
    if (!lookupRow.isEnabled()) {
      // when row is disabled, don't allow
      return false;
    }
    TriState activeByLookupCall = getLookupCall().getActive();
    switch (activeByLookupCall) {
      case TRUE:
        return lookupRow.isActive();
      case FALSE:
        return !lookupRow.isActive();
      default: // UNDEFINED
        return true;
    }
  }

  private boolean textEquals(String displayText, String lookupRowText) {
    return StringUtility.equalsIgnoreNewLines(StringUtility.emptyIfNull(displayText), StringUtility.emptyIfNull(lookupRowText));
  }

  private void setValidationError(String displayText, String errorMessage, int code) {
    setCurrentLookupRow(null);
    setDisplayText(displayText);
    removeErrorStatus(ValidationFailedStatus.class); // remove existing validation errors first
    addErrorStatus(new ValidationFailedStatus(errorMessage, ValidationFailedStatus.ERROR, code));
  }

  @Override
  public void applyLazyStyles() {
    // Waits if necessary for the lookup row context to be installed (asynchronous operation)
    m_contextInstalledCondition.waitFor(1, TimeUnit.MINUTES);
  }

  @Override
  protected void valueChangedInternal() {
    m_contextInstalledCondition.setBlocking(true);

    // When a current lookup-row is available, we don't need to perform a lookup
    // Usually this happens after the user has selected a row from the proposal-chooser (table or tree).
    final ILookupRow<LOOKUP_KEY> currentLookupRow = getCurrentLookupRow();
    if (currentLookupRow != null) {
      installLookupRowContext(currentLookupRow);
      m_contextInstalledCondition.setBlocking(false);
      return;
    }

    if (getLookupCall() == null) {
      m_contextInstalledCondition.setBlocking(false);
      return;
    }

    // When no current-lookup row is available we must perform a lookup by key (local or remote)
    final LOOKUP_KEY lookupKey = interceptConvertValueToKey(getValue());

    m_valueChangedLookupCounter.incrementAndGet();
    final IFuture<Void> future = callKeyLookupInBackground(lookupKey, new ILookupRowFetchedCallback<LOOKUP_KEY>() {

      @Override
      public void onSuccess(final List<? extends ILookupRow<LOOKUP_KEY>> rows) {
        installLookupRowContext(CollectionUtility.firstElement(rows));
      }

      @Override
      public void onFailure(final RuntimeException exception) {
        BEANS.get(ExceptionHandler.class).handle(exception);
      }
    });
    future.whenDone(new IDoneHandler<Void>() {

      @Override
      public void onDone(DoneEvent<Void> event) {
        // Release guard only upon very recent lookup has been finished.
        if (m_valueChangedLookupCounter.decrementAndGet() == 0) {
          m_contextInstalledCondition.setBlocking(false);
        }
      }
    }, null);
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
      catch (RuntimeException | PlatformError e) {
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
    if (text != null && (!isMultilineText() && (getLookupCall() == null || !getLookupCall().isMultilineText()))) {
      text = text.replaceAll("[\\n\\r]+", " ");
    }
    return text;
  }

  @Override
  public void refreshDisplayText() {
    if (getLookupCall() != null && getValue() != null) {
      try {
        List<? extends ILookupRow<LOOKUP_KEY>> rows = callKeyLookup(interceptConvertValueToKey(getValue()));
        installLookupRowContext(CollectionUtility.firstElement(rows));
      }
      catch (RuntimeException | PlatformError e) {
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
