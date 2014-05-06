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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.ConfigurationUtility;
import org.eclipse.scout.commons.EventListenerList;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.TriState;
import org.eclipse.scout.commons.annotations.ConfigOperation;
import org.eclipse.scout.commons.annotations.ConfigProperty;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.annotations.ScoutSdkIgnore;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.exception.ProcessingStatus;
import org.eclipse.scout.commons.holders.Holder;
import org.eclipse.scout.commons.job.JobEx;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.services.lookup.FormFieldProvisioningContext;
import org.eclipse.scout.rt.client.services.lookup.ILookupCallProvisioningService;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.form.FormEvent;
import org.eclipse.scout.rt.client.ui.form.FormListener;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractValueField;
import org.eclipse.scout.rt.shared.AbstractIcons;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.shared.data.basic.FontSpec;
import org.eclipse.scout.rt.shared.data.form.ValidationRule;
import org.eclipse.scout.rt.shared.services.common.code.CODES;
import org.eclipse.scout.rt.shared.services.common.code.ICodeType;
import org.eclipse.scout.rt.shared.services.common.exceptionhandler.IExceptionHandlerService;
import org.eclipse.scout.rt.shared.services.lookup.CodeLookupCall;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCallFetcher;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;
import org.eclipse.scout.rt.shared.services.lookup.LookupRow;
import org.eclipse.scout.service.SERVICES;

/**
 * This class is not thought to directly subclass. Use {@link AbstractSmartField} or {@link AbstractProposalField}
 * instead.
 */
@ScoutSdkIgnore
public abstract class AbstractContentAssistField<VALUE_TYPE, KEY_TYPE> extends AbstractValueField<VALUE_TYPE> implements IContentAssistField<VALUE_TYPE, KEY_TYPE> {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractContentAssistField.class);

  public final ILookupRow<KEY_TYPE> EMPTY_LOOKUP_ROW = new LookupRow<KEY_TYPE>(null, "", null, null, null, null, null, true);

  private final EventListenerList m_listenerList = new EventListenerList();
  // chooser security
  private Class<? extends ICodeType<?, KEY_TYPE>> m_codeTypeClass;
  private ILookupCall<KEY_TYPE> m_lookupCall;

  // cached lookup row
  private P_ProposalFormListener m_proposalFormListener;
  private IContentAssistFieldProposalFormProvider<KEY_TYPE> m_proposalFormProvider;
  private P_LookupRowFetcherPropertyListener m_lookupRowFetcherPropertyListener;
  private IContentAssistFieldLookupRowFetcher<KEY_TYPE> m_lookupRowFetcher;
  private int m_maxRowCount;
  private String m_browseNewText;
  private boolean m_installingRowContext = false;
  private LookupRow m_decorationRow;

  private TriState m_activeFilter;
  private boolean m_activeFilterEnabled;
  private boolean m_browseAutoExpandAll;
  private boolean m_browseHierarchy;
  private boolean m_loadIncremental;
  private int m_proposalFormHeight;

  private ILookupRow<KEY_TYPE> m_currentLookupRow;

  private Class<? extends IContentAssistFieldTable<VALUE_TYPE>> m_contentAssistTableClazz;

  public AbstractContentAssistField() {
    this(true);
  }

  public AbstractContentAssistField(boolean callInitializer) {
    super(callInitializer);
  }

  /*
   * Configuration
   */
  @ConfigProperty(ConfigProperty.ICON_ID)
  @Order(270)
  protected String getConfiguredBrowseIconId() {
    return null;
  }

  @ConfigProperty(ConfigProperty.ICON_ID)
  @Order(230)
  protected String getConfiguredIconId() {
    return AbstractIcons.SmartFieldBrowse;
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
   * the value 0 for numbers and "" for Strings will be set to null, if this
   * flag is set to true
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(300)
  @ValidationRule(ValidationRule.ZERO_NULL_EQUALITY)
  protected boolean getConfiguredTreat0AsNull() {
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
   * If this method is not overwritten, and the content assist field has a codeType then
   * the value of browseHierarchy is automatically determined by {@link ICodeType#isHierarchy()}
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(310)
  protected boolean getConfiguredBrowseHierarchy() {
    return false;
  }

  /**
   * When the smart proposal finds no matching records and this property is not
   * null, then it displays a link or menu with this label.<br>
   * When clicked the method {@link #execBrowseNew(String)} is invoked, which in
   * most cases is implemented as opening a "New XY..." dialog
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
  @ValidationRule(ValidationRule.CODE_TYPE)
  protected Class<? extends ICodeType<?, KEY_TYPE>> getConfiguredCodeType() {
    return null;
  }

  /**
   * variant B: lookup by backend lookup service<br>
   * 3.0: no support for {@code<eval>} tags anymore<br>
   * 3.0: still valid are {@code<text><key><all><rec>} tags in lookup statements
   * in the backend
   */
  @ConfigProperty(ConfigProperty.LOOKUP_CALL)
  @Order(250)
  @ValidationRule(ValidationRule.LOOKUP_CALL)
  protected Class<? extends ILookupCall<KEY_TYPE>> getConfiguredLookupCall() {
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
   * @return
   *         default height: 280
   */
  @ConfigProperty(ConfigProperty.INTEGER)
  @Order(280)
  protected int getConfiguredProposalFormHeight() {
    return 280;
  }

  @Order(290)
  protected Class<? extends IContentAssistFieldTable<VALUE_TYPE>> getConfiguredContentAssistTable() {
    return null;
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
  protected ILookupRow<KEY_TYPE> execBrowseNew(String searchText) throws ProcessingException {
    return null;
  }

  /**
   * called before any lookup is performed (key, text, browse)
   */
  @ConfigOperation
  @Order(230)
  protected void execPrepareLookup(ILookupCall<KEY_TYPE> call) throws ProcessingException {
  }

  /**
   * called before key lookup but after prepareLookup()
   */
  @ConfigOperation
  @Order(240)
  protected void execPrepareKeyLookup(ILookupCall<KEY_TYPE> call, KEY_TYPE key) throws ProcessingException {
  }

  /**
   * called before text lookup but after prepareLookup()
   */
  @ConfigOperation
  @Order(250)
  protected void execPrepareTextLookup(ILookupCall<KEY_TYPE> call, String text) throws ProcessingException {
  }

  /**
   * called before browse lookup but after prepareLookup()
   */
  @ConfigOperation
  @Order(260)
  protected void execPrepareBrowseLookup(ILookupCall<KEY_TYPE> call, String browseHint) throws ProcessingException {
  }

  /**
   * called before rec lookup but after prepareLookup()
   */
  @ConfigOperation
  @Order(270)
  protected void execPrepareRecLookup(ILookupCall<KEY_TYPE> call, KEY_TYPE parentKey) throws ProcessingException {
  }

  /**
   * @param call
   *          that produced this result
   * @param result
   *          live list containing the result rows. Add, remove, set, replace
   *          and clear of entries in this list is supported
   */
  @ConfigOperation
  @Order(280)
  protected void execFilterLookupResult(ILookupCall<KEY_TYPE> call, List<ILookupRow<KEY_TYPE>> result) throws ProcessingException {
  }

  /**
   * @param call
   *          that produced this result
   * @param result
   *          live list containing the result rows. Add, remove, set, replace
   *          and clear of entries in this list is supported
   */
  @ConfigOperation
  @Order(290)
  protected void execFilterKeyLookupResult(ILookupCall<KEY_TYPE> call, List<ILookupRow<KEY_TYPE>> result) throws ProcessingException {
  }

  /**
   * @param call
   *          that produced this result
   * @param result
   *          live list containing the result rows. Add, remove, set, replace
   *          and clear of entries in this list is supported
   */
  @ConfigOperation
  @Order(300)
  protected void execFilterTextLookupResult(ILookupCall<KEY_TYPE> call, List<ILookupRow<KEY_TYPE>> result) throws ProcessingException {
  }

  /**
   * @param call
   *          that produced this result
   * @param result
   *          live list containing the result rows. Add, remove, set, replace
   *          and clear of entries in this list is supported
   */
  @ConfigOperation
  @Order(310)
  protected void execFilterBrowseLookupResult(ILookupCall<KEY_TYPE> call, List<ILookupRow<KEY_TYPE>> result) throws ProcessingException {
  }

  /**
   * @param call
   *          that produced this result
   * @param result
   *          live list containing the result rows. Add, remove, set, replace
   *          and clear of entries in this list is supported
   */
  @ConfigOperation
  @Order(320)
  protected void execFilterRecLookupResult(ILookupCall<KEY_TYPE> call, List<ILookupRow<KEY_TYPE>> result) throws ProcessingException {
  }

  @Override
  public boolean acceptBrowseHierarchySelection(KEY_TYPE value, int level, boolean leaf) {
    return true;
  }

  // override to freeze
  @Override
  protected final boolean getConfiguredAutoDisplayText() {
    return true;
  }

  @Override
  public void setTooltipText(String text) {
    super.setTooltipText(text);
    if (!m_installingRowContext) {
      //Ticket 85'572: background color gets reseted after selecting a value
      m_decorationRow.setTooltipText(getTooltipText());
    }
  }

  @Override
  public void setBackgroundColor(String c) {
    super.setBackgroundColor(c);
    if (!m_installingRowContext) {
      //Ticket 85'572: background color gets reseted after selecting a value
      m_decorationRow.setBackgroundColor(getBackgroundColor());
    }
  }

  @Override
  public void setForegroundColor(String c) {
    super.setForegroundColor(c);
    if (!m_installingRowContext) {
      //Ticket 85'572: background color gets reseted after selecting a value
      m_decorationRow.setForegroundColor(getForegroundColor());
    }
  }

  @Override
  public void setFont(FontSpec f) {
    super.setFont(f);
    if (!m_installingRowContext) {
      //Ticket 85'572: background color gets reseted after selecting a value
      m_decorationRow.setFont(getFont());
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  protected void initConfig() {
    m_activeFilter = TriState.TRUE;
    m_decorationRow = new LookupRow<KEY_TYPE>(null, "", null, null, null, null, null, true);
    super.initConfig();
    setActiveFilterEnabled(getConfiguredActiveFilterEnabled());
    setBrowseHierarchy(getConfiguredBrowseHierarchy());
    setBrowseAutoExpandAll(getConfiguredBrowseAutoExpandAll());
    setBrowseIconId(getConfiguredBrowseIconId());
    setBrowseLoadIncremental(getConfiguredBrowseLoadIncremental());
    setIconId(getConfiguredIconId());
    setBrowseMaxRowCount(getConfiguredBrowseMaxRowCount());
    setBrowseNewText(getConfiguredBrowseNewText());
    setProposalFormProvider(createProposalFormProvider());
    setProposalFormHeight(getConfiguredProposalFormHeight());
    // content assist table
    Class<? extends IContentAssistFieldTable<VALUE_TYPE>> contentAssistTableClazz = getConfiguredContentAssistTable();
    // if no table is configured try to find a fitting inner class
    if (contentAssistTableClazz == null) {
      // try to find inner class
      Class[] dca = ConfigurationUtility.getDeclaredPublicClasses(getClass());
      contentAssistTableClazz = (Class<? extends IContentAssistFieldTable<VALUE_TYPE>>) ConfigurationUtility.filterClass(dca, IContentAssistFieldTable.class);
    }
    // if no inner class use default
    if (contentAssistTableClazz == null) {
      contentAssistTableClazz = (Class<? extends IContentAssistFieldTable<VALUE_TYPE>>) ContentAssistFieldTable.class;
    }
    setContentAssistTableClass(contentAssistTableClazz);
    IContentAssistFieldLookupRowFetcher<KEY_TYPE> lookupRowFetcher = createLookupRowFetcher();
    lookupRowFetcher.addPropertyChangeListener(new P_LookupRowFetcherPropertyListener());
    setLookupRowFetcher(lookupRowFetcher);
    // code type
    if (getConfiguredCodeType() != null) {
      setCodeTypeClass(getConfiguredCodeType());
    }
    // lookup call
    Class<? extends ILookupCall<KEY_TYPE>> lsCls = getConfiguredLookupCall();
    if (lsCls != null) {
      try {
        ILookupCall<KEY_TYPE> call = lsCls.newInstance();
        setLookupCall(call);
      }
      catch (Exception e) {
        SERVICES.getService(IExceptionHandlerService.class).handleException(new ProcessingException(this.getClass().getSimpleName(), e));
      }
    }
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

  // main handler
  private void fireSmartFieldEvent(ContentAssistFieldEvent e) {
    EventListener[] listeners = m_listenerList.getListeners(ContentAssistFieldListener.class);
    if (listeners != null && listeners.length > 0) {
      for (int i = 0; i < listeners.length; i++) {
        ((ContentAssistFieldListener) listeners[i]).fieldChanged(e);
      }
    }
  }

  @Override
  public boolean isActiveFilterEnabled() {
    return m_activeFilterEnabled;
  }

  @Override
  public void setActiveFilterEnabled(boolean b) {
    m_activeFilterEnabled = b;
  }

  @Override
  public TriState getActiveFilter() {
    return m_activeFilter;
  }

  @Override
  public void setActiveFilter(TriState t) {
    if (isActiveFilterEnabled()) {
      if (t == null) {
        t = TriState.TRUE;
      }
      m_activeFilter = t;
    }
  }

  @Override
  public void setProposalFormHeight(int proposalFormHeight) {
    m_proposalFormHeight = proposalFormHeight;
  }

  @Override
  public int getProposalFormHeight() {
    return m_proposalFormHeight;
  }

  /**
   * @param configuredContentAssistTableClass
   */
  private void setContentAssistTableClass(Class<? extends IContentAssistFieldTable<VALUE_TYPE>> configuredContentAssistTableClass) {
    m_contentAssistTableClazz = configuredContentAssistTableClass;
  }

  @Override
  public Class<? extends IContentAssistFieldTable<VALUE_TYPE>> getContentAssistFieldTableClass() {
    return m_contentAssistTableClazz;
  }

  /**
   * see {@link AbstractSmartField#execBrowseNew(String)}
   */
  @Override
  public void doBrowseNew(String newText) {
    if (getBrowseNewText() != null) {
      try {
        ILookupRow<KEY_TYPE> newRow = execBrowseNew(newText);
        if (newRow == null) {
          // nop
        }
        else {
          acceptProposal(newRow);
        }
      }
      catch (ProcessingException e) {
        SERVICES.getService(IExceptionHandlerService.class).handleException(e);
      }
      catch (Throwable t) {
        SERVICES.getService(IExceptionHandlerService.class).handleException(new ProcessingException("Unexpected", t));
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
  public boolean isBrowseHierarchy() {
    return m_browseHierarchy;
  }

  @Override
  public void setBrowseHierarchy(boolean b) {
    m_browseHierarchy = b;
  }

  @Override
  public int getBrowseMaxRowCount() {
    return m_maxRowCount;
  }

  @Override
  public void setBrowseMaxRowCount(int n) {
    m_maxRowCount = n;
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
  public Class<? extends ICodeType<?, KEY_TYPE>> getCodeTypeClass() {
    return m_codeTypeClass;
  }

  @Override
  public void setCodeTypeClass(Class<? extends ICodeType<?, KEY_TYPE>> codeType) {
    m_codeTypeClass = codeType;
    // create lookup service call
    m_lookupCall = null;
    if (m_codeTypeClass != null) {
      m_lookupCall = CodeLookupCall.newInstanceByService(m_codeTypeClass);
      ICodeType t = CODES.getCodeType(m_codeTypeClass);
      if (t != null) {
        if (!ConfigurationUtility.isMethodOverwrite(AbstractContentAssistField.class, "getConfiguredBrowseHierarchy", new Class[0], this.getClass())) {
          setBrowseHierarchy(t.isHierarchy());
        }
      }
    }
  }

  @Override
  public ILookupCall<KEY_TYPE> getLookupCall() {
    return m_lookupCall;
  }

  @Override
  public void setLookupCall(ILookupCall<KEY_TYPE> call) {
    m_lookupCall = call;
  }

  @Override
  public void setUniquelyDefinedValue(boolean background) throws ProcessingException {
    ILookupCallFetcher<KEY_TYPE> fetcher = new ILookupCallFetcher<KEY_TYPE>() {
      @Override
      public void dataFetched(List<? extends ILookupRow<KEY_TYPE>> rows, ProcessingException failed) {
        if (failed == null) {
          if (rows.size() == 1) {
            acceptProposal(rows.get(0));
          }
        }
      }
    };
    if (background) {
      callBrowseLookupInBackground(IContentAssistField.BROWSE_ALL_TEXT, 2, fetcher);
    }
    else {
      fetcher.dataFetched(callBrowseLookup(IContentAssistField.BROWSE_ALL_TEXT, 2), null);
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public IContentAssistFieldProposalForm<KEY_TYPE> getProposalForm() {
    return (IContentAssistFieldProposalForm<KEY_TYPE>) propertySupport.getProperty(PROP_PROPOSAL_FORM);
  }

  protected void registerProposalFormInternal(IContentAssistFieldProposalForm<KEY_TYPE> form) {
    IContentAssistFieldProposalForm oldForm = getProposalForm();
    if (oldForm == form) {
      return;
    }
    // old form
    if (oldForm != null) {
      if (m_proposalFormListener != null) {
        oldForm.removeFormListener(m_proposalFormListener);
        m_proposalFormListener = null;
      }
      try {
        oldForm.doClose();
      }
      catch (Throwable t) {
        // nop
      }
    }
    // new form
    if (form != null) {
      if (m_proposalFormListener == null) {
        m_proposalFormListener = new P_ProposalFormListener();
      }
      form.addFormListener(m_proposalFormListener);
    }
    propertySupport.setProperty(PROP_PROPOSAL_FORM, form);
  }

  protected void unregisterProposalFormInternal(IContentAssistFieldProposalForm form) {
    if (form != null) {
      IContentAssistFieldProposalForm oldForm = getProposalForm();
      if (oldForm == form) {
        propertySupport.setProperty(PROP_PROPOSAL_FORM, null);
      }
      if (form.isFormOpen()) {
        try {
          form.doClose();
        }
        catch (ProcessingException e) {
          // nop
        }
      }
    }
  }

  @Override
  public IContentAssistFieldProposalFormProvider<KEY_TYPE> getProposalFormProvider() {
    return m_proposalFormProvider;
  }

  @Override
  public void setProposalFormProvider(IContentAssistFieldProposalFormProvider<KEY_TYPE> provider) {
    m_proposalFormProvider = provider;
  }

  protected abstract IContentAssistFieldProposalForm<KEY_TYPE> createProposalForm() throws ProcessingException;

  public IContentAssistFieldLookupRowFetcher<KEY_TYPE> getLookupRowFetcher() {
    return m_lookupRowFetcher;
  }

  public void setLookupRowFetcher(IContentAssistFieldLookupRowFetcher<KEY_TYPE> fetcher) {
    m_lookupRowFetcher = fetcher;
  }

  /**
   * Returns the content assist field's proposal form with the use of a {@link IContentAssistFieldProposalFormProvider}.
   * <p>
   * To provide a custom proposal form create a custom proposal form provider and inject it with
   * {@link #createProposalFormProvider()} or {@link #setProposalFormProvider()}.
   * </p>
   * 
   * @return {@link#ISmartFieldProposalForm}
   * @throws ProcessingException
   */
  protected IContentAssistFieldProposalForm<KEY_TYPE> createProposalForm(boolean allowCustomText) throws ProcessingException {
    IContentAssistFieldProposalFormProvider<KEY_TYPE> proposalFormProvider = getProposalFormProvider();
    if (proposalFormProvider == null) {
      return null;
    }
    return proposalFormProvider.createProposalForm(this, allowCustomText);
  }

  protected IContentAssistFieldProposalFormProvider<KEY_TYPE> createProposalFormProvider() {
    return new DefaultContentAssistFieldProposalFormProvider<KEY_TYPE>();
  }

  @Override
  protected final VALUE_TYPE execValidateValue(VALUE_TYPE rawValue) throws ProcessingException {
    return rawValue;
  }

  @Override
  protected VALUE_TYPE validateValueInternal(VALUE_TYPE rawKey) throws ProcessingException {
    if (rawKey instanceof Number) {
      if (getConfiguredTreat0AsNull()) {
        if (((Number) rawKey).longValue() == 0) {
          rawKey = null;
        }
      }
    }
    else if (rawKey instanceof String) {
      if (getConfiguredTreat0AsNull()) {
        if (((String) rawKey).length() == 0) {
          rawKey = null;
        }
      }
    }
    return super.validateValueInternal(rawKey);
  }

  @Override
  public void revertValue() {
    setValue(getValue());
  }

  /**
   * Notice: This method is called from a worker originated outside the scout
   * thread (sync into scout model thread)
   */
  protected void installLookupRowContext(ILookupRow<KEY_TYPE> row) {
    try {
      m_installingRowContext = true;
      String text = row.getText();
      if (text != null) {
        text = text.replaceAll("[\\n\\r]+", " ");
      }
      setDisplayText(text);
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

  @Override
  public void doSearch(boolean selectCurrentValue, boolean synchronous) {
    doSearch(getLookupRowFetcher().getLastSearchText(), selectCurrentValue, synchronous);
  }

  @Override
  public void doSearch(String searchText, boolean selectCurrentValue, boolean synchronous) {
    IContentAssistFieldProposalForm<KEY_TYPE> proposalForm = getProposalForm();
    if (proposalForm != null) {
      proposalForm.setTablePopulateStatus(new ProcessingStatus(ScoutTexts.get("searchingProposals"), ProcessingStatus.WARNING));
    }
    getLookupRowFetcher().update(searchText, selectCurrentValue, synchronous);
  }

  public void setCurrentLookupRow(ILookupRow<KEY_TYPE> row) {
    m_currentLookupRow = row;
  }

  public ILookupRow<KEY_TYPE> getCurrentLookupRow() {
    return m_currentLookupRow;
  }

  @Override
  public void prepareKeyLookup(ILookupCall<KEY_TYPE> call, KEY_TYPE key) throws ProcessingException {
    call.setKey(key);
    call.setText(null);
    call.setAll(null);
    call.setRec(null);
    call.setActive(TriState.UNDEFINED);
    //when there is a master value defined in the original call, don't set it to null when no master value is available
    if (getMasterValue() != null || getLookupCall() == null || getLookupCall().getMaster() == null) {
      call.setMaster(getMasterValue());
    }
    execPrepareLookup(call);
    execPrepareKeyLookup(call, key);
  }

  @Override
  public void prepareTextLookup(ILookupCall<KEY_TYPE> call, String text) throws ProcessingException {
    String textPattern = text;
    if (textPattern == null) {
      textPattern = "";
    }
    textPattern = textPattern.toLowerCase();
    IDesktop desktop = ClientSyncJob.getCurrentSession().getDesktop();
    if (desktop != null && desktop.isAutoPrefixWildcardForTextSearch()) {
      textPattern = "*" + textPattern;
    }
    if (!textPattern.endsWith("*")) {
      textPattern = textPattern + "*";
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
    execPrepareLookup(call);
    execPrepareTextLookup(call, text);
  }

  @Override
  public void prepareBrowseLookup(ILookupCall<KEY_TYPE> call, String browseHint, TriState activeState) throws ProcessingException {
    call.setKey(null);
    call.setText(null);
    call.setAll(browseHint);
    call.setRec(null);
    call.setActive(activeState);
    //when there is a master value defined in the original call, don't set it to null when no master value is available
    if (getMasterValue() != null || getLookupCall() == null || getLookupCall().getMaster() == null) {
      call.setMaster(getMasterValue());
    }
    execPrepareLookup(call);
    execPrepareBrowseLookup(call, browseHint);
  }

  @Override
  public void prepareRecLookup(ILookupCall<KEY_TYPE> call, KEY_TYPE parentKey, TriState activeState) throws ProcessingException {
    call.setKey(null);
    call.setText(null);
    call.setAll(null);
    call.setRec(parentKey);
    //when there is a master value defined in the original call, don't set it to null when no master value is available
    if (getMasterValue() != null || getLookupCall() == null || getLookupCall().getMaster() == null) {
      call.setMaster(getMasterValue());
    }
    call.setActive(activeState);
    execPrepareLookup(call);
    execPrepareRecLookup(call, parentKey);
  }

  protected void filterKeyLookup(ILookupCall<KEY_TYPE> call, List<ILookupRow<KEY_TYPE>> result) throws ProcessingException {
    execFilterLookupResult(call, result);
    execFilterKeyLookupResult(call, result);
  }

  private void filterTextLookup(ILookupCall<KEY_TYPE> call, List<ILookupRow<KEY_TYPE>> result) throws ProcessingException {
    execFilterLookupResult(call, result);
    execFilterTextLookupResult(call, result);
  }

  private void filterBrowseLookup(ILookupCall<KEY_TYPE> call, List<ILookupRow<KEY_TYPE>> result) throws ProcessingException {
    execFilterLookupResult(call, result);
    execFilterBrowseLookupResult(call, result);
  }

  private void filterRecLookup(ILookupCall<KEY_TYPE> call, List<ILookupRow<KEY_TYPE>> result) throws ProcessingException {
    execFilterLookupResult(call, result);
    execFilterRecLookupResult(call, result);
  }

  @Override
  public List<? extends ILookupRow<KEY_TYPE>> callKeyLookup(KEY_TYPE key) throws ProcessingException {
    List<? extends ILookupRow<KEY_TYPE>> data = null;
    ILookupCall<KEY_TYPE> call = getLookupCall();
    if (call != null) {
      call = SERVICES.getService(ILookupCallProvisioningService.class).newClonedInstance(call, new FormFieldProvisioningContext(AbstractContentAssistField.this));
      prepareKeyLookup(call, key);
      data = call.getDataByKey();
    }
    List<ILookupRow<KEY_TYPE>> result = CollectionUtility.arrayList(data);
    filterKeyLookup(call, result);
    return cleanupResultList(result);
  }

  @Override
  public List<? extends ILookupRow<KEY_TYPE>> callTextLookup(String text, int maxRowCount) throws ProcessingException {
    final Holder<List<? extends ILookupRow<KEY_TYPE>>> rowsHolder = new Holder<List<? extends ILookupRow<KEY_TYPE>>>();
    final Holder<ProcessingException> failedHolder = new Holder<ProcessingException>(ProcessingException.class, new ProcessingException("callback was not invoked"));
    callTextLookupInternal(text, maxRowCount, new ILookupCallFetcher<KEY_TYPE>() {
      @Override
      public void dataFetched(List<? extends ILookupRow<KEY_TYPE>> rows, ProcessingException failed) {
        rowsHolder.setValue(rows);
        failedHolder.setValue(failed);
      }
    }, false);
    if (failedHolder.getValue() != null) {
      throw failedHolder.getValue();
    }
    else {
      return rowsHolder.getValue();
    }
  }

  @Override
  public JobEx callTextLookupInBackground(String text, int maxRowCount, ILookupCallFetcher<KEY_TYPE> fetcher) {
    return callTextLookupInternal(text, maxRowCount, fetcher, true);
  }

  private JobEx callTextLookupInternal(String text, int maxRowCount, final ILookupCallFetcher<KEY_TYPE> fetcher, final boolean background) {
    final ILookupCall<KEY_TYPE> call = (getLookupCall() != null ? SERVICES.getService(ILookupCallProvisioningService.class).newClonedInstance(getLookupCall(), new FormFieldProvisioningContext(AbstractContentAssistField.this)) : null);
    final IClientSession session = ClientSyncJob.getCurrentSession();
    ILookupCallFetcher<KEY_TYPE> internalFetcher = new ILookupCallFetcher<KEY_TYPE>() {
      @Override
      public void dataFetched(final List<? extends ILookupRow<KEY_TYPE>> rows, final ProcessingException failed) {
        ClientSyncJob scoutSyncJob = new ClientSyncJob("Smartfield text lookup", session) {
          @Override
          protected void runVoid(IProgressMonitor monitor) throws Throwable {
            if (failed == null) {
              ArrayList<ILookupRow<KEY_TYPE>> result = new ArrayList<ILookupRow<KEY_TYPE>>(rows);
              try {
                filterTextLookup(call, result);
                fetcher.dataFetched(cleanupResultList(result), null);
              }
              catch (ProcessingException e) {
                fetcher.dataFetched(null, e);
              }
            }
            else {
              fetcher.dataFetched(null, failed);
            }
          }
        };
        if (background) {
          scoutSyncJob.schedule();
        }
        else {
          scoutSyncJob.runNow(new NullProgressMonitor());
        }
      }
    };
    //
    if (call != null) {
      if (maxRowCount > 0) {
        call.setMaxRowCount(maxRowCount);
      }
      else {
        call.setMaxRowCount(getBrowseMaxRowCount());
      }
      if (background) {
        try {
          prepareTextLookup(call, text);
          return call.getDataByTextInBackground(internalFetcher);
        }
        catch (ProcessingException e1) {
          internalFetcher.dataFetched(null, e1);
        }
      }
      else {
        try {
          prepareTextLookup(call, text);
          internalFetcher.dataFetched(call.getDataByText(), null);
        }
        catch (ProcessingException e) {
          internalFetcher.dataFetched(null, e);
        }
      }
    }
    else {
      internalFetcher.dataFetched(new ArrayList<ILookupRow<KEY_TYPE>>(), null);
    }
    return null;
  }

  @Override
  public List<? extends ILookupRow<KEY_TYPE>> callBrowseLookup(String browseHint, int maxRowCount) throws ProcessingException {
    return callBrowseLookup(browseHint, maxRowCount, isActiveFilterEnabled() ? getActiveFilter() : TriState.TRUE);
  }

  @Override
  public List<? extends ILookupRow<KEY_TYPE>> callBrowseLookup(String browseHint, int maxRowCount, TriState activeState) throws ProcessingException {
    final Holder<List<? extends ILookupRow<KEY_TYPE>>> rowsHolder = new Holder<List<? extends ILookupRow<KEY_TYPE>>>();
    final Holder<ProcessingException> failedHolder = new Holder<ProcessingException>(ProcessingException.class, new ProcessingException("callback was not invoked"));
    callBrowseLookupInternal(browseHint, maxRowCount, activeState, new ILookupCallFetcher<KEY_TYPE>() {
      @Override
      public void dataFetched(List<? extends ILookupRow<KEY_TYPE>> rows, ProcessingException failed) {
        rowsHolder.setValue(rows);
        failedHolder.setValue(failed);
      }
    }, false);
    if (failedHolder.getValue() != null) {
      throw failedHolder.getValue();
    }
    else {
      return rowsHolder.getValue();
    }
  }

  @Override
  public JobEx callBrowseLookupInBackground(String browseHint, int maxRowCount, ILookupCallFetcher<KEY_TYPE> fetcher) {
    return callBrowseLookupInBackground(browseHint, maxRowCount, isActiveFilterEnabled() ? getActiveFilter() : TriState.TRUE, fetcher);
  }

  @Override
  public JobEx callBrowseLookupInBackground(String browseHint, int maxRowCount, TriState activeState, ILookupCallFetcher<KEY_TYPE> fetcher) {
    return callBrowseLookupInternal(browseHint, maxRowCount, activeState, fetcher, true);
  }

  private JobEx callBrowseLookupInternal(String browseHint, int maxRowCount, TriState activeState, final ILookupCallFetcher<KEY_TYPE> fetcher, final boolean background) {
    final ILookupCall<KEY_TYPE> call = (getLookupCall() != null ? SERVICES.getService(ILookupCallProvisioningService.class).newClonedInstance(getLookupCall(), new FormFieldProvisioningContext(AbstractContentAssistField.this)) : null);
    final IClientSession session = ClientSyncJob.getCurrentSession();
    ILookupCallFetcher<KEY_TYPE> internalFetcher = new ILookupCallFetcher<KEY_TYPE>() {
      @Override
      public void dataFetched(final List<? extends ILookupRow<KEY_TYPE>> rows, final ProcessingException failed) {
        ClientSyncJob scoutSyncJob = new ClientSyncJob("ContentAssistField browse lookup", session) {
          @Override
          protected void runVoid(IProgressMonitor monitor) throws Throwable {
            if (failed == null) {
              ArrayList<ILookupRow<KEY_TYPE>> result = new ArrayList<ILookupRow<KEY_TYPE>>(rows);
              try {
                filterBrowseLookup(call, result);
                fetcher.dataFetched(cleanupResultList(result), null);
              }
              catch (ProcessingException e) {
                fetcher.dataFetched(null, e);
              }
            }
            else {
              fetcher.dataFetched(null, failed);
            }
          }
        };
        if (background) {
          scoutSyncJob.schedule();
        }
        else {
          scoutSyncJob.runNow(new NullProgressMonitor());
        }
      }
    };
    //
    if (call != null) {
      if (maxRowCount > 0) {
        call.setMaxRowCount(maxRowCount);
      }
      else {
        call.setMaxRowCount(getBrowseMaxRowCount());
      }
      if (background) {
        try {
          prepareBrowseLookup(call, browseHint, activeState);
          return call.getDataByAllInBackground(internalFetcher);
        }
        catch (ProcessingException e1) {
          internalFetcher.dataFetched(null, e1);
        }
      }
      else {
        try {
          prepareBrowseLookup(call, browseHint, activeState);
          internalFetcher.dataFetched(call.getDataByAll(), null);
        }
        catch (ProcessingException e) {
          internalFetcher.dataFetched(null, e);
        }
      }
    }
    else {
      internalFetcher.dataFetched(new ArrayList<ILookupRow<KEY_TYPE>>(), null);
    }
    return null;
  }

  @Override
  public List<ILookupRow<KEY_TYPE>> callSubTreeLookup(KEY_TYPE parentKey) throws ProcessingException {
    return callSubTreeLookup(parentKey, isActiveFilterEnabled() ? getActiveFilter() : TriState.TRUE);
  }

  @Override
  public List<ILookupRow<KEY_TYPE>> callSubTreeLookup(KEY_TYPE parentKey, TriState activeState) throws ProcessingException {
    List<? extends ILookupRow<KEY_TYPE>> data = null;
    ILookupCall<KEY_TYPE> call = getLookupCall();
    if (call != null) {
      call = SERVICES.getService(ILookupCallProvisioningService.class).newClonedInstance(call, new FormFieldProvisioningContext(AbstractContentAssistField.this));
      call.setMaxRowCount(getBrowseMaxRowCount());
      prepareRecLookup(call, parentKey, activeState);
      data = call.getDataByRec();
    }
    ArrayList<ILookupRow<KEY_TYPE>> result;
    if (data != null) {
      result = new ArrayList<ILookupRow<KEY_TYPE>>(data);
    }
    else {
      result = new ArrayList<ILookupRow<KEY_TYPE>>(0);
    }
    filterRecLookup(call, result);
    return cleanupResultList(result);
  }

  protected List<ILookupRow<KEY_TYPE>> cleanupResultList(List<ILookupRow<KEY_TYPE>> list) {
    List<ILookupRow<KEY_TYPE>> rows = new ArrayList<ILookupRow<KEY_TYPE>>();
    for (ILookupRow<KEY_TYPE> r : list) {
      if (r != null) {
        rows.add(r);
      }
    }
    return rows;
  }

  protected abstract void handleProposalFormClosed(IContentAssistFieldProposalForm<KEY_TYPE> proposalForm) throws ProcessingException;

  /**
   * @param newValue
   */
  protected abstract void handleFetchResult(IContentAssistFieldDataFetchResult<KEY_TYPE> result);

  protected IContentAssistFieldLookupRowFetcher<KEY_TYPE> createLookupRowFetcher() {
    if (isBrowseHierarchy()) {
      return new HierachycalContentAssistDataFetcher<KEY_TYPE>(this);
    }
    else {
      return new ContentAssistFieldDataFetcher<KEY_TYPE>(this);
    }
  }

  /*
   * inner classes
   */

  // end private class

  private class P_ProposalFormListener implements FormListener {
    @SuppressWarnings("unchecked")
    @Override
    public void formChanged(FormEvent e) throws ProcessingException {
      switch (e.getType()) {
        case FormEvent.TYPE_CLOSED: {
          handleProposalFormClosed((IContentAssistFieldProposalForm<KEY_TYPE>) e.getForm());
          break;
        }
      }
    }

  }// end private class

  private class P_LookupRowFetcherPropertyListener implements PropertyChangeListener {
    @SuppressWarnings("unchecked")
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      if (IContentAssistFieldLookupRowFetcher.PROP_SEARCH_RESULT.equals(evt.getPropertyName())) {
        handleFetchResult((IContentAssistFieldDataFetchResult<KEY_TYPE>) evt.getNewValue());
      }
    }

  }

}
