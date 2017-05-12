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
package org.eclipse.scout.rt.client.ui.form.fields.smartfield2;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.scout.rt.client.context.ClientRunContext;
import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.client.extension.ui.form.fields.IFormFieldExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.smartfield2.ISmartField2Extension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.smartfield2.SmartField2Chains.SmartField2BrowseNewChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.smartfield2.SmartField2Chains.SmartField2FilterBrowseLookupResultChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.smartfield2.SmartField2Chains.SmartField2FilterKeyLookupResultChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.smartfield2.SmartField2Chains.SmartField2FilterLookupResultChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.smartfield2.SmartField2Chains.SmartField2FilterRecLookupResultChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.smartfield2.SmartField2Chains.SmartField2FilterTextLookupResultChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.smartfield2.SmartField2Chains.SmartField2PrepareBrowseLookupChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.smartfield2.SmartField2Chains.SmartField2PrepareKeyLookupChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.smartfield2.SmartField2Chains.SmartField2PrepareLookupChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.smartfield2.SmartField2Chains.SmartField2PrepareRecLookupChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.smartfield2.SmartField2Chains.SmartField2PrepareTextLookupChain;
import org.eclipse.scout.rt.client.job.ModelJobs;
import org.eclipse.scout.rt.client.services.lookup.FormFieldProvisioningContext;
import org.eclipse.scout.rt.client.services.lookup.ILookupCallProvisioningService;
import org.eclipse.scout.rt.client.session.ClientSessionProvider;
import org.eclipse.scout.rt.client.ui.basic.table.columns.ColumnDescriptor;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractValueField;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.AbstractProposalField;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.AbstractSmartField;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.ContentAssistFieldDataFetcher;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.ContentAssistFieldListener;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.ContentAssistSearchParam;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.HierarchicalContentAssistDataFetcher;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.IContentAssistFieldDataFetchResult;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.IContentAssistFieldLookupRowFetcher;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.IContentAssistFieldUIFacade;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.IContentAssistSearchParam;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.ILookupRowProvider;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.LookupRowHelper;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigOperation;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.exception.ExceptionHandler;
import org.eclipse.scout.rt.platform.exception.PlatformError;
import org.eclipse.scout.rt.platform.exception.VetoException;
import org.eclipse.scout.rt.platform.job.DoneEvent;
import org.eclipse.scout.rt.platform.job.IBlockingCondition;
import org.eclipse.scout.rt.platform.job.IDoneHandler;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.reflect.ConfigurationUtility;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.EventListenerList;
import org.eclipse.scout.rt.platform.util.FinalValue;
import org.eclipse.scout.rt.platform.util.ObjectUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.platform.util.ToStringBuilder;
import org.eclipse.scout.rt.platform.util.TriState;
import org.eclipse.scout.rt.platform.util.concurrent.FutureCancelledError;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.platform.util.concurrent.ThreadInterruptedError;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.data.basic.FontSpec;
import org.eclipse.scout.rt.shared.services.common.code.ICodeType;
import org.eclipse.scout.rt.shared.services.lookup.CodeLookupCall;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRowFetchedCallback;
import org.eclipse.scout.rt.shared.services.lookup.LocalLookupCall;
import org.eclipse.scout.rt.shared.services.lookup.LookupCall;
import org.eclipse.scout.rt.shared.services.lookup.LookupRow;

/**
 * This class is not thought to directly subclass. Use {@link AbstractSmartField} or {@link AbstractProposalField}
 * instead.
 */
@ClassId("444e6fb6-3b0b-4917-933e-b6eb81345499")
public abstract class AbstractSmartField2<VALUE> extends AbstractValueField<VALUE> implements ISmartField2<VALUE> {

  public static final int NOT_UNIQUE_ERROR_CODE = 1;
  public static final int NO_RESULTS_ERROR_CODE = 2;

  /**
   * Null object used for {@link #installLookupRowContext(ILookupRow)}.
   */
  private static final ILookupRow EMPTY_LOOKUP_ROW = new LookupRow<>(null, "");

  @SuppressWarnings("unchecked")
  private static final <KEY> ILookupRow<KEY> emptyLookupRow() {
    return EMPTY_LOOKUP_ROW;
  }

  private final EventListenerList m_listenerList = new EventListenerList();

  // chooser security
  private Class<? extends ICodeType<?, VALUE>> m_codeTypeClass;
  private ILookupCall<VALUE> m_lookupCall;

  // cached lookup row
  private IContentAssistFieldLookupRowFetcher<VALUE> m_lookupRowFetcher;
  private String m_browseNewText;
  private boolean m_installingRowContext = false;
  private LookupRow m_decorationRow;

  private boolean m_browseAutoExpandAll;
  private boolean m_loadIncremental;
  private boolean m_loadParentNodes;
  private String m_wildcard;
  private SmartField2Result m_result;
  private String m_variant;

  private final IBlockingCondition m_contextInstalledCondition = Jobs.newBlockingCondition(false);
  private final AtomicInteger m_valueChangedLookupCounter = new AtomicInteger();

  /**
   * Provides the label-texts for the radio-buttons of the active-filter.
   */
  private String[] m_activeFilterLabels = {
      TEXTS.get("ui.All"),
      TEXTS.get("ui.Inactive"),
      TEXTS.get("ui.Active")};

  private ILookupRow<VALUE> m_currentLookupRow;

  private volatile IFuture<?> m_lookupFuture;

  public AbstractSmartField2() {
    this(true);
  }

  public AbstractSmartField2(boolean callInitializer) {
    super(false); // do not auto-initialize via super constructor, because final members of this class would not be set yet.
    if (callInitializer) {
      callInitializer();
    }
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
   * When the smart proposal finds no matching records and this property is not null, then it displays a link or menu
   * with this label.<br>
   * When clicked the method {@link #interceptBrowseNew(String)} is invoked, which in most cases is implemented as
   * opening a "New XY..." dialog
   */
  @ConfigProperty(ConfigProperty.STRING)
  @Order(315)
  protected String getConfiguredBrowseNewText() {
    return null;
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
   * null, no column headers are visible in the proposal chooser. If the returned value is a string array it contains
   * the texts used for column headers, make sure that the number of elements in the array is equals to the number of
   * cells in the proposal chooser table.
   *
   * @return
   */
  @ConfigProperty(ConfigProperty.STRING)
  @Order(310)
  protected ColumnDescriptor[] getConfiguredColumnDescriptors() {
    return null;
  }

  @ConfigProperty(ConfigProperty.OBJECT)
  @Order(320)
  protected String getConfiguredVariant() {
    return VARIANT_DEFAULT;
  }

  /**
   * see {@link #getConfiguredBrowseNewText()}<br>
   * In most cases this method is implemented as opening a "New XY..." dialog.
   * <p>
   * The smartfield waits for the return of this method:<br>
   * If null, it does nothing.<br>
   * If {@link LookupRow#getKey()} is set, this key is set as the smartfields new value.<br>
   * If {@link LookupRow#getText()} is set, this text is used as a search text and a parse is performed.
   */
  @ConfigOperation
  @Order(225)
  protected ILookupRow<VALUE> execBrowseNew(String searchText) {
    return null;
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
  protected void execPrepareBrowseLookup(ILookupCall<VALUE> call, String browseHint) {
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
  public void setTooltipText(String text) {
    super.setTooltipText(text);
    if (!m_installingRowContext) {
      // Ticket 85'572: background color gets reseted after selecting a value
      m_decorationRow.withTooltipText(getTooltipText());
    }
  }

  @Override
  public void setBackgroundColor(String c) {
    super.setBackgroundColor(c);
    if (!m_installingRowContext) {
      // Ticket 85'572: background color gets reseted after selecting a value
      m_decorationRow.withBackgroundColor(getBackgroundColor());
    }
  }

  @Override
  public void setForegroundColor(String c) {
    super.setForegroundColor(c);
    if (!m_installingRowContext) {
      // Ticket 85'572: background color gets reseted after selecting a value
      m_decorationRow.withForegroundColor(getForegroundColor());
    }
  }

  @Override
  public void setFont(FontSpec f) {
    super.setFont(f);
    if (!m_installingRowContext) {
      // Ticket 85'572: background color gets reseted after selecting a value
      m_decorationRow.withFont(getFont());
    }
  }

  @Override
  protected void initConfig() {
    setActiveFilter(TriState.TRUE);
    m_decorationRow = new LookupRow<VALUE>(null, "");
    super.initConfig();
    setActiveFilterEnabled(getConfiguredActiveFilterEnabled());
    setBrowseHierarchy(getConfiguredBrowseHierarchy());
    setBrowseAutoExpandAll(getConfiguredBrowseAutoExpandAll());
    setBrowseIconId(getConfiguredBrowseIconId());
    setBrowseLoadIncremental(getConfiguredBrowseLoadIncremental());
    setLoadParentNodes(getConfiguredLoadParentNodes());
    setMultilineText(getConfiguredMultilineText());
    setBrowseMaxRowCount(getConfiguredBrowseMaxRowCount());
    setBrowseNewText(getConfiguredBrowseNewText());
    setColumnDescriptors(getConfiguredColumnDescriptors());
    setVariant(getConfiguredVariant());

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
  }

  private void initLookupRowFetcher() {
    IContentAssistFieldLookupRowFetcher<VALUE> lookupRowFetcher = createLookupRowFetcher();
    lookupRowFetcher.addPropertyChangeListener(new P_LookupRowFetcherPropertyListener());
    setLookupRowFetcher(lookupRowFetcher);
  }

  /**
   * Model Observer
   */
  @Override
  public void addSmartFieldListener(ContentAssistFieldListener listener) {
    m_listenerList.add(ContentAssistFieldListener.class, listener);
  }

  @Override
  public void removeSmartFieldListener(ContentAssistFieldListener listener) {
    m_listenerList.remove(ContentAssistFieldListener.class, listener);
  }

  @Override
  public boolean isActiveFilterEnabled() {
    return propertySupport.getPropertyBool(PROP_ACTIVE_FILTER_ENABLED);
  }

  @Override
  public void setActiveFilterEnabled(boolean b) {
    propertySupport.setPropertyBool(PROP_ACTIVE_FILTER_ENABLED, b);
  }

  @Override
  public TriState getActiveFilter() {
    return (TriState) propertySupport.getProperty(PROP_ACTIVE_FILTER);
  }

  @Override
  public void setActiveFilter(TriState t) {
    if (isActiveFilterEnabled()) {
      if (t == null) {
        t = TriState.TRUE;
      }
      propertySupport.setProperty(PROP_ACTIVE_FILTER, t);
    }
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
  public SmartField2Result getResult() {
    return m_result;
  }

  protected void setResult(SmartField2Result<VALUE> result) {
    propertySupport.firePropertyChange(PROP_RESULT, null, result);
  }

  /**
   * see {@link AbstractSmartField#interceptBrowseNew(String)}
   */
  @Override
  public void doBrowseNew(String newText) {
    if (getBrowseNewText() != null) {
      try {
        ILookupRow<VALUE> newRow = interceptBrowseNew(newText);
        if (newRow == null) {
          // nop
        }
        else {
          acceptProposal(newRow);
        }
      }
      catch (Exception e) {
        BEANS.get(ExceptionHandler.class).handle(e);
      }
    }
  }

  @Override
  public String getBrowseIconId() {
    return propertySupport.getPropertyString(PROP_BROWSE_ICON_ID);
  }

  @Override
  public void setBrowseIconId(String s) {
    propertySupport.setPropertyString(PROP_BROWSE_ICON_ID, s);
  }

  @Override
  public String getIconId() {
    return propertySupport.getPropertyString(PROP_ICON_ID);
  }

  @Override
  public void setIconId(String s) {
    propertySupport.setPropertyString(PROP_ICON_ID, s);
  }

  @Override
  public void setMultilineText(boolean b) {
    propertySupport.setPropertyBool(PROP_MULTILINE_TEXT, b);
  }

  @Override
  public boolean isMultilineText() {
    return propertySupport.getPropertyBool(PROP_MULTILINE_TEXT);
  }

  @Override
  public boolean isBrowseAutoExpandAll() {
    return m_browseAutoExpandAll;
  }

  @Override
  public void setBrowseAutoExpandAll(boolean b) {
    m_browseAutoExpandAll = b;
  }

  @Override
  public boolean isBrowseLoadIncremental() {
    return m_loadIncremental;
  }

  @Override
  public void setBrowseLoadIncremental(boolean b) {
    m_loadIncremental = b;
  }

  @Override
  public boolean isLoadParentNodes() {
    return m_loadParentNodes;
  }

  @Override
  public void setLoadParentNodes(boolean b) {
    m_loadParentNodes = b;
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
  public int getBrowseMaxRowCount() {
    return propertySupport.getPropertyInt(PROP_BROWSE_MAX_ROW_COUNT);
  }

  @Override
  public void setBrowseMaxRowCount(int browseMaxRowCount) {
    propertySupport.setPropertyInt(PROP_BROWSE_MAX_ROW_COUNT, browseMaxRowCount);
  }

  @Override
  public String getBrowseNewText() {
    return m_browseNewText;
  }

  @Override
  public void setBrowseNewText(String s) {
    m_browseNewText = s;
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
      if (t != null && !ConfigurationUtility.isMethodOverwrite(AbstractSmartField2.class, "getConfiguredBrowseHierarchy", new Class[0], this.getClass())) {
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

  @Override
  public void setUniquelyDefinedValue(boolean background) {
    ILookupRowFetchedCallback<VALUE> callback = new ILookupRowFetchedCallback<VALUE>() {

      @Override
      public void onSuccess(List<? extends ILookupRow<VALUE>> rows) {
        if (rows.size() == 1) {
          acceptProposal(rows.get(0));
        }
      }

      @Override
      public void onFailure(RuntimeException e) {
        // NOOP
      }
    };
    if (background) {
      callBrowseLookupInBackground(getWildcard(), 2, callback);
    }
    else {
      callback.onSuccess(callBrowseLookup(getWildcard(), 2));
    }
  }

  public IContentAssistFieldLookupRowFetcher<VALUE> getLookupRowFetcher() {
    return m_lookupRowFetcher;
  }

  public void setLookupRowFetcher(IContentAssistFieldLookupRowFetcher<VALUE> fetcher) {
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
    if (getCurrentLookupRow() == null) {
      return true;
    }

    return validKey == getCurrentLookupRow().getKey() || (validKey != null && validKey.equals(getCurrentLookupRow().getKey()));
  }

  @Override
  protected final VALUE execValidateValue(VALUE rawValue) {
    return rawValue;
  }

  @Override
  public void clearProposal() {
    throw new UnsupportedOperationException();
  }

  /**
   * Notice: This method is called from a worker originated outside the scout thread (sync into scout model thread)
   */
  protected void installLookupRowContext(ILookupRow<VALUE> row) {
    if (row == null) {
      row = emptyLookupRow();
    }

    m_installingRowContext = true;
    try {
      if (StringUtility.hasText(row.getTooltipText())) {
        setTooltipText(row.getTooltipText());
      }
      else {
        setTooltipText(m_decorationRow.getTooltipText());
      }

      if (StringUtility.hasText(row.getBackgroundColor())) {
        setBackgroundColor(row.getBackgroundColor());
      }
      else {
        setBackgroundColor(m_decorationRow.getBackgroundColor());
      }

      if (StringUtility.hasText(row.getForegroundColor())) {
        setForegroundColor(row.getForegroundColor());
      }
      else {
        setForegroundColor(m_decorationRow.getForegroundColor());
      }

      if (row.getFont() != null) {
        setFont(row.getFont());
      }
      else {
        setFont(m_decorationRow.getFont());
      }
    }
    finally {
      m_installingRowContext = false;
    }
  }

  @Override
  public String getDisplayText() {
    applyLazyStyles();
    return super.getDisplayText();
  }

  @Override
  public String getTooltipText() {
    applyLazyStyles();
    return super.getTooltipText();
  }

  @Override
  public String getBackgroundColor() {
    applyLazyStyles();
    return super.getBackgroundColor();
  }

  @Override
  public String getForegroundColor() {
    applyLazyStyles();
    return super.getForegroundColor();
  }

  @Override
  public FontSpec getFont() {
    applyLazyStyles();
    return super.getFont();
  }

  protected String toSearchText(String text) {
    return StringUtility.isNullOrEmpty(text) ? getWildcard() : text;
  }

  public void setCurrentLookupRow(ILookupRow<VALUE> row) {
    m_currentLookupRow = row;
  }

  public ILookupRow<VALUE> getCurrentLookupRow() {
    return m_currentLookupRow;
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
  public void prepareBrowseLookup(ILookupCall<VALUE> call, String browseHint, TriState activeState) {
    call.setKey(null);
    call.setText(null);
    call.setAll(browseHint);
    call.setRec(null);
    call.setActive(activeState);
    //when there is a master value defined in the original call, don't set it to null when no master value is available
    if (getMasterValue() != null || getLookupCall() == null || getLookupCall().getMaster() == null) {
      call.setMaster(getMasterValue());
    }
    interceptPrepareLookup(call);
    interceptPrepareBrowseLookup(call, browseHint);
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
    doSearch(text, false, true);
    IContentAssistFieldDataFetchResult<VALUE> fetchResult = getLookupRowFetcher().getResult();

    int numResults = 0;
    if (fetchResult != null && fetchResult.getLookupRows() != null) {
      numResults = fetchResult.getLookupRows().size();
      if (numResults == 1) {
        ILookupRow<VALUE> singleMatchLookupRow = CollectionUtility.firstElement(fetchResult.getLookupRows());
        setCurrentLookupRow(singleMatchLookupRow);
        return returnLookupRowAsValue(singleMatchLookupRow);
      }
    }

    VetoException veto = new VetoException(ScoutTexts.get("SmartFieldCannotComplete", text));
    veto.withCode(numResults > 1 ? NOT_UNIQUE_ERROR_CODE : NO_RESULTS_ERROR_CODE);
    throw veto;
  }

  protected VALUE returnLookupRowAsValue(ILookupRow<VALUE> lookupRow) {
    return lookupRow.getKey();
  }

  @Override
  protected VALUE parseValueInternal(String text) {
    ILookupRow<VALUE> currentLookupRow = getCurrentLookupRow();
    if (currentLookupRow == null) {
      return handleMissingLookupRow(text);
    }
    else {
      return returnLookupRowAsValue(getCurrentLookupRow());
    }
  }

  @Override
  protected VALUE validateValueInternal(VALUE rawValue) {
    VALUE validatedValue = super.validateValueInternal(rawValue);

    // when value is null, current lookupRow must be null too
    if (validatedValue == null) {
      setCurrentLookupRow(null);
      return validatedValue;
    }

    // set currentLookupRow to null, when new value doesn't match lookupRow
    // we must do this every time setValue() is called.
    ILookupRow<VALUE> currentLookupRow = getCurrentLookupRow();
    if (currentLookupRow != null && !lookupRowMatchesValue(currentLookupRow, validatedValue)) {
      setCurrentLookupRow(null);
    }

    return validatedValue;
  }

  /**
   * Returns true if the given value matches the given lookup-row. The default impl. checks if the key of the lookup-row
   * matches. Override this method to implement another behavior.
   */
  protected boolean lookupRowMatchesValue(ILookupRow<VALUE> lookupRow, VALUE value) {
    return ObjectUtility.equals(lookupRow.getKey(), value);
  }

  //search and update the field with the result

  @Override
  public void lookupAll() {
    doSearch(null, false, false);
  }

  @Override
  public void lookupByText(String text) {
    doSearch(text, false, false);
  }

  @Override
  public void doSearch(boolean selectCurrentValue, boolean synchronous) {
    doSearch(getLookupRowFetcher().getLastSearchText(), selectCurrentValue, synchronous);
  }

  @Override
  public void doSearch(String text, boolean selectCurrentValue, boolean synchronous) {
    IContentAssistSearchParam<VALUE> param = ContentAssistSearchParam.createTextParam(toSearchText(text), selectCurrentValue);
    doSearch(param, synchronous);
  }

  @Override
  public void doSearch(IContentAssistSearchParam<VALUE> param, boolean synchronous) {
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
  public List<? extends ILookupRow<VALUE>> callBrowseLookup(String browseHint, int maxRowCount) {
    return callBrowseLookup(browseHint, maxRowCount, isActiveFilterEnabled() ? getActiveFilter() : TriState.TRUE);
  }

  @Override
  public List<? extends ILookupRow<VALUE>> callBrowseLookup(String browseHint, int maxRowCount, TriState activeState) {
    LookupRowCollector<VALUE> collector = new LookupRowCollector<>();
    fetchLookupRows(newByAllLookupRowProvider(browseHint, activeState), collector, false, maxRowCount);
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
    return callBrowseLookupInBackground(null, cancelRunningJobs);
  }

  @Override
  public IFuture<List<ILookupRow<VALUE>>> callBrowseLookupInBackground(String browseHint, boolean cancelRunningJobs) {
    TriState activeState = isActiveFilterEnabled() ? getActiveFilter() : TriState.TRUE;
    final ILookupRowProvider<VALUE> provider = newByAllLookupRowProvider(browseHint, activeState);
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
    return BEANS.get(ILookupCallProvisioningService.class).newClonedInstance(getLookupCall(), new FormFieldProvisioningContext(AbstractSmartField2.this));
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
  public IFuture<Void> callBrowseLookupInBackground(String browseHint, int maxRowCount, ILookupRowFetchedCallback<VALUE> callback) {
    return callBrowseLookupInBackground(browseHint, maxRowCount, isActiveFilterEnabled() ? getActiveFilter() : TriState.TRUE, callback);
  }

  @Override
  public IFuture<Void> callBrowseLookupInBackground(String browseHint, int maxRowCount, TriState activeState, ILookupRowFetchedCallback<VALUE> callback) {
    return fetchLookupRows(newByAllLookupRowProvider(browseHint, activeState), callback, true, maxRowCount);
  }

  protected void cleanupResultList(final List<ILookupRow<VALUE>> list) {
    final Iterator<? extends ILookupRow<VALUE>> iterator = list.iterator();
    while (iterator.hasNext()) {
      final ILookupRow<VALUE> candidate = iterator.next();
      if (candidate == null) {
        iterator.remove();
      }
    }
  }

  protected void handleFetchResult(IContentAssistFieldDataFetchResult<VALUE> result) {
    if (result == null) {
      setResult(null);
    }
    else {
      setResult(new SmartField2Result<VALUE>(result));
    }
  }

  protected IContentAssistFieldLookupRowFetcher<VALUE> createLookupRowFetcher() {
    if (isBrowseHierarchy()) {
      return new HierarchicalContentAssistDataFetcher<VALUE>(new SmartField2ContentAssistAdapter<VALUE>(this));
    }
    else {
      return new ContentAssistFieldDataFetcher<VALUE>(new SmartField2ContentAssistAdapter<VALUE>(this));
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
      if (IContentAssistFieldLookupRowFetcher.PROP_SEARCH_RESULT.equals(evt.getPropertyName())) {
        handleFetchResult((IContentAssistFieldDataFetchResult<VALUE>) evt.getNewValue());
      }
    }
  }

  protected static class LocalSmartField2Extension<VALUE, OWNER extends AbstractSmartField2<VALUE>> extends LocalValueFieldExtension<VALUE, OWNER>
      implements ISmartField2Extension<VALUE, OWNER> {

    public LocalSmartField2Extension(OWNER owner) {
      super(owner);
    }

    @Override
    public void execFilterBrowseLookupResult(SmartField2FilterBrowseLookupResultChain<VALUE> chain, ILookupCall<VALUE> call, List<ILookupRow<VALUE>> result) {
      getOwner().execFilterBrowseLookupResult(call, result);
    }

    @Override
    public ILookupRow<VALUE> execBrowseNew(SmartField2BrowseNewChain<VALUE> chain, String searchText) {
      return getOwner().execBrowseNew(searchText);
    }

    @Override
    public void execFilterKeyLookupResult(SmartField2FilterKeyLookupResultChain<VALUE> chain, ILookupCall<VALUE> call, List<ILookupRow<VALUE>> result) {
      getOwner().execFilterKeyLookupResult(call, result);
    }

    @Override
    public void execPrepareLookup(SmartField2PrepareLookupChain<VALUE> chain, ILookupCall<VALUE> call) {
      getOwner().execPrepareLookup(call);
    }

    @Override
    public void execPrepareTextLookup(SmartField2PrepareTextLookupChain<VALUE> chain, ILookupCall<VALUE> call, String text) {
      getOwner().execPrepareTextLookup(call, text);
    }

    @Override
    public void execPrepareBrowseLookup(SmartField2PrepareBrowseLookupChain<VALUE> chain, ILookupCall<VALUE> call, String browseHint) {
      getOwner().execPrepareBrowseLookup(call, browseHint);
    }

    @Override
    public void execFilterTextLookupResult(SmartField2FilterTextLookupResultChain<VALUE> chain, ILookupCall<VALUE> call, List<ILookupRow<VALUE>> result) {
      getOwner().execFilterTextLookupResult(call, result);
    }

    @Override
    public void execPrepareRecLookup(SmartField2PrepareRecLookupChain<VALUE> chain, ILookupCall<VALUE> call, VALUE parentKey) {
      getOwner().execPrepareRecLookup(call, parentKey);
    }

    @Override
    public void execFilterLookupResult(SmartField2FilterLookupResultChain<VALUE> chain, ILookupCall<VALUE> call, List<ILookupRow<VALUE>> result) {
      getOwner().execFilterLookupResult(call, result);
    }

    @Override
    public void execFilterRecLookupResult(SmartField2FilterRecLookupResultChain<VALUE> chain, ILookupCall<VALUE> call, List<ILookupRow<VALUE>> result) {
      getOwner().execFilterRecLookupResult(call, result);
    }

    @Override
    public void execPrepareKeyLookup(SmartField2PrepareKeyLookupChain<VALUE> chain, ILookupCall<VALUE> call, VALUE key) {
      getOwner().execPrepareKeyLookup(call, key);
    }
  }

  @Override
  protected ISmartField2Extension<VALUE, ? extends AbstractSmartField2<VALUE>> createLocalExtension() {
    return new LocalSmartField2Extension<VALUE, AbstractSmartField2<VALUE>>(this);
  }

  protected final void interceptFilterBrowseLookupResult(ILookupCall<VALUE> call, List<ILookupRow<VALUE>> result) {
    List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions = getAllExtensions();
    SmartField2FilterBrowseLookupResultChain<VALUE> chain = new SmartField2FilterBrowseLookupResultChain<VALUE>(extensions);
    chain.execFilterBrowseLookupResult(call, result);
  }

  protected final ILookupRow<VALUE> interceptBrowseNew(String searchText) {
    List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions = getAllExtensions();
    SmartField2BrowseNewChain<VALUE> chain = new SmartField2BrowseNewChain<VALUE>(extensions);
    return chain.execBrowseNew(searchText);
  }

  protected final void interceptFilterKeyLookupResult(ILookupCall<VALUE> call, List<ILookupRow<VALUE>> result) {
    List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions = getAllExtensions();
    SmartField2FilterKeyLookupResultChain<VALUE> chain = new SmartField2FilterKeyLookupResultChain<VALUE>(extensions);
    chain.execFilterKeyLookupResult(call, result);
  }

  protected final void interceptPrepareLookup(ILookupCall<VALUE> call) {
    List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions = getAllExtensions();
    SmartField2PrepareLookupChain<VALUE> chain = new SmartField2PrepareLookupChain<VALUE>(extensions);
    chain.execPrepareLookup(call);
  }

  protected final void interceptPrepareTextLookup(ILookupCall<VALUE> call, String text) {
    List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions = getAllExtensions();
    SmartField2PrepareTextLookupChain<VALUE> chain = new SmartField2PrepareTextLookupChain<VALUE>(extensions);
    chain.execPrepareTextLookup(call, text);
  }

  protected final void interceptPrepareBrowseLookup(ILookupCall<VALUE> call, String browseHint) {
    List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions = getAllExtensions();
    SmartField2PrepareBrowseLookupChain<VALUE> chain = new SmartField2PrepareBrowseLookupChain<VALUE>(extensions);
    chain.execPrepareBrowseLookup(call, browseHint);
  }

  protected final void interceptFilterTextLookupResult(ILookupCall<VALUE> call, List<ILookupRow<VALUE>> result) {
    List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions = getAllExtensions();
    SmartField2FilterTextLookupResultChain<VALUE> chain = new SmartField2FilterTextLookupResultChain<VALUE>(extensions);
    chain.execFilterTextLookupResult(call, result);
  }

  protected final void interceptPrepareRecLookup(ILookupCall<VALUE> call, VALUE parentKey) {
    List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions = getAllExtensions();
    SmartField2PrepareRecLookupChain<VALUE> chain = new SmartField2PrepareRecLookupChain<VALUE>(extensions);
    chain.execPrepareRecLookup(call, parentKey);
  }

  protected final void interceptFilterLookupResult(ILookupCall<VALUE> call, List<ILookupRow<VALUE>> result) {
    List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions = getAllExtensions();
    SmartField2FilterLookupResultChain<VALUE> chain = new SmartField2FilterLookupResultChain<VALUE>(extensions);
    chain.execFilterLookupResult(call, result);
  }

  protected final void interceptFilterRecLookupResult(ILookupCall<VALUE> call, List<ILookupRow<VALUE>> result) {
    List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions = getAllExtensions();
    SmartField2FilterRecLookupResultChain<VALUE> chain = new SmartField2FilterRecLookupResultChain<VALUE>(extensions);
    chain.execFilterRecLookupResult(call, result);
  }

  protected final void interceptPrepareKeyLookup(ILookupCall<VALUE> call, VALUE key) {
    List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions = getAllExtensions();
    SmartField2PrepareKeyLookupChain<VALUE> chain = new SmartField2PrepareKeyLookupChain<VALUE>(extensions);
    chain.execPrepareKeyLookup(call, key);
  }

  @Override
  public IContentAssistFieldUIFacade getUIFacade() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void acceptProposal(ILookupRow<VALUE> row) {
    setCurrentLookupRow(row);
    setValue(row.getKey());
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
    final ILookupRow<VALUE> currentLookupRow = getCurrentLookupRow();
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
    // final VALUE lookupKey = interceptConvertValueToKey(getValue());
    final VALUE lookupKey = getValue();

    m_valueChangedLookupCounter.incrementAndGet();
    final IFuture<Void> future = callKeyLookupInBackground(lookupKey, new ILookupRowFetchedCallback<VALUE>() {

      @Override
      public void onSuccess(final List<? extends ILookupRow<VALUE>> rows) {
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
    if (isProposal()) {
      return super.formatValueInternal(validKey);
    }

    if (validKey == null) {
      return "";
    }

    if (getLookupCall() == null) {
      return "";
    }

    ILookupRow<VALUE> currentLookupRow = getCurrentLookupRow();
    if (currentLookupRow == null) {
      try {
        // List<? extends ILookupRow<VALUE>> lookupRows = callKeyLookup(interceptConvertValueToKey(validKey));
        List<? extends ILookupRow<VALUE>> lookupRows = callKeyLookup(validKey);
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
  private String lookupRowAsText(ILookupRow<VALUE> currentLookupRow) {
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
        // List<? extends ILookupRow<VALUE>> rows = callKeyLookup(interceptConvertValueToKey(getValue()));
        List<? extends ILookupRow<VALUE>> rows = callKeyLookup(getValue());
        installLookupRowContext(CollectionUtility.firstElement(rows));
      }
      catch (RuntimeException | PlatformError e) {
        BEANS.get(ExceptionHandler.class).handle(e);
      }
    }
  }

  @Override
  public String getVariant() {
    return m_variant;
  }

  @Override
  public void setVariant(String variant) {
    m_variant = variant;
  }

  @Override
  public boolean isProposal() {
    return VARIANT_PROPOSAL.equals(m_variant);
  }

  // ==== Lookup row fetching strategies ==== //

  /**
   * Creates a {@link ILookupRowProvider} to fetch a row by key.
   *
   * @see LookupCall#getDataByKey()
   * @see LookupCall#getDataByAllInBackground(ILookupRowFetchedCallback)
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
   * @see LookupCall#getDataByAllInBackground(ILookupRowFetchedCallback)
   */
  protected ILookupRowProvider<VALUE> newByAllLookupRowProvider(final String browseHint, final TriState activeState) {
    return new ILookupRowProvider<VALUE>() {

      @Override
      public void beforeProvide(ILookupCall<VALUE> lookupCall) {
        prepareBrowseLookup(lookupCall, browseHint, activeState);
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
        ToStringBuilder sb = new ToStringBuilder(this)
            .attr("All Lookup")
            .attr("browseHint", browseHint)
            .attr("activeState", activeState);
        return sb.toString();
      }

    };
  }

  /**
   * Creates a {@link ILookupRowProvider} to fetch rows matching the given text.
   *
   * @see LookupCall#getDataByText()
   * @see LookupCall#getDataByTextInBackground(ILookupRowFetchedCallback)
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
      callback.onSuccess(Collections.<ILookupRow<VALUE>> emptyList());
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
          ModelJobs.schedule(new IRunnable() {

            @Override
            public void run() throws Exception {
              updateField(rows, exception);
            }
          }, ModelJobs.newInput(ClientRunContexts.copyCurrent())
              .withName("Updating {}", AbstractSmartField2.this.getClass().getName()))
              .awaitDone(); // block the current thread until completed
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
          callback.onSuccess(Collections.<ILookupRow<VALUE>> emptyList());
        }
        catch (final RuntimeException e) { // NOSONAR
          callback.onFailure(exception);
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
