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
import java.util.Arrays;
import java.util.EventListener;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.ConfigurationUtility;
import org.eclipse.scout.commons.EventListenerList;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.TriState;
import org.eclipse.scout.commons.TypeCastUtility;
import org.eclipse.scout.commons.annotations.ConfigOperation;
import org.eclipse.scout.commons.annotations.ConfigProperty;
import org.eclipse.scout.commons.annotations.ConfigPropertyValue;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.exception.VetoException;
import org.eclipse.scout.commons.holders.Holder;
import org.eclipse.scout.commons.job.JobEx;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ClientAsyncJob;
import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.basic.cell.ICell;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.form.FormEvent;
import org.eclipse.scout.rt.client.ui.form.FormListener;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractValueField;
import org.eclipse.scout.rt.client.ui.form.fields.ParsingFailedStatus;
import org.eclipse.scout.rt.client.ui.form.fields.button.IButton;
import org.eclipse.scout.rt.shared.AbstractIcons;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.shared.data.basic.FontSpec;
import org.eclipse.scout.rt.shared.data.form.ValidationRule;
import org.eclipse.scout.rt.shared.services.common.code.CODES;
import org.eclipse.scout.rt.shared.services.common.code.ICodeType;
import org.eclipse.scout.rt.shared.services.common.exceptionhandler.IExceptionHandlerService;
import org.eclipse.scout.rt.shared.services.lookup.CodeLookupCall;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCallFetcher;
import org.eclipse.scout.rt.shared.services.lookup.LocalLookupCall;
import org.eclipse.scout.rt.shared.services.lookup.LookupCall;
import org.eclipse.scout.rt.shared.services.lookup.LookupRow;
import org.eclipse.scout.service.SERVICES;

public abstract class AbstractSmartField<T> extends AbstractValueField<T> implements ISmartField<T> {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractSmartField.class);

  private final EventListenerList m_listenerList = new EventListenerList();
  private ISmartFieldUIFacade m_uiFacade;
  // chooser security
  private Class<? extends ICodeType> m_codeTypeClass;
  private LookupCall m_lookupCall;
  // text fetching tread
  private Thread m_textFetchBackgroundThread;
  // cached lookup row
  private LookupRow m_currentLookupRow;
  private P_GetLookupRowByKeyJob m_currentGetLookupRowByKeyJob;
  private P_ProposalFormListener m_proposalFormListener;
  private ISmartFieldProposalFormProvider m_proposalFormProvider;
  private int m_maxRowCount;
  private String m_browseNewText;
  private boolean m_installingRowContext = false;
  private LookupRow m_decorationRow;

  private IMenu[] m_menus;
  private TriState m_activeFilter;
  private boolean m_activeFilterEnabled;
  private boolean m_browseAutoExpandAll;
  private boolean m_browseHierarchy;
  private boolean m_loadIncremental;
  private boolean m_allowCustomText;

  public AbstractSmartField() {
    this(true);
  }

  public AbstractSmartField(boolean callInitializer) {
    super(callInitializer);
  }

  /*
   * Configuration
   */
  @ConfigProperty(ConfigProperty.ICON_ID)
  @Order(270)
  @ConfigPropertyValue("null")
  protected String getConfiguredBrowseIconId() {
    return null;
  }

  @ConfigProperty(ConfigProperty.ICON_ID)
  @Order(230)
  @ConfigPropertyValue("AbstractIcons.SmartFieldBrowse")
  protected String getConfiguredIconId() {
    return AbstractIcons.SmartFieldBrowse;
  }

  /**
   * valid when configuredBrowseHierarchy=true
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(280)
  @ConfigPropertyValue("true")
  protected boolean getConfiguredBrowseAutoExpandAll() {
    return true;
  }

  /**
   * Code-Assistant<br>
   * Don't just allow smart field values, but also custom text as valid values;
   * smartfield is simply used as code assistent
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(290)
  @ConfigPropertyValue("false")
  protected boolean getConfiguredAllowCustomText() {
    return false;
  }

  /**
   * the value 0 for numbers and "" for Strings will be set to null, if this
   * flag is set to true
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(300)
  @ConfigPropertyValue("true")
  @ValidationRule(ValidationRule.ZERO_NULL_EQUALITY)
  protected boolean getConfiguredTreat0AsNull() {
    return true;
  }

  /**
   * valid when configuredBrowseHierarchy=true
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(240)
  @ConfigPropertyValue("false")
  protected boolean getConfiguredBrowseLoadIncremental() {
    return false;
  }

  /**
   * If this method is not overwritten, and the smartfield has a codeType then
   * the value of browseHierarchy is automatically determined by {@link ICodeType#isHierarchy()}
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(310)
  @ConfigPropertyValue("false")
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
  @ConfigPropertyValue("null")
  protected String getConfiguredBrowseNewText() {
    return null;
  }

  /**
   * variant A: lookup by code type
   */
  @ConfigProperty(ConfigProperty.CODE_TYPE)
  @Order(260)
  @ConfigPropertyValue("null")
  @ValidationRule(ValidationRule.CODE_TYPE)
  protected Class<? extends ICodeType<?>> getConfiguredCodeType() {
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
  @ConfigPropertyValue("null")
  @ValidationRule(ValidationRule.LOOKUP_CALL)
  protected Class<? extends LookupCall> getConfiguredLookupCall() {
    return null;
  }

  @ConfigProperty(ConfigProperty.INTEGER)
  @Order(265)
  @ConfigPropertyValue("100")
  protected int getConfiguredBrowseMaxRowCount() {
    return 100;
  }

  /**
   * @return true: inactive rows are display together with active rows<br>
   *         false: inactive rows ae only displayed when selected by the model
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(270)
  @ConfigPropertyValue("false")
  protected boolean getConfiguredActiveFilterEnabled() {
    return false;
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
  protected LookupRow execBrowseNew(String searchText) throws ProcessingException {
    return null;
  }

  /**
   * called before any lookup is performed (key, text, browse)
   */
  @ConfigOperation
  @Order(230)
  protected void execPrepareLookup(LookupCall call) throws ProcessingException {
  }

  /**
   * called before key lookup but after prepareLookup()
   */
  @ConfigOperation
  @Order(240)
  protected void execPrepareKeyLookup(LookupCall call, T key) throws ProcessingException {
  }

  /**
   * called before text lookup but after prepareLookup()
   */
  @ConfigOperation
  @Order(250)
  protected void execPrepareTextLookup(LookupCall call, String text) throws ProcessingException {
  }

  /**
   * called before browse lookup but after prepareLookup()
   */
  @ConfigOperation
  @Order(260)
  protected void execPrepareBrowseLookup(LookupCall call, String browseHint) throws ProcessingException {
  }

  /**
   * called before rec lookup but after prepareLookup()
   */
  @ConfigOperation
  @Order(270)
  protected void execPrepareRecLookup(LookupCall call, T parentKey) throws ProcessingException {
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
  protected void execFilterLookupResult(LookupCall call, List<LookupRow> result) throws ProcessingException {
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
  protected void execFilterKeyLookupResult(LookupCall call, List<LookupRow> result) throws ProcessingException {
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
  protected void execFilterTextLookupResult(LookupCall call, List<LookupRow> result) throws ProcessingException {
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
  protected void execFilterBrowseLookupResult(LookupCall call, List<LookupRow> result) throws ProcessingException {
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
  protected void execFilterRecLookupResult(LookupCall call, List<LookupRow> result) throws ProcessingException {
  }

  @Override
  public boolean acceptBrowseHierarchySelection(T value, int level, boolean leaf) {
    return true;
  }

  private Class<? extends IMenu>[] getConfiguredMenus() {
    Class[] dca = ConfigurationUtility.getDeclaredPublicClasses(getClass());
    return ConfigurationUtility.sortFilteredClassesByOrderAnnotation(dca, IMenu.class);
  }

  // override to freeze
  @Override
  @ConfigPropertyValue("true")
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

  @Override
  protected void initConfig() {
    m_uiFacade = new P_UIFacade();
    m_activeFilter = TriState.TRUE;
    m_decorationRow = new LookupRow(null, "", null, null, null, null, null, true);
    super.initConfig();
    setActiveFilterEnabled(getConfiguredActiveFilterEnabled());
    setBrowseHierarchy(getConfiguredBrowseHierarchy());
    setBrowseAutoExpandAll(getConfiguredBrowseAutoExpandAll());
    setBrowseIconId(getConfiguredBrowseIconId());
    setBrowseLoadIncremental(getConfiguredBrowseLoadIncremental());
    setIconId(getConfiguredIconId());
    setBrowseMaxRowCount(getConfiguredBrowseMaxRowCount());
    setBrowseNewText(getConfiguredBrowseNewText());
    setAllowCustomText(getConfiguredAllowCustomText());
    setProposalFormProvider(createProposalFormProvider());
    // code type
    if (getConfiguredCodeType() != null) {
      setCodeTypeClass(getConfiguredCodeType());
    }
    // lookup call
    Class<? extends LookupCall> lsCls = getConfiguredLookupCall();
    if (lsCls != null) {
      try {
        LookupCall call = lsCls.newInstance();
        setLookupCall(call);
      }
      catch (Exception e) {
        SERVICES.getService(IExceptionHandlerService.class).handleException(new ProcessingException(this.getClass().getSimpleName(), e));
      }
    }
    // menus
    ArrayList<IMenu> menuList = new ArrayList<IMenu>();
    Class<? extends IMenu>[] a = getConfiguredMenus();
    for (int i = 0; i < a.length; i++) {
      try {
        IMenu menu = ConfigurationUtility.newInnerInstance(this, a[i]);
        menuList.add(menu);
      }
      catch (Exception e) {
        SERVICES.getService(IExceptionHandlerService.class).handleException(new ProcessingException(this.getClass().getSimpleName(), e));
      }
    }
    try {
      injectMenusInternal(menuList);
    }
    catch (Exception e) {
      LOG.error("error occured while dynamically contributing menus.", e);
    }
    m_menus = menuList.toArray(new IMenu[0]);

    // convenience check for allowCustomText=true
    if (isAllowCustomText() && getHolderType() != String.class) {
      LOG.warn(getClass().getName() + ": allowCustomText=true is normally used only on smart fields of generic type String.");
    }
  }

  /**
   * Override this internal method only in order to make use of dynamic menus<br>
   * Used to manage menu list and add/remove menus
   * 
   * @param menuList
   *          live and mutable list of configured menus
   */
  protected void injectMenusInternal(List<IMenu> menuList) {
  }

  @Override
  public IMenu[] getMenus() {
    return m_menus;
  }

  @Override
  public boolean hasMenus() {
    return m_menus.length > 0;
  }

  /**
   * Model Observer
   */
  @Override
  public void addSmartFieldListener(SmartFieldListener listener) {
    m_listenerList.add(SmartFieldListener.class, listener);
  }

  @Override
  public void removeSmartFieldListener(SmartFieldListener listener) {
    m_listenerList.remove(SmartFieldListener.class, listener);
  }

  // main handler
  private void fireSmartFieldEvent(SmartFieldEvent e) {
    EventListener[] listeners = m_listenerList.getListeners(SmartFieldListener.class);
    if (listeners != null && listeners.length > 0) {
      for (int i = 0; i < listeners.length; i++) {
        ((SmartFieldListener) listeners[i]).smartFieldChanged(e);
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

  /**
   * see {@link AbstractSmartField#execBrowseNew(String)}
   */
  @SuppressWarnings("unchecked")
  @Override
  public void doBrowseNew(String newText) {
    if (getBrowseNewText() != null) {
      try {
        LookupRow newRow = execBrowseNew(newText);
        if (newRow == null) {
          // nop
        }
        else if (newRow.getKey() != null) {
          setValue((T) newRow.getKey());
        }
        else if (newRow.getText() != null) {
          parseValue(newRow.getText());
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
  public boolean isAllowCustomText() {
    return m_allowCustomText;
  }

  @Override
  public void setAllowCustomText(boolean b) {
    m_allowCustomText = b;
  }

  @Override
  public Class<? extends ICodeType> getCodeTypeClass() {
    return m_codeTypeClass;
  }

  @Override
  public void setCodeTypeClass(Class<? extends ICodeType> codeType) {
    m_codeTypeClass = codeType;
    // create lookup service call
    m_lookupCall = null;
    if (m_codeTypeClass != null) {
      m_lookupCall = new CodeLookupCall(m_codeTypeClass);
      ICodeType t = CODES.getCodeType(m_codeTypeClass);
      if (t != null) {
        if (!ConfigurationUtility.isMethodOverwrite(AbstractSmartField.class, "getConfiguredBrowseHierarchy", new Class[0], this.getClass())) {
          setBrowseHierarchy(t.isHierarchy());
        }
      }
    }
  }

  @Override
  public LookupCall getLookupCall() {
    return m_lookupCall;
  }

  @Override
  public void setLookupCall(LookupCall call) {
    m_lookupCall = call;
  }

  @Override
  public void setUniquelyDefinedValue(boolean background) throws ProcessingException {
    ILookupCallFetcher fetcher = new ILookupCallFetcher() {
      @Override
      @SuppressWarnings("unchecked")
      public void dataFetched(LookupRow[] rows, ProcessingException failed) {
        if (failed == null) {
          if (rows.length == 1) {
            T uniqueValue = (T) rows[0].getKey();
            setValue(uniqueValue);
          }
        }
      }
    };
    if (background) {
      callBrowseLookupInBackground(ISmartField.BROWSE_ALL_TEXT, 2, fetcher);
    }
    else {
      fetcher.dataFetched(callBrowseLookup(ISmartField.BROWSE_ALL_TEXT, 2), null);
    }
  }

  @Override
  public ISmartFieldProposalForm getProposalForm() {
    return (ISmartFieldProposalForm) propertySupport.getProperty(PROP_PROPOSAL_FORM);
  }

  private void registerProposalFormInternal(ISmartFieldProposalForm form) {
    ISmartFieldProposalForm oldForm = getProposalForm();
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

  private void unregisterProposalFormInternal(ISmartFieldProposalForm form) {
    if (form != null) {
      ISmartFieldProposalForm oldForm = getProposalForm();
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
  public ISmartFieldProposalFormProvider getProposalFormProvider() {
    return m_proposalFormProvider;
  }

  @Override
  public void setProposalFormProvider(ISmartFieldProposalFormProvider provider) {
    m_proposalFormProvider = provider;
  }

  public LookupRow getCurrentLookupRow() {
    return m_currentLookupRow;
  }

  /**
   * Returns the smartfield's proposal form with the use of a {@link ISmartFieldProposalFormProvider}.
   * <p>
   * To provide a custom proposal form create a custom proposal form provider and inject it with
   * {@link #createProposalFormProvider()} or {@link #setProposalFormProvider()}.
   * </p>
   * 
   * @return {@link#ISmartFieldProposalForm}
   * @throws ProcessingException
   */
  protected ISmartFieldProposalForm createProposalForm() throws ProcessingException {
    ISmartFieldProposalFormProvider proposalFormProvider = getProposalFormProvider();
    if (proposalFormProvider == null) {
      return null;
    }

    return proposalFormProvider.createProposalForm(this);
  }

  protected ISmartFieldProposalFormProvider createProposalFormProvider() {
    return new DefaultSmartFieldProposalFormProvider();
  }

  @Override
  protected T parseValueInternal(String text) throws ProcessingException {
    if (text != null && text.length() == 0) {
      text = null;
    }
    ISmartFieldProposalForm smartForm = getProposalForm();
    LookupRow acceptedProposalRow = null;
    if (smartForm != null && StringUtility.equalsIgnoreNewLines(smartForm.getSearchText(), text)) {
      acceptedProposalRow = smartForm.getAcceptedProposal();
    }
    //
    try {
      String oldText = getDisplayText();
      boolean parsingError = (getErrorStatus() instanceof ParsingFailedStatus);
      if (acceptedProposalRow == null && (!parsingError) && m_currentLookupRow != null && StringUtility.equalsIgnoreNewLines(StringUtility.emptyIfNull(text), StringUtility.emptyIfNull(oldText))) {
        // no change
        return getValue();
      }
      else {
        // changed
        if (acceptedProposalRow != null) {
          m_currentLookupRow = acceptedProposalRow;
          return TypeCastUtility.castValue(m_currentLookupRow.getKey(), getHolderType());
        }
        else if (text == null) {
          m_currentLookupRow = EMPTY_LOOKUP_ROW;
          return null;
        }
        else {
          if (smartForm == null) {
            smartForm = createProposalForm();
            smartForm.setSearchText(text);
            smartForm.startForm();
            smartForm.update(false, true);
          }
          else {
            smartForm.setSearchText(text);
            smartForm.update(false, true);
          }
          acceptedProposalRow = smartForm.getAcceptedProposal();
          if (acceptedProposalRow != null) {
            m_currentLookupRow = acceptedProposalRow;
            return TypeCastUtility.castValue(m_currentLookupRow.getKey(), getHolderType());
          }
          else {
            // no match possible and proposal is inactive; reject change
            registerProposalFormInternal(smartForm);
            smartForm = null;// prevent close in finally
            if (isAllowCustomText()) {
              m_currentLookupRow = new LookupRow(text, text);
              return TypeCastUtility.castValue(m_currentLookupRow.getKey(), getHolderType());
            }
            else {
              throw new VetoException(ScoutTexts.get("SmartFieldCannotComplete", text));
            }
          }
        }
      }
    }
    finally {
      unregisterProposalFormInternal(smartForm);
    }
  }

  @Override
  protected final T execValidateValue(T rawValue) throws ProcessingException {
    return rawValue;
  }

  @Override
  protected T validateValueInternal(T rawKey) throws ProcessingException {
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
  protected String formatValueInternal(T validKey) {
    // check if current context row is still valid
    if (m_currentLookupRow != null) {
      if (validKey == m_currentLookupRow.getKey() || (validKey != null && validKey.equals(m_currentLookupRow.getKey()))) {
        // still valid
      }
      else {
        m_currentLookupRow = null;
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
    if (m_currentLookupRow == null) {
      if (validKey == null) {
        m_currentLookupRow = EMPTY_LOOKUP_ROW;
      }
    }
    if (m_currentLookupRow != null) {
      installLookupRowContext(m_currentLookupRow);
      String text = m_currentLookupRow.getText();
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
            LookupRow[] rows = callKeyLookup(validKey);
            if (rows != null && rows.length > 0) {
              installLookupRowContext(rows[0]);
            }
            else {
              installLookupRowContext(EMPTY_LOOKUP_ROW);
            }
          }
          else {
            // enqueue LookupRow fetcher
            // this will lateron call installLookupRowContext()
            LookupCall call = (LookupCall) getLookupCall().clone();
            prepareKeyLookup(call, validKey);
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
        LookupRow[] rows = callKeyLookup(getValue());
        if (rows != null && rows.length > 0) {
          installLookupRowContext(rows[0]);
        }
      }
      catch (ProcessingException e) {
        SERVICES.getService(IExceptionHandlerService.class).handleException(e);
      }
    }
  }

  @Override
  public void revertValue() {
    setValue(getValue());
  }

  @Override
  @SuppressWarnings("unchecked")
  public void acceptProposal(LookupRow row) {
    m_currentLookupRow = row;
    if (isAllowCustomText()) {
      setValue((T) row.getText());
    }
    else {
      setValue((T) row.getKey());
    }
  }

  @Override
  public ISmartFieldUIFacade getUIFacade() {
    return m_uiFacade;
  }

  /**
   * Notice: This method is called from a worker originated outside the scout
   * thread (sync into scout model thread)
   */
  private void installLookupRowContext(LookupRow row) {
    try {
      m_installingRowContext = true;
      m_currentLookupRow = row;
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
  public void applyLazyStyles() {
    // override: ensure that (async loading) lookup context has been set
    if (m_currentGetLookupRowByKeyJob != null) {
      if (m_currentGetLookupRowByKeyJob.getClientSession() == ClientSyncJob.getCurrentSession() && ClientSyncJob.isSyncClientJob()) {
        m_currentGetLookupRowByKeyJob.runNow(new NullProgressMonitor());
      }
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
  public void prepareKeyLookup(LookupCall call, T key) throws ProcessingException {
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
  public void prepareTextLookup(LookupCall call, String text) throws ProcessingException {
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
  public void prepareBrowseLookup(LookupCall call, String browseHint, TriState activeState) throws ProcessingException {
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
  public void prepareRecLookup(LookupCall call, T parentKey, TriState activeState) throws ProcessingException {
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

  private void filterKeyLookup(LookupCall call, List<LookupRow> result) throws ProcessingException {
    execFilterLookupResult(call, result);
    execFilterKeyLookupResult(call, result);
    /*
     * ticket 79027
     */
    if (result.size() == 0) {
      if (isAllowCustomText()) {
        String key = "" + call.getKey();
        result.add(new LookupRow(key, key));
      }
    }
  }

  private void filterTextLookup(LookupCall call, List<LookupRow> result) throws ProcessingException {
    execFilterLookupResult(call, result);
    execFilterTextLookupResult(call, result);
  }

  private void filterBrowseLookup(LookupCall call, List<LookupRow> result) throws ProcessingException {
    execFilterLookupResult(call, result);
    execFilterBrowseLookupResult(call, result);
  }

  private void filterRecLookup(LookupCall call, List<LookupRow> result) throws ProcessingException {
    execFilterLookupResult(call, result);
    execFilterRecLookupResult(call, result);
  }

  @Override
  public LookupRow[] callKeyLookup(T key) throws ProcessingException {
    LookupRow[] data = null;
    LookupCall call = getLookupCall();
    if (call != null) {
      call = (LookupCall) call.clone();
      prepareKeyLookup(call, key);
      data = call.getDataByKey();
    }
    ArrayList<LookupRow> result;
    if (data != null) {
      result = new ArrayList<LookupRow>(Arrays.asList(data));
    }
    else {
      result = new ArrayList<LookupRow>();
    }
    filterKeyLookup(call, result);
    return cleanupResultList(result);
  }

  @Override
  public LookupRow[] callTextLookup(String text, int maxRowCount) throws ProcessingException {
    final Holder<LookupRow[]> rowsHolder = new Holder<LookupRow[]>(LookupRow[].class);
    final Holder<ProcessingException> failedHolder = new Holder<ProcessingException>(ProcessingException.class, new ProcessingException("callback was not invoked"));
    callTextLookupInternal(text, maxRowCount, new ILookupCallFetcher() {
      @Override
      public void dataFetched(LookupRow[] rows, ProcessingException failed) {
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
  public JobEx callTextLookupInBackground(String text, int maxRowCount, ILookupCallFetcher fetcher) {
    return callTextLookupInternal(text, maxRowCount, fetcher, true);
  }

  private JobEx callTextLookupInternal(String text, int maxRowCount, final ILookupCallFetcher fetcher, final boolean background) {
    final LookupCall call = (getLookupCall() != null ? (LookupCall) getLookupCall().clone() : null);
    final IClientSession session = ClientSyncJob.getCurrentSession();
    ILookupCallFetcher internalFetcher = new ILookupCallFetcher() {
      @Override
      public void dataFetched(final LookupRow[] rows, final ProcessingException failed) {
        ClientSyncJob scoutSyncJob = new ClientSyncJob("Smartfield text lookup", session) {
          @Override
          protected void runVoid(IProgressMonitor monitor) throws Throwable {
            if (failed == null) {
              ArrayList<LookupRow> result = new ArrayList<LookupRow>(Arrays.asList(rows));
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
      internalFetcher.dataFetched(new LookupRow[0], null);
    }
    return null;
  }

  @Override
  public LookupRow[] callBrowseLookup(String browseHint, int maxRowCount) throws ProcessingException {
    return callBrowseLookup(browseHint, maxRowCount, isActiveFilterEnabled() ? getActiveFilter() : TriState.TRUE);
  }

  @Override
  public LookupRow[] callBrowseLookup(String browseHint, int maxRowCount, TriState activeState) throws ProcessingException {
    final Holder<LookupRow[]> rowsHolder = new Holder<LookupRow[]>(LookupRow[].class);
    final Holder<ProcessingException> failedHolder = new Holder<ProcessingException>(ProcessingException.class, new ProcessingException("callback was not invoked"));
    callBrowseLookupInternal(browseHint, maxRowCount, activeState, new ILookupCallFetcher() {
      @Override
      public void dataFetched(LookupRow[] rows, ProcessingException failed) {
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
  public JobEx callBrowseLookupInBackground(String browseHint, int maxRowCount, ILookupCallFetcher fetcher) {
    return callBrowseLookupInBackground(browseHint, maxRowCount, isActiveFilterEnabled() ? getActiveFilter() : TriState.TRUE, fetcher);
  }

  @Override
  public JobEx callBrowseLookupInBackground(String browseHint, int maxRowCount, TriState activeState, ILookupCallFetcher fetcher) {
    return callBrowseLookupInternal(browseHint, maxRowCount, activeState, fetcher, true);
  }

  private JobEx callBrowseLookupInternal(String browseHint, int maxRowCount, TriState activeState, final ILookupCallFetcher fetcher, final boolean background) {
    final LookupCall call = (getLookupCall() != null ? (LookupCall) getLookupCall().clone() : null);
    final IClientSession session = ClientSyncJob.getCurrentSession();
    ILookupCallFetcher internalFetcher = new ILookupCallFetcher() {
      @Override
      public void dataFetched(final LookupRow[] rows, final ProcessingException failed) {
        ClientSyncJob scoutSyncJob = new ClientSyncJob("Smartfield browse lookup", session) {
          @Override
          protected void runVoid(IProgressMonitor monitor) throws Throwable {
            if (failed == null) {
              ArrayList<LookupRow> result = new ArrayList<LookupRow>(Arrays.asList(rows));
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
      internalFetcher.dataFetched(new LookupRow[0], null);
    }
    return null;
  }

  @Override
  public LookupRow[] callSubTreeLookup(T parentKey) throws ProcessingException {
    return callSubTreeLookup(parentKey, isActiveFilterEnabled() ? getActiveFilter() : TriState.TRUE);
  }

  @Override
  public LookupRow[] callSubTreeLookup(T parentKey, TriState activeState) throws ProcessingException {
    LookupRow[] data = null;
    LookupCall call = getLookupCall();
    if (call != null) {
      call = (LookupCall) call.clone();
      call.setMaxRowCount(getBrowseMaxRowCount());
      prepareRecLookup(call, parentKey, activeState);
      data = call.getDataByRec();
    }
    ArrayList<LookupRow> result;
    if (data != null) {
      result = new ArrayList<LookupRow>(Arrays.asList(data));
    }
    else {
      result = new ArrayList<LookupRow>(0);
    }
    filterRecLookup(call, result);
    return cleanupResultList(result);
  }

  private LookupRow[] cleanupResultList(List<LookupRow> list) {
    int len = 0;
    for (LookupRow r : list) {
      if (r != null) {
        len++;
      }
    }
    LookupRow[] a = new LookupRow[len];
    int index = 0;
    for (LookupRow r : list) {
      if (r != null) {
        a[index] = r;
        index++;
      }
    }
    return a;
  }

  /*
   * inner classes
   */

  private class P_GetLookupRowByKeyJob extends ClientSyncJob {
    private LookupRow[] m_rows;
    private final ClientAsyncJob m_backgroundJob;

    public P_GetLookupRowByKeyJob(final LookupCall call) {
      super("Fetch smartfield data for " + getLabel(), getCurrentSession());
      // immediately start a thread that fetches data async
      m_backgroundJob = new ClientAsyncJob("Fetch smartfield data", ClientSyncJob.getCurrentSession()) {
        @Override
        protected void runVoid(IProgressMonitor monitor) throws Throwable {
          ArrayList<LookupRow> result = new ArrayList<LookupRow>(Arrays.asList(call.getDataByKey()));
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
            if (m_rows != null && m_rows.length > 0) {
              installLookupRowContext(m_rows[0]);
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
  }// end private class

  private class P_ProposalFormListener implements FormListener {
    @Override
    public void formChanged(FormEvent e) throws ProcessingException {
      switch (e.getType()) {
        case FormEvent.TYPE_CLOSED: {
          if (getProposalForm() == e.getForm()) {
            ISmartFieldProposalForm f = (ISmartFieldProposalForm) e.getForm();
            if (f.getCloseSystemType() == IButton.SYSTEM_TYPE_OK) {
              LookupRow row = f.getAcceptedProposal();
              if (row != null) {
                acceptProposal(row);
              }
            }
            else {
              if (!isAllowCustomText()) {
                revertValue();
              }
            }
            registerProposalFormInternal(null);
          }
          break;
        }
      }
    }
  }// end private class

  private class P_UIFacade implements ISmartFieldUIFacade {
    private Map<ICell, LookupRow> m_validProposals;

    @Override
    public IMenu[] firePopupFromUI() {
      T smartValue = getValue();
      ArrayList<IMenu> filteredMenus = new ArrayList<IMenu>();
      for (IMenu m : getMenus()) {
        IMenu validMenu = null;
        if ((!m.isInheritAccessibility()) || isEnabled()) {
          if (m.isEmptySpaceAction()) {
            validMenu = m;
          }
          else if (m.isSingleSelectionAction()) {
            if (smartValue != null) {
              validMenu = m;
            }
          }
        }
        //
        if (validMenu != null) {
          validMenu.prepareAction();
          if (validMenu.isVisible()) {
            filteredMenus.add(validMenu);
          }
        }
      }
      return filteredMenus.toArray(new IMenu[0]);
    }

    @Override
    public void openProposalFromUI(String newText, boolean selectCurrentValue) {
      if (newText == null) {
        newText = BROWSE_ALL_TEXT;
      }
      try {
        ISmartFieldProposalForm smartForm = getProposalForm();
        if (smartForm == null) {
          setActiveFilter(TriState.TRUE);
          smartForm = createProposalForm();
          smartForm.setSearchText(newText);
          smartForm.startForm();
          if (smartForm.isFormOpen()) {
            smartForm.update(selectCurrentValue, false);
            registerProposalFormInternal(smartForm);
          }
        }
        else {
          if (!StringUtility.equalsIgnoreNewLines(smartForm.getSearchText(), newText)) {
            smartForm.setSearchText(newText);
            smartForm.update(false, false);
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
        ISmartFieldProposalForm smartForm = getProposalForm();
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
    public boolean setTextFromUI(String text) {
      String currentValidText = (m_currentLookupRow != null ? m_currentLookupRow.getText() : null);
      ISmartFieldProposalForm smartForm = getProposalForm();
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
              if (isAllowCustomText()) {
                return parseValue(text);
              }
              else {
                // ...and the current value is incomplete -> force proposal
                // selection
                smartForm.forceProposalSelection();
                return false;
              }
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
    public void unregisterProposalFormFromUI(ISmartFieldProposalForm form) {
      unregisterProposalFormInternal(form);
    }
  }

}
