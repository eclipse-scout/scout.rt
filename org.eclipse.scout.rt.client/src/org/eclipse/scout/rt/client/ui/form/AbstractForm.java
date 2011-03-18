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
package org.eclipse.scout.rt.client.ui.form;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.Serializable;
import java.security.Permission;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.commons.BeanUtility;
import org.eclipse.scout.commons.ConfigurationUtility;
import org.eclipse.scout.commons.EventListenerList;
import org.eclipse.scout.commons.OptimisticLock;
import org.eclipse.scout.commons.StoppableThread;
import org.eclipse.scout.commons.annotations.ConfigOperation;
import org.eclipse.scout.commons.annotations.ConfigProperty;
import org.eclipse.scout.commons.annotations.ConfigPropertyValue;
import org.eclipse.scout.commons.annotations.FormData;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.annotations.FormData.SdkCommand;
import org.eclipse.scout.commons.beans.AbstractPropertyObserver;
import org.eclipse.scout.commons.beans.FastPropertyDescriptor;
import org.eclipse.scout.commons.beans.IPropertyFilter;
import org.eclipse.scout.commons.exception.IProcessingStatus;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.exception.VetoException;
import org.eclipse.scout.commons.holders.Holder;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.commons.xmlparser.SimpleXmlElement;
import org.eclipse.scout.rt.client.BlockingCondition;
import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.services.common.search.ISearchFilterService;
import org.eclipse.scout.rt.client.ui.DataChangeListener;
import org.eclipse.scout.rt.client.ui.WeakDataChangeListener;
import org.eclipse.scout.rt.client.ui.action.keystroke.IKeyStroke;
import org.eclipse.scout.rt.client.ui.basic.filechooser.FileChooser;
import org.eclipse.scout.rt.client.ui.desktop.AbstractDesktop;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.IValueField;
import org.eclipse.scout.rt.client.ui.form.fields.button.ButtonEvent;
import org.eclipse.scout.rt.client.ui.form.fields.button.ButtonListener;
import org.eclipse.scout.rt.client.ui.form.fields.button.IButton;
import org.eclipse.scout.rt.client.ui.form.fields.composer.IComposerField;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.IGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.tabbox.ITabBox;
import org.eclipse.scout.rt.client.ui.form.fields.wrappedform.IWrappedFormField;
import org.eclipse.scout.rt.client.ui.form.internal.FindFieldByFormDataIdVisitor;
import org.eclipse.scout.rt.client.ui.form.internal.FindFieldBySimpleClassNameVisitor;
import org.eclipse.scout.rt.client.ui.form.internal.FormDataPropertyFilter;
import org.eclipse.scout.rt.client.ui.messagebox.IMessageBox;
import org.eclipse.scout.rt.client.ui.messagebox.MessageBox;
import org.eclipse.scout.rt.client.ui.profiler.DesktopProfiler;
import org.eclipse.scout.rt.client.ui.wizard.IWizard;
import org.eclipse.scout.rt.client.ui.wizard.IWizardStep;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.shared.data.form.AbstractFormData;
import org.eclipse.scout.rt.shared.data.form.fields.AbstractFormFieldData;
import org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData;
import org.eclipse.scout.rt.shared.data.form.fields.tablefield.AbstractTableFieldData;
import org.eclipse.scout.rt.shared.data.form.properties.AbstractPropertyData;
import org.eclipse.scout.rt.shared.services.common.exceptionhandler.IExceptionHandlerService;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;
import org.eclipse.scout.rt.shared.services.common.security.IAccessControlService;
import org.eclipse.scout.service.SERVICES;

@FormData(value = AbstractFormData.class, sdkCommand = SdkCommand.USE)
public abstract class AbstractForm extends AbstractPropertyObserver implements IForm {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractForm.class);

  private boolean m_initialized;
  private final EventListenerList m_listenerList = new EventListenerList();
  private IFormUIFacade m_uiFacade;
  private IWizardStep m_wizardStep;
  private boolean m_modal;// no property, is fixed
  private boolean m_cacheBounds; // no property is fixed
  private boolean m_askIfNeedSave;
  private boolean m_buttonsArmed;
  private boolean m_closeTimerArmed;
  private boolean m_formStored;
  private boolean m_formLoading;
  private final BlockingCondition m_blockingCondition;
  private boolean m_autoRegisterInDesktopOnStart;
  private int m_displayHint;// no property, is fixed
  private String m_displayViewId;// no property, is fixed
  private boolean m_displayHintLocked = false;// no property, is fixed
  private int m_closeType = IButton.SYSTEM_TYPE_NONE;
  private String m_basicTitle;
  private String m_subTitle;
  private String m_cancelVerificationText;
  private File m_lastXmlFileForStorage;
  private IGroupBox m_mainBox;
  private IWrappedFormField m_wrappedFormField;
  private P_SystemButtonListener m_systemButtonListener;
  private final OptimisticLock m_systemButtonListenerLock;
  private IFormHandler m_handler;
  // access control
  private boolean m_enabledGranted;
  private boolean m_visibleGranted;
  // search
  private SearchFilter m_searchFilter;

  // current timers
  private P_CloseTimer m_scoutCloseTimer;
  private HashMap<String, P_Timer> m_scoutTimerMap;
  private String m_iconId;
  private DataChangeListener m_internalDataChangeListener;

  public AbstractForm() throws ProcessingException {
    this(true);
  }

  public AbstractForm(boolean callInitializer) throws ProcessingException {
    if (DesktopProfiler.getInstance().isEnabled()) {
      DesktopProfiler.getInstance().registerForm(this);
    }
    m_enabledGranted = true;
    m_visibleGranted = true;
    m_formLoading = true;
    m_blockingCondition = new BlockingCondition(false);
    m_systemButtonListenerLock = new OptimisticLock();
    if (callInitializer) {
      callInitializer();
    }
  }

  protected void callInitializer() throws ProcessingException {
    if (!m_initialized) {
      initConfig();
      postInitConfig();
      m_initialized = true;
    }
  }

  /*
   * Configuration
   */
  @ConfigProperty(ConfigProperty.TEXT)
  @Order(10)
  @ConfigPropertyValue("null")
  protected String getConfiguredTitle() {
    return null;
  }

  @ConfigProperty(ConfigProperty.INTEGER)
  @Order(20)
  @ConfigPropertyValue("0")
  protected int/* seconds */getConfiguredCloseTimer() {
    return 0;
  }

  @ConfigProperty(ConfigProperty.INTEGER)
  @Order(40)
  @ConfigPropertyValue("0")
  protected int/* seconds */getConfiguredCustomTimer() {
    return 0;
  }

  @ConfigProperty(ConfigProperty.STRING)
  @Order(60)
  @ConfigPropertyValue("null")
  protected String getConfiguredDoc() {
    return null;
  }

  @ConfigProperty(ConfigProperty.TEXT)
  @Order(90)
  @ConfigPropertyValue("ScoutTexts.get(\"FormSaveChangesQuestion\")")
  protected String getConfiguredCancelVerificationText() {
    return ScoutTexts.get("FormSaveChangesQuestion");
  }

  @ConfigProperty(ConfigProperty.FORM_DISPLAY_HINT)
  @Order(100)
  @ConfigPropertyValue("DISPLAY_HINT_DIALOG")
  protected int getConfiguredDisplayHint() {
    return DISPLAY_HINT_DIALOG;
  }

  @ConfigProperty(ConfigProperty.FORM_VIEW_ID)
  @Order(105)
  @ConfigPropertyValue("null")
  protected String getConfiguredDisplayViewId() {
    return null;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(108)
  @ConfigPropertyValue("false")
  protected boolean getConfiguredMinimizeEnabled() {
    return false;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(109)
  @ConfigPropertyValue("false")
  protected boolean getConfiguredMaximizeEnabled() {
    return false;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(110)
  @ConfigPropertyValue("false")
  protected boolean getConfiguredMinimized() {
    return false;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(112)
  @ConfigPropertyValue("false")
  protected boolean getConfiguredMaximized() {
    return false;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(120)
  @ConfigPropertyValue("true")
  protected boolean getConfiguredModal() {
    return true;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(140)
  @ConfigPropertyValue("false")
  protected boolean getConfiguredCacheBounds() {
    return false;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(150)
  @ConfigPropertyValue("true")
  protected boolean getConfiguredAskIfNeedSave() {
    return true;
  }

  @ConfigProperty(ConfigProperty.ICON_ID)
  @Order(160)
  @ConfigPropertyValue("null")
  protected String getConfiguredIconId() {
    return null;
  }

  /**
   * This method is called to get an exclusive key of the form. The key is used
   * to open the same form with the same handler only once. Obviously this
   * behavior can only be used for view forms.
   * 
   * @see AbstractDesktop#getSimilarViewForms(IForm)
   * @return null for exclusive form behavior an exclusive key to ensure similar
   *         handling.
   * @throws ProcessingException
   */
  public Object computeExclusiveKey() throws ProcessingException {
    return null;
  }

  /**
   * Initialize the form and all of its fields.
   * By default any of the #start* methods of the form call this method
   * <p>
   * This method is called in the process of the initialization. The UI is not ready yet.
   * 
   * @throws ProcessingException
   */
  @ConfigOperation
  @Order(10)
  protected void execInitForm() throws ProcessingException {
  }

  /**
   * This method is called when UI is ready.
   * 
   * @throws ProcessingException
   */
  @ConfigOperation
  @Order(11)
  protected void execFormActivated() throws ProcessingException {
  }

  /**
   * see {@link IDesktop#dataChanged(Object...)}
   */
  @ConfigOperation
  @Order(13)
  protected void execDataChanged(Object... dataTypes) throws ProcessingException {
  }

  /**
   * This method is called in order to check field validity.<br>
   * This method is called before {@link IFormHandler#execCheckFields()} and
   * before the form is validated and stored.<br>
   * After this method, the form is checking fields itself and displaying a
   * dialog with missing and invalid fields.
   * 
   * @return true when this check is done and further checks can continue, false
   *         to silently cancel the current process
   * @throws ProcessingException
   *           to cancel the current process with error handling and user
   *           notification such as a dialog
   */
  @ConfigOperation
  @Order(13)
  protected boolean execCheckFields() throws ProcessingException {
    return true;
  }

  /**
   * This method is called in order to update derived states like button
   * enablings.<br>
   * This method is called before {@link IFormHandler#execValidate()} and before
   * the form is stored.
   * 
   * @return true when validate is successful, false to silently cancel the
   *         current process
   * @throws ProcessingException
   *           to cancel the current process with error handling and user
   *           notification such as a dialog
   */
  @ConfigOperation
  @Order(14)
  protected boolean execValidate() throws ProcessingException {
    return true;
  }

  /**
   * This method is called in order to update pages on the desktop after the
   * form stored data.<br>
   * This method is called after {@link IFormHandler#execStore()}.
   */
  @ConfigOperation
  @Order(16)
  protected void execStored() throws ProcessingException {
  }

  /**
   * @throws ProcessingException
   *           / {@link VetoException} if the exception should produce further
   *           info messages (default)
   */
  @ConfigOperation
  @Order(17)
  protected void execOnVetoException(VetoException e, int code) throws ProcessingException {
    throw e;
  }

  /**
   * @param kill
   *          true if a widget close icon (normally the X on the titlebar) was
   *          pressed or ESC was pressed
   * @param enabledButtonSystemTypes
   *          set of all {@link IButton#SYSTEM_TYPE_*} of all enabled and
   *          visible buttons of this form
   */
  @ConfigOperation
  @Order(18)
  protected void execOnCloseRequest(boolean kill, final HashSet<Integer> enabledButtonSystemTypes) throws ProcessingException {
    if (enabledButtonSystemTypes.contains(IButton.SYSTEM_TYPE_CLOSE)) {
      doClose();
    }
    else if (enabledButtonSystemTypes.contains(IButton.SYSTEM_TYPE_CANCEL)) {
      doCancel();
    }
    else if (!isAskIfNeedSave()) {
      doClose();
    }
    else {
      LOG.warn("Trying to close a form (" + getClass().getName() + " - " + getTitle() + ") with no close button! override getConfiguredAskIfNeedSave() to false to make this form is unsaveable.");
    }
  }

  @ConfigOperation
  @Order(19)
  protected void execDisposeForm() throws ProcessingException {
  }

  @ConfigOperation
  @Order(20)
  protected void execCloseTimer() throws ProcessingException {
    doClose();
  }

  @ConfigOperation
  @Order(30)
  protected void execInactivityTimer() throws ProcessingException {
    doClose();
  }

  @ConfigOperation
  @Order(40)
  protected void execTimer(String timerId) throws ProcessingException {
    LOG.info("execTimer " + timerId);
  }

  /**
   * add verbose information to the search filter
   */
  @ConfigOperation
  @Order(50)
  protected void execAddSearchTerms(SearchFilter search) {
  }

  private Class<? extends IKeyStroke>[] getConfiguredKeyStrokes() {
    Class[] dca = ConfigurationUtility.getDeclaredPublicClasses(getClass());
    return ConfigurationUtility.filterClasses(dca, IKeyStroke.class);
  }

  private Class<? extends IGroupBox> getConfiguredMainBox() {
    Class[] dca = ConfigurationUtility.getDeclaredPublicClasses(getClass());
    return ConfigurationUtility.filterClass(dca, IGroupBox.class);
  }

  protected void initConfig() throws ProcessingException {
    m_uiFacade = new P_UIFacade();
    m_scoutTimerMap = new HashMap<String, P_Timer>();
    m_autoRegisterInDesktopOnStart = true;
    // add mainbox if getter returns null
    IGroupBox rootBox = getRootGroupBox();
    if (rootBox == null) {
      Class<? extends IGroupBox> mainBoxClass = getConfiguredMainBox();
      try {
        m_mainBox = ConfigurationUtility.newInnerInstance(this, mainBoxClass);
      }
      catch (Throwable t) {
        throw new ProcessingException("mainBox: " + ((mainBoxClass == null) ? "not defined." : mainBoxClass.getName()), t);
      }
      rootBox = getRootGroupBox();
    }
    rootBox.setFormInternal(this);
    rootBox.setMainBox(true);
    rootBox.updateKeyStrokes();
    //
    if (getConfiguredCloseTimer() > 0) {
      setCloseTimer(getConfiguredCloseTimer());
    }
    if (getConfiguredCustomTimer() > 0) {
      setTimer("custom", getConfiguredCustomTimer());
    }
    setModal(getConfiguredModal());
    setDisplayHint(getConfiguredDisplayHint());
    setDisplayViewId(getConfiguredDisplayViewId());
    if (getConfiguredCancelVerificationText() != null) {
      setCancelVerificationText(getConfiguredCancelVerificationText());
    }
    if (getConfiguredTitle() != null) {
      setTitle(getConfiguredTitle());
    }
    setMinimizeEnabled(getConfiguredMinimizeEnabled());
    setMaximizeEnabled(getConfiguredMaximizeEnabled());
    setMinimized(getConfiguredMinimized());
    setMaximized(getConfiguredMaximized());
    setCacheBounds(getConfiguredCacheBounds());
    setAskIfNeedSave(getConfiguredAskIfNeedSave());
    setIconId(getConfiguredIconId());

    // visit all system buttons and attach observer
    m_systemButtonListener = new P_SystemButtonListener();// is auto-detaching
    IFormFieldVisitor v2 = new IFormFieldVisitor() {
      public boolean visitField(IFormField field, int level, int fieldIndex) {
        if (field instanceof IButton) {
          if (((IButton) field).getSystemType() != IButton.SYSTEM_TYPE_NONE) {
            ((IButton) field).addButtonListener(m_systemButtonListener);
          }
        }
        return true;
      }
    };
    visitFields(v2);
    getRootGroupBox().addPropertyChangeListener(new P_MainBoxPropertyChangeProxy());
    setButtonsArmed(true);
  }

  public void setEnabledPermission(Permission p) {
    boolean b;
    if (p != null) {
      b = SERVICES.getService(IAccessControlService.class).checkPermission(p);
    }
    else {
      b = true;
    }
    setEnabledGranted(b);
  }

  public boolean isEnabledGranted() {
    return m_enabledGranted;
  }

  public void setEnabledGranted(boolean b) {
    m_enabledGranted = b;
    IGroupBox box = getRootGroupBox();
    if (box != null) {
      box.setEnabledGranted(b);
    }
  }

  public void setVisiblePermission(Permission p) {
    boolean b;
    if (p != null) {
      b = SERVICES.getService(IAccessControlService.class).checkPermission(p);
    }
    else {
      b = true;
    }
    setVisibleGranted(b);
  }

  public boolean isVisibleGranted() {
    return m_visibleGranted;
  }

  public void setVisibleGranted(boolean b) {
    m_visibleGranted = b;
    IGroupBox box = getRootGroupBox();
    if (box != null) {
      box.setVisibleGranted(b);
    }
  }

  public String getIconId() {
    return propertySupport.getPropertyString(PROP_ICON_ID);
  }

  public void setIconId(String iconId) {
    propertySupport.setPropertyString(PROP_ICON_ID, iconId);
  }

  public String getPerspectiveId() {
    return propertySupport.getPropertyString(PROP_PERSPECTIVE_ID);
  }

  public void setPerspectiveId(String perspectiveId) {
    propertySupport.setPropertyString(PROP_PERSPECTIVE_ID, perspectiveId);
  }

  /**
   * Register a {@link DataChangeListener} on the desktop for these dataTypes<br>
   * Example:
   * 
   * <pre>
   * registerDataChangeListener(CRMEnum.Company, CRMEnum.Project, CRMEnum.Task);
   * </pre>
   */
  public void registerDataChangeListener(Object... dataTypes) {
    if (m_internalDataChangeListener == null) {
      m_internalDataChangeListener = new WeakDataChangeListener() {
        public void dataChanged(Object... innerDataTypes) throws ProcessingException {
          execDataChanged(innerDataTypes);
        }
      };
    }
    IDesktop desktop = getDesktop();
    if (desktop == null) {
      desktop = ClientSyncJob.getCurrentSession().getVirtualDesktop();
    }
    desktop.addDataChangeListener(m_internalDataChangeListener, dataTypes);
  }

  /**
   * Unregister the {@link DataChangeListener} from the desktop for these
   * dataTypes<br>
   * Example:
   * 
   * <pre>
   * unregisterDataChangeListener(CRMEnum.Company, CRMEnum.Project, CRMEnum.Task);
   * </pre>
   */
  public void unregisterDataChangeListener(Object... dataTypes) {
    if (m_internalDataChangeListener != null) {
      getDesktop().removeDataChangeListener(m_internalDataChangeListener, dataTypes);
    }
  }

  protected IForm startInternalExclusive(IFormHandler handler) throws ProcessingException {
    if (m_blockingCondition.isBlocking()) {
      throw new ProcessingException("The form " + getFormId() + " has already been started");
    }
    for (IForm simCandidate : getDesktop().getSimilarViewForms(this)) {
      if (handler != null && simCandidate.getHandler() != null && handler.getClass().getName() == simCandidate.getHandler().getClass().getName()) {
        if (simCandidate.getHandler().isOpenExclusive() && handler.isOpenExclusive()) {
          getDesktop().ensureVisible(simCandidate);
          return simCandidate;
        }
      }
    }
    return startInternal(handler);
  }

  /**
   * This method is called from the implemented handler methods in a explicit
   * form subclass
   */
  protected IForm startInternal(final IFormHandler handler) throws ProcessingException {
    if (m_blockingCondition.isBlocking()) {
      throw new ProcessingException("The form " + getFormId() + " has already been started");
    }
    setHandler(handler);
    m_closeType = IButton.SYSTEM_TYPE_NONE;
    m_displayHintLocked = true;
    m_blockingCondition.setBlocking(true);
    try {
      // check if form was made invisible ( = access control denied access)
      if (!getRootGroupBox().isVisible()) {
        disposeFormInternal();
        return this;
      }
      initForm();
      // check if form was made invisible ( = access control denied access)
      if (!getRootGroupBox().isVisible()) {
        // make sure the form is storing since it is not showing
        disposeFormInternal();
        return this;
      }
      loadStateInternal();
      // check if form was made invisible ( = access control denied access)
      if (!isFormOpen()) {
        disposeFormInternal();
        return this;
      }
      if (!getRootGroupBox().isVisible()) {
        disposeFormInternal();
        return this;
      }
      if (getHandler().isGuiLess()) {
        // make sure the form is storing since it is not showing
        storeStateInternal();
        markSaved();
        doFinally();
        disposeFormInternal();
        return this;
      }
    }
    catch (ProcessingException e) {
      e.addContextMessage(AbstractForm.this.getClass().getSimpleName());
      SERVICES.getService(IExceptionHandlerService.class).handleException(e);
      disposeFormInternal();
      if (e instanceof VetoException) {
        try {
          execOnVetoException((VetoException) e, e.getStatus().getCode());
        }
        catch (ProcessingException ex) {
          // nop
        }
        return this;
      }
      else {
        throw e;
      }
    }
    catch (Throwable t) {
      disposeFormInternal();
      throw new ProcessingException("failed showing " + getTitle(), t);
    }
    // request a gui
    setButtonsArmed(true);
    setCloseTimerArmed(true);
    // register in desktop or wizard (legacy wizard only)
    if (isAutoAddRemoveOnDesktop()) {
      IDesktop desktop = getDesktop();
      if (desktop != null) {
        desktop.addForm(this);
      }
    }
    return this;
  }

  public void startWizardStep(IWizardStep wizardStep, Class<? extends IFormHandler> handlerType) throws ProcessingException {
    setAutoAddRemoveOnDesktop(false);
    IFormHandler formHandler = null;
    if (handlerType != null) {
      try {
        formHandler = ConfigurationUtility.newInnerInstance(this, handlerType);
      }
      catch (Exception e) {
        throw new ProcessingException("" + handlerType + " is not an internal form handler", e);
      }
    }
    m_wizardStep = wizardStep;
    setModal(false);
    setAskIfNeedSave(false);
    // hide top level process buttons with a system type
    for (IFormField f : getRootGroupBox().getFields()) {
      if (f instanceof IButton) {
        IButton b = (IButton) f;
        if (b.getSystemType() != IButton.SYSTEM_TYPE_NONE) {
          // hide
          b.setVisible(false);
          b.setVisibleGranted(false);
        }
      }
    }
    //
    // start
    startInternal(formHandler);
  }

  public void waitFor() throws ProcessingException {
    // check if the desktop is observing this process
    IDesktop desktop = ClientSyncJob.getCurrentSession().getDesktop();
    if (desktop == null || !desktop.isOpened()) {
      throw new ProcessingException("Cannot wait for " + getClass().getName() + ". There is no desktop or the desktop has not yet been opened in the ui", null, WAIT_FOR_ERROR_CODE);
    }
    // wait
    try {
      m_blockingCondition.waitFor();
    }
    catch (InterruptedException e) {
      throw new ProcessingException(ScoutTexts.get("UserInterrupted"), e);
    }
  }

  public void exportFormData(AbstractFormData target) throws ProcessingException {
    // locally declared form properties
    Map<String, Object> properties = BeanUtility.getProperties(this, AbstractForm.class, new FormDataPropertyFilter());
    BeanUtility.setProperties(target, properties, false, null);
    // all fields
    Map<Integer, Map<String/* qualified field id */, AbstractFormFieldData>> breathFirstMap = target.getAllFieldsRec();
    for (Map<String/* qualified field id */, AbstractFormFieldData> targetMap : breathFirstMap.values()) {
      for (Map.Entry<String, AbstractFormFieldData> e : targetMap.entrySet()) {
        String fieldQId = e.getKey();
        AbstractFormFieldData data = e.getValue();
        FindFieldByFormDataIdVisitor v = new FindFieldByFormDataIdVisitor(fieldQId);
        visitFields(v);
        IFormField f = v.getField();
        if (f != null) {
          // locally declared field properties
          properties = BeanUtility.getProperties(f, AbstractFormField.class, new FormDataPropertyFilter());
          BeanUtility.setProperties(data, properties, false, null);
          // field state
          f.exportFormFieldData(data);
        }
        else {
          LOG.warn("cannot find field data for '" + fieldQId + " in form " + getClass().getName() + "'");
        }
      }
    }
  }

  public void importFormData(AbstractFormData source) throws ProcessingException {
    importFormData(source, false, null);
  }

  public void importFormData(AbstractFormData source, boolean valueChangeTriggersEnabled, IPropertyFilter filter) throws ProcessingException {
    // locally declared form properties
    Map<String, Object> properties = BeanUtility.getProperties(source, AbstractFormData.class, new FormDataPropertyFilter());
    for (Iterator<String> it = properties.keySet().iterator(); it.hasNext();) {
      AbstractPropertyData pd = source.getPropertyById(it.next());
      if (pd != null && !pd.isValueSet()) {
        it.remove();
      }
    }
    BeanUtility.setProperties(this, properties, false, null);
    // sort fields, first non-slave fields, then slave fields in transitive
    // order
    LinkedList<IFormField> masterList = new LinkedList<IFormField>();
    LinkedList<IFormField> slaveList = new LinkedList<IFormField>();
    HashMap<IFormField, AbstractFormFieldData> dataMap = new HashMap<IFormField, AbstractFormFieldData>();
    // all fields
    Map<Integer, Map<String/* qualified field id */, AbstractFormFieldData>> breathFirstMap = source.getAllFieldsRec();
    for (Map<String/* qualified field id */, AbstractFormFieldData> sourceMap : breathFirstMap.values()) {
      for (Map.Entry<String, AbstractFormFieldData> e : sourceMap.entrySet()) {
        String fieldQId = e.getKey();
        AbstractFormFieldData data = e.getValue();
        FindFieldByFormDataIdVisitor v = new FindFieldByFormDataIdVisitor(fieldQId);
        visitFields(v);
        IFormField f = v.getField();
        if (f != null) {
          dataMap.put(f, data);
          if (f.getMasterField() != null) {
            int index = slaveList.indexOf(f.getMasterField());
            if (index >= 0) {
              slaveList.add(index + 1, f);
            }
            else {
              slaveList.add(0, f);
            }
          }
          else {
            masterList.add(f);
          }
        }
        else {
          LOG.warn("cannot find field data for '" + fieldQId + " in form " + getClass().getName() + "'");
        }
      }
    }
    for (IFormField f : masterList) {
      AbstractFormFieldData data = dataMap.get(f);
      // locally declared field properties
      Class stopClass;
      if (data instanceof AbstractTableFieldData) stopClass = AbstractTableFieldData.class;
      else if (data instanceof AbstractValueFieldData) stopClass = AbstractValueFieldData.class;
      else stopClass = AbstractFormFieldData.class;
      properties = BeanUtility.getProperties(data, stopClass, new FormDataPropertyFilter());
      for (Iterator<String> it = properties.keySet().iterator(); it.hasNext();) {
        AbstractPropertyData pd = data.getPropertyById(it.next());
        if (pd != null && !pd.isValueSet()) {
          it.remove();
        }
      }
      BeanUtility.setProperties(f, properties, false, null);
      // field state
      f.importFormFieldData(data, valueChangeTriggersEnabled);
    }
    for (IFormField f : slaveList) {
      AbstractFormFieldData data = dataMap.get(f);
      // locally declared field properties
      Class stopClass;
      if (data instanceof AbstractTableFieldData) stopClass = AbstractTableFieldData.class;
      else if (data instanceof AbstractValueFieldData) stopClass = AbstractValueFieldData.class;
      else stopClass = AbstractFormFieldData.class;
      properties = BeanUtility.getProperties(data, stopClass, new FormDataPropertyFilter());
      for (Iterator<String> it = properties.keySet().iterator(); it.hasNext();) {
        AbstractPropertyData pd = data.getPropertyById(it.next());
        if (pd != null && !pd.isValueSet()) {
          it.remove();
        }
      }
      BeanUtility.setProperties(f, properties, false, null);
      // field state
      f.importFormFieldData(data, valueChangeTriggersEnabled);
    }
  }

  public static String parseFormId(String className) {
    String s = className;
    int i = Math.max(s.lastIndexOf('$'), s.lastIndexOf('.'));
    s = s.substring(i + 1);
    return s;
  }

  public String getFormId() {
    return parseFormId(getClass().getName());
  }

  public IFormHandler getHandler() {
    return m_handler;
  }

  public void setHandler(IFormHandler handler) {
    if (handler != m_handler) {
      if (m_handler != null) {
        m_handler.setFormInternal(null);
      }
      if (handler == null) {
        handler = new NullFormHandler();
      }
      m_handler = handler;
      m_handler.setFormInternal(this);
    }
  }

  public IWizard getWizard() {
    return (getWizardStep() != null) ? getWizardStep().getWizard() : null;
  }

  public IWizardStep getWizardStep() {
    return m_wizardStep;
  }

  public IFormField getFocusOwner() {
    if (getDesktop() != null) {
      IFormField field = getDesktop().getFocusOwner();
      if (field != null) {
        IForm form = field.getForm();
        while (form != null) {
          if (form == this) {
            return field;
          }
          // next
          form = form.getOuterForm();
        }
      }
    }
    return null;
  }

  public IFormField[] getAllFields() {
    P_AbstractCollectingFieldVisitor<IFormField> v = new P_AbstractCollectingFieldVisitor<IFormField>() {
      public boolean visitField(IFormField field, int level, int fieldIndex) {
        collect(field);
        return true;
      }
    };
    visitFields(v);
    return v.getCollection().toArray(new IFormField[0]);
  }

  public boolean visitFields(IFormFieldVisitor visitor) {
    return getRootGroupBox().visitFields(visitor, 0);
  }

  /**
   * Convenience for ClientJob.getCurrentSession().getDesktop()
   */
  public IDesktop getDesktop() {
    return ClientSyncJob.getCurrentSession().getDesktop();
  }

  public final SearchFilter getSearchFilter() {
    if (m_searchFilter == null) {
      resetSearchFilter();
    }
    return m_searchFilter;
  }

  public final void setSearchFilter(SearchFilter searchFilter) {
    m_searchFilter = searchFilter;
  }

  /**
   * Alias for {@link #resetSearchFilter()}
   */
  public void rebuildSearchFilter() {
    resetSearchFilter();
  }

  public void resetSearchFilter() {
    if (m_searchFilter == null) {
      SearchFilter filter;
      ISearchFilterService sfs = SERVICES.getService(ISearchFilterService.class);
      if (sfs != null) {
        filter = sfs.createNewSearchFilter();
      }
      else {
        filter = new SearchFilter();
      }
      m_searchFilter = filter;
    }
    try {
      execResetSearchFilter(m_searchFilter);
    }
    catch (ProcessingException e) {
      SERVICES.getService(IExceptionHandlerService.class).handleException(e);
    }
    catch (Throwable t) {
      SERVICES.getService(IExceptionHandlerService.class).handleException(new ProcessingException("Unexpected", t));
    }
  }

  /**
   * Called when saving the form via {@link #resetSearchFilter()}.
   * <p>
   * This operation fills up the search filter and subclass override sets the formData property of the
   * {@link SearchFilter#setFormData(AbstractFormData)} and adds verbose texts with
   * {@link SearchFilter#addDisplayText(String)}
   * <p>
   * May call {@link #setSearchFilter(SearchFilter)}
   * 
   * @param searchFilter
   *          is never null
   */
  @ConfigOperation
  @Order(10)
  protected void execResetSearchFilter(final SearchFilter searchFilter) throws ProcessingException {
    searchFilter.clear();
    // add verbose field texts
    // do not use visitor, so children can block traversal on whole subtrees
    getRootGroupBox().applySearch(searchFilter);
    // add verbose form texts
    execAddSearchTerms(searchFilter);
    // override may add form data
  }

  public boolean isFormStored() {
    return m_formStored;
  }

  public void setFormStored(boolean b) {
    m_formStored = b;
  }

  public boolean isFormLoading() {
    return m_formLoading;
  }

  private void setFormLoading(boolean b) {
    m_formLoading = b;
  }

  /**
   * Mainbox getter
   */
  public IGroupBox getRootGroupBox() {
    return m_mainBox;
  }

  public IForm getOuterForm() {
    return m_wrappedFormField != null ? m_wrappedFormField.getForm() : null;
  }

  public IWrappedFormField getOuterFormField() {
    return m_wrappedFormField;
  }

  public void setWrapperFieldInternal(IWrappedFormField w) {
    m_wrappedFormField = w;
  }

  public IFormField getFieldById(final String id) {
    return getRootGroupBox().getFieldById(id);
  }

  public <T extends IFormField> T getFieldById(String id, Class<T> type) {
    return getRootGroupBox().getFieldById(id, type);
  }

  public <T extends IFormField> T getFieldByClass(Class<T> c) {
    return getRootGroupBox().getFieldByClass(c);
  }

  /**
   * override in subclasses to perform form initialization before handler starts
   */
  protected void postInitConfig() throws ProcessingException {
    FormUtility.postInitConfig(this);
    FormUtility.rebuildFieldGrid(this, true);
  }

  public void initForm() throws ProcessingException {
    // form
    initFormInternal();
    // fields
    FormUtility.initFormFields(this);
    // custom
    execInitForm();
  }

  protected void initFormInternal() throws ProcessingException {
  }

  public int getCloseSystemType() {
    return m_closeType;
  }

  public void setCloseSystemType(int type) {
    m_closeType = type;
  }

  /**
   * do not use or override this internal method
   */
  protected void loadStateInternal() throws ProcessingException {
    fireFormLoadBefore();
    if (!isFormOpen()) {
      return;
    }
    getHandler().onLoad();
    if (!isFormOpen()) {
      return;
    }
    fireFormLoadAfter();
    if (!isFormOpen()) {
      return;
    }
    // set all values to 'unchanged'
    markSaved();
    setFormLoading(false);
    getHandler().onPostLoad();
    if (!isFormOpen()) {
      return;
    }
    // if not visible mode, mark form changed
    if (getHandler().isGuiLess()) {
      touch();
    }
    fireFormLoadComplete();
  }

  /**
   * Store state of form, regardless of validity and completeness do not use or
   * override this internal method directly
   */
  protected void storeStateInternal() throws ProcessingException {
    if (!m_blockingCondition.isBlocking()) {
      // String
      // msg="The form "+getFormId()+" was disposed. No more actions are allowed on it.";
      String msg = ScoutTexts.get("FormDisposedMessage").replace("#1#", getTitle());
      LOG.error(msg);
      throw new VetoException(msg);
    }
    fireFormStoreBefore();
    m_formStored = true;
    try {
      rebuildSearchFilter();
      m_searchFilter.setCompleted(true);
      getHandler().onStore();
      execStored();
    }
    catch (ProcessingException e) {
      // clear search
      if (m_searchFilter != null) m_searchFilter.clear();
      // store was not successfully stored
      m_formStored = false;
      throwVetoExceptionInternal(e);
      // if exception was caught and suppressed, this form was after all successfully stored
      // normally this code is not reached since the exception will  be passed out
      m_formStored = true;
    }
    catch (Throwable t) {
      // clear search
      if (m_searchFilter != null) m_searchFilter.clear();
      throw new ProcessingException("form: " + getTitle(), t);
    }
    fireFormStoreAfter();
  }

  /**
   * do not use or override this internal method
   */
  protected void discardStateInternal() throws ProcessingException {
    fireFormDiscarded();
    getHandler().onDiscard();
  }

  public void setCloseTimer(int seconds) {
    if (seconds > 0) {
      setCloseTimerArmed(true);
      m_scoutCloseTimer = new P_CloseTimer(seconds);
      m_scoutCloseTimer.start();
    }
    else {
      removeCloseTimer();
    }
  }

  /**
   * do not use or override this internal method
   */
  protected void throwVetoExceptionInternal(ProcessingException e) throws ProcessingException {
    if (e instanceof VetoException) {
      if (!e.isConsumed()) {
        execOnVetoException((VetoException) e, e.getStatus().getCode());
        // if it was not re-thrown it is assumed to be consumed
        e.consume();
      }
    }
    throw e;
  }

  public void removeCloseTimer() {
    setCloseTimerArmed(false);
    m_scoutCloseTimer = null;
    setSubTitle(null);
  }

  public void validateForm() throws ProcessingException {
    if (!execCheckFields()) {
      VetoException veto = new VetoException("Validate " + getClass().getSimpleName());
      veto.consume();
      throw veto;
    }
    if (!getHandler().onCheckFields()) {
      VetoException veto = new VetoException("Validate " + getClass().getSimpleName());
      veto.consume();
      throw veto;
    }
    // check all fields that might be invalid
    final ArrayList<String> invalidTexts = new ArrayList<String>();
    final ArrayList<String> mandatoryTexts = new ArrayList<String>();
    P_AbstractCollectingFieldVisitor<IFormField> v = new P_AbstractCollectingFieldVisitor<IFormField>() {
      public boolean visitField(IFormField f, int level, int fieldIndex) {
        if (!f.isContentValid()) {
          if (f.getErrorStatus() != null) {
            invalidTexts.add(f.getFullyQualifiedLabel(": ") + ": " + f.getErrorStatus().getMessage());
          }
          else {
            mandatoryTexts.add(f.getFullyQualifiedLabel(": "));
          }
          if (getCollectionCount() == 0) {
            collect(f);
          }
        }
        return true;
      }
    };
    visitFields(v);
    if (v.getCollectionCount() > 0) {
      IFormField first = v.getCollection().get(0);
      if (LOG.isInfoEnabled()) {
        LOG.info("there are fields with errors");
      }
      StringBuffer buf = new StringBuffer();
      if (mandatoryTexts.size() > 0) {
        buf.append(ScoutTexts.get("FormEmptyMandatoryFieldsMessage") + "\n\n");
        for (Iterator it = mandatoryTexts.iterator(); it.hasNext();) {
          buf.append("- " + it.next() + "\n");
        }
        buf.append("\n");
      }
      if (invalidTexts.size() > 0) {
        buf.append(ScoutTexts.get("FormInvalidFieldsMessage") + "\n\n");
        for (Iterator it = invalidTexts.iterator(); it.hasNext();) {
          buf.append("- " + it.next() + "\n");
        }
      }
      String introText = ScoutTexts.get("FormIncompleteIntro");
      first.requestFocus();
      VetoException veto = new VetoException(introText, buf.toString());
      throw veto;
    }
    if (!execValidate()) {
      VetoException veto = new VetoException("Validate " + getClass().getSimpleName());
      veto.consume();
      throw veto;
    }
    if (!getHandler().onValidate()) {
      VetoException veto = new VetoException("Validate " + getClass().getSimpleName());
      veto.consume();
      throw veto;
    }
  }

  /**
   * attach a statement (mode) that is executed every interval
   * 
   * @since Build 195 09.02.2005, imo
   */
  public void setTimer(String timerId, int seconds) {
    removeTimer(timerId);
    if (seconds > 0) {
      P_Timer tim = new P_Timer(seconds, timerId);
      m_scoutTimerMap.put(timerId, tim);
      tim.start();
    }
  }

  /**
   * remove a statement (mode) that is executed every interval
   * 
   * @since Build 195 09.02.2005, imo
   */
  public void removeTimer(String timerId) {
    P_Timer tim = m_scoutTimerMap.remove(timerId);
    if (tim != null) {
      tim.setStopSignal();
    }
  }

  public void doClose() throws ProcessingException {
    if (!isFormOpen()) {
      return;
    }
    m_closeType = IButton.SYSTEM_TYPE_CLOSE;
    discardStateInternal();
    doFinally();
    disposeFormInternal();
  }

  public void doCancel() throws ProcessingException {
    if (!isFormOpen()) {
      return;
    }
    m_closeType = IButton.SYSTEM_TYPE_CANCEL;
    try {
      // ensure all fields have the right save-needed-state
      checkSaveNeeded();
      // find any fields that needs save
      P_AbstractCollectingFieldVisitor<IFormField> collector = new P_AbstractCollectingFieldVisitor<IFormField>() {
        public boolean visitField(IFormField field, int level, int fieldIndex) {
          if (field.isSaveNeeded()) {
            collect(field);
            return false;
          }
          else {
            return true;
          }
        }
      };
      visitFields(collector);
      if (collector.getCollectionCount() > 0 && isAskIfNeedSave()) {
        MessageBox messageBox = new MessageBox(
            null,
            getCancelVerificationText(),
            null,
            ScoutTexts.get("Yes"),
            ScoutTexts.get("No"),
            ScoutTexts.get("Cancel")
            );
        messageBox.setSeverity(IProcessingStatus.INFO);
        int result = messageBox.startMessageBox();
        if (result == IMessageBox.YES_OPTION) {
          doOk();
          return;
        }
        else if (result == IMessageBox.NO_OPTION) {
          doClose();
          return;
        }
        else {
          VetoException e = new VetoException(ScoutTexts.get("UserCancelledOperation"));
          e.consume();
          throw e;
        }
      }
      discardStateInternal();
      doFinally();
      disposeFormInternal();
    }
    catch (ProcessingException e) {
      m_closeType = IButton.SYSTEM_TYPE_NONE;
      throw e;
    }
  }

  public void doReset() {
    setFormLoading(true);
    // reset values
    P_AbstractCollectingFieldVisitor v = new P_AbstractCollectingFieldVisitor() {
      public boolean visitField(IFormField field, int level, int fieldIndex) {
        if (field instanceof IValueField) {
          IValueField f = (IValueField) field;
          f.resetValue();
        }
        else if (field instanceof IComposerField) {
          IComposerField f = (IComposerField) field;
          f.resetValue();
        }
        return true;
      }
    };
    visitFields(v);
    try {
      // init again
      initForm();
      // load again
      loadStateInternal();
    }
    catch (ProcessingException e) {
      e.addContextMessage(ScoutTexts.get("FormReset") + " " + getTitle());
      SERVICES.getService(IExceptionHandlerService.class).handleException(e);
    }
  }

  /**
   * Save data and close the form.
   * It will make this decision based on {@link #isSaveNeeded}.
   * Saving usually involves calling the <code>execStore</code> method
   * of the current form handler.
   * 
   * @see AbstractFormHandler#execStore()
   */
  public void doOk() throws ProcessingException {
    if (!isFormOpen()) {
      return;
    }
    try {
      m_closeType = IButton.SYSTEM_TYPE_NONE;
      checkSaveNeeded();
      validateForm();
      m_closeType = IButton.SYSTEM_TYPE_OK;
      if (isSaveNeeded()) {
        storeStateInternal();
        markSaved();
      }
      doFinally();
      disposeFormInternal();
    }
    catch (ProcessingException e) {
      m_closeType = IButton.SYSTEM_TYPE_NONE;
      throwVetoExceptionInternal(e);
    }
  }

  public void doSaveWithoutMarkerChange() throws ProcessingException {
    if (!isFormOpen()) {
      return;
    }
    try {
      m_closeType = IButton.SYSTEM_TYPE_NONE;
      // ensure all fields have the right save-needed-state
      checkSaveNeeded();
      validateForm();
      m_closeType = IButton.SYSTEM_TYPE_SAVE_WITHOUT_MARKER_CHANGE;
      storeStateInternal();
      // do not set to "markSaved"
    }
    catch (ProcessingException e) {
      m_closeType = IButton.SYSTEM_TYPE_NONE;
      throwVetoExceptionInternal(e);
    }
  }

  public void doSave() throws ProcessingException {
    if (!isFormOpen()) {
      return;
    }
    try {
      m_closeType = IButton.SYSTEM_TYPE_NONE;
      // ensure all fields have the right save-needed-state
      checkSaveNeeded();
      validateForm();
      m_closeType = IButton.SYSTEM_TYPE_SAVE;
      if (isSaveNeeded()) {
        storeStateInternal();
        markSaved();
      }
    }
    catch (ProcessingException e) {
      m_closeType = IButton.SYSTEM_TYPE_NONE;
      throwVetoExceptionInternal(e);
    }
  }

  public void setAllEnabled(final boolean b) {
    P_AbstractCollectingFieldVisitor v = new P_AbstractCollectingFieldVisitor() {
      public boolean visitField(IFormField f, int level, int fieldIndex) {
        boolean filteredB = b;
        /*
         * @since 3.0 all items are enabled/disabled. a dialog can still be
         * closed using the X in the window header if(f instanceof IButton){
         * IButton b=(IButton)f; if(b.isProcessButton()){
         * switch(b.getSystemType()){ case IButton.SYSTEM_TYPE_CLOSE: case
         * IButton.SYSTEM_TYPE_CANCEL:{ filteredB=true; break; } } } }
         */
        //
        f.setEnabled(filteredB);
        return true;
      }
    };
    visitFields(v);
  }

  public void doFinally() {
    try {
      getHandler().onFinally();
    }
    catch (ProcessingException se) {
      se.addContextMessage(ScoutTexts.get("FormFinally") + " " + getTitle());
      SERVICES.getService(IExceptionHandlerService.class).handleException(se);
    }
    catch (Throwable t) {
      ProcessingException e = new ProcessingException(ScoutTexts.get("FormFinally") + " " + getTitle(), t);
      SERVICES.getService(IExceptionHandlerService.class).handleException(e);
    }
  }

  public String getCancelVerificationText() {
    return m_cancelVerificationText;
  }

  public void setCancelVerificationText(String text) {
    m_cancelVerificationText = text;
  }

  public List<? extends IFormField> getInvalidFields() {
    // check all fields that might be invalid
    P_AbstractCollectingFieldVisitor<IFormField> v = new P_AbstractCollectingFieldVisitor<IFormField>() {
      public boolean visitField(IFormField f, int level, int fieldIndex) {
        if (!f.isContentValid()) {
          collect(f);
        }
        return true;
      }
    };
    visitFields(v);
    return v.getCollection();
  }

  public final void checkSaveNeeded() {
    // call checkSaveNeeded on all fields
    P_AbstractCollectingFieldVisitor<IFormField> v = new P_AbstractCollectingFieldVisitor<IFormField>() {
      public boolean visitField(IFormField f, int level, int fieldIndex) {
        if (f instanceof IFormField) {
          ((IFormField) f).checkSaveNeeded();
        }
        return true;
      }
    };
    visitFields(v);
  }

  private boolean/* ok */checkForVerifyingFields() {
    // check all fields that might be invalid
    P_AbstractCollectingFieldVisitor v = new P_AbstractCollectingFieldVisitor() {
      public boolean visitField(IFormField field, int level, int fieldIndex) {
        if (field instanceof IValueField) {
          IValueField f = (IValueField) field;
          if (f.isValueChanging() || f.isValueParsing()) {
            return false;
          }
        }
        return true;
      }
    };
    return visitFields(v);
  }

  private void closeFormInternal(boolean kill) {
    if (isFormOpen()) {
      try {
        // check if there is an active close, cancel or finish button
        final HashSet<Integer> enabledSystemTypes = new HashSet<Integer>();
        IFormFieldVisitor v = new IFormFieldVisitor() {
          public boolean visitField(IFormField field, int level, int fieldIndex) {
            if (field instanceof IButton) {
              IButton b = (IButton) field;
              if (b.isEnabled() && b.isVisible()) {
                enabledSystemTypes.add(b.getSystemType());
              }
            }
            return true;
          }
        };
        visitFields(v);
        execOnCloseRequest(kill, enabledSystemTypes);
      }
      catch (ProcessingException se) {
        se.addContextMessage(ScoutTexts.get("FormClosing") + " " + getTitle());
        SERVICES.getService(IExceptionHandlerService.class).handleException(se);
      }
      catch (Throwable t) {
        ProcessingException e = new ProcessingException(ScoutTexts.get("FormClosing") + " " + getTitle(), t);
        SERVICES.getService(IExceptionHandlerService.class).handleException(e);
      }
    }
  }

  public void touch() {
    getRootGroupBox().touch();
  }

  public boolean isSaveNeeded() {
    return getRootGroupBox().isSaveNeeded();
  }

  public void markSaved() {
    getRootGroupBox().markSaved();
  }

  public boolean isEmpty() {
    return getRootGroupBox().isEmpty();
  }

  /**
   * do not use or override this internal method
   */
  protected void disposeFormInternal() {
    if (!isFormOpen()) {
      return;
    }
    try {
      setButtonsArmed(false);
      setCloseTimerArmed(false);
      //
      m_scoutCloseTimer = null;
      for (Iterator it = m_scoutTimerMap.values().iterator(); it.hasNext();) {
        ((P_Timer) it.next()).setStopSignal();
      }
      m_scoutTimerMap.clear();
      m_displayHintLocked = false;
      m_formLoading = true;
    }
    catch (Throwable t) {
      LOG.warn("Form " + getClass().getName(), t);
    }
    // dispose fields
    FormUtility.disposeFormFields(this);
    // dispose form configuration
    try {
      execDisposeForm();
    }
    catch (Throwable t) {
      LOG.warn("Form " + getClass().getName(), t);
    }
    try {
      // deregister of wizard / desktop
      if (isAutoAddRemoveOnDesktop()) {
        IDesktop desktop = getDesktop();
        if (desktop != null) {
          desktop.removeForm(this);
        }
      }
    }
    finally {
      // unlock
      m_blockingCondition.release();
      // fire
      fireFormClosed();
    }
  }

  public boolean isShowing() {
    IDesktop desktop = getDesktop();
    if (desktop != null) {
      return desktop.isShowing(this);
    }
    else {
      return false;
    }
  }

  public boolean isFormClosed() {
    return !isFormOpen();
  }

  public boolean isFormOpen() {
    return m_blockingCondition.isBlocking();
  }

  public Object getCustomProperty(String propName) {
    return propertySupport.getProperty(propName);
  }

  public void setCustomProperty(String propName, Object o) {
    propertySupport.setProperty(propName, o);
  }

  public boolean hasProperty(String name) {
    return propertySupport.hasProperty(name);
  }

  public void setXML(String xml) throws ProcessingException {
    if (xml == null) {
      return;
    }
    SimpleXmlElement root = new SimpleXmlElement();
    try {
      root.parseString(xml);
    }
    catch (Throwable t) {
      throw new ProcessingException("Loading xml", t);
    }
    loadXML(root);
  }

  public String getXML(String encoding) throws ProcessingException {
    if (encoding == null) {
      encoding = "UTF-8";
    }
    try {
      SimpleXmlElement e = storeXML();
      ByteArrayOutputStream bo = new ByteArrayOutputStream();
      e.writeDocument(bo, null, encoding);
      return new String(bo.toByteArray());
    }
    catch (Throwable e) {
      if (e instanceof ProcessingException) {
        throw (ProcessingException) e;
      }
      else {
        throw new ProcessingException("form : " + getTitle() + ", encoding: " + encoding, e);
      }
    }
  }

  public SimpleXmlElement storeXML() throws ProcessingException {
    SimpleXmlElement root = new SimpleXmlElement("form-state");
    storeXML(root);
    return root;
  }

  public void storeXML(SimpleXmlElement root) throws ProcessingException {
    root.setAttribute("formId", getFormId());
    // add custom properties
    SimpleXmlElement xProps = new SimpleXmlElement("properties");
    root.addChild(xProps);
    IPropertyFilter filter = new IPropertyFilter() {
      public boolean accept(FastPropertyDescriptor descriptor) {
        if (descriptor.getPropertyType().isInstance(IFormField.class)) {
          return false;
        }
        if (!descriptor.getPropertyType().isPrimitive() && !Serializable.class.isAssignableFrom(descriptor.getPropertyType())) {
          return false;
        }
        if (descriptor.getReadMethod() == null || descriptor.getWriteMethod() == null) {
          return false;
        }
        return true;
      }
    };
    Map props = BeanUtility.getProperties(this, AbstractForm.class, filter);
    for (Iterator it = props.entrySet().iterator(); it.hasNext();) {
      Map.Entry entry = (Map.Entry) it.next();
      try {
        SimpleXmlElement xProp = new SimpleXmlElement("property");
        xProps.addChild(xProp);
        xProp.setAttribute("name", entry.getKey());
        xProp.setObjectAttribute("value", entry.getValue());
      }
      catch (Exception e) {
        throw new ProcessingException("property " + entry.getKey() + " with value " + entry.getValue(), e);
      }
    }
    // add fields
    final SimpleXmlElement xFields = new SimpleXmlElement("fields");
    root.addChild(xFields);
    final Holder<ProcessingException> exceptionHolder = new Holder<ProcessingException>(ProcessingException.class);
    P_AbstractCollectingFieldVisitor v = new P_AbstractCollectingFieldVisitor() {
      public boolean visitField(IFormField field, int level, int fieldIndex) {
        SimpleXmlElement xField = new SimpleXmlElement("field");
        try {
          field.storeXML(xField);
          xFields.addChild(xField);
        }
        catch (ProcessingException e) {
          exceptionHolder.setValue(e);
          return false;
        }
        return true;
      }
    };
    visitFields(v);
    if (exceptionHolder.getValue() != null) {
      throw exceptionHolder.getValue();
    }
  }

  public void loadXML(SimpleXmlElement root) throws ProcessingException {
    String formId = getFormId();
    String xmlId = root.getStringAttribute("formId", "");
    if (!formId.equals(xmlId)) {
      throw new ProcessingException("xml id=" + xmlId + " does not match form id=" + formId);
    }
    // load properties
    HashMap<String, Object> props = new HashMap<String, Object>();
    SimpleXmlElement xProps = root.getChild("properties");
    if (xProps != null) {
      for (Iterator it = xProps.getChildren("property").iterator(); it.hasNext();) {
        SimpleXmlElement xProp = (SimpleXmlElement) it.next();
        String name = xProp.getStringAttribute("name");
        try {
          Object value = xProp.getObjectAttribute("value", null, getClass().getClassLoader());
          props.put(name, value);
        }
        catch (Exception e) {
          LOG.warn("property " + name, e);
        }
      }
    }
    BeanUtility.setProperties(this, props, true, null);
    // load fields
    SimpleXmlElement xFields = root.getChild("fields");
    if (xFields != null) {
      for (Iterator it = xFields.getChildren("field").iterator(); it.hasNext();) {
        SimpleXmlElement xField = (SimpleXmlElement) it.next();
        FindFieldBySimpleClassNameVisitor v = new FindFieldBySimpleClassNameVisitor(xField.getStringAttribute("fieldId"));
        visitFields(v);
        IFormField f = v.getField();
        if (f != null) {
          f.loadXML(xField);
        }
      }
    }
    // in all tabboxes select the first tab that contains data, iff the current
    // tab has no values set
    getRootGroupBox().visitFields(new IFormFieldVisitor() {
      public boolean visitField(IFormField field, int level, int fieldIndex) {
        if (field instanceof ITabBox) {
          ITabBox tabBox = (ITabBox) field;
          IGroupBox selbox = tabBox.getSelectedTab();
          if (selbox == null || !selbox.isSaveNeeded()) {
            for (IGroupBox g : tabBox.getGroupBoxes()) {
              if (g.isSaveNeeded()) {
                tabBox.setSelectedTab(g);
                break;
              }
            }
          }
        }
        return true;
      }
    }, 0);
  }

  public void doExportXml(boolean saveAs) {
    while (true) {
      File path = m_lastXmlFileForStorage;
      if (saveAs || path == null) {
        File dir = m_lastXmlFileForStorage;
        if (dir != null) {
          dir = dir.getParentFile();
        }
        File[] a = new FileChooser(dir, new String[]{"xml"}, false).startChooser();
        if (a.length == 0) {
          break;
        }
        else {
          path = a[0];
        }
      }
      // export search parameters
      try {
        storeXML().writeDocument(new FileWriter(path), null, "UTF-8");
        if (path != null) {
          m_lastXmlFileForStorage = path;
        }
        break;
      }
      catch (Throwable t) {
        SERVICES.getService(IExceptionHandlerService.class).handleException(new ProcessingException(ScoutTexts.get("FormExportXml") + " " + getTitle(), t));
        saveAs = true;
      }
    }// end while nok
  }

  public void doImportXml() {
    File dir = m_lastXmlFileForStorage;
    if (dir != null) {
      dir = dir.getParentFile();
    }
    File[] a = new FileChooser(dir, new String[]{"xml"}, true).startChooser();
    if (a.length == 1) {
      File newPath = a[0];
      String text = null;
      try {
        SimpleXmlElement e = new SimpleXmlElement();
        e.parseStream(new FileInputStream(newPath));
        // load xml to search
        m_lastXmlFileForStorage = newPath;
        loadXML(e);
      }
      catch (Exception e) {
        LOG.warn("loading: " + newPath + " text: " + text + " Exception: " + e);
        new MessageBox(
            null,
            ScoutTexts.get("LoadFormXmlFailedText"),
            null,
            ScoutTexts.get("Ok"),
            null,
            null).startMessageBox();
        m_lastXmlFileForStorage = null;
      }
    }
  }

  public void printForm(PrintDevice device, Map<String, Object> parameters) {
    try {
      firePrint(null, device, parameters);
    }
    catch (ProcessingException e) {
      e.addContextMessage(ScoutTexts.get("FormPrint") + " " + getTitle());
      SERVICES.getService(IExceptionHandlerService.class).handleException(e);
    }
  }

  public void printField(IFormField field, PrintDevice device, Map<String, Object> parameters) {
    try {
      firePrint(field, device, parameters);
    }
    catch (ProcessingException e) {
      e.addContextMessage(ScoutTexts.get("FormPrint") + " " + (field != null ? field.getLabel() : getTitle()));
      SERVICES.getService(IExceptionHandlerService.class).handleException(e);
    }
  }

  public void activate() {
    if (getDesktop() != null) {
      getDesktop().ensureVisible(this);
    }
  }

  /**
   * Model Observer .
   */
  public void addFormListener(FormListener listener) {
    m_listenerList.add(FormListener.class, listener);
  }

  public void removeFormListener(FormListener listener) {
    m_listenerList.remove(FormListener.class, listener);
  }

  private void fireFormLoadBefore() throws ProcessingException {
    fireFormEvent(new FormEvent(this, FormEvent.TYPE_LOAD_BEFORE));
  }

  private void fireFormLoadAfter() throws ProcessingException {
    fireFormEvent(new FormEvent(this, FormEvent.TYPE_LOAD_AFTER));
  }

  private void fireFormLoadComplete() throws ProcessingException {
    fireFormEvent(new FormEvent(this, FormEvent.TYPE_LOAD_COMPLETE));
  }

  private void fireFormStoreBefore() throws ProcessingException {
    fireFormEvent(new FormEvent(this, FormEvent.TYPE_STORE_BEFORE));
  }

  private void fireFormDiscarded() {
    try {
      fireFormEvent(new FormEvent(this, FormEvent.TYPE_DISCARDED));
    }
    catch (ProcessingException e) {
      e.addContextMessage(ScoutTexts.get("FormFireDiscarded") + " " + getTitle());
      SERVICES.getService(IExceptionHandlerService.class).handleException(e);
    }
  }

  private void fireFormStoreAfter() throws ProcessingException {
    fireFormEvent(new FormEvent(this, FormEvent.TYPE_STORE_AFTER));
  }

  private void firePrint(IFormField root, PrintDevice device, Map<String, Object> parameters) throws ProcessingException {
    fireFormEvent(new FormEvent(this, FormEvent.TYPE_PRINT, root, device, parameters));
  }

  /**
   * send request that form was activated by gui
   */
  private void fireFormActivated() {
    try {
      execFormActivated();
      fireFormEvent(new FormEvent(this, FormEvent.TYPE_ACTIVATED));

    }
    catch (ProcessingException e) {
      e.addContextMessage(ScoutTexts.get("FormFireActivated") + " " + getTitle());
      SERVICES.getService(IExceptionHandlerService.class).handleException(e);
    }
  }

  /**
   * send request that form was closed by gui
   */
  private void fireFormClosed() {
    try {
      fireFormEvent(new FormEvent(this, FormEvent.TYPE_CLOSED));
    }
    catch (ProcessingException e) {
      e.addContextMessage(ScoutTexts.get("FormFireClosed") + " " + getTitle());
      SERVICES.getService(IExceptionHandlerService.class).handleException(e);
    }
  }

  private void fireFormEvent(FormEvent e) throws ProcessingException {
    EventListener[] listeners = m_listenerList.getListeners(FormListener.class);
    if (listeners != null && listeners.length > 0) {
      ProcessingException pe = null;
      for (int i = 0; i < listeners.length; i++) {
        try {
          ((FormListener) listeners[i]).formChanged(e);
        }
        catch (ProcessingException ex) {
          if (pe == null) {
            pe = ex;
          }
        }
        catch (Throwable t) {
          if (pe == null) {
            pe = new ProcessingException("Unexpected", t);
          }
        }
      }
      if (pe != null) {
        throw pe;
      }
    }
  }

  public void structureChanged(IFormField causingField) {
    fireFormStructureChanged(causingField);
  }

  private void fireFormStructureChanged(IFormField causingField) {
    try {
      fireFormEvent(new FormEvent(this, FormEvent.TYPE_STRUCTURE_CHANGED, causingField));
    }
    catch (ProcessingException e) {
      e.addContextMessage(ScoutTexts.get("FormFireStructureChanged") + " " + getTitle());
      SERVICES.getService(IExceptionHandlerService.class).handleException(e);
    }
  }

  private void fireFormToFront() {
    try {
      fireFormEvent(new FormEvent(this, FormEvent.TYPE_TO_FRONT));
    }
    catch (ProcessingException e) {
      e.addContextMessage(ScoutTexts.get("FormFireToFront") + " " + getTitle());
      SERVICES.getService(IExceptionHandlerService.class).handleException(e);
    }
  }

  private void fireFormToBack() {
    try {
      fireFormEvent(new FormEvent(this, FormEvent.TYPE_TO_BACK));
    }
    catch (ProcessingException e) {
      e.addContextMessage(ScoutTexts.get("FormFireToBack") + " " + getTitle());
      SERVICES.getService(IExceptionHandlerService.class).handleException(e);
    }
  }

  public String getBasicTitle() {
    return m_basicTitle;
  }

  public void setBasicTitle(String basicTitle) {
    m_basicTitle = basicTitle;
    composeTitle();
  }

  public String getSubTitle() {
    return m_subTitle;
  }

  public void setSubTitle(String subTitle) {
    m_subTitle = subTitle;
    composeTitle();
  }

  public void setTitle(String s) {
    m_basicTitle = s;
    m_subTitle = null;
    composeTitle();
  }

  private void composeTitle() {
    StringBuffer buf = new StringBuffer();
    String basic = getBasicTitle();
    if (basic != null) {
      if (buf.length() > 0) {
        buf.append(" - ");
      }
      buf.append(basic);
    }
    String sub = getSubTitle();
    if (sub != null) {
      if (buf.length() > 0) {
        buf.append(" - ");
      }
      buf.append(sub);
    }
    propertySupport.setPropertyString(PROP_TITLE, buf.toString());
  }

  public String getTitle() {
    return propertySupport.getPropertyString(PROP_TITLE);
  }

  public boolean isMaximizeEnabled() {
    return propertySupport.getPropertyBool(PROP_MAXIMIZE_ENABLED);
  }

  public void setMaximizeEnabled(boolean b) {
    propertySupport.setPropertyBool(PROP_MAXIMIZE_ENABLED, b);
  }

  public boolean isMinimizeEnabled() {
    return propertySupport.getPropertyBool(PROP_MINIMIZE_ENABLED);
  }

  public void setMinimizeEnabled(boolean b) {
    propertySupport.setPropertyBool(PROP_MINIMIZE_ENABLED, b);
  }

  public boolean isMaximized() {
    return propertySupport.getPropertyBool(PROP_MAXIMIZED);
  }

  public void setMaximized(boolean b) {
    if (isMaximizeEnabled()) {
      if (b) {
        propertySupport.setPropertyBool(PROP_MINIMIZED, false);
      }
      // maximized state of ui could be out of sync, fire always
      propertySupport.setPropertyAlwaysFire(PROP_MAXIMIZED, b);
    }
  }

  public boolean isMinimized() {
    return propertySupport.getPropertyBool(PROP_MINIMIZED);
  }

  public void setMinimized(boolean b) {
    if (isMinimizeEnabled()) {
      if (b) {
        propertySupport.setPropertyBool(PROP_MAXIMIZED, false);
      }
      // minimized state of ui could be out of sync, fire always
      propertySupport.setPropertyAlwaysFire(PROP_MINIMIZED, b);

    }
  }

  public boolean isAutoAddRemoveOnDesktop() {
    return m_autoRegisterInDesktopOnStart;
  }

  public void setAutoAddRemoveOnDesktop(boolean b) {
    m_autoRegisterInDesktopOnStart = b;
  }

  public boolean isModal() {
    return m_modal;
  }

  public void setModal(boolean b) {
    if (b) {
      switch (getDisplayHint()) {
        case DISPLAY_HINT_POPUP_DIALOG:
        case DISPLAY_HINT_DIALOG: {
          m_modal = b;
          break;
        }
        default: {
          LOG.warn("cannot set property 'modal' to true with current display hint type");
        }
      }
    }
    else {
      m_modal = b;
    }
  }

  public void setCacheBounds(boolean cacheBounds) {
    m_cacheBounds = cacheBounds;
  }

  public boolean isCacheBounds() {
    return m_cacheBounds;
  }

  public int getDisplayHint() {
    return m_displayHint;
  }

  public void setDisplayHint(int i) {
    switch (i) {
      case DISPLAY_HINT_DIALOG: {
        m_displayHint = i;
        break;
      }
      case DISPLAY_HINT_POPUP_WINDOW: {
        m_displayHint = i;
        setModal(false);
        break;
      }
      case DISPLAY_HINT_POPUP_DIALOG: {
        m_displayHint = i;
        break;
      }
      case DISPLAY_HINT_VIEW: {
        m_displayHint = i;
        setModal(false);
        break;
      }
      default: {
        throw new IllegalArgumentException("invalid displayHint " + i);
      }
    }
  }

  public String getDisplayViewId() {
    return m_displayViewId;
  }

  public void setDisplayViewId(String viewId) {
    m_displayViewId = viewId;
  }

  public boolean isAskIfNeedSave() {
    return m_askIfNeedSave;
  }

  public void setAskIfNeedSave(boolean b) {
    m_askIfNeedSave = b;
  }

  public void toFront() {
    fireFormToFront();
  }

  public void toBack() {
    fireFormToBack();
  }

  public boolean isButtonsArmed() {
    return m_buttonsArmed;
  }

  public void setButtonsArmed(boolean b) {
    m_buttonsArmed = b;
  }

  public boolean isCloseTimerArmed() {
    return m_closeTimerArmed;
  }

  public void setCloseTimerArmed(boolean b) {
    m_closeTimerArmed = b;
  }

  @Override
  public String toString() {
    return "Form " + getFormId();
  }

  public IFormUIFacade getUIFacade() {
    return m_uiFacade;
  }

  protected void handleSystemButtonEventInternal(ButtonEvent e) {
    switch (e.getType()) {
      case ButtonEvent.TYPE_CLICKED: {
        // disable close timer
        setCloseTimerArmed(false);
        if (isButtonsArmed()) {
          if (checkForVerifyingFields()) {
            try {
              IButton src = (IButton) e.getSource();
              switch (src.getSystemType()) {
                case IButton.SYSTEM_TYPE_CANCEL: {
                  doCancel();
                  break;
                }
                case IButton.SYSTEM_TYPE_CLOSE: {
                  doClose();
                  break;
                }
                case IButton.SYSTEM_TYPE_OK: {
                  doOk();
                  break;
                }
                case IButton.SYSTEM_TYPE_RESET: {
                  doReset();
                  break;
                }
                case IButton.SYSTEM_TYPE_SAVE: {
                  doSave();
                  break;
                }
                case IButton.SYSTEM_TYPE_SAVE_WITHOUT_MARKER_CHANGE: {
                  doSaveWithoutMarkerChange();
                  break;
                }
              }
            }
            catch (ProcessingException se) {
              se.addContextMessage(ScoutTexts.get("FormButtonClicked") + " " + getTitle() + "." + e.getButton().getLabel());
              SERVICES.getService(IExceptionHandlerService.class).handleException(se);
            }
          }// end
        }
        break;
      }
    }
  }

  /**
   * Button controller for ok,cancel, save etc.
   */
  private class P_SystemButtonListener implements ButtonListener {
    public void buttonChanged(ButtonEvent e) {
      // auto-detaching
      if (m_systemButtonListener != this) {
        ((IButton) e.getSource()).removeButtonListener(this);
        return;
      }
      /*
       * Ticket 76711: added optimistic lock
       */
      try {
        if (m_systemButtonListenerLock.acquire()) {
          //
          handleSystemButtonEventInternal(e);
        }// end if optimistic lock
      }// end try optimistic lock
      finally {
        m_systemButtonListenerLock.release();
      }
    }
  }// end private class

  private class P_MainBoxPropertyChangeProxy implements PropertyChangeListener {
    public void propertyChange(PropertyChangeEvent e) {
      if (IFormField.PROP_SAVE_NEEDED.equals(e.getPropertyName())) {
        propertySupport.firePropertyChange(PROP_SAVE_NEEDED, e.getOldValue(), e.getNewValue());
      }
      else if (IFormField.PROP_EMPTY.equals(e.getPropertyName())) {
        propertySupport.firePropertyChange(PROP_EMPTY, e.getOldValue(), e.getNewValue());
      }
    }
  }

  /**
   * form custom timer
   */
  private class P_Timer extends StoppableThread {
    private final long m_intervalMillis;
    private final String m_timerId;
    private boolean m_execPending = false;// do not execute while exec is pending
    private final IClientSession m_session;

    public P_Timer(long seconds, String timerId) {
      setDaemon(true);
      m_intervalMillis = seconds * 1000L;
      m_timerId = timerId;
      m_session = ClientSyncJob.getCurrentSession();
    }

    @Override
    public void run() {
      long next = ((System.currentTimeMillis() + 999L) / 1000L) * 1000L + m_intervalMillis;
      while (m_scoutTimerMap != null && !isStopSignal()) {
        try {
          sleep(250);
        }
        catch (InterruptedException ex) {
        }
        // active?
        if ((!m_execPending) && (!isStopSignal())) {
          // next ready?
          if (next < System.currentTimeMillis()) {
            m_execPending = true;
            new ClientSyncJob("Form timer", m_session) {
              @Override
              protected void runVoid(IProgressMonitor monitor) throws Throwable {
                try {
                  if (LOG.isInfoEnabled()) {
                    LOG.info("timer " + m_timerId);
                  }
                  execTimer(m_timerId);
                }
                catch (ProcessingException se) {
                  se.addContextMessage(ScoutTexts.get("FormTimerActivated") + " " + getTitle() + "." + m_timerId);
                  SERVICES.getService(IExceptionHandlerService.class).handleException(se);
                }
                finally {
                  m_execPending = false;// continue scheduling
                }
              }
            }.schedule();
          }
        }
        // update next
        while (next < System.currentTimeMillis()) {
          next = next + m_intervalMillis;
        }
      }
    }
  }// end private class

  /**
   * form close timer
   */
  private class P_CloseTimer extends StoppableThread {
    private long m_seconds;
    private final IClientSession m_session;

    public P_CloseTimer(long seconds) {
      setName("IForm.P_CloseTimer");
      setDaemon(true);
      m_seconds = seconds;
      m_session = ClientSyncJob.getCurrentSession();
    }

    @Override
    public void run() {
      while (this == m_scoutCloseTimer && m_seconds > 0 && isCloseTimerArmed()) {
        new ClientSyncJob("Form close countdown", m_session) {
          @Override
          protected void runVoid(IProgressMonitor monitor) throws Throwable {
            setSubTitle("" + m_seconds);
          }
        }.schedule();
        try {
          sleep(1000);
        }
        catch (InterruptedException ex) {
        }
        m_seconds--;
      }
      if (this == m_scoutCloseTimer) {
        new ClientSyncJob("Form close timer", m_session) {
          @Override
          protected void runVoid(IProgressMonitor monitor) throws Throwable {
            try {
              if (isCloseTimerArmed()) {
                execCloseTimer();
              }
              else {
                setSubTitle(null);
              }
            }
            catch (ProcessingException se) {
              se.addContextMessage(ScoutTexts.get("FormCloseTimerActivated") + " " + getTitle());
              SERVICES.getService(IExceptionHandlerService.class).handleException(se);
            }
          }
        }.schedule();
      }
    }
  }// end private class

  private abstract static class P_AbstractCollectingFieldVisitor<T> implements IFormFieldVisitor {
    private final ArrayList<T> m_list = new ArrayList<T>();

    public void collect(T o) {
      m_list.add(o);
    }

    public int getCollectionCount() {
      return m_list.size();
    }

    public List<T> getCollection() {
      return m_list;
    }

  }

  private class P_UIFacade implements IFormUIFacade {
    public void fireFormActivatedFromUI() {
      fireFormActivated();
    }

    public void fireFormClosingFromUI() {
      // check if some field is verifying input. In this case cancel ui call
      if (!checkForVerifyingFields()) {
        return;
      }
      closeFormInternal(false);
    }

    public void fireFormKilledFromUI() {
      closeFormInternal(true);
    }
  }// end private class

}
