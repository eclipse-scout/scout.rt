/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.form.fields.smartfield;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.eclipse.scout.rt.client.ModelContextProxy;
import org.eclipse.scout.rt.client.ModelContextProxy.ModelContext;
import org.eclipse.scout.rt.client.context.ClientRunContext;
import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.client.extension.ui.form.fields.IFormFieldExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.smartfield.ISmartFieldExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.smartfield.SmartFieldChains.SmartFieldFilterBrowseLookupResultChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.smartfield.SmartFieldChains.SmartFieldFilterKeyLookupResultChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.smartfield.SmartFieldChains.SmartFieldFilterLookupResultChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.smartfield.SmartFieldChains.SmartFieldFilterRecLookupResultChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.smartfield.SmartFieldChains.SmartFieldFilterTextLookupResultChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.smartfield.SmartFieldChains.SmartFieldPrepareBrowseLookupChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.smartfield.SmartFieldChains.SmartFieldPrepareKeyLookupChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.smartfield.SmartFieldChains.SmartFieldPrepareLookupChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.smartfield.SmartFieldChains.SmartFieldPrepareRecLookupChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.smartfield.SmartFieldChains.SmartFieldPrepareTextLookupChain;
import org.eclipse.scout.rt.client.job.ModelJobs;
import org.eclipse.scout.rt.client.services.lookup.FormFieldProvisioningContext;
import org.eclipse.scout.rt.client.services.lookup.ILookupCallProvisioningService;
import org.eclipse.scout.rt.client.services.lookup.ILookupCallResult;
import org.eclipse.scout.rt.client.services.lookup.IQueryParam;
import org.eclipse.scout.rt.client.services.lookup.QueryParam;
import org.eclipse.scout.rt.client.session.ClientSessionProvider;
import org.eclipse.scout.rt.client.ui.basic.table.columns.ColumnDescriptor;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractValueField;
import org.eclipse.scout.rt.client.ui.form.fields.ValidationFailedStatus;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigOperation;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.exception.ExceptionHandler;
import org.eclipse.scout.rt.platform.exception.PlatformError;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.exception.VetoException;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.reflect.ConfigurationUtility;
import org.eclipse.scout.rt.platform.text.TEXTS;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.FinalValue;
import org.eclipse.scout.rt.platform.util.ObjectUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.platform.util.ToStringBuilder;
import org.eclipse.scout.rt.platform.util.TriState;
import org.eclipse.scout.rt.platform.util.concurrent.FutureCancelledError;
import org.eclipse.scout.rt.platform.util.concurrent.ThreadInterruptedError;
import org.eclipse.scout.rt.shared.services.common.code.ICodeType;
import org.eclipse.scout.rt.shared.services.lookup.CodeLookupCall;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRowFetchedCallback;
import org.eclipse.scout.rt.shared.services.lookup.LocalLookupCall;
import org.eclipse.scout.rt.shared.services.lookup.LookupCall;
import org.eclipse.scout.rt.shared.services.lookup.LookupRow;

@ClassId("444e6fb6-3b0b-4917-933e-b6eb81345499")
public abstract class AbstractSmartField<VALUE> extends AbstractValueField<VALUE> implements ISmartField<VALUE> {

  private final ISmartFieldUIFacade<VALUE> m_uiFacade;
  private Class<? extends ICodeType<?, VALUE>> m_codeTypeClass;
  private ILookupCall<VALUE> m_lookupCall;
  private ISmartFieldLookupRowFetcher<VALUE> m_lookupRowFetcher;
  private String m_wildcard;

  /**
   * Provides the label-texts for the radio-buttons of the active-filter.
   */
  private final String[] m_activeFilterLabels = {
      TEXTS.get("ui.All"),
      TEXTS.get("ui.Inactive"),
      TEXTS.get("ui.Active")};

  private volatile IFuture<?> m_lookupFuture;
  private ProcessingException m_validationError;

  public AbstractSmartField() {
    this(true);
  }

  public AbstractSmartField(boolean callInitializer) {
    super(false); // do not auto-initialize via super constructor, because final members of this class would not be set yet.
    this.m_uiFacade = this.createUIFacade();
    if (callInitializer) {
      callInitializer();
    }
  }

  @SuppressWarnings("unchecked")
  protected ISmartFieldUIFacade<VALUE> createUIFacade() {
    return BEANS.get(ModelContextProxy.class).newProxy(new SmartFieldUIFacade(this), ModelContext.copyCurrent());
  }

  /*
   * Configuration
   */
  @ConfigProperty(ConfigProperty.ICON_ID)
  @Order(270)
  protected String getConfiguredBrowseIconId() {
    return null;
  }

  /**
   * Configures whether the field may display multiple lines of text.<br>
   * <p>
   * Subclasses can override this method. Default is false.
   *
   * @since 5.1
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(290)
  protected boolean getConfiguredMultilineText() {
    return false;
  }

  /**
   * valid when configuredBrowseHierarchy=true
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(280)
  protected boolean getConfiguredBrowseAutoExpandAll() {
    return true;
  }

  /**
   * valid when configuredBrowseHierarchy=true
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(240)
  protected boolean getConfiguredBrowseLoadIncremental() {
    return false;
  }

  /**
   * valid when configuredBrowseHierarchy=true
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(240)
  protected boolean getConfiguredLoadParentNodes() {
    return true;
  }

  /**
   * If this method is not overwritten, and the content assist field has a codeType then the value of browseHierarchy is
   * automatically determined by {@link ICodeType#isHierarchy()}
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(310)
  protected boolean getConfiguredBrowseHierarchy() {
    return false;
  }

  /**
   * Configures whether this smartfield only shows proposals if a text search string has been entered.<br>
   * Set this property to {@code true} if you expect a large amount of data for an unconstrained search.<br>
   * The default value is {@code false}.
   *
   * @return {@code true} if the smartfield should only show a proposal list if a search text is entered by the user.
   *         {@code false} (which is the default) if all proposals should be shown if no text search is done.
   */
  @Order(315)
  @ConfigProperty(ConfigProperty.BOOLEAN)
  protected boolean getConfiguredSearchRequired() {
    return false;
  }

  /**
   * variant A: lookup by code type
   */
  @ConfigProperty(ConfigProperty.CODE_TYPE)
  @Order(260)
  protected Class<? extends ICodeType<?, VALUE>> getConfiguredCodeType() {
    return null;
  }

  /**
   * variant B: lookup by backend lookup service<br>
   * 3.0: no support for {@code<eval>} tags anymore<br>
   * 3.0: still valid are {@code<text><key><all><rec>} tags in lookup statements in the backend
   */
  @ConfigProperty(ConfigProperty.LOOKUP_CALL)
  @Order(250)
  protected Class<? extends ILookupCall<VALUE>> getConfiguredLookupCall() {
    return null;
  }

  @ConfigProperty(ConfigProperty.INTEGER)
  @Order(265)
  protected int getConfiguredBrowseMaxRowCount() {
    return 100;
  }

  /**
   * @return true: inactive rows are display together with active rows<br>
   *         false: inactive rows ae only displayed when selected by the model
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(270)
  protected boolean getConfiguredActiveFilterEnabled() {
    return false;
  }

  /**
   * @return default height: 280
   */
  @ConfigProperty(ConfigProperty.INTEGER)
  @Order(280)
  protected int getConfiguredProposalFormHeight() {
    return 280;
  }

  @ConfigProperty(ConfigProperty.STRING)
  @Order(300)
  protected String getConfiguredWildcard() {
    return "*";
  }

  /**
   * This property has only an effect when the smart field has a table proposal chooser. When the returned value is
   * <code>null</code>, the table proposal chooser has only one column (showing the lookup row text) without column
   * header.
   * <p>
   * To change this default behavior, return an array of {@link ColumnDescriptor}s. Additional columns are filled using
   * {@link LookupRow#getAdditionalTableRowData()}, linked by the <code>propertyName</code>.
   */
  @ConfigProperty(ConfigProperty.STRING)
  @Order(310)
  protected ColumnDescriptor[] getConfiguredColumnDescriptors() {
    return null;
  }

  @ConfigProperty(ConfigProperty.OBJECT)
  @Order(320)
  protected String getConfiguredDisplayStyle() {
    return DISPLAY_STYLE_DEFAULT;
  }

  /**
   * Configures the initial value of {@link AbstractSmartField#getMaxLength()
   * <p>
   * Subclasses can override this method
   * <p>
   * Default is 500
   */
  @ConfigProperty(ConfigProperty.INTEGER)
  @Order(330)
  protected int getConfiguredMaxLength() {
    return 500;
  }

  /**
   * called before any lookup is performed (key, text, browse)
   */
  @ConfigOperation
  @Order(230)
  protected void execPrepareLookup(ILookupCall<VALUE> call) {
  }

  /**
   * called before key lookup but after prepareLookup()
   */
  @ConfigOperation
  @Order(240)
  protected void execPrepareKeyLookup(ILookupCall<VALUE> call, VALUE key) {
  }

  /**
   * called before text lookup but after prepareLookup()
   */
  @ConfigOperation
  @Order(250)
  protected void execPrepareTextLookup(ILookupCall<VALUE> call, String text) {
  }

  /**
   * called before browse lookup but after prepareLookup()
   */
  @ConfigOperation
  @Order(260)
  protected void execPrepareBrowseLookup(ILookupCall<VALUE> call) {
  }

  /**
   * called before rec lookup but after prepareLookup()
   */
  @ConfigOperation
  @Order(270)
  protected void execPrepareRecLookup(ILookupCall<VALUE> call, VALUE parentKey) {
  }

  /**
   * @param call
   *          that produced this result
   * @param result
   *          live list containing the result rows. Add, remove, set, replace and clear of entries in this list is
   *          supported
   */
  @ConfigOperation
  @Order(280)
  protected void execFilterLookupResult(ILookupCall<VALUE> call, List<ILookupRow<VALUE>> result) {
  }

  /**
   * @param call
   *          that produced this result
   * @param result
   *          live list containing the result rows. Add, remove, set, replace and clear of entries in this list is
   *          supported
   */
  @ConfigOperation
  @Order(290)
  protected void execFilterKeyLookupResult(ILookupCall<VALUE> call, List<ILookupRow<VALUE>> result) {
  }

  /**
   * @param call
   *          that produced this result
   * @param result
   *          live list containing the result rows. Add, remove, set, replace and clear of entries in this list is
   *          supported
   */
  @ConfigOperation
  @Order(300)
  protected void execFilterTextLookupResult(ILookupCall<VALUE> call, List<ILookupRow<VALUE>> result) {
  }

  /**
   * @param call
   *          that produced this result
   * @param result
   *          live list containing the result rows. Add, remove, set, replace and clear of entries in this list is
   *          supported
   */
  @ConfigOperation
  @Order(310)
  protected void execFilterBrowseLookupResult(ILookupCall<VALUE> call, List<ILookupRow<VALUE>> result) {
  }

  /**
   * @param call
   *          that produced this result
   * @param result
   *          live list containing the result rows. Add, remove, set, replace and clear of entries in this list is
   *          supported
   */
  @ConfigOperation
  @Order(320)
  protected void execFilterRecLookupResult(ILookupCall<VALUE> call, List<ILookupRow<VALUE>> result) {
  }

  @Override
  public boolean acceptBrowseHierarchySelection(VALUE value, int level, boolean leaf) {
    return true;
  }

  @Override
  protected void initConfig() {
    setResult(null);
    setActiveFilter(TriState.TRUE);
    super.initConfig();
    setActiveFilterEnabled(getConfiguredActiveFilterEnabled());
    setBrowseHierarchy(getConfiguredBrowseHierarchy());
    setBrowseAutoExpandAll(getConfiguredBrowseAutoExpandAll());
    setBrowseLoadIncremental(getConfiguredBrowseLoadIncremental());
    setSearchRequired(getConfiguredSearchRequired());
    setLoadParentNodes(getConfiguredLoadParentNodes());
    setMultilineText(getConfiguredMultilineText());
    setBrowseMaxRowCount(getConfiguredBrowseMaxRowCount());
    setColumnDescriptors(getConfiguredColumnDescriptors());
    setDisplayStyle(getConfiguredDisplayStyle());

    initLookupRowFetcher();
    // code type
    if (getConfiguredCodeType() != null) {
      setCodeTypeClass(getConfiguredCodeType());
    }
    // lookup call
    Class<? extends ILookupCall<VALUE>> lookupCallClass = getConfiguredLookupCall();
    if (lookupCallClass != null) {
      ILookupCall<VALUE> call = BEANS.get(lookupCallClass);
      setLookupCall(call);
    }
    setWildcard(getConfiguredWildcard());
    setMaxLength(getConfiguredMaxLength());
  }

  private void initLookupRowFetcher() {
    ISmartFieldLookupRowFetcher<VALUE> lookupRowFetcher = createLookupRowFetcher();
    lookupRowFetcher.addPropertyChangeListener(new P_LookupRowFetcherPropertyListener());
    setLookupRowFetcher(lookupRowFetcher);
  }

  @Override
  protected void execMarkSaved() {
    super.execMarkSaved();
    TriState activeFilter = getActiveFilter();
    setInitActiveFilter(activeFilter);
  }

  @Override
  public void resetValue() {
    super.resetValue();
    TriState activeFilter = getInitActiveFilter();
    setActiveFilter(activeFilter);
  }

  @Override
  public boolean isActiveFilterEnabled() {
    return propertySupport.getPropertyBool(PROP_ACTIVE_FILTER_ENABLED);
  }

  @Override
  public void setActiveFilterEnabled(boolean activeFilterEnabled) {
    propertySupport.setPropertyBool(PROP_ACTIVE_FILTER_ENABLED, activeFilterEnabled);
  }

  @Override
  public TriState getActiveFilter() {
    return (TriState) propertySupport.getProperty(PROP_ACTIVE_FILTER);
  }

  @Override
  public void setActiveFilter(TriState activeFilter) {
    if (activeFilter == null) {
      activeFilter = TriState.TRUE;
    }
    propertySupport.setProperty(PROP_ACTIVE_FILTER, activeFilter);
  }

  @Override
  public void setInitActiveFilter(TriState initActiveFilter) {
    propertySupport.setProperty(PROP_INIT_ACTIVE_FILTER, initActiveFilter);
  }

  @Override
  public TriState getInitActiveFilter() {
    return (TriState) propertySupport.getProperty(PROP_INIT_ACTIVE_FILTER);
  }

  @Override
  public void setActiveFilterLabel(TriState state, String label) {
    m_activeFilterLabels[getIndexForTriState(state)] = label;
  }

  @Override
  public String[] getActiveFilterLabels() {
    return Arrays.copyOf(m_activeFilterLabels, m_activeFilterLabels.length);
  }

  private int getIndexForTriState(TriState state) {
    if (state.isUndefined()) {
      return 0;
    }
    else if (state.isFalse()) {
      return 1;
    }
    else {
      return 2;
    }
  }

  @Override
  public ILookupCallResult getResult() {
    return (ILookupCallResult) propertySupport.getProperty(PROP_RESULT);
  }

  protected void setResult(ILookupCallResult<VALUE> result) {
    propertySupport.setProperty(PROP_RESULT, result);
  }

  @Override
  public void setMultilineText(boolean multilineText) {
    boolean changed = propertySupport.setPropertyBool(PROP_MULTILINE_TEXT, multilineText);
    if (!multilineText & changed && isInitConfigDone()) {
      setValue(getValue());
    }
  }

  @Override
  public boolean isMultilineText() {
    return propertySupport.getPropertyBool(PROP_MULTILINE_TEXT);
  }

  @Override
  public boolean isBrowseAutoExpandAll() {
    return propertySupport.getPropertyBool(PROP_BROWSE_AUTO_EXPAND_ALL);
  }

  @Override
  public void setBrowseAutoExpandAll(boolean browseAutoExpandAll) {
    propertySupport.setPropertyBool(PROP_BROWSE_AUTO_EXPAND_ALL, browseAutoExpandAll);
  }

  @Override
  public boolean isBrowseLoadIncremental() {
    return propertySupport.getPropertyBool(PROP_BROWSE_LOAD_INCREMENTAL);
  }

  @Override
  public void setBrowseLoadIncremental(boolean browseLoadIncremental) {
    propertySupport.setPropertyBool(PROP_BROWSE_LOAD_INCREMENTAL, browseLoadIncremental);
  }

  @Override
  public boolean isLoadParentNodes() {
    return propertySupport.getPropertyBool(PROP_BROWSE_LOAD_PARENT_NODES);
  }

  @Override
  public void setLoadParentNodes(boolean loadParentNodes) {
    propertySupport.setPropertyBool(PROP_BROWSE_LOAD_PARENT_NODES, loadParentNodes);
  }

  @Override
  public boolean isBrowseHierarchy() {
    return propertySupport.getPropertyBool(PROP_BROWSE_HIERARCHY);
  }

  @Override
  public void setBrowseHierarchy(boolean browseHierarchy) {
    propertySupport.setPropertyBool(PROP_BROWSE_HIERARCHY, browseHierarchy);
    initLookupRowFetcher();
  }

  @Override
  public boolean isSearchRequired() {
    return propertySupport.getPropertyBool(PROP_SEARCH_REQUIRED);
  }

  @Override
  public void setSearchRequired(boolean searchRequired) {
    propertySupport.setPropertyBool(PROP_SEARCH_REQUIRED, searchRequired);
  }

  @Override
  public int getBrowseMaxRowCount() {
    return propertySupport.getPropertyInt(PROP_BROWSE_MAX_ROW_COUNT);
  }

  @Override
  public void setBrowseMaxRowCount(int browseMaxRowCount) {
    propertySupport.setPropertyInt(PROP_BROWSE_MAX_ROW_COUNT, browseMaxRowCount);
  }

  @Override
  public ColumnDescriptor[] getColumnDescriptors() {
    return (ColumnDescriptor[]) getProperty(PROP_COLUMN_DESCRIPTORS);
  }

  @Override
  public void setColumnDescriptors(ColumnDescriptor[] columnDescriptors) {
    setProperty(PROP_COLUMN_DESCRIPTORS, columnDescriptors);
  }

  @Override
  public void setMaxLength(int maxLength) {
    boolean changed = propertySupport.setPropertyInt(PROP_MAX_LENGTH, Math.max(0, maxLength));
    if (changed && isInitConfigDone()) {
      setValue(getValue());
    }
  }

  @Override
  public int getMaxLength() {
    return propertySupport.getPropertyInt(PROP_MAX_LENGTH);
  }

  @Override
  public Class<? extends ICodeType<?, VALUE>> getCodeTypeClass() {
    return m_codeTypeClass;
  }

  @Override
  public void setCodeTypeClass(Class<? extends ICodeType<?, VALUE>> codeType) {
    m_codeTypeClass = codeType;
    // create lookup service call
    m_lookupCall = null;
    if (m_codeTypeClass != null) {
      CodeLookupCall<VALUE> codeLookupCall = CodeLookupCall.newInstanceByService(m_codeTypeClass);
      m_lookupCall = codeLookupCall;
      ICodeType t = BEANS.opt(m_codeTypeClass);
      if (t != null && !ConfigurationUtility.isMethodOverwrite(AbstractSmartField.class, "getConfiguredBrowseHierarchy", new Class[0], this.getClass())) {
        setBrowseHierarchy(t.isHierarchy());
      }
    }
  }

  @Override
  public ILookupCall<VALUE> getLookupCall() {
    return m_lookupCall;
  }

  @Override
  public void setLookupCall(ILookupCall<VALUE> call) {
    m_lookupCall = call;
  }

  /**
   * @param wildcard
   *          Wildcard character used in lookup calls
   */
  @Override
  public void setWildcard(String wildcard) {
    if (StringUtility.isNullOrEmpty(wildcard)) {
      throw new IllegalArgumentException("Wildcard must not be null nor empty!");
    }
    m_wildcard = wildcard;
    if (m_lookupCall != null) {
      m_lookupCall.setWildcard(wildcard);
    }
  }

  @Override
  public String getWildcard() {
    return m_wildcard;
  }

  public ISmartFieldLookupRowFetcher<VALUE> getLookupRowFetcher() {
    return m_lookupRowFetcher;
  }

  public void setLookupRowFetcher(ISmartFieldLookupRowFetcher<VALUE> fetcher) {
    m_lookupRowFetcher = fetcher;
  }

  /**
   * Checks if the current context row is still valid. If the current context row is not set, it is considered valid.
   *
   * @param validKey
   *          Valid key
   * @return {@code true} if the current context row is valid, {@code false} otherwise.
   */
  protected boolean isCurrentLookupRowValid(VALUE validKey) {
    if (getLookupRow() == null) {
      return true;
    }
    return validKey == getLookupRow().getKey() || (validKey != null && validKey.equals(getLookupRow().getKey()));
  }

  @Override
  public void setLookupRow(ILookupRow<VALUE> lookupRow) {
    propertySupport.setProperty(PROP_LOOKUP_ROW, lookupRow);
  }

  @Override
  @SuppressWarnings("unchecked")
  public ILookupRow<VALUE> getLookupRow() {
    return (ILookupRow<VALUE>) propertySupport.getProperty(PROP_LOOKUP_ROW);
  }

  @Override
  public void setValueByLookupRow(ILookupRow<VALUE> lookupRow) {
    m_validationError = null;
    ILookupRow<VALUE> oldRow = getLookupRow();
    try {
      if (lookupRow == null) {
        setLookupRow(null);
        setValue(null);
      }
      else if (lookupRow.isEnabled()) {
        setLookupRow(lookupRow);
        setValue(getValueFromLookupRow(lookupRow));
      }
      // don't do anything if row is disabled
    }
    finally {
      // when an error occurred during validation, reset the lookup-row
      // to the previously used row (because new value was not set).
      if (m_validationError != null) {
        setLookupRow(oldRow);
      }
    }
  }

  @Override
  protected void handleValidationFailed(ProcessingException e, VALUE rawValue) {
    // don't call super, because we don't want to update the display text
    // because this would trigger execFormatValue and start a new lookup
    addErrorStatus(new ValidationFailedStatus<>(e, rawValue));
    m_validationError = e;
  }

  /**
   * @param lookupRow
   *          Lookup row used to resolve the value
   * @return a property from the lookup row which is used as value (usually this is the key of the lookup row)
   */
  protected VALUE getValueFromLookupRow(ILookupRow<VALUE> lookupRow) {
    return lookupRow.getKey();
  }

  @Override
  public void prepareKeyLookup(ILookupCall<VALUE> call, VALUE key) {
    call.setKey(key);
    call.setText(null);
    call.setAll(null);
    call.setRec(null);
    call.setActive(TriState.UNDEFINED);
    // when there is a master value defined in the original call, don't set it to null when no master value is available
    if (getMasterValue() != null || getLookupCall() == null || getLookupCall().getMaster() == null) {
      call.setMaster(getMasterValue());
    }
    interceptPrepareLookup(call);
    interceptPrepareKeyLookup(call, key);
  }

  @Override
  public void prepareTextLookup(ILookupCall<VALUE> call, String text) {
    String textPattern = text;
    if (textPattern == null) {
      textPattern = "";
    }
    textPattern = textPattern.toLowerCase();
    IDesktop desktop = ClientSessionProvider.currentSession().getDesktop();
    if (desktop != null && desktop.isAutoPrefixWildcardForTextSearch()) {
      textPattern = getWildcard() + textPattern;
    }
    if (!textPattern.endsWith(getWildcard())) {
      textPattern = textPattern + getWildcard();
    }
    //localLookupCalls should return hierarchical matches as well (children of exact matches), if field is configured accordingly
    if (call instanceof LocalLookupCall) {
      ((LocalLookupCall) call).setHierarchicalLookup(isBrowseHierarchy());
    }

    call.setKey(null);
    call.setText(textPattern);
    call.setAll(null);
    call.setRec(null);
    call.setActive(isActiveFilterEnabled() ? getActiveFilter() : TriState.TRUE);
    //when there is a master value defined in the original call, don't set it to null when no master value is available
    if (getMasterValue() != null || getLookupCall() == null || getLookupCall().getMaster() == null) {
      call.setMaster(getMasterValue());
    }
    interceptPrepareLookup(call);
    interceptPrepareTextLookup(call, text);
  }

  @Override
  public void prepareBrowseLookup(ILookupCall<VALUE> call, TriState activeState) {
    call.setKey(null);
    call.setText(null);
    call.setAll(getWildcard());
    call.setRec(null);
    call.setActive(activeState);
    // when there is a master value defined in the original call, don't set it to null when no master value is available
    if (getMasterValue() != null || getLookupCall() == null || getLookupCall().getMaster() == null) {
      call.setMaster(getMasterValue());
    }
    interceptPrepareLookup(call);
    interceptPrepareBrowseLookup(call);
  }

  @Override
  public void prepareRecLookup(ILookupCall<VALUE> call, VALUE parentKey, TriState activeState) {
    call.setKey(null);
    call.setText(null);
    call.setAll(null);
    call.setRec(parentKey);
    //when there is a master value defined in the original call, don't set it to null when no master value is available
    if (getMasterValue() != null || getLookupCall() == null || getLookupCall().getMaster() == null) {
      call.setMaster(getMasterValue());
    }
    call.setActive(activeState);
    interceptPrepareLookup(call);
    interceptPrepareRecLookup(call, parentKey);
  }

  protected VALUE handleMissingLookupRow(String text) {
    lookupByTextInternal(text, true);
    ILookupCallResult<VALUE> fetchResult = getLookupRowFetcher().getResult();

    int numResults = 0;
    if (fetchResult != null && fetchResult.getLookupRows() != null) {
      numResults = fetchResult.getLookupRows().size();
      if (numResults == 1) {
        ILookupRow<VALUE> singleMatchLookupRow = CollectionUtility.firstElement(fetchResult.getLookupRows());
        setLookupRow(singleMatchLookupRow);
        return returnLookupRowAsValue(singleMatchLookupRow);
      }
    }

    boolean notUnique = numResults > 1;
    VetoException veto = new VetoException(TEXTS.get(notUnique ? "SmartFieldNotUnique" : "SmartFieldCannotComplete", text));
    veto.withCode(notUnique ? NOT_UNIQUE_ERROR_CODE : NO_RESULTS_ERROR_CODE);
    throw veto;
  }

  protected VALUE returnLookupRowAsValue(ILookupRow<VALUE> lookupRow) {
    return lookupRow.getKey();
  }

  @Override
  protected VALUE parseValueInternal(String text) {
    ILookupRow<VALUE> currentLookupRow = getLookupRow();
    if (currentLookupRow == null) {
      return handleMissingLookupRow(text);
    }
    else {
      return returnLookupRowAsValue(getLookupRow());
    }
  }

  /**
   * You should not override this internal method. Use <code>execValidateValue</code> instead.
   */
  @Override
  protected VALUE validateValueInternal(VALUE rawValue) {
    VALUE validatedValue = super.validateValueInternal(rawValue);

    // when value is null, current lookupRow must be null too
    if (validatedValue == null) {
      setLookupRow(null);
      return validatedValue;
    }

    // set currentLookupRow to null, when new value doesn't match lookupRow
    // we must do this every time setValue() is called.
    ILookupRow<VALUE> currentLookupRow = getLookupRow();
    if (currentLookupRow != null && !lookupRowMatchesValue(currentLookupRow, validatedValue)) {
      setLookupRow(null);
    }

    return validatedValue;
  }

  /**
   * Returns true if the given value matches the given lookup-row. The default impl. checks if the key of the lookup-row
   * matches. Override this method to implement another behavior.
   */
  protected boolean lookupRowMatchesValue(ILookupRow<VALUE> lookupRow, VALUE value) {
    return ObjectUtility.equals(getValueFromLookupRow(lookupRow), value);
  }

  // search and update the field with the result

  @Override
  public void lookupByAll() {
    doSearch(QueryParam.createByAll(), false);
  }

  @Override
  public void lookupByText(String text) {
    lookupByTextInternal(text, false);
  }

  protected void lookupByTextInternal(String text, boolean synchronous) {
    text = StringUtility.substring(text, 0, getMaxLength());
    doSearch(QueryParam.createByText(text), synchronous);
  }

  @Override
  public void lookupByRec(VALUE parentKey) {
    doSearch(QueryParam.<VALUE> createByRec(parentKey), false);
  }

  @Override
  public void lookupByKey(VALUE key) {
    doSearch(QueryParam.<VALUE> createByKey(key), false);
  }

  @Override
  public void doSearch(IQueryParam<VALUE> param, boolean synchronous) {
    getLookupRowFetcher().update(param, synchronous);
  }

  // blocking lookups
  @Override
  public List<? extends ILookupRow<VALUE>> callKeyLookup(VALUE key) {
    LookupRowCollector<VALUE> collector = new LookupRowCollector<>();
    fetchLookupRows(newByKeyLookupRowProvider(key), collector, false, 1);
    return collector.get();
  }

  @Override
  public List<? extends ILookupRow<VALUE>> callTextLookup(String text, int maxRowCount) {
    LookupRowCollector<VALUE> collector = new LookupRowCollector<>();
    fetchLookupRows(newByTextLookupRowProvider(text), collector, false, maxRowCount);
    return collector.get();
  }

  @Override
  public List<? extends ILookupRow<VALUE>> callBrowseLookup(int maxRowCount) {
    return callBrowseLookup(maxRowCount, isActiveFilterEnabled() ? getActiveFilter() : TriState.TRUE);
  }

  @Override
  public List<? extends ILookupRow<VALUE>> callBrowseLookup(int maxRowCount, TriState activeState) {
    LookupRowCollector<VALUE> collector = new LookupRowCollector<>();
    fetchLookupRows(newByAllLookupRowProvider(activeState), collector, false, maxRowCount);
    return collector.get();
  }

  @Override
  public List<ILookupRow<VALUE>> callSubTreeLookup(VALUE parentKey) {
    return callSubTreeLookup(parentKey, isActiveFilterEnabled() ? getActiveFilter() : TriState.TRUE);
  }

  @Override
  public List<ILookupRow<VALUE>> callSubTreeLookup(final VALUE parentKey, final TriState activeState) {
    final ILookupRowProvider<VALUE> provider = newByRecLookupRowProvider(parentKey, activeState);
    return BEANS.get(LookupRowHelper.class).lookup(provider, cloneLookupCall());
  }

  // non-blocking lookups

  @Override
  public IFuture<List<ILookupRow<VALUE>>> callKeyLookupInBackground(final VALUE key, boolean cancelRunningJobs) {
    ILookupRowProvider<VALUE> provider = newByKeyLookupRowProvider(key);
    return callInBackground(provider, cancelRunningJobs);
  }

  @Override
  public IFuture<List<ILookupRow<VALUE>>> callTextLookupInBackground(String text, boolean cancelRunningJobs) {
    final ILookupRowProvider<VALUE> provider = newByTextLookupRowProvider(text);
    return callInBackground(provider, cancelRunningJobs);
  }

  @Override
  public IFuture<List<ILookupRow<VALUE>>> callBrowseLookupInBackground(boolean cancelRunningJobs) {
    TriState activeState = isActiveFilterEnabled() ? getActiveFilter() : TriState.TRUE;
    final ILookupRowProvider<VALUE> provider = newByAllLookupRowProvider(activeState);
    return callInBackground(provider, cancelRunningJobs);
  }

  @Override
  public IFuture<List<ILookupRow<VALUE>>> callSubTreeLookupInBackground(final VALUE parentKey, boolean cancelRunningJobs) {
    TriState activeState = isActiveFilterEnabled() ? getActiveFilter() : TriState.TRUE;
    return callSubTreeLookupInBackground(parentKey, activeState, cancelRunningJobs);
  }

  @Override
  public IFuture<List<ILookupRow<VALUE>>> callSubTreeLookupInBackground(final VALUE parentKey, final TriState activeState, boolean cancelRunningJobs) {
    final ILookupRowProvider<VALUE> provider = newByRecLookupRowProvider(parentKey, activeState);
    return callInBackground(provider, cancelRunningJobs);
  }

  protected IFuture<List<ILookupRow<VALUE>>> callInBackground(final ILookupRowProvider<VALUE> provider, boolean cancelRunningJobs) {
    if (cancelRunningJobs) {
      cancelPotentialLookup();
    }
    IFuture<List<ILookupRow<VALUE>>> futureResult = BEANS.get(LookupRowHelper.class).scheduleLookup(provider, cloneLookupCall());
    m_lookupFuture = futureResult;
    return futureResult;
  }

  protected ILookupCall<VALUE> cloneLookupCall() {
    return BEANS.get(ILookupCallProvisioningService.class).newClonedInstance(getLookupCall(), new FormFieldProvisioningContext(AbstractSmartField.this));
  }

  // non-blocking lookups using callbacks (legacy)
  @Override
  public IFuture<Void> callKeyLookupInBackground(VALUE key, ILookupRowFetchedCallback<VALUE> callback) {
    return fetchLookupRows(newByKeyLookupRowProvider(key), callback, true, 1);
  }

  @Override
  public IFuture<Void> callTextLookupInBackground(String text, int maxRowCount, ILookupRowFetchedCallback<VALUE> callback) {
    return fetchLookupRows(newByTextLookupRowProvider(text), callback, true, maxRowCount);
  }

  @Override
  public IFuture<Void> callBrowseLookupInBackground(int maxRowCount, ILookupRowFetchedCallback<VALUE> callback) {
    return callBrowseLookupInBackground(maxRowCount, isActiveFilterEnabled() ? getActiveFilter() : TriState.TRUE, callback);
  }

  @Override
  public IFuture<Void> callBrowseLookupInBackground(int maxRowCount, TriState activeState, ILookupRowFetchedCallback<VALUE> callback) {
    return fetchLookupRows(newByAllLookupRowProvider(activeState), callback, true, maxRowCount);
  }

  protected void cleanupResultList(final List<ILookupRow<VALUE>> list) {
    list.removeIf(Objects::isNull);
  }

  protected void handleFetchResult(ILookupCallResult<VALUE> result) {
    if (result == null) {
      setResult(null);
    }
    else {
      if (isBrowseHierarchy()) {
        result = addHierarchicalResults(result);
      }
      setResult(result);
    }
  }

  protected ILookupCallResult<VALUE> addHierarchicalResults(ILookupCallResult<VALUE> result) {
    return new HierarchicalLookupResultBuilder<>(this).addParentLookupRows(result);
  }

  protected ISmartFieldLookupRowFetcher<VALUE> createLookupRowFetcher() {
    if (isBrowseHierarchy()) {
      return new HierarchicalSmartFieldDataFetcher<>(this);
    }
    else {
      return new SmartFieldDataFetcher<>(this);
    }
  }

  /*
   * inner classes
   */

  // end private class

  private class P_LookupRowFetcherPropertyListener implements PropertyChangeListener {
    @SuppressWarnings("unchecked")
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      if (ISmartFieldLookupRowFetcher.PROP_SEARCH_RESULT.equals(evt.getPropertyName())) {
        handleFetchResult((ILookupCallResult<VALUE>) evt.getNewValue());
      }
    }
  }

  protected static class LocalSmartFieldExtension<VALUE, OWNER extends AbstractSmartField<VALUE>> extends LocalValueFieldExtension<VALUE, OWNER>
      implements ISmartFieldExtension<VALUE, OWNER> {

    public LocalSmartFieldExtension(OWNER owner) {
      super(owner);
    }

    @Override
    public void execFilterBrowseLookupResult(SmartFieldFilterBrowseLookupResultChain<VALUE> chain, ILookupCall<VALUE> call, List<ILookupRow<VALUE>> result) {
      getOwner().execFilterBrowseLookupResult(call, result);
    }

    @Override
    public void execFilterKeyLookupResult(SmartFieldFilterKeyLookupResultChain<VALUE> chain, ILookupCall<VALUE> call, List<ILookupRow<VALUE>> result) {
      getOwner().execFilterKeyLookupResult(call, result);
    }

    @Override
    public void execPrepareLookup(SmartFieldPrepareLookupChain<VALUE> chain, ILookupCall<VALUE> call) {
      getOwner().execPrepareLookup(call);
    }

    @Override
    public void execPrepareTextLookup(SmartFieldPrepareTextLookupChain<VALUE> chain, ILookupCall<VALUE> call, String text) {
      getOwner().execPrepareTextLookup(call, text);
    }

    @Override
    public void execPrepareBrowseLookup(SmartFieldPrepareBrowseLookupChain<VALUE> chain, ILookupCall<VALUE> call) {
      getOwner().execPrepareBrowseLookup(call);
    }

    @Override
    public void execFilterTextLookupResult(SmartFieldFilterTextLookupResultChain<VALUE> chain, ILookupCall<VALUE> call, List<ILookupRow<VALUE>> result) {
      getOwner().execFilterTextLookupResult(call, result);
    }

    @Override
    public void execPrepareRecLookup(SmartFieldPrepareRecLookupChain<VALUE> chain, ILookupCall<VALUE> call, VALUE parentKey) {
      getOwner().execPrepareRecLookup(call, parentKey);
    }

    @Override
    public void execFilterLookupResult(SmartFieldFilterLookupResultChain<VALUE> chain, ILookupCall<VALUE> call, List<ILookupRow<VALUE>> result) {
      getOwner().execFilterLookupResult(call, result);
    }

    @Override
    public void execFilterRecLookupResult(SmartFieldFilterRecLookupResultChain<VALUE> chain, ILookupCall<VALUE> call, List<ILookupRow<VALUE>> result) {
      getOwner().execFilterRecLookupResult(call, result);
    }

    @Override
    public void execPrepareKeyLookup(SmartFieldPrepareKeyLookupChain<VALUE> chain, ILookupCall<VALUE> call, VALUE key) {
      getOwner().execPrepareKeyLookup(call, key);
    }
  }

  @Override
  protected ISmartFieldExtension<VALUE, ? extends AbstractSmartField<VALUE>> createLocalExtension() {
    return new LocalSmartFieldExtension<>(this);
  }

  protected final void interceptFilterBrowseLookupResult(ILookupCall<VALUE> call, List<ILookupRow<VALUE>> result) {
    List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions = getAllExtensions();
    SmartFieldFilterBrowseLookupResultChain<VALUE> chain = new SmartFieldFilterBrowseLookupResultChain<>(extensions);
    chain.execFilterBrowseLookupResult(call, result);
  }

  protected final void interceptFilterKeyLookupResult(ILookupCall<VALUE> call, List<ILookupRow<VALUE>> result) {
    List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions = getAllExtensions();
    SmartFieldFilterKeyLookupResultChain<VALUE> chain = new SmartFieldFilterKeyLookupResultChain<>(extensions);
    chain.execFilterKeyLookupResult(call, result);
  }

  protected final void interceptPrepareLookup(ILookupCall<VALUE> call) {
    List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions = getAllExtensions();
    SmartFieldPrepareLookupChain<VALUE> chain = new SmartFieldPrepareLookupChain<>(extensions);
    chain.execPrepareLookup(call);
  }

  protected final void interceptPrepareTextLookup(ILookupCall<VALUE> call, String text) {
    List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions = getAllExtensions();
    SmartFieldPrepareTextLookupChain<VALUE> chain = new SmartFieldPrepareTextLookupChain<>(extensions);
    chain.execPrepareTextLookup(call, text);
  }

  protected final void interceptPrepareBrowseLookup(ILookupCall<VALUE> call) {
    List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions = getAllExtensions();
    SmartFieldPrepareBrowseLookupChain<VALUE> chain = new SmartFieldPrepareBrowseLookupChain<>(extensions);
    chain.execPrepareBrowseLookup(call);
  }

  protected final void interceptFilterTextLookupResult(ILookupCall<VALUE> call, List<ILookupRow<VALUE>> result) {
    List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions = getAllExtensions();
    SmartFieldFilterTextLookupResultChain<VALUE> chain = new SmartFieldFilterTextLookupResultChain<>(extensions);
    chain.execFilterTextLookupResult(call, result);
  }

  protected final void interceptPrepareRecLookup(ILookupCall<VALUE> call, VALUE parentKey) {
    List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions = getAllExtensions();
    SmartFieldPrepareRecLookupChain<VALUE> chain = new SmartFieldPrepareRecLookupChain<>(extensions);
    chain.execPrepareRecLookup(call, parentKey);
  }

  protected final void interceptFilterLookupResult(ILookupCall<VALUE> call, List<ILookupRow<VALUE>> result) {
    List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions = getAllExtensions();
    SmartFieldFilterLookupResultChain<VALUE> chain = new SmartFieldFilterLookupResultChain<>(extensions);
    chain.execFilterLookupResult(call, result);
  }

  protected final void interceptFilterRecLookupResult(ILookupCall<VALUE> call, List<ILookupRow<VALUE>> result) {
    List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions = getAllExtensions();
    SmartFieldFilterRecLookupResultChain<VALUE> chain = new SmartFieldFilterRecLookupResultChain<>(extensions);
    chain.execFilterRecLookupResult(call, result);
  }

  protected final void interceptPrepareKeyLookup(ILookupCall<VALUE> call, VALUE key) {
    List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions = getAllExtensions();
    SmartFieldPrepareKeyLookupChain<VALUE> chain = new SmartFieldPrepareKeyLookupChain<>(extensions);
    chain.execPrepareKeyLookup(call, key);
  }

  @Override
  public ISmartFieldUIFacade<VALUE> getUIFacade() {
    return m_uiFacade;
  }

  @Override
  protected String formatValueInternal(VALUE validKey) {
    if (validKey == null) {
      return "";
    }

    if (getLookupCall() == null) {
      return "";
    }

    ILookupRow<VALUE> currentLookupRow = getLookupRow();
    if (currentLookupRow == null || !lookupRowMatchesValue(currentLookupRow, validKey)) {
      try {
        List<? extends ILookupRow<VALUE>> lookupRows = callKeyLookup(validKey);
        if (!lookupRows.isEmpty()) {
          currentLookupRow = lookupRows.get(0);
          setLookupRow(currentLookupRow);
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

  private String lookupRowAsText(ILookupRow<VALUE> currentLookupRow) {
    String text = currentLookupRow.getText();
    if (text != null && (!isMultilineText() && (getLookupCall() == null || !getLookupCall().isMultilineText()))) {
      text = text.replaceAll("[\\n\\r]+", " ");
    }
    return text;
  }

  @Override
  public void refreshDisplayText() {
    setLookupRow(null); // clear cached lookup row
    setDisplayText(interceptFormatValue(getValue()));
  }

  @Override
  public String getDisplayStyle() {
    return propertySupport.getPropertyString(PROP_DISPLAY_STYLE);
  }

  @Override
  public void setDisplayStyle(String displayStlye) {
    propertySupport.setPropertyString(PROP_DISPLAY_STYLE, displayStlye);
  }

  // ==== Lookup row fetching strategies ==== //

  /**
   * Creates a {@link ILookupRowProvider} to fetch a row by key.
   *
   * @see LookupCall#getDataByKey()
   * @see LookupCall#getDataByAllInBackground(RunContext, ILookupRowFetchedCallback)
   */
  protected ILookupRowProvider<VALUE> newByKeyLookupRowProvider(final VALUE key) {
    return new ILookupRowProvider<VALUE>() {

      @Override
      public void beforeProvide(ILookupCall<VALUE> lookupCall) {
        prepareKeyLookup(lookupCall, key);
      }

      @Override
      public void afterProvide(ILookupCall<VALUE> lookupCall, List<ILookupRow<VALUE>> result) {
        interceptFilterLookupResult(lookupCall, result);
        interceptFilterKeyLookupResult(lookupCall, result);
        cleanupResultList(result);
      }

      @Override
      public void provideSync(ILookupCall<VALUE> lookupCall, ILookupRowFetchedCallback<VALUE> callback) {
        callback.onSuccess(provide(lookupCall));
      }

      @Override
      public IFuture<Void> provideAsync(ILookupCall<VALUE> lookupCall, ILookupRowFetchedCallback<VALUE> callback, ClientRunContext clientRunContext) {
        return lookupCall.getDataByKeyInBackground(clientRunContext, callback);
      }

      @SuppressWarnings("unchecked")
      @Override
      public List<ILookupRow<VALUE>> provide(ILookupCall<VALUE> lookupCall) {
        return (List<ILookupRow<VALUE>>) lookupCall.getDataByKey();
      }

      @Override
      public String toString() {
        ToStringBuilder sb = new ToStringBuilder(this)
            .attr("Key Lookup")
            .attr("key", key);
        return sb.toString();
      }
    };
  }

  /**
   * Creates a {@link ILookupRowProvider} to fetch all rows.
   *
   * @see LookupCall#getDataByAll()
   * @see LookupCall#getDataByAllInBackground(RunContext, ILookupRowFetchedCallback)
   */
  protected ILookupRowProvider<VALUE> newByAllLookupRowProvider(final TriState activeState) {
    return new ILookupRowProvider<VALUE>() {

      @Override
      public void beforeProvide(ILookupCall<VALUE> lookupCall) {
        prepareBrowseLookup(lookupCall, activeState);
      }

      @Override
      public void afterProvide(ILookupCall<VALUE> lookupCall, List<ILookupRow<VALUE>> result) {
        interceptFilterLookupResult(lookupCall, result);
        interceptFilterBrowseLookupResult(lookupCall, result);
        cleanupResultList(result);
      }

      @Override
      public void provideSync(ILookupCall<VALUE> lookupCall, ILookupRowFetchedCallback<VALUE> callback) {
        callback.onSuccess(provide(lookupCall));
      }

      @Override
      public IFuture<Void> provideAsync(ILookupCall<VALUE> lookupCall, ILookupRowFetchedCallback<VALUE> callback, ClientRunContext clientRunContext) {
        return lookupCall.getDataByAllInBackground(clientRunContext, callback);
      }

      @SuppressWarnings("unchecked")
      @Override
      public List<ILookupRow<VALUE>> provide(ILookupCall<VALUE> lookupCall) {
        return (List<ILookupRow<VALUE>>) lookupCall.getDataByAll();
      }

      @Override
      public String toString() {
        return new ToStringBuilder(this)
            .attr("All Lookup")
            .attr("activeState", activeState)
            .toString();
      }

    };
  }

  /**
   * Creates a {@link ILookupRowProvider} to fetch rows matching the given text.
   *
   * @see LookupCall#getDataByText()
   * @see LookupCall#getDataByAllInBackground(RunContext, ILookupRowFetchedCallback)
   */
  protected ILookupRowProvider<VALUE> newByTextLookupRowProvider(final String text) {
    return new ILookupRowProvider<VALUE>() {

      @Override
      public void beforeProvide(ILookupCall<VALUE> lookupCall) {
        prepareTextLookup(lookupCall, text);
      }

      @Override
      public void afterProvide(ILookupCall<VALUE> call, List<ILookupRow<VALUE>> result) {
        interceptFilterLookupResult(call, result);
        interceptFilterTextLookupResult(call, result);
        cleanupResultList(result);
      }

      @Override
      public void provideSync(ILookupCall<VALUE> lookupCall, ILookupRowFetchedCallback<VALUE> callback) {
        callback.onSuccess(provide(lookupCall));
      }

      @Override
      public IFuture<Void> provideAsync(ILookupCall<VALUE> lookupCall, ILookupRowFetchedCallback<VALUE> callback, ClientRunContext clientRunContext) {
        return lookupCall.getDataByTextInBackground(clientRunContext, callback);
      }

      @SuppressWarnings("unchecked")
      @Override
      public List<ILookupRow<VALUE>> provide(ILookupCall<VALUE> lookupCall) {
        return (List<ILookupRow<VALUE>>) lookupCall.getDataByText();
      }

      @Override
      public String toString() {
        ToStringBuilder sb = new ToStringBuilder(this)
            .attr("Text Lookup")
            .attr("text", text);
        return sb.toString();
      }
    };
  }

  /**
   * @see LookupCall#getDataByRec()
   */
  protected ILookupRowProvider<VALUE> newByRecLookupRowProvider(final VALUE parentKey, final TriState activeState) {
    return new ILookupRowProvider<VALUE>() {

      @SuppressWarnings("unchecked")
      @Override
      public List<ILookupRow<VALUE>> provide(ILookupCall<VALUE> lookupCall) {
        return (List<ILookupRow<VALUE>>) lookupCall.getDataByRec();
      }

      @Override
      public void beforeProvide(ILookupCall<VALUE> lookupCall) {
        prepareRecLookup(lookupCall, parentKey, activeState);
      }

      @Override
      public void afterProvide(ILookupCall<VALUE> lookupCall, List<ILookupRow<VALUE>> result) {
        interceptFilterLookupResult(lookupCall, result);
        interceptFilterRecLookupResult(lookupCall, result);
        cleanupResultList(result);
      }

      @Override
      public void provideSync(ILookupCall<VALUE> lookupCall, ILookupRowFetchedCallback<VALUE> callback) {
        throw new UnsupportedOperationException("Legacy calls not supported");
      }

      @Override
      public IFuture<Void> provideAsync(ILookupCall<VALUE> lookupCall, ILookupRowFetchedCallback<VALUE> callback, ClientRunContext clientRunContext) {
        throw new UnsupportedOperationException("Legacy calls not supported");
      }

      @Override
      public String toString() {
        ToStringBuilder sb = new ToStringBuilder(this)
            .attr("Rec Lookup")
            .attr("parentKey", parentKey)
            .attr("activeState", activeState);
        return sb.toString();
      }
    };
  }

  /**
   * Loads lookup rows according to the specified {@link ILookupRowProvider}, and notifies the specified callback upon
   * fetching completed.
   *
   * @return {@link IFuture} if data is fetched asynchronously, or <code>null</code> for synchronous fetching, or if
   *         using {@link LocalLookupCall}.
   */
  private IFuture<Void> fetchLookupRows(final ILookupRowProvider<VALUE> dataProvider, final ILookupRowFetchedCallback<VALUE> callback, final boolean asynchronousFetching, final int maxRowCount) {
    cancelPotentialLookup();

    if (getLookupCall() == null) {
      callback.onSuccess(Collections.emptyList());
      return null;
    }

    // Prepare the lookup call.
    final ILookupCall<VALUE> lookupCall = cloneLookupCall();
    lookupCall.setMaxRowCount(maxRowCount > 0 ? maxRowCount : getBrowseMaxRowCount());

    // Prepare processing of the fetched rows.
    final ILookupRowFetchedCallback<VALUE> internalCallback = new ILookupRowFetchedCallback<VALUE>() {

      @Override
      public void onSuccess(final List<? extends ILookupRow<VALUE>> rows) {
        joinModelThreadAndUpdateField(rows, null);
      }

      @Override
      public void onFailure(final RuntimeException e) {
        joinModelThreadAndUpdateField(null, e);
      }

      private void joinModelThreadAndUpdateField(final List<? extends ILookupRow<VALUE>> rows, final RuntimeException exception) {
        if (ModelJobs.isModelThread()) {
          updateField(rows, exception);
        }
        else {
          // Synchronize with the model thread.
          // Note: The model job will not commence execution if the current ClientRunContext is or gets cancelled.
          try {
            ClientRunContext callerRunContext = ClientRunContexts.copyCurrent();
            if (callerRunContext.getRunMonitor().isCancelled()) {
              return;
            }
            ModelJobs.schedule(() -> updateField(rows, exception), ModelJobs.newInput(callerRunContext)
                .withName("Updating {}", AbstractSmartField.this.getClass().getName()))
                .awaitDone(); // block the current thread until completed
          }
          catch (ThreadInterruptedError e) { // NOSONAR
            /*
            This state can be reached by a race condition when the job's RunMonitor is in canceled state and later the ModelJob is run.
            This yields to a Thread.interrupt in the RunContext.ThreadInterrupter...

            at RunContext$ThreadInterrupter.cancel(boolean) line: 563
            at RunMonitor.cancel(ICancellable, boolean) line: 160
            at RunMonitor.registerCancellable(ICancellable) line: 104  <---------------------
            at RunContext$ThreadInterrupter.<init>(Thread, RunMonitor) line: 545
            at ClientRunContext(RunContext).call(Callable<RESULT>, Class<IExceptionTranslator<EXCEPTION>>) line: 154
            at RunContextRunner<RESULT>.intercept(Chain<RESULT>) line: 38

            which itself causes the running job to be interrupted with a InterruptedException

            at org.eclipse.scout.rt.platform.job.internal.JobExceptionTranslator.translateInterruptedException(JobExceptionTranslator.java:49)
            at org.eclipse.scout.rt.platform.job.internal.JobFutureTask.awaitDone(JobFutureTask.java:339)
            at org.eclipse.scout.rt.client.ui.form.fields.smartfield2.AbstractSmartField2$7.joinModelThreadAndUpdateField(AbstractSmartField2.java:1598)
            at org.eclipse.scout.rt.client.ui.form.fields.smartfield2.AbstractSmartField2$7.onSuccess(AbstractSmartField2.java:1575)
            at org.eclipse.scout.rt.shared.services.lookup.LookupCall.loadData(LookupCall.java:437)
            at org.eclipse.scout.rt.shared.services.lookup.LookupCall$5.run(LookupCall.java:417)
            */
          }
        }
      }

      private void updateField(final List<? extends ILookupRow<VALUE>> rows, final RuntimeException exception) {
        try {
          if (exception != null) {
            throw exception; // throw to handle exception at the end.
          }

          final List<ILookupRow<VALUE>> result = new ArrayList<>(rows);
          dataProvider.afterProvide(lookupCall, result);
          callback.onSuccess(result);
        }
        catch (FutureCancelledError | ThreadInterruptedError e) { // NOSONAR
          callback.onSuccess(Collections.emptyList());
        }
        catch (final RuntimeException e) {
          callback.onFailure(e);
        }
      }
    };

    // Start fetching lookup rows.
    IFuture<Void> asyncLookupFuture = null;
    try {
      dataProvider.beforeProvide(lookupCall);
      if (asynchronousFetching) {
        asyncLookupFuture = dataProvider.provideAsync(lookupCall, internalCallback, ClientRunContexts.copyCurrent());
      }
      else {
        dataProvider.provideSync(lookupCall, internalCallback);
        asyncLookupFuture = null;
      }
    }
    catch (final RuntimeException e) {
      internalCallback.onFailure(e);
      asyncLookupFuture = null;
    }
    m_lookupFuture = asyncLookupFuture;
    return asyncLookupFuture;
  }

  /**
   * {@link ILookupRowFetchedCallback} but with functionality to collect the result.
   */
  protected static class LookupRowCollector<VALUE> implements ILookupRowFetchedCallback<VALUE> {

    private final FinalValue<List<? extends ILookupRow<VALUE>>> m_rows = new FinalValue<>();
    private final FinalValue<RuntimeException> m_exception = new FinalValue<>();

    @Override
    public void onSuccess(List<? extends ILookupRow<VALUE>> rows) {
      m_rows.set(rows);
    }

    @Override
    public void onFailure(RuntimeException e) {
      m_exception.set(e);
    }

    /**
     * Returns the result, or throws the exception on failure.
     */
    public List<? extends ILookupRow<VALUE>> get() {
      if (m_rows.isSet()) {
        return m_rows.get();
      }
      else if (m_exception.isSet()) {
        throw m_exception.get();
      }
      else {
        throw new IllegalStateException("Lookup row fetching not completed yet");
      }
    }
  }

  protected void cancelPotentialLookup() {
    if (m_lookupFuture == null) {
      return;
    }
    if (m_lookupFuture.containsExecutionHint(EXECUTION_HINT_INITIAL_LOOKUP)) {
      return;
    }
    m_lookupFuture.cancel(false);
  }
}
