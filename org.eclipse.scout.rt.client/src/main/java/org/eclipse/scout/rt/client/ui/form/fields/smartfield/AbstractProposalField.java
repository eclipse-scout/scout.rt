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

import java.util.Collection;
import java.util.List;

import org.eclipse.scout.rt.client.ModelContextProxy;
import org.eclipse.scout.rt.client.ModelContextProxy.ModelContext;
import org.eclipse.scout.rt.client.context.ClientRunContext;
import org.eclipse.scout.rt.client.extension.ui.form.fields.smartfield.IProposalFieldExtension;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.util.CompareUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRowFetchedCallback;
import org.eclipse.scout.rt.shared.services.lookup.LookupRow;

/**
 * This field is similar to the smart field but also allows custom text. A proposal field is always of the value type
 * {@link String}. The proposals are delivered as lookup rows of any type.
 */
@ClassId("61dd2913-49f2-4637-8d05-b0c324ee172a")
public abstract class AbstractProposalField<LOOKUP_KEY> extends AbstractContentAssistField<String, LOOKUP_KEY> implements IProposalField<LOOKUP_KEY> {

  private final IContentAssistFieldUIFacade m_uiFacade = BEANS.get(ModelContextProxy.class).newProxy(new ContentAssistFieldUIFacade<LOOKUP_KEY>(this), ModelContext.copyCurrent());

  public AbstractProposalField() {
    this(true);
  }

  public AbstractProposalField(boolean callInitializer) {
    super(callInitializer);
  }

  @Override
  protected void initConfig() {
    super.initConfig();
    setMaxLength(getConfiguredMaxLength());
    setTrimText(getConfiguredTrimText());
    setAutoCloseChooser(getConfiguredAutoCloseChooser());
  }

  @Override
  public IContentAssistFieldUIFacade getUIFacade() {
    return m_uiFacade;
  }

  /**
   * Configures whether the proposal chooser should automatically be closed when there are no proposals available.
   * <p>
   * Subclasses can override this method. Default is true.
   *
   * @since 6.0
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  protected boolean getConfiguredAutoCloseChooser() {
    return true;
  }

  /**
   * Configures the initial value of {@link AbstractProposalField#getMaxLength() <p> Subclasses can override this
   * method<p> Default is 4000
   *
   * @since 6.1
   */
  @ConfigProperty(ConfigProperty.INTEGER)
  protected int getConfiguredMaxLength() {
    return 4000;
  }

  /**
   * @return true if leading and trailing whitespace should be stripped from the entered text while validating the
   *         value. default is true.
   * @since 6.1
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  protected boolean getConfiguredTrimText() {
    return true;
  }

  @Override
  public void applyLazyStyles() {
  }

  /**
   * Returns the key, if available, <code>null</code> otherwise.
   */
  @Override
  public LOOKUP_KEY getValueAsLookupKey() {
    ILookupRow<LOOKUP_KEY> lookupRow = getCurrentLookupRow();
    if (lookupRow != null) {
      return lookupRow.getKey();
    }
    return null;
  }

  @Override
  public void setAutoCloseChooser(boolean autoCloseChooser) {
    propertySupport.setProperty(PROP_AUTO_CLOSE_CHOOSER, autoCloseChooser);
  }

  @Override
  public boolean isAutoCloseChooser() {
    return propertySupport.getPropertyBool(PROP_AUTO_CLOSE_CHOOSER);
  }

  @Override
  public void setMaxLength(int maxLength) {
    boolean changed = propertySupport.setPropertyInt(PROP_MAX_LENGTH, Math.max(0, maxLength));
    if (changed && isInitialized()) {
      setValue(getValue());
    }
  }

  @Override
  public int getMaxLength() {
    return propertySupport.getPropertyInt(PROP_MAX_LENGTH);
  }

  @Override
  public void setTrimText(boolean b) {
    boolean changed = propertySupport.setPropertyBool(PROP_TRIM_TEXT_ON_VALIDATE, b);
    if (b && changed && isInitialized()) {
      setValue(getValue());
    }
  }

  @Override
  public boolean isTrimText() {
    return propertySupport.getPropertyBool(PROP_TRIM_TEXT_ON_VALIDATE);
  }

  @Override
  public void acceptProposal(ILookupRow<LOOKUP_KEY> row) {
    setCurrentLookupRow(row);
    setValue(row.getText());
  }

  @Override
  protected void installLookupRowContext(ILookupRow<LOOKUP_KEY> row) {
    setCurrentLookupRow(row);
    super.installLookupRowContext(row);
  }

  @Override
  protected String validateValueInternal(String rawValue) {
    String validValue = super.validateValueInternal(rawValue);
    if (validValue != null) {
      if (isTrimText()) {
        validValue = validValue.trim();
      }
      if (validValue.length() > getMaxLength()) {
        validValue = validValue.substring(0, getMaxLength());
      }
    }
    return StringUtility.nullIfEmpty(validValue);
  }

  @Override
  protected String formatValueInternal(final String rawValue) {
    String validValue = rawValue;
    ILookupRow<LOOKUP_KEY> currentLookupRow = getCurrentLookupRow();
    if (currentLookupRow != null) {
      installLookupRowContext(currentLookupRow);
      String validValueText = StringUtility.emptyIfNull(rawValue);
      String lookupRowText = StringUtility.emptyIfNull(currentLookupRow.getText());
      if (!lookupRowText.equals(validValueText)) {
        if (isMultilineText()) {
          validValue = lookupRowText;
        }
        else {
          validValue = lookupRowText.replaceAll("[\\n\\r]+", " ");
        }
      }
      if (validValue != null) {
        if (isTrimText()) {
          validValue = validValue.trim();
        }
        if (validValue.length() > getMaxLength()) {
          validValue = validValue.substring(0, getMaxLength());
        }
      }
    }
    return validValue;
  }

  @Override
  protected String returnLookupRowAsValue(ILookupRow<LOOKUP_KEY> lookupRow) {
    return lookupRow.getText();
  }

  @Override
  protected boolean lookupRowMatchesValue(ILookupRow<LOOKUP_KEY> lookupRow, String value) {
    return CompareUtility.equals(lookupRow.getText(), value);
  }

  @Override
  protected String handleMissingLookupRow(String text) {
    return text;
  }

  @Override
  protected boolean handleAcceptByDisplayText(String text) {
    setValue(text);
    return false;
  }

  @Override
  protected IProposalChooser<?, LOOKUP_KEY> createProposalChooser() {
    return createProposalChooser(true);
  }

  @Override
  protected ILookupRowProvider<LOOKUP_KEY> newByKeyLookupRowProvider(final LOOKUP_KEY key) {
    final ILookupRowProvider<LOOKUP_KEY> delegate = super.newByKeyLookupRowProvider(key);
    return new ILookupRowProvider<LOOKUP_KEY>() {

      @Override
      public void beforeProvide(final ILookupCall<LOOKUP_KEY> lookupCall) {
        delegate.beforeProvide(lookupCall);
      }

      @Override
      public void afterProvide(final ILookupCall<LOOKUP_KEY> lookupCall, final List<ILookupRow<LOOKUP_KEY>> result) {
        delegate.afterProvide(lookupCall, result);
        // ticket #79027
        if (result.isEmpty()) {
          LookupRow<LOOKUP_KEY> newRow = new LookupRow<>(lookupCall.getKey(), String.valueOf(lookupCall.getKey()));
          result.add(newRow);
        }
      }

      @Override
      public void provideSync(final ILookupCall<LOOKUP_KEY> lookupCall, final ILookupRowFetchedCallback<LOOKUP_KEY> callback) {
        delegate.provideSync(lookupCall, callback);
      }

      @Override
      public IFuture<Void> provideAsync(final ILookupCall<LOOKUP_KEY> lookupCall, final ILookupRowFetchedCallback<LOOKUP_KEY> callback, final ClientRunContext clientRunContext) {
        return delegate.provideAsync(lookupCall, callback, clientRunContext);
      }

      @Override
      public List<ILookupRow<LOOKUP_KEY>> provide(ILookupCall<LOOKUP_KEY> lookupCall) {
        return delegate.provide(lookupCall);
      }
    };
  }

  @Override
  protected void handleFetchResult(IContentAssistFieldDataFetchResult<LOOKUP_KEY> result) {
    // Do nothing when fetcher has just started (see ContentAssistFieldDataFetcher#update)
    if (result == null) {
      return;
    }
    IProposalChooser<?, LOOKUP_KEY> proposalChooser = getProposalChooser();
    Collection<? extends ILookupRow<LOOKUP_KEY>> rows = result.getLookupRows();
    if (isAutoCloseChooser() && (rows == null || rows.isEmpty())) {
      unregisterProposalChooserInternal();
    }
    else {
      if (proposalChooser == null) {
        proposalChooser = registerProposalChooserInternal();
      }
      proposalChooser.dataFetchedDelegate(result, getBrowseMaxRowCount());
    }
  }

  protected static class LocalProposalFieldExtension<LOOKUP_KEY, OWNER extends AbstractProposalField<LOOKUP_KEY>> extends LocalContentAssistFieldExtension<String, LOOKUP_KEY, OWNER> implements IProposalFieldExtension<LOOKUP_KEY, OWNER> {

    public LocalProposalFieldExtension(OWNER owner) {
      super(owner);
    }
  }

  @Override
  protected IProposalFieldExtension<LOOKUP_KEY, ? extends AbstractProposalField<LOOKUP_KEY>> createLocalExtension() {
    return new LocalProposalFieldExtension<LOOKUP_KEY, AbstractProposalField<LOOKUP_KEY>>(this);
  }

}
