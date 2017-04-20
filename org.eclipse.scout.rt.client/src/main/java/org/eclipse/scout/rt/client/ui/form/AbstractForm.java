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
package org.eclipse.scout.rt.client.ui.form;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.Writer;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.security.Permission;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EventListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.eclipse.scout.rt.client.ModelContextProxy;
import org.eclipse.scout.rt.client.ModelContextProxy.ModelContext;
import org.eclipse.scout.rt.client.context.ClientRunContext;
import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.client.dto.Data;
import org.eclipse.scout.rt.client.dto.FormData;
import org.eclipse.scout.rt.client.dto.FormData.SdkCommand;
import org.eclipse.scout.rt.client.extension.ui.form.FormChains.FormAddSearchTermsChain;
import org.eclipse.scout.rt.client.extension.ui.form.FormChains.FormCheckFieldsChain;
import org.eclipse.scout.rt.client.extension.ui.form.FormChains.FormCloseTimerChain;
import org.eclipse.scout.rt.client.extension.ui.form.FormChains.FormCreateFormDataChain;
import org.eclipse.scout.rt.client.extension.ui.form.FormChains.FormDataChangedChain;
import org.eclipse.scout.rt.client.extension.ui.form.FormChains.FormDisposeFormChain;
import org.eclipse.scout.rt.client.extension.ui.form.FormChains.FormFormActivatedChain;
import org.eclipse.scout.rt.client.extension.ui.form.FormChains.FormInactivityTimerChain;
import org.eclipse.scout.rt.client.extension.ui.form.FormChains.FormInitFormChain;
import org.eclipse.scout.rt.client.extension.ui.form.FormChains.FormOnCloseRequestChain;
import org.eclipse.scout.rt.client.extension.ui.form.FormChains.FormOnVetoExceptionChain;
import org.eclipse.scout.rt.client.extension.ui.form.FormChains.FormResetSearchFilterChain;
import org.eclipse.scout.rt.client.extension.ui.form.FormChains.FormStoredChain;
import org.eclipse.scout.rt.client.extension.ui.form.FormChains.FormTimerChain;
import org.eclipse.scout.rt.client.extension.ui.form.FormChains.FormValidateChain;
import org.eclipse.scout.rt.client.extension.ui.form.FormChains.IsSaveNeededFieldsChain;
import org.eclipse.scout.rt.client.extension.ui.form.IFormExtension;
import org.eclipse.scout.rt.client.extension.ui.form.MoveFormFieldsHandler;
import org.eclipse.scout.rt.client.job.ModelJobs;
import org.eclipse.scout.rt.client.services.common.search.ISearchFilterService;
import org.eclipse.scout.rt.client.ui.DataChangeListener;
import org.eclipse.scout.rt.client.ui.IDisplayParent;
import org.eclipse.scout.rt.client.ui.IEventHistory;
import org.eclipse.scout.rt.client.ui.WeakDataChangeListener;
import org.eclipse.scout.rt.client.ui.basic.filechooser.FileChooser;
import org.eclipse.scout.rt.client.ui.desktop.AbstractDesktop;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.OpenUriAction;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.IFormFieldFilter;
import org.eclipse.scout.rt.client.ui.form.fields.IValidateContentDescriptor;
import org.eclipse.scout.rt.client.ui.form.fields.IValueField;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractCancelButton;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractCloseButton;
import org.eclipse.scout.rt.client.ui.form.fields.button.ButtonEvent;
import org.eclipse.scout.rt.client.ui.form.fields.button.ButtonListener;
import org.eclipse.scout.rt.client.ui.form.fields.button.IButton;
import org.eclipse.scout.rt.client.ui.form.fields.composer.IComposerField;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.IGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.tabbox.ITabBox;
import org.eclipse.scout.rt.client.ui.form.fields.wrappedform.IWrappedFormField;
import org.eclipse.scout.rt.client.ui.form.internal.FindFieldByFormDataIdVisitor;
import org.eclipse.scout.rt.client.ui.form.internal.FindFieldByXmlIdsVisitor;
import org.eclipse.scout.rt.client.ui.form.internal.FormDataPropertyFilter;
import org.eclipse.scout.rt.client.ui.messagebox.IMessageBox;
import org.eclipse.scout.rt.client.ui.messagebox.MessageBoxes;
import org.eclipse.scout.rt.client.ui.wizard.IWizard;
import org.eclipse.scout.rt.client.ui.wizard.IWizardStep;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigOperation;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.exception.ExceptionHandler;
import org.eclipse.scout.rt.platform.exception.PlatformError;
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.exception.PlatformExceptionTranslator;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.exception.VetoException;
import org.eclipse.scout.rt.platform.holders.Holder;
import org.eclipse.scout.rt.platform.holders.IHolder;
import org.eclipse.scout.rt.platform.html.HTML;
import org.eclipse.scout.rt.platform.html.IHtmlContent;
import org.eclipse.scout.rt.platform.html.IHtmlListElement;
import org.eclipse.scout.rt.platform.job.IBlockingCondition;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.reflect.AbstractPropertyObserver;
import org.eclipse.scout.rt.platform.reflect.ConfigurationUtility;
import org.eclipse.scout.rt.platform.reflect.FastPropertyDescriptor;
import org.eclipse.scout.rt.platform.reflect.IPropertyFilter;
import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.eclipse.scout.rt.platform.status.IMultiStatus;
import org.eclipse.scout.rt.platform.status.IStatus;
import org.eclipse.scout.rt.platform.status.MultiStatus;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.BeanUtility;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.EventListenerList;
import org.eclipse.scout.rt.platform.util.PreferredValue;
import org.eclipse.scout.rt.platform.util.XmlUtility;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.data.basic.NamedBitMaskHelper;
import org.eclipse.scout.rt.shared.data.form.AbstractFormData;
import org.eclipse.scout.rt.shared.data.form.IPropertyHolder;
import org.eclipse.scout.rt.shared.data.form.fields.AbstractFormFieldData;
import org.eclipse.scout.rt.shared.data.form.properties.AbstractPropertyData;
import org.eclipse.scout.rt.shared.extension.AbstractExtension;
import org.eclipse.scout.rt.shared.extension.ContributionComposite;
import org.eclipse.scout.rt.shared.extension.IContributionOwner;
import org.eclipse.scout.rt.shared.extension.IExtensibleObject;
import org.eclipse.scout.rt.shared.extension.IExtension;
import org.eclipse.scout.rt.shared.extension.ObjectExtensions;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;
import org.eclipse.scout.rt.shared.services.common.security.IAccessControlService;
import org.quartz.SimpleScheduleBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

@ClassId("cec05259-9e6f-480c-94fa-f02f56e777f7")
@FormData(value = AbstractFormData.class, sdkCommand = SdkCommand.USE)
public abstract class AbstractForm extends AbstractPropertyObserver implements IForm, IExtensibleObject, IContributionOwner {

  private static final String INITIALIZED = "INITIALIZED";
  private static final String CACHE_BOUNDS = "CACHE_BOUNDS";
  private static final String ASK_IF_NEED_SAVE = "ASK_IF_NEED_SAVE";
  private static final String BUTTONS_ARMED = "BUTTONS_ARMED";
  private static final String CLOSE_TIMER_ARMED = "CLOSE_TIMER_ARMED";
  private static final String SHOW_ON_START = "SHOW_ON_START";
  private static final String FORM_STORED = "FORM_STORED";
  private static final String FORM_LOADING = "FORM_LOADING";
  private static final String FORM_STARTED = "FORM_STARTED";

  private static final Logger LOG = LoggerFactory.getLogger(AbstractForm.class);
  private static final NamedBitMaskHelper FLAGS_BIT_HELPER = new NamedBitMaskHelper(INITIALIZED, CACHE_BOUNDS, ASK_IF_NEED_SAVE,
      BUTTONS_ARMED, CLOSE_TIMER_ARMED, SHOW_ON_START);
  private static final NamedBitMaskHelper STATE_BIT_HELPER = new NamedBitMaskHelper(FORM_STORED, FORM_LOADING, FORM_STARTED);

  private final PreferredValue<IDisplayParent> m_displayParent;
  private final EventListenerList m_listenerList;
  private final PreferredValue<Boolean> m_modal; // no property, is fixed
  private final IBlockingCondition m_blockingCondition;
  private final ObjectExtensions<AbstractForm, IFormExtension<? extends AbstractForm>> m_objectExtensions;
  private final IEventHistory<FormEvent> m_eventHistory;

  /**
   * Provides 8 boolean flags.<br>
   * Currently used: {@link #INITIALIZED}, {@link #CACHE_BOUNDS}, {@link #ASK_IF_NEED_SAVE}, {@link #BUTTONS_ARMED} ,
   * {@link #CLOSE_TIMER_ARMED}, {@link #SHOW_ON_START}
   */
  private byte m_flags;

  /**
   * Provides 8 boolean flags.<br>
   * Currently used: {@link #FORM_STORED}, {@link #FORM_LOADING}, {@link #FORM_STARTED}
   */
  private byte m_states;

  private IFormUIFacade m_uiFacade;
  private IWizardStep m_wizardStep;
  private int m_displayHint;// no property, is fixed
  private String m_displayViewId;// no property, is fixed
  private int m_closeType;
  private String m_cancelVerificationText;
  private IGroupBox m_mainBox;
  private IWrappedFormField m_wrappedFormField;
  private ButtonListener m_systemButtonListener;
  private String m_classId;
  private ClientRunContext m_initialClientRunContext; // ClientRunContext of the calling context during initialization.
  private IFormHandler m_handler; // never null (ensured by setHandler())
  private SearchFilter m_searchFilter;

  // current timers
  private IFuture<?> m_closeTimerFuture;
  private Map<String, IFuture<Void>> m_timerFutureMap;
  private DataChangeListener m_internalDataChangeListener;

  // field replacement support
  private Map<Class<?>, Class<? extends IFormField>> m_fieldReplacements;
  private IContributionOwner m_contributionHolder;

  public AbstractForm() {
    this(true);
  }

  public AbstractForm(boolean callInitializer) {
    m_listenerList = new EventListenerList();
    m_modal = new PreferredValue<Boolean>(false, false);
    m_closeType = IButton.SYSTEM_TYPE_NONE;
    m_displayParent = new PreferredValue<>(null, false);
    m_eventHistory = createEventHistory();
    setHandler(new NullFormHandler());
    setFormLoading(true);
    m_blockingCondition = Jobs.newBlockingCondition(false);
    m_objectExtensions = new ObjectExtensions<AbstractForm, IFormExtension<? extends AbstractForm>>(this, true);

    if (callInitializer) {
      callInitializer();
    }
  }

  @Override
  public final List<? extends IFormExtension<? extends AbstractForm>> getAllExtensions() {
    return m_objectExtensions.getAllExtensions();
  }

  @Override
  public <T extends IExtension<?>> T getExtension(Class<T> c) {
    return m_objectExtensions.getExtension(c);
  }

  @Override
  public final List<Object> getAllContributions() {
    return m_contributionHolder.getAllContributions();
  }

  @Override
  public final <T> List<T> getContributionsByClass(Class<T> type) {
    return m_contributionHolder.getContributionsByClass(type);
  }

  @Override
  public final <T> T getContribution(Class<T> contribution) {
    return m_contributionHolder.getContribution(contribution);
  }

  protected void callInitializer() {
    if (isInitialized()) {
      return;
    }

    // Remember the initial ClientRunContext to not loose the Form from current calling context.
    m_initialClientRunContext = ClientRunContexts.copyCurrent();

    // Run the initialization on behalf of this Form.
    ClientRunContexts.copyCurrent().withForm(this).run(new IRunnable() {
      @Override
      public void run() throws Exception {
        interceptInitConfig();
        postInitConfig();
        setInitialized();
      }
    });
  }

  protected IFormExtension<? extends AbstractForm> createLocalExtension() {
    return new LocalFormExtension<AbstractForm>(this);
  }

  /*
   * Configuration
   */
  /**
   * @return the localized title property of the form. Use {@link TEXTS}.
   */
  @ConfigProperty(ConfigProperty.TEXT)
  @Order(10)
  protected String getConfiguredTitle() {
    return null;
  }

  /**
   * @return the localized sub-title property of the form. Use {@link TEXTS}.
   */
  @ConfigProperty(ConfigProperty.TEXT)
  @Order(20)
  protected String getConfiguredSubTitle() {
    return null;
  }

  @ConfigProperty(ConfigProperty.TEXT)
  @Order(30)
  protected String getConfiguredCancelVerificationText() {
    return ScoutTexts.get("FormSaveChangesQuestion");
  }

  /**
   * Overwrite to set the display hint for this {@link IForm}. By default, {@link #DISPLAY_HINT_DIALOG} is configured.
   * <ul>
   * <li>{@link #DISPLAY_HINT_VIEW}</li>
   * <li>{@link #DISPLAY_HINT_DIALOG}</li>
   * <li>{@link #DISPLAY_HINT_POPUP_WINDOW}</li>
   * </ul>
   */
  @ConfigProperty(ConfigProperty.FORM_DISPLAY_HINT)
  @Order(40)
  protected int getConfiguredDisplayHint() {
    return DISPLAY_HINT_DIALOG;
  }

  /**
   * Overwrite to set the display parent for this {@link IForm}. By default, this method returns <code>null</code>,
   * meaning that it is derived from the current calling context, or is {@link IDesktop} for views.
   * <p>
   * A display parent is the anchor to attach this {@link IForm} to, and affects its accessibility and modality scope.
   * Possible parents are {@link IDesktop}, {@link IOutline}, or {@link IForm}:
   * <ul>
   * <li>Desktop: Form is always accessible; blocks the entire desktop if modal;</li>
   * <li>Outline: Form is only accessible when the given outline is active; blocks only the outline if modal;</li>
   * <li>Form: Form is only accessible when the given Form is active; blocks only the Form if modal;</li>
   * </ul>
   *
   * @return like {@link IDesktop}, {@link IOutline} or {@link IForm}, or <code>null</code> to derive from the calling
   *         context.
   */
  @ConfigProperty(ConfigProperty.OBJECT)
  @Order(50)
  protected IDisplayParent getConfiguredDisplayParent() {
    return null;
  }

  @ConfigProperty(ConfigProperty.FORM_VIEW_ID)
  @Order(60)
  protected String getConfiguredDisplayViewId() {
    return null;
  }

  /**
   * @return if the form can be minimized.
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(70)
  protected boolean getConfiguredMinimizeEnabled() {
    return false;
  }

  /**
   * @return if the form can be maximized.
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(80)
  protected boolean getConfiguredMaximizeEnabled() {
    return false;
  }

  /**
   * @return defines if the form is initially minimized
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(90)
  protected boolean getConfiguredMinimized() {
    return false;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(100)
  protected boolean getConfiguredMaximized() {
    return false;
  }

  /**
   * Overwrite to set the modality hint for this {@link IForm}. By default, this method returns
   * {@link #MODALITY_HINT_AUTO}, meaning that modality is derived from the configured <code>display-hint</code>. For
   * dialogs, it is <code>true</code>, for views <code>false</code>.
   *
   * @return {@link #MODALITY_HINT_MODAL} to make this {@link IForm} modal in respect to its {@link IDisplayParent}, or
   *         {@link #MODALITY_HINT_MODELESS} otherwise, or {@link #MODALITY_HINT_AUTO} to derive modality from the
   *         <code>display-hint</code>.
   * @see #MODALITY_HINT_MODAL
   * @see #MODALITY_HINT_MODELESS
   * @see #MODALITY_HINT_AUTO
   * @see #getConfiguredDisplayHint()
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(110)
  protected int getConfiguredModalityHint() {
    return MODALITY_HINT_AUTO;
  }

  /**
   * Configure whether to show this {@link IForm} once started.
   * <p>
   * If set to <code>true</code> and this {@link IForm} is started, it is added to the {@link IDesktop} in order to be
   * displayed. By default, this property is set to <code>true</code>.
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(120)
  protected boolean getConfiguredShowOnStart() {
    return true;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(130)
  protected boolean getConfiguredCacheBounds() {
    return false;
  }

  /**
   * AskIfNeedSave defines if a message box with yes, no and cancel option is shown to the user for confirmation after
   * having made at least one change in the form and then having pressed the cancel button.
   *
   * @return <code>true</code> if message box is shown for confirmation, <code>false</code> otherwise.
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(140)
  protected boolean getConfiguredAskIfNeedSave() {
    return true;
  }

  /**
   * @return configured icon ID for this form
   */
  @ConfigProperty(ConfigProperty.ICON_ID)
  @Order(150)
  protected String getConfiguredIconId() {
    return null;
  }

  /**
   * Configures the css class(es) of this form.
   * <p>
   * Subclasses can override this method. Default is {@code null}.
   *
   * @return a string containing one or more classes separated by space, or null if no class should be set.
   */
  @ConfigProperty(ConfigProperty.STRING)
  @Order(160)
  protected String getConfiguredCssClass() {
    return null;
  }

  @ConfigProperty(ConfigProperty.INTEGER)
  @Order(170)
  protected int/* seconds */ getConfiguredCloseTimer() {
    return 0;
  }

  @ConfigProperty(ConfigProperty.INTEGER)
  @Order(180)
  protected int/* seconds */ getConfiguredCustomTimer() {
    return 0;
  }

  /**
   * Override this method to mark a form as closable. In case of closable the close button [X] is displayed either in
   * the form or the view header. The default implementations says a form displayed as a dialog containing a
   * {@link AbstractCloseButton} or a {@link AbstractCancelButton} is closable.
   *
   * @return
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(190)
  protected boolean getConfiguredClosable() {
    // legacy support if not overwritten
    return getDisplayHint() == DISPLAY_HINT_DIALOG && hasCloseOrCancelButton();
  }

  /**
   * Whether or not a changed form should display the save needed state (dirty) in the dialog or view header.
   *
   * @return true to display the save needed state, false otherwise.
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(200)
  protected boolean getConfiguredSaveNeededVisible() {
    return false;
  }

  /**
   * This method is called to get an exclusive key of the form. The key is used to open the same form with the same
   * handler only once. Obviously this behavior can only be used for view forms.
   *
   * @see AbstractDesktop#getSimilarViewForms(IForm)
   * @return null for exclusive form behavior an exclusive key to ensure similar handling.
   */
  @Override
  public Object computeExclusiveKey() {
    return null;
  }

  /**
   * Initialize the form and all of its fields. By default any of the #start* methods of the form call this method
   * <p>
   * This method is called in the process of the initialization. The UI is not ready yet.
   */
  @ConfigOperation
  @Order(10)
  protected void execInitForm() {
  }

  /**
   * This method is called when UI is ready.
   */
  @ConfigOperation
  @Order(11)
  protected void execFormActivated() {
  }

  /**
   * see {@link IDesktop#dataChanged(Object...)}
   */
  @ConfigOperation
  @Order(13)
  protected void execDataChanged(Object... dataTypes) {
  }

  /**
   * This method is called in order to check field validity.<br>
   * This method is called before {@link IFormHandler#interceptCheckFields()} and before the form is validated and
   * stored.<br>
   * After this method, the form is checking fields itself and displaying a dialog with missing and invalid fields.
   *
   * @return true when this check is done and further checks can continue, false to silently cancel the current process
   * @throws ProcessingException
   *           to cancel the current process with error handling and user notification such as a dialog
   */
  @ConfigOperation
  @Order(13)
  protected boolean execCheckFields() {
    return true;
  }

  /**
   * This method is called in order to update derived states like button enablings.<br>
   * This method is called before {@link IFormHandler#interceptValidate()} and before the form is stored.
   *
   * @return true when validate is successful, false to silently cancel the current process
   * @throws ProcessingException
   *           to cancel the current process with error handling and user notification such as a dialog
   */
  @ConfigOperation
  @Order(14)
  protected boolean execValidate() {
    return true;
  }

  /**
   * This method is called in order to update pages on the desktop after the form stored data.<br>
   * This method is called after {@link IFormHandler#execStore()}.
   */
  @ConfigOperation
  @Order(16)
  protected void execStored() {
  }

  @ConfigOperation
  @Order(50)
  protected boolean execIsSaveNeeded() {
    return getRootGroupBox().isSaveNeeded();
  }

  /**
   * @throws ProcessingException
   *           / {@link VetoException} if the exception should produce further info messages (default)
   */
  @ConfigOperation
  @Order(17)
  protected void execOnVetoException(VetoException e, int code) {
    throw e;
  }

  /**
   * @param kill
   *          true if a widget close icon (normally the X on the titlebar) was pressed or ESC was pressed
   * @param enabledButtonSystemTypes
   *          set of all <code>IButton#SYSTEM_TYPE_*</code> of all enabled and visible buttons of this form (never
   *          <code>null</code>)
   */
  @ConfigOperation
  @Order(18)
  protected void execOnCloseRequest(boolean kill, Set<Integer> enabledButtonSystemTypes) {
    if (enabledButtonSystemTypes.contains(IButton.SYSTEM_TYPE_CLOSE)) {
      doClose();
    }
    else {
      doCancel();
    }
  }

  @ConfigOperation
  @Order(19)
  protected void execDisposeForm() {
  }

  @ConfigOperation
  @Order(20)
  protected void execCloseTimer() {
    doClose();
  }

  @ConfigOperation
  @Order(30)
  protected void execInactivityTimer() {
    doClose();
  }

  @ConfigOperation
  @Order(40)
  protected void execTimer(String timerId) {
    LOG.info("execTimer {}", timerId);
  }

  /**
   * add verbose information to the search filter
   */
  @ConfigOperation
  @Order(50)
  protected void execAddSearchTerms(SearchFilter search) {
  }

  protected boolean hasCloseOrCancelButton() {
    for (IFormField f : getAllFields()) {
      if (f.isEnabled() && f.isVisible() && (f instanceof IButton)) {
        switch (((IButton) f).getSystemType()) {
          case IButton.SYSTEM_TYPE_CLOSE:
          case IButton.SYSTEM_TYPE_CANCEL: {
            return true;
          }
        }
      }
    }
    return false;
  }

  private Class<? extends IGroupBox> getConfiguredMainBox() {
    Class[] dca = ConfigurationUtility.getDeclaredPublicClasses(getClass());
    return ConfigurationUtility.filterClassIgnoringInjectFieldAnnotation(dca, IGroupBox.class);
  }

  private List<Class<IFormField>> getConfiguredInjectedFields() {
    Class[] dca = ConfigurationUtility.getDeclaredPublicClasses(getClass());
    return ConfigurationUtility.filterClassesWithInjectFieldAnnotation(dca, IFormField.class);
  }

  protected final void interceptInitConfig() {
    final Holder<RuntimeException> exceptionHolder = new Holder<>(RuntimeException.class, null);
    final Holder<PlatformError> errorHolder = new Holder<>(PlatformError.class, null);
    m_objectExtensions.initConfig(createLocalExtension(), new Runnable() {
      @Override
      public void run() {
        try {
          initConfig();
        }
        catch (RuntimeException e) {
          exceptionHolder.setValue(e);
        }
        catch (PlatformError e) {
          errorHolder.setValue(e);
        }
      }
    });
    if (exceptionHolder.getValue() != null) {
      throw exceptionHolder.getValue();
    }
    else if (errorHolder.getValue() != null) {
      throw errorHolder.getValue();
    }
  }

  protected void initConfig() {
    m_uiFacade = BEANS.get(ModelContextProxy.class).newProxy(new P_UIFacade(), ModelContext.copyCurrent().withForm(this));
    m_timerFutureMap = new HashMap<>();
    setShowOnStart(getConfiguredShowOnStart());
    m_contributionHolder = new ContributionComposite(this);

    // prepare injected fields
    List<Class<IFormField>> fieldArray = getConfiguredInjectedFields();
    DefaultFormFieldInjection injectedFields = null;

    IGroupBox rootBox = getRootGroupBox();
    try {
      if (fieldArray.size() > 0) {
        injectedFields = new DefaultFormFieldInjection(this);
        injectedFields.addFields(fieldArray);
        FormFieldInjectionThreadLocal.push(injectedFields);
      }

      // add mainbox if getter returns null
      if (rootBox == null) {
        List<IGroupBox> contributedFields = m_contributionHolder.getContributionsByClass(IGroupBox.class);
        rootBox = CollectionUtility.firstElement(contributedFields);
        if (rootBox == null) {
          Class<? extends IGroupBox> mainBoxClass = getConfiguredMainBox();
          if (mainBoxClass != null) {
            rootBox = ConfigurationUtility.newInnerInstance(this, mainBoxClass);
          }
        }
        m_mainBox = rootBox;
      }
    }
    finally {
      if (injectedFields != null) {
        m_fieldReplacements = injectedFields.getReplacementMapping();
        FormFieldInjectionThreadLocal.pop(injectedFields);
      }
    }
    if (rootBox != null) {
      rootBox.setFormInternal(this);
      rootBox.setMainBox(true);
      rootBox.updateKeyStrokes();
      if (rootBox.isScrollable().isUndefined()) {
        rootBox.setScrollable(true);
      }
    }

    // move form fields
    new MoveFormFieldsHandler(this).moveFields();

    //
    if (getConfiguredCloseTimer() > 0) {
      setCloseTimer(getConfiguredCloseTimer());
    }
    if (getConfiguredCustomTimer() > 0) {
      setTimer("custom", getConfiguredCustomTimer());
    }

    if (getConfiguredCancelVerificationText() != null) {
      setCancelVerificationText(getConfiguredCancelVerificationText());
    }
    if (getConfiguredTitle() != null) {
      setTitle(getConfiguredTitle());
    }
    if (getConfiguredSubTitle() != null) {
      setSubTitle(getConfiguredSubTitle());
    }
    setMinimizeEnabled(getConfiguredMinimizeEnabled());
    setMaximizeEnabled(getConfiguredMaximizeEnabled());
    setMinimized(getConfiguredMinimized());
    setMaximized(getConfiguredMaximized());
    setCacheBounds(getConfiguredCacheBounds());
    setAskIfNeedSave(getConfiguredAskIfNeedSave());
    setIconId(getConfiguredIconId());
    setCssClass((getConfiguredCssClass()));

    // Set 'modality' as preferred value if not 'auto'.
    int modalityHint = getConfiguredModalityHint();
    if (modalityHint != MODALITY_HINT_AUTO) {
      m_modal.set(modalityHint == MODALITY_HINT_MODAL, true);
    }

    // Set 'displayParent' as preferred value if not null; must precede setting of the 'displayHint'.
    IDisplayParent displayParent = getConfiguredDisplayParent();
    if (displayParent != null) {
      m_displayParent.set(displayParent, true);
    }
    else {
      m_displayParent.set(getDesktop(), false);
    }

    setDisplayHint(getConfiguredDisplayHint());
    setDisplayViewId(getConfiguredDisplayViewId());
    setClosable(getConfiguredClosable());
    setSaveNeededVisible(getConfiguredSaveNeededVisible());

    // visit all system buttons and attach observer
    m_systemButtonListener = new P_SystemButtonListener();// is auto-detaching
    IFormFieldVisitor v2 = new IFormFieldVisitor() {
      @Override
      public boolean visitField(IFormField field, int level, int fieldIndex) {
        if (field instanceof IButton) {
          final IButton button = (IButton) field;
          if (button.getSystemType() != IButton.SYSTEM_TYPE_NONE) {
            button.addButtonListener(m_systemButtonListener);
          }
        }
        return true;
      }
    };
    visitFields(v2);
    getRootGroupBox().addPropertyChangeListener(new P_MainBoxPropertyChangeProxy());
    setButtonsArmed(true);
  }

  private boolean isInitialized() {
    return FLAGS_BIT_HELPER.isBitSet(INITIALIZED, m_flags);
  }

  private void setInitialized() {
    m_flags = FLAGS_BIT_HELPER.setBit(INITIALIZED, m_flags);
  }

  @Override
  public void setEnabledPermission(Permission p) {
    boolean b = true;
    if (p != null) {
      b = BEANS.get(IAccessControlService.class).checkPermission(p);
    }
    setEnabledGranted(b);
  }

  @Override
  public boolean isEnabledGranted() {
    IGroupBox box = getRootGroupBox();
    if (box == null) {
      return false;
    }
    return box.isEnabledGranted();
  }

  @Override
  public void setEnabledGranted(boolean b) {
    IGroupBox box = getRootGroupBox();
    if (box != null) {
      box.setEnabledGranted(b);
    }
  }

  @Override
  public void setVisiblePermission(Permission p) {
    boolean b = true;
    if (p != null) {
      b = BEANS.get(IAccessControlService.class).checkPermission(p);
    }
    setVisibleGranted(b);
  }

  @Override
  public boolean isVisibleGranted() {
    IGroupBox box = getRootGroupBox();
    if (box == null) {
      return false;
    }
    return box.isVisibleGranted();
  }

  @Override
  public void setVisibleGranted(boolean visible) {
    IGroupBox box = getRootGroupBox();
    if (box != null) {
      box.setVisibleGranted(visible);
    }
  }

  @Override
  public String getIconId() {
    return propertySupport.getPropertyString(PROP_ICON_ID);
  }

  @Override
  public void setIconId(String iconId) {
    propertySupport.setPropertyString(PROP_ICON_ID, iconId);
  }

  @Override
  public String getCssClass() {
    return propertySupport.getPropertyString(PROP_CSS_CLASS);
  }

  @Override
  public void setCssClass(String cssClass) {
    propertySupport.setPropertyString(PROP_CSS_CLASS, cssClass);
  }

  @Override
  public boolean isClosable() {
    return propertySupport.getPropertyBool(PROP_CLOSABLE);
  }

  @Override
  public void setClosable(boolean closable) {
    propertySupport.setPropertyBool(PROP_CLOSABLE, closable);
  }

  @Override
  public boolean isSaveNeededVisible() {
    return propertySupport.getPropertyBool(PROP_SAVE_NEEDED_VISIBLE);
  }

  @Override
  public void setSaveNeededVisible(boolean saveNeededVisible) {
    propertySupport.setPropertyBool(PROP_SAVE_NEEDED_VISIBLE, saveNeededVisible);
  }

  @Override
  public IMultiStatus getStatus() {
    final IMultiStatus ms = getStatusInternal();
    return (ms == null) ? null : new MultiStatus(ms);
  }

  @Override
  public boolean hasStatus(IStatus status) {
    final IMultiStatus ms = getStatusInternal();
    if (ms != null) {
      return ms.equals(status) || ms.containsStatus(status);
    }
    return false;
  }

  /**
   * @return the live error status
   */
  protected MultiStatus getStatusInternal() {
    return (MultiStatus) propertySupport.getProperty(PROP_STATUS);
  }

  @Override
  public void setStatus(IMultiStatus status) {
    setStatusInternal(new MultiStatus(status));
  }

  protected void setStatusInternal(MultiStatus status) {
    propertySupport.setProperty(PROP_STATUS, status);
  }

  @Override
  public void clearStatus() {
    propertySupport.setProperty(PROP_STATUS, null);
  }

  /**
   * Adds an error status
   */
  @Override
  public void addStatus(IStatus newStatus) {
    final MultiStatus status = ensureMultiStatus(getStatusInternal());
    // Create a copy, otherwise no PropertyChange event is fired
    final MultiStatus copy = new MultiStatus(status);
    copy.add(newStatus);
    setStatus(copy);
  }

  @Override
  public void removeStatus(IStatus status) {
    final MultiStatus ms = getStatusInternal();
    if (ms != null) {
      if (ms.equals(status)) {
        clearStatus();
      }
      else if (ms.containsStatus(status)) {
        // Create a copy, otherwise no PropertyChange event is fired
        final MultiStatus copy = new MultiStatus(ms);
        copy.removeAll(status);
        if (copy.getChildren().isEmpty()) {
          clearStatus();
        }
        else {
          setStatusInternal(copy);
        }
      }
    }
  }

  private MultiStatus ensureMultiStatus(IStatus s) {
    if (s instanceof MultiStatus) {
      return (MultiStatus) s;
    }
    final MultiStatus ms = new MultiStatus();
    if (s != null) {
      ms.add(s);
    }
    return ms;
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
        @Override
        public void dataChanged(Object... innerDataTypes) {
          interceptDataChanged(innerDataTypes);
        }
      };
    }
    getDesktop().addDataChangeListener(m_internalDataChangeListener, dataTypes);
  }

  /**
   * Unregister the {@link DataChangeListener} from the desktop for these dataTypes<br>
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

  protected IForm startInternalExclusive(IFormHandler handler) {
    if (m_blockingCondition.isBlocking()) {
      throw new ProcessingException("The form " + getFormId() + " has already been started");
    }
    for (IForm simCandidate : getDesktop().getSimilarViewForms(this)) {
      if (handler != null
          && simCandidate.getHandler() != null
          && handler.getClass().getName() == simCandidate.getHandler().getClass().getName()
          && simCandidate.getHandler().isOpenExclusive()
          && handler.isOpenExclusive()) {
        getDesktop().activateForm(simCandidate);
        return simCandidate;
      }
    }
    return startInternal(handler);
  }

  @Override
  public void start() {
    startInternal(getHandler());
  }

  /**
   * This method is called from the implemented handler methods in a explicit form subclass
   */
  protected IForm startInternal(final IFormHandler handler) {
    ClientRunContexts.copyCurrent().withForm(this).run(new IRunnable() {

      @Override
      public void run() throws Exception {
        if (isBlockingInternal()) {
          throw new IllegalStateException("The form " + getFormId() + " has already been started");
        }
        // Ensure that boolean is set not only once by the constructor
        setFormLoading(true);
        setHandler(handler);
        m_closeType = IButton.SYSTEM_TYPE_NONE;
        m_blockingCondition.setBlocking(true);
        try {
          initForm();
          loadStateInternal();

          // if form was disposed during initForm() or loadStateInternal()
          if (!isBlockingInternal()) {
            return;
          }

          if (getHandler().isGuiLess()) {
            // make sure the form is storing since it is not showing
            storeStateInternal();
            markSaved();
            doFinally();
            disposeFormInternal();
            return;
          }
        }
        catch (RuntimeException | PlatformError e) {
          disposeFormInternal();

          PlatformException pe = BEANS.get(PlatformExceptionTranslator.class).translate(e)
              .withContextInfo("form", AbstractForm.this.getClass().getName());
          if (pe instanceof VetoException) {
            VetoException ve = (VetoException) pe;
            interceptOnVetoException(ve, ve.getStatus().getCode());
          }
          throw pe;
        }

        setButtonsArmed(true);
        setCloseTimerArmed(true);
        setFormStarted(true);

        // Notify the UI to display this form.
        if (isShowOnStart()) {
          IDesktop desktop = getDesktop();
          if (desktop == null || !desktop.isOpened()) {
            throw new ProcessingException("There is no desktop or it is not open in the UI.");
          }
          else {
            desktop.showForm(AbstractForm.this);
          }
        }
      }
    });

    return this;
  }

  @Override
  public void startWizardStep(IWizardStep wizardStep, Class<? extends IFormHandler> handlerType) {
    if (handlerType != null) {
      IFormHandler formHandler = ConfigurationUtility.newInnerInstance(this, handlerType);
      setHandler(formHandler);
    }
    m_wizardStep = wizardStep;
    setShowOnStart(false);
    setAskIfNeedSave(false);
    final String systemButtonHiddenInWizard = "systemButtonHiddenInWizard";
    // hide top level process buttons with a system type
    for (IFormField f : getRootGroupBox().getFields()) {
      if (f instanceof IButton) {
        IButton b = (IButton) f;
        if (b.getSystemType() != IButton.SYSTEM_TYPE_NONE) {
          // hide
          b.setVisible(false, systemButtonHiddenInWizard);
        }
      }
    }
    // start
    start();
  }

  @Override
  public void startWizardStep(IWizardStep<?> wizardStep) {
    startWizardStep(wizardStep, null);
  }

  @Override
  public void waitFor() {
    // Do not exit upon interruption.
    m_blockingCondition.waitForUninterruptibly(ModelJobs.EXECUTION_HINT_UI_INTERACTION_REQUIRED);
  }

  protected static Class<?> getDataAnnotationValue(Class<?> clazz) {
    while (clazz != null && !Object.class.equals(clazz)) {
      Data annotation = clazz.getAnnotation(Data.class);
      if (annotation != null) {
        Class<?> value = annotation.value();
        if (value != null && !Object.class.equals(value)) {
          return value;
        }
      }
      clazz = clazz.getSuperclass();
    }
    return null;
  }

  private void exportExtensionProperties(Object o, IPropertyHolder target) {
    if (!(o instanceof IExtensibleObject)) {
      return;
    }
    for (IExtension<?> ex : ((IExtensibleObject) o).getAllExtensions()) {
      Class<?> dto = getDataAnnotationValue(ex.getClass());
      if (dto != null && !Object.class.equals(dto)) {
        Object propertyTarget = target.getContribution(dto);
        Map<String, Object> fieldProperties = BeanUtility.getProperties(ex, AbstractFormField.class, new FormDataPropertyFilter());
        BeanUtility.setProperties(propertyTarget, fieldProperties, false, null);
      }
    }
  }

  @Override
  public AbstractFormData createFormData() {
    return interceptCreateFormData();
  }

  @Override
  public void exportFormData(final AbstractFormData target) {
    // locally declared form properties
    Map<String, Object> properties = BeanUtility.getProperties(this, AbstractForm.class, new FormDataPropertyFilter());
    BeanUtility.setProperties(target, properties, false, null);
    // properties in extensions of form
    exportExtensionProperties(this, target);
    final Set<IFormField> exportedFields = new HashSet<IFormField>();

    // all fields
    Map<Integer, Map<String/* qualified field id */, AbstractFormFieldData>> breadthFirstMap = target.getAllFieldsRec();
    for (Map<String/* qualified field id */, AbstractFormFieldData> targetMap : breadthFirstMap.values()) {
      for (Map.Entry<String, AbstractFormFieldData> e : targetMap.entrySet()) {
        String fieldQId = e.getKey();
        AbstractFormFieldData data = e.getValue();

        FindFieldByFormDataIdVisitor v = new FindFieldByFormDataIdVisitor(fieldQId, this);
        visitFields(v);
        IFormField f = v.getField();
        if (f != null) {
          // field properties
          properties = BeanUtility.getProperties(f, AbstractFormField.class, new FormDataPropertyFilter());
          BeanUtility.setProperties(data, properties, false, null);
          exportExtensionProperties(f, data);

          // field state
          f.exportFormFieldData(data);

          // remember exported fields
          exportedFields.add(f);
        }
        else {
          LOG.warn("Cannot find field with id '{}' in form '{}' for DTO '{}'.", fieldQId, getClass().getName(), data.getClass().getName());
        }
      }
    }

    // visit remaining fields (there could be an extension with properties e.g. on a groupbox)
    final Holder<RuntimeException> exHolder = new Holder<>(RuntimeException.class);
    final Holder<PlatformError> errorHolder = new Holder<>(PlatformError.class);
    visitFields(new IFormFieldVisitor() {
      @Override
      public boolean visitField(IFormField field, int level, int fieldIndex) {
        if (exportedFields.contains(field)) {
          // already exported -> skip
          return true;
        }

        final IForm formOfField = field.getForm();
        if (formOfField == null) {
          // either form has not been initialized or the field is part of a composite field, that does not override setForminternal -> skip
          LOG.info("Extension properties are not exported for fields on which getForm() returns null. "
              + "Ensure that the form is initialized and that the field's parent invokes field.setFormInternal(IForm) [exportingForm={}, field={}]",
              AbstractForm.this.getClass().getName(), field.getClass().getName());
          return true;
        }
        if (formOfField != AbstractForm.this) {
          // field belongs to another form -> skip
          return true;
        }

        try {
          exportExtensionProperties(field, target);
        }
        catch (RuntimeException e) {
          exHolder.setValue(e);
        }
        catch (PlatformError e) {
          errorHolder.setValue(e);
        }

        return exHolder.getValue() == null && errorHolder.getValue() == null;
      }
    });
    if (exHolder.getValue() != null) {
      throw exHolder.getValue();
    }
    else if (errorHolder.getValue() != null) {
      throw errorHolder.getValue();
    }
  }

  @Override
  public void importFormData(AbstractFormData source) {
    importFormData(source, false, null);
  }

  @Override
  public void importFormData(AbstractFormData source, boolean valueChangeTriggersEnabled) {
    importFormData(source, valueChangeTriggersEnabled, null);
  }

  @Override
  public void importFormData(AbstractFormData source, boolean valueChangeTriggersEnabled, IPropertyFilter filter) {
    importFormData(source, valueChangeTriggersEnabled, filter, null);
  }

  private void removeNotSetProperties(IPropertyHolder dto, Map<String, Object> properties) {
    for (Iterator<String> it = properties.keySet().iterator(); it.hasNext();) {
      String propertyId = it.next();
      AbstractPropertyData pd = dto.getPropertyById(propertyId);
      if (pd != null && !pd.isValueSet()) {
        it.remove();
      }
    }
  }

  private void importProperties(IPropertyHolder source, Object target, Class<?> stopClass, IPropertyFilter filter) {
    // local properties
    Map<String, Object> properties = BeanUtility.getProperties(source, stopClass, filter);
    if (!properties.isEmpty()) {
      removeNotSetProperties(source, properties);
      BeanUtility.setProperties(target, properties, false, null);
    }

    // properties of the extensions
    List<Object> allContributions = source.getAllContributions();
    if (!allContributions.isEmpty()) {
      for (Object con : allContributions) {
        if (con instanceof IPropertyHolder) {
          IPropertyHolder data = (IPropertyHolder) con;
          Map<String, Object> extensionProperties = BeanUtility.getProperties(data, stopClass, filter);
          if (!extensionProperties.isEmpty()) {
            Object clientPart = getClientPartOfExtensionOrContributionRec(data, target);
            if (clientPart != null) {
              removeNotSetProperties(data, extensionProperties);
              BeanUtility.setProperties(clientPart, extensionProperties, false, null);
            }
            else {
              LOG.warn("cannot find extension for property data '{}' in form '{}'.", data.getClass().getName(), this.getClass().getName());
            }
          }
        }
      }
    }
  }

  private Object getClientPartOfExtensionOrContribution(Object extToSearch, Object owner) {
    if (owner instanceof IExtensibleObject) {
      IExtensibleObject exOwner = (IExtensibleObject) owner;
      for (IExtension<?> ex : exOwner.getAllExtensions()) {
        Class<?> dto = getDataAnnotationValue(ex.getClass());
        if (extToSearch.getClass().equals(dto)) {
          return ex;
        }
      }
    }
    if (owner instanceof IContributionOwner) {
      IContributionOwner compOwner = (IContributionOwner) owner;
      for (Object o : compOwner.getAllContributions()) {
        FormData annotation = o.getClass().getAnnotation(FormData.class);
        if (annotation != null && annotation.value().equals(extToSearch.getClass())) {
          return o;
        }
      }
    }
    return null;
  }

  private Object getClientPartOfExtensionOrContributionRec(final Object extToSearch, Object owner) {
    Object ext = getClientPartOfExtensionOrContribution(extToSearch, owner);
    if (ext != null) {
      return ext;
    }

    // search for the extension in the children
    final IHolder<Object> result = new Holder<Object>(Object.class);
    IFormFieldVisitor visitor = new IFormFieldVisitor() {
      @Override
      public boolean visitField(IFormField field, int level, int fieldIndex) {
        result.setValue(getClientPartOfExtensionOrContribution(extToSearch, field));
        return result.getValue() == null;
      }
    };
    if (owner instanceof IForm) {
      ((IForm) owner).visitFields(visitor);
    }
    else if (owner instanceof IFormField) {
      ((IFormField) owner).acceptVisitor(visitor, 0, 0, true);
    }
    return result.getValue();
  }

  private static Class<?> getFieldStopClass(Object data) {
    if (data instanceof AbstractFormFieldData) {
      return ((AbstractFormFieldData) data).getFieldStopClass();
    }
    return AbstractFormFieldData.class;
  }

  @Override
  public void importFormData(AbstractFormData source, boolean valueChangeTriggersEnabled, IPropertyFilter filter, IFormFieldFilter formFieldFilter) {
    Assertions.assertNotNull(source, "source form data must not be null");
    if (filter == null) {
      filter = new FormDataPropertyFilter();
    }

    // form properties
    importProperties(source, this, AbstractFormData.class, filter);

    // sort fields, first non-slave fields, then slave fields in transitive order
    LinkedList<IFormField> masterList = new LinkedList<IFormField>();
    LinkedList<IFormField> slaveList = new LinkedList<IFormField>();
    HashMap<IFormField, AbstractFormFieldData> dataMap = new HashMap<IFormField, AbstractFormFieldData>();

    // collect fields and split them into masters/slaves
    Map<Integer, Map<String/* qualified field id */, AbstractFormFieldData>> breadthFirstMap = source.getAllFieldsRec();
    for (Map<String/* qualified field id */, AbstractFormFieldData> sourceMap : breadthFirstMap.values()) {
      for (Map.Entry<String, AbstractFormFieldData> e : sourceMap.entrySet()) {
        String fieldQId = e.getKey();
        AbstractFormFieldData data = e.getValue();
        FindFieldByFormDataIdVisitor v = new FindFieldByFormDataIdVisitor(fieldQId, this);
        visitFields(v);
        IFormField f = v.getField();
        if (f != null) {
          if (formFieldFilter == null || formFieldFilter.accept(f)) {
            dataMap.put(f, data);
            if (f.getMasterField() != null) {
              int index = slaveList.indexOf(f.getMasterField());
              if (index >= 0) {
                slaveList.add(index + 1, f);
              }
              else {
                slaveList.addFirst(f);
              }
            }
            else {
              masterList.add(f);
            }
          }
        }
        else {
          LOG.warn("cannot find field data for '{}' in form '{}'.", fieldQId, getClass().getName());
        }
      }
    }
    for (IFormField f : masterList) {
      importFormField(f, dataMap, valueChangeTriggersEnabled, filter);
    }
    for (IFormField f : slaveList) {
      importFormField(f, dataMap, valueChangeTriggersEnabled, filter);
    }
  }

  private void importFormField(IFormField f, Map<IFormField, AbstractFormFieldData> dataMap, boolean valueChangeTriggersEnabled, IPropertyFilter filter) {
    AbstractFormFieldData data = dataMap.get(f);
    // form field properties
    importProperties(data, f, getFieldStopClass(data), filter);
    // field state
    f.importFormFieldData(data, valueChangeTriggersEnabled);
  }

  public static String parseFormId(String className) {
    String s = className;
    int i = Math.max(s.lastIndexOf('$'), s.lastIndexOf('.'));
    s = s.substring(i + 1);
    return s;
  }

  @Override
  public String getFormId() {
    return parseFormId(getClass().getName());
  }

  /**
   * <p>
   * <ul>
   * <li>If a classId was set with {@link #setClassId(String)} this value is returned.
   * <li>Else if the class is annotated with {@link ClassId}, the annotation value is returned.
   * <li>Otherwise the class name is returned.
   * </ul>
   */
  @Override
  public String classId() {
    if (m_classId != null) {
      return m_classId;
    }
    String simpleClassId = ConfigurationUtility.getAnnotatedClassIdWithFallback(getClass());
    if (getOuterFormField() != null) {
      return simpleClassId + ID_CONCAT_SYMBOL + getOuterFormField().classId();
    }
    return simpleClassId;
  }

  @Override
  public void setClassId(String classId) {
    m_classId = classId;
  }

  @Override
  public IFormHandler getHandler() {
    return m_handler;
  }

  @Override
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

  @Override
  public IWizard getWizard() {
    return (getWizardStep() != null) ? getWizardStep().getWizard() : null;
  }

  @Override
  public IWizardStep getWizardStep() {
    return m_wizardStep;
  }

  @Override
  public List<IFormField> getAllFields() {
    P_AbstractCollectingFieldVisitor<IFormField> v = new P_AbstractCollectingFieldVisitor<IFormField>() {
      @Override
      public boolean visitField(IFormField field, int level, int fieldIndex) {
        collect(field);
        return true;
      }
    };
    visitFields(v);
    return v.getCollection();
  }

  @Override
  public boolean visitFields(IFormFieldVisitor visitor) {
    return getRootGroupBox().visitFields(visitor);
  }

  /**
   * @return {@link IDesktop} from current calling context.
   */
  protected IDesktop getDesktop() {
    return IDesktop.CURRENT.get();
  }

  @Override
  public final SearchFilter getSearchFilter() {
    if (m_searchFilter == null) {
      resetSearchFilter();
    }
    return m_searchFilter;
  }

  @Override
  public final void setSearchFilter(SearchFilter searchFilter) {
    m_searchFilter = searchFilter;
  }

  /**
   * Alias for {@link #resetSearchFilter()}
   */
  public void rebuildSearchFilter() {
    resetSearchFilter();
  }

  @Override
  public void resetSearchFilter() {
    if (m_searchFilter == null) {
      SearchFilter filter;
      ISearchFilterService sfs = BEANS.get(ISearchFilterService.class);
      if (sfs != null) {
        filter = sfs.createNewSearchFilter();
      }
      else {
        filter = new SearchFilter();
      }
      m_searchFilter = filter;
    }
    try {
      interceptResetSearchFilter(m_searchFilter);
    }
    catch (Exception e) {
      BEANS.get(ExceptionHandler.class).handle(e);
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
   * <p>
   * Attaches a filled form data to the search filter if {@link #execCreateFormData()} returns a value.
   *
   * @param searchFilter
   *          is never null
   */
  @ConfigOperation
  @Order(10)
  protected void execResetSearchFilter(final SearchFilter searchFilter) {
    searchFilter.clear();
    // add verbose field texts
    // do not use visitor, so children can block traversal on whole subtrees
    getRootGroupBox().applySearch(searchFilter);
    // add verbose form texts
    interceptAddSearchTerms(searchFilter);
    // override may add form data
    AbstractFormData data = createFormData();
    if (data != null) {
      exportFormData(data);
      getSearchFilter().setFormData(data);
    }
  }

  /**
   * Creates an empty form data that can be used for example by {@link #interceptResetSearchFilter(SearchFilter)}.
   * <p>
   * The default creates a new instance based on the {@link FormData} annotation.
   *
   * @since 3.8
   */
  @ConfigOperation
  @Order(11)
  protected AbstractFormData execCreateFormData() {
    Class<? extends AbstractFormData> formDataClass = getFormDataClass();
    if (formDataClass == null) {
      return null;
    }
    try {
      return formDataClass.newInstance();
    }
    catch (InstantiationException | IllegalAccessException e) {
      BEANS.get(ExceptionHandler.class).handle(new ProcessingException("error creating instance of class '" + formDataClass.getName() + "'.", e));
      return null;
    }
  }

  protected Class<? extends AbstractFormData> getFormDataClass() {
    FormData formDataAnnotation = getClass().getAnnotation(FormData.class);

    //look in superclasses for annotation
    Class<?> superclazz = getClass().getSuperclass();
    while (formDataAnnotation == null && superclazz != null) {
      formDataAnnotation = superclazz.getAnnotation(FormData.class);
      superclazz = superclazz.getSuperclass();
    }

    if (formDataAnnotation == null) {
      //no annotation found..
      return null;
    }

    @SuppressWarnings("unchecked")
    Class<? extends AbstractFormData> formDataClass = formDataAnnotation.value();
    if (formDataClass == null) {
      return null;
    }
    if (AbstractFormData.class.isAssignableFrom(formDataClass) && !Modifier.isAbstract(formDataClass.getModifiers())) {
      return formDataClass;
    }
    return null;
  }

  @Override
  public boolean isFormStored() {
    return STATE_BIT_HELPER.isBitSet(FORM_STORED, m_states);
  }

  @Override
  public void setFormStored(boolean b) {
    m_states = STATE_BIT_HELPER.changeBit(FORM_STORED, b, m_states);
  }

  @Override
  public boolean isFormLoading() {
    return STATE_BIT_HELPER.isBitSet(FORM_LOADING, m_states);
  }

  private void setFormLoading(boolean b) {
    m_states = STATE_BIT_HELPER.changeBit(FORM_LOADING, b, m_states);
  }

  /**
   * Mainbox getter
   */
  @Override
  public IGroupBox getRootGroupBox() {
    return m_mainBox;
  }

  @Override
  public IForm getOuterForm() {
    return m_wrappedFormField != null ? m_wrappedFormField.getForm() : null;
  }

  @Override
  public IWrappedFormField getOuterFormField() {
    return m_wrappedFormField;
  }

  @Override
  public void setWrapperFieldInternal(IWrappedFormField w) {
    m_wrappedFormField = w;
  }

  @Override
  public IFormField getFieldById(final String id) {
    return getRootGroupBox().getFieldById(id);
  }

  @Override
  public <T extends IFormField> T getFieldById(String id, Class<T> type) {
    return getRootGroupBox().getFieldById(id, type);
  }

  @Override
  public <T extends IFormField> T getFieldByClass(Class<T> c) {
    return getRootGroupBox().getFieldByClass(c);
  }

  /**
   * override in subclasses to perform form initialization before handler starts
   */
  protected void postInitConfig() {
    FormUtility.postInitConfig(this);
    FormUtility.rebuildFieldGrid(this, true);
  }

  @Override
  public void initForm() {
    // form
    initFormInternal();
    // fields
    FormUtility.initFormFields(this);
    // custom
    interceptInitForm();
  }

  protected void initFormInternal() {
    calculateSaveNeeded();
  }

  @Override
  public int getCloseSystemType() {
    return m_closeType;
  }

  @Override
  public void setCloseSystemType(int type) {
    m_closeType = type;
  }

  /**
   * do not use or override this internal method
   */
  protected void loadStateInternal() {
    fireFormLoadBefore();
    if (!isBlockingInternal()) {
      return;
    }
    getHandler().onLoad();
    if (!isBlockingInternal()) {
      return;
    }
    fireFormLoadAfter();
    if (!isBlockingInternal()) {
      return;
    }
    // set all values to 'unchanged'
    markSaved();
    rebuildSearchFilter();
    setFormLoading(false);
    getHandler().onPostLoad();
    if (!isBlockingInternal()) {
      return;
    }
    // if not visible mode, mark form changed
    if (getHandler().isGuiLess()) {
      touch();
    }
    fireFormLoadComplete();
  }

  /**
   * Store state of form, regardless of validity and completeness do not use or override this internal method directly
   */
  protected void storeStateInternal() {
    if (!m_blockingCondition.isBlocking()) {
      String msg = TEXTS.get("FormDisposedMessage", getTitle());
      LOG.error(msg);
      throw new VetoException(msg);
    }
    fireFormStoreBefore();
    setFormStored(true);
    try {
      rebuildSearchFilter();
      m_searchFilter.setCompleted(true);
      getHandler().onStore();
      interceptStored();
      if (!isFormStored()) {
        //the form was marked as not stored in AbstractFormHandler#execStore() or AbstractForm#execStored().
        ProcessingException e = new ProcessingException("Form was marked as not stored.");
        e.consume();
        throw e;
      }
    }
    catch (RuntimeException | PlatformError e) {
      // clear search
      if (m_searchFilter != null) {
        m_searchFilter.clear();
      }
      // store was not successfully stored
      setFormStored(false);
      if (e instanceof RuntimeException) { // NOSONAR
        throwVetoExceptionInternal((RuntimeException) e);
      }
      else if (e instanceof PlatformError) { // NOSONAR
        throw e;
      }
      // if exception was caught and suppressed, this form was after all successfully stored
      // normally this code is not reached since the exception will be passed out
      setFormStored(true);
    }
    fireFormStoreAfter();
  }

  /**
   * do not use or override this internal method
   */
  protected void discardStateInternal() {
    fireFormDiscarded();
    getHandler().onDiscard();
  }

  @Override
  public void setCloseTimer(int seconds) {
    removeCloseTimer();

    if (seconds > 0) {
      setCloseTimerArmed(true);
      m_closeTimerFuture = installFormCloseTimer(seconds);
    }
  }

  /**
   * do not use or override this internal method
   */
  protected void throwVetoExceptionInternal(final RuntimeException e) {
    PlatformException pe = BEANS.get(PlatformExceptionTranslator.class).translate(e)
        .withContextInfo("form", getClass().getName());
    if (pe instanceof VetoException && !pe.isConsumed()) {
      VetoException ve = (VetoException) pe;
      interceptOnVetoException(ve, ve.getStatus().getCode());
      ve.consume(); // if it was not re-thrown it is assumed to be consumed
    }
    throw e;
  }

  @Override
  public void removeCloseTimer() {
    setCloseTimerArmed(false);
    setSubTitle(null);
  }

  @Override
  public void validateForm() {
    if (!interceptCheckFields()) {
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
    P_AbstractCollectingFieldVisitor<IValidateContentDescriptor> v = new P_AbstractCollectingFieldVisitor<IValidateContentDescriptor>() {
      @Override
      public boolean visitField(IFormField f, int level, int fieldIndex) {
        IValidateContentDescriptor desc = f.validateContent();
        if (desc != null) {
          if (desc.getErrorStatus() != null) {
            invalidTexts.add(desc.getDisplayText() + ": " + desc.getErrorStatus().getMessage());
          }
          else {
            mandatoryTexts.add(desc.getDisplayText());
          }
          if (getCollectionCount() == 0) {
            collect(desc);
          }
        }
        return true;
      }
    };
    visitFields(v);
    if (v.getCollectionCount() > 0) {
      IValidateContentDescriptor firstProblem = v.getCollection().get(0);
      if (LOG.isInfoEnabled()) {
        LOG.info("there are fields with errors");
      }
      if (firstProblem != null) {
        firstProblem.activateProblemLocation();
      }
      throw new VetoException().withHtmlMessage(createValidationMessageBoxHtml(invalidTexts, mandatoryTexts));
    }
    if (!interceptValidate()) {
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

  protected IHtmlContent createValidationMessageBoxHtml(final List<String> invalidTexts, final List<String> mandatoryTexts) {
    List<CharSequence> content = new ArrayList<>();

    if (mandatoryTexts.size() > 0) {
      content.add(HTML.bold(ScoutTexts.get("FormEmptyMandatoryFieldsMessage")));

      List<IHtmlListElement> mandatoryTextElements = new ArrayList<>();
      for (String e : mandatoryTexts) {
        mandatoryTextElements.add(HTML.li(e));
      }
      content.add(HTML.ul(mandatoryTextElements));
      content.add(HTML.br());
    }
    if (invalidTexts.size() > 0) {
      content.add(HTML.bold(ScoutTexts.get("FormInvalidFieldsMessage")));

      List<IHtmlListElement> invalidTextElements = new ArrayList<>();
      for (String e : invalidTexts) {
        invalidTextElements.add(HTML.li(e));
      }
      content.add(HTML.ul(invalidTextElements));
    }
    return HTML.fragment(content);
  }

  /**
   * attach a statement (mode) that is executed every interval
   *
   * @since Build 195 09.02.2005, imo
   */
  @Override
  public void setTimer(String timerId, int seconds) {
    removeTimer(timerId);
    if (seconds > 0) {
      IFuture<Void> future = startTimer(seconds, timerId);
      m_timerFutureMap.put(timerId, future);
    }
  }

  /**
   * remove a statement (mode) that is executed every interval
   *
   * @since Build 195 09.02.2005, imo
   */
  @Override
  public void removeTimer(String timerId) {
    IFuture<Void> future = m_timerFutureMap.remove(timerId);
    if (future != null) {
      future.cancel(false);
    }
  }

  @Override
  public void doClose() {
    if (!isBlockingInternal()) {
      return;
    }
    try {
      m_closeType = IButton.SYSTEM_TYPE_CLOSE;
      discardStateInternal();
      doFinally();
      disposeFormInternal();
    }
    catch (RuntimeException | PlatformError e) {
      throw BEANS.get(PlatformExceptionTranslator.class).translate(e)
          .withContextInfo("form", getClass().getName());
    }
  }

  @Override
  public void doCancel() {
    if (!isBlockingInternal()) {
      return;
    }
    m_closeType = IButton.SYSTEM_TYPE_CANCEL;
    try {
      // ensure all fields have the right save-needed-state
      checkSaveNeeded();
      // find any fields that needs save
      P_AbstractCollectingFieldVisitor<IFormField> collector = new P_AbstractCollectingFieldVisitor<IFormField>() {
        @Override
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
        int result = MessageBoxes.createYesNoCancel().withDisplayParent(this).withHeader(getCancelVerificationText()).withSeverity(IStatus.INFO).show();

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
    catch (RuntimeException | PlatformError e) {
      m_closeType = IButton.SYSTEM_TYPE_NONE;
      throw BEANS.get(PlatformExceptionTranslator.class).translate(e)
          .withContextInfo("form", getClass().getName());
    }
  }

  @Override
  public void doReset() {
    setFormLoading(true);
    // reset values
    P_AbstractCollectingFieldVisitor v = new P_AbstractCollectingFieldVisitor() {
      @Override
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
    try {
      visitFields(v);
      // init again
      initForm();
      // load again
      loadStateInternal();
    }
    catch (RuntimeException | PlatformError e) {
      throw BEANS.get(PlatformExceptionTranslator.class).translate(e)
          .withContextInfo("form", getClass().getName());
    }
  }

  /**
   * Save data and close the form. It will make this decision based on {@link #isSaveNeeded}. Saving usually involves
   * calling the <code>execStore</code> method of the current form handler.
   *
   * @see AbstractFormHandler#execStore()
   */
  @Override
  public void doOk() {
    if (!isBlockingInternal()) {
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
    catch (RuntimeException e) {
      m_closeType = IButton.SYSTEM_TYPE_NONE;
      throwVetoExceptionInternal(e);
    }
    catch (PlatformError e) {
      m_closeType = IButton.SYSTEM_TYPE_NONE;
      throw e;
    }
  }

  @Override
  public void doSaveWithoutMarkerChange() {
    if (!isBlockingInternal()) {
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
    catch (RuntimeException e) {
      m_closeType = IButton.SYSTEM_TYPE_NONE;
      throwVetoExceptionInternal(e);
    }
    catch (PlatformError e) {
      m_closeType = IButton.SYSTEM_TYPE_NONE;
      throw e;
    }
  }

  @Override
  public void doSave() {
    if (!isBlockingInternal()) {
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
    catch (RuntimeException e) {
      m_closeType = IButton.SYSTEM_TYPE_NONE;
      throwVetoExceptionInternal(e);
    }
    catch (PlatformError e) {
      m_closeType = IButton.SYSTEM_TYPE_NONE;
      throw e;
    }
  }

  @Override
  public void setAllEnabled(final boolean b) {
    IGroupBox box = getRootGroupBox();
    if (box != null) {
      box.setEnabled(b, false, true);
    }
  }

  @Override
  public void doFinally() {
    try {
      getHandler().onFinally();
    }
    catch (RuntimeException | PlatformError e) {
      throw BEANS.get(PlatformExceptionTranslator.class).translate(e)
          .withContextInfo("form", getClass().getName());
    }
  }

  @Override
  public String getCancelVerificationText() {
    return m_cancelVerificationText;
  }

  @Override
  public void setCancelVerificationText(String text) {
    m_cancelVerificationText = text;
  }

  @Override
  public List<? extends IFormField> getInvalidFields() {
    // check all fields that might be invalid
    P_AbstractCollectingFieldVisitor<IFormField> v = new P_AbstractCollectingFieldVisitor<IFormField>() {
      @Override
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

  @Override
  public final void checkSaveNeeded() {
    // call checkSaveNeeded on all fields
    P_AbstractCollectingFieldVisitor<IFormField> v = new P_AbstractCollectingFieldVisitor<IFormField>() {
      @Override
      public boolean visitField(IFormField f, int level, int fieldIndex) {
        f.checkSaveNeeded();
        return true;
      }
    };
    visitFields(v);
    calculateSaveNeeded();
  }

  private boolean/* ok */ checkForVerifyingFields() {
    // check all fields that might be invalid
    P_AbstractCollectingFieldVisitor v = new P_AbstractCollectingFieldVisitor() {
      @Override
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
    if (isBlockingInternal()) {
      try {
        // check if there is an active close, cancel or finish button
        final Set<Integer> enabledSystemTypes = new HashSet<Integer>();
        final Set<IButton> enabledSystemButtons = new HashSet<IButton>();
        IFormFieldVisitor v = new IFormFieldVisitor() {
          @Override
          public boolean visitField(IFormField field, int level, int fieldIndex) {
            if (field instanceof IButton) {
              IButton b = (IButton) field;
              if (b.isEnabled() && b.isVisible() && b.isEnabledProcessing()) {
                enabledSystemTypes.add(b.getSystemType());
                enabledSystemButtons.add(b);
              }
            }
            return true;
          }
        };
        try {
          visitFields(v);
          for (IButton b : enabledSystemButtons) {
            b.setEnabledProcessing(false);
          }
          interceptOnCloseRequest(kill, enabledSystemTypes);
        }
        finally {
          for (IButton b : enabledSystemButtons) {
            b.setEnabledProcessing(true);
          }
        }
      }
      catch (RuntimeException | PlatformError e) {
        throw BEANS.get(PlatformExceptionTranslator.class).translate(e)
            .withContextInfo("form", getClass().getName());
      }
    }
  }

  @Override
  public void touch() {
    getRootGroupBox().touch();
  }

  protected void calculateSaveNeeded() {
    propertySupport.setPropertyBool(PROP_SAVE_NEEDED, interceptIsSaveNeeded());
  }

  @Override
  public boolean isSaveNeeded() {
    return propertySupport.getPropertyBool(PROP_SAVE_NEEDED);
  }

  @Override
  public void markSaved() {
    getRootGroupBox().markSaved();
  }

  @Override
  public boolean isEmpty() {
    return getRootGroupBox().isEmpty();
  }

  /**
   * do not use or override this internal method
   */
  protected void disposeFormInternal() {
    if (!isBlockingInternal()) {
      return;
    }

    try {
      setButtonsArmed(false);
      setCloseTimerArmed(false);
      setFormStarted(false);

      // Cancel and remove timers
      Iterator<IFuture<Void>> iterator = m_timerFutureMap.values().iterator();
      while (iterator.hasNext()) {
        iterator.next().cancel(false);
        iterator.remove();
      }

      // Dispose Fields and Form
      try {
        FormUtility.disposeFormFields(this);
        interceptDisposeForm();
        unregisterDataChangeListener((Object[]) null);
      }
      catch (Exception t) {
        LOG.warn("Failed to dispose Form {}", getClass().getName(), t);
      }

      // Detach Form from Desktop.
      getDesktop().hideForm(this);

      // Link all Forms which have this Form as 'displayParent' with the Desktop.
      List<IForm> forms = getDesktop().getForms(this);
      for (IForm childForm : forms) {
        childForm.setDisplayParent(getDesktop());
      }
    }
    finally {
      m_blockingCondition.setBlocking(false);
      fireFormClosed();
    }
  }

  @Override
  public boolean isShowing() {
    return getDesktop().isShowing(this);
  }

  @Override
  public boolean isFormClosed() {
    return !isBlockingInternal();
  }

  @Override
  public boolean isFormStartable() {
    return !isFormStarted() && !isBlockingInternal();
  }

  @Override
  public boolean isFormStarted() {
    return STATE_BIT_HELPER.isBitSet(FORM_STARTED, m_states);
  }

  private void setFormStarted(boolean started) {
    m_states = STATE_BIT_HELPER.changeBit(FORM_STARTED, started, m_states);
  }

  protected boolean isBlockingInternal() {
    return m_blockingCondition.isBlocking();
  }

  @Override
  public Object getProperty(String name) {
    return propertySupport.getProperty(name);
  }

  @Override
  public void setProperty(String name, Object value) {
    propertySupport.setProperty(name, value);
  }

  @Override
  public boolean hasProperty(String name) {
    return propertySupport.hasProperty(name);
  }

  @Override
  public void loadFromXmlString(String xml) {
    if (xml == null) {
      return;
    }
    Document xmlDocument = XmlUtility.getXmlDocument(xml);
    loadFromXml(xmlDocument.getDocumentElement());
  }

  @Override
  public String storeToXmlString() {
    try {
      Document e = storeToXml();
      return XmlUtility.wellformDocument(e);
    }
    catch (RuntimeException e) {
      throw BEANS.get(PlatformExceptionTranslator.class)
          .translate(e)
          .withContextInfo("form", getClass().getName());
    }
  }

  @Override
  public Document storeToXml() {
    Document doc = XmlUtility.createNewXmlDocument("form-state");
    storeToXml(doc.getDocumentElement());
    return doc;
  }

  @Override
  public void storeToXml(Element root) {
    root.setAttribute("formId", getFormId());
    root.setAttribute("formQname", getClass().getName());
    // add custom properties
    Element xProps = root.getOwnerDocument().createElement("properties");
    root.appendChild(xProps);
    IPropertyFilter filter = new IPropertyFilter() {
      @Override
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
    Map<String, Object> props = BeanUtility.getProperties(this, AbstractForm.class, filter);
    for (Entry<String, Object> entry : props.entrySet()) {
      try {
        Element xProp = root.getOwnerDocument().createElement("property");
        xProps.appendChild(xProp);
        xProp.setAttribute("name", entry.getKey());
        XmlUtility.setObjectAttribute(xProp, "value", entry.getValue());
      }
      catch (Exception e) {
        throw new ProcessingException("property " + entry.getKey() + " with value " + entry.getValue(), e);
      }
    }
    // add fields
    final Element xFields = root.getOwnerDocument().createElement("fields");
    root.appendChild(xFields);
    final Holder<RuntimeException> exceptionHolder = new Holder<>(RuntimeException.class);
    final Holder<PlatformError> errorHolder = new Holder<>(PlatformError.class);
    P_AbstractCollectingFieldVisitor v = new P_AbstractCollectingFieldVisitor() {
      @Override
      public boolean visitField(IFormField field, int level, int fieldIndex) {
        if (field.getForm() != AbstractForm.this) {
          // field is part of a wrapped form and is handled by the AbstractWrappedFormField
          return true;
        }
        Element xField = xFields.getOwnerDocument().createElement("field");
        try {
          field.storeToXml(xField);
          xFields.appendChild(xField);
        }
        catch (RuntimeException e) {
          exceptionHolder.setValue(e);
          return false;
        }
        catch (PlatformError e) {
          errorHolder.setValue(e);
          return false;
        }
        return true;
      }
    };
    visitFields(v);
    if (exceptionHolder.getValue() != null) {
      throw exceptionHolder.getValue();
    }
    else if (errorHolder.getValue() != null) {
      throw errorHolder.getValue();
    }
  }

  @Override
  public void loadFromXml(Element root) {
    String formId = getFormId();
    String xmlId = root.getAttribute("formId");
    if (!formId.equals(xmlId)) {
      throw new ProcessingException("xml id='{}' does not match form id='{}'", xmlId, formId);
    }

    // load properties
    Map<String, Object> props = new HashMap<String, Object>();
    Element xProps = XmlUtility.getFirstChildElement(root, "properties");
    if (xProps != null) {
      for (Element xProp : XmlUtility.getChildElements(xProps, "property")) {
        String name = xProp.getAttribute("name");
        try {
          Object o = XmlUtility.getObjectAttribute(xProp, "value");
          props.put(name, o);
        }
        catch (Exception e) {
          LOG.warn("Could not load XML property {}", name, e);
        }
      }
    }

    BeanUtility.setProperties(this, props, true, null);
    // load fields
    Element xFields = XmlUtility.getFirstChildElement(root, "fields");
    if (xFields != null) {
      for (Element xField : XmlUtility.getChildElements(xFields, "field")) {
        List<String> xmlFieldIds = new LinkedList<String>();
        // add enclosing field path to xml field IDs
        for (Element element : XmlUtility.getChildElements(xField, "enclosingField")) {
          xmlFieldIds.add(element.getAttribute("fieldId"));
        }
        xmlFieldIds.add(xField.getAttribute("fieldId"));
        FindFieldByXmlIdsVisitor v = new FindFieldByXmlIdsVisitor(xmlFieldIds.toArray(new String[xmlFieldIds.size()]));
        visitFields(v);
        IFormField f = v.getField();
        if (f != null) {
          f.loadFromXml(xField);
        }
      }
    }
    // in all tabboxes select the first tab that contains data, iff the current
    // tab has no values set
    getRootGroupBox().visitFields(new IFormFieldVisitor() {
      @Override
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
    });
  }

  @Override
  public void doExportXml(boolean saveAs) {
    // export search parameters
    try (ByteArrayOutputStream bos = new ByteArrayOutputStream(); Writer w = new OutputStreamWriter(bos, StandardCharsets.UTF_8)) {
      XmlUtility.wellformDocument(storeToXml(), w);
      BinaryResource res = new BinaryResource("form.xml", bos.toByteArray());
      getDesktop().openUri(res, OpenUriAction.DOWNLOAD);
    }
    catch (Exception e) {
      BEANS.get(ExceptionHandler.class).handle(new ProcessingException(ScoutTexts.get("FormExportXml") + " " + getTitle(), e));
    }
  }

  @Override
  public void doImportXml() {
    try {
      List<BinaryResource> a = new FileChooser(Collections.singletonList("xml"), false).startChooser();
      if (a.size() == 1) {
        BinaryResource newPath = a.get(0);
        try (InputStream in = new ByteArrayInputStream(newPath.getContent())) { // NOSONAR
          Document doc = XmlUtility.getXmlDocument(in);
          // load xml to search
          loadFromXml(doc.getDocumentElement());
        }
        catch (Exception e) {
          LOG.warn("Could not load XML from file: {}", newPath, e);
          MessageBoxes.createOk().withDisplayParent(this).withHeader(TEXTS.get("LoadFormXmlFailedText")).show();
        }
      }
    }
    catch (Exception e) {
      BEANS.get(ExceptionHandler.class).handle(e);
    }
  }

  @Override
  public void activate() {
    getDesktop().activateForm(this);
  }

  @Override
  public void requestFocus(IFormField f) {
    if (f == null || f.getForm() != this) {
      return;
    }
    fireRequestFocus(f);
  }

  /**
   * @return Returns a map having old field classes as keys and replacement field classes as values. <code>null</code>
   *         is returned if no form fields are replaced. Do not use this internal method.
   * @since 3.8.2
   */
  public Map<Class<?>, Class<? extends IFormField>> getFormFieldReplacementsInternal() {
    return m_fieldReplacements;
  }

  /**
   * Registers the given form field replacements on this form. Do not use this internal method.
   *
   * @param replacements
   *          Map having old field classes as key and replacing field classes as values.
   * @since 3.8.2
   */
  public void registerFormFieldReplacementsInternal(Map<Class<?>, Class<? extends IFormField>> replacements) {
    if (replacements == null || replacements.isEmpty()) {
      return;
    }
    if (m_fieldReplacements == null) {
      m_fieldReplacements = new HashMap<Class<?>, Class<? extends IFormField>>();
    }
    m_fieldReplacements.putAll(replacements);
  }

  /**
   * Model Observer.
   */
  @Override
  public void addFormListener(FormListener listener) {
    m_listenerList.add(FormListener.class, listener);
  }

  @Override
  public void removeFormListener(FormListener listener) {
    m_listenerList.remove(FormListener.class, listener);
  }

  protected IEventHistory<FormEvent> createEventHistory() {
    return new DefaultFormEventHistory(5000L);
  }

  @Override
  public IEventHistory<FormEvent> getEventHistory() {
    return m_eventHistory;
  }

  private void fireFormLoadBefore() {
    fireFormEvent(new FormEvent(this, FormEvent.TYPE_LOAD_BEFORE));
  }

  private void fireFormLoadAfter() {
    fireFormEvent(new FormEvent(this, FormEvent.TYPE_LOAD_AFTER));
  }

  private void fireFormLoadComplete() {
    fireFormEvent(new FormEvent(this, FormEvent.TYPE_LOAD_COMPLETE));
  }

  private void fireFormStoreBefore() {
    fireFormEvent(new FormEvent(this, FormEvent.TYPE_STORE_BEFORE));
  }

  private void fireFormDiscarded() {
    try {
      fireFormEvent(new FormEvent(this, FormEvent.TYPE_DISCARDED));
    }
    catch (RuntimeException | PlatformError e) {
      throw BEANS.get(PlatformExceptionTranslator.class).translate(e)
          .withContextInfo("form", getClass().getName());
    }
  }

  private void fireFormStoreAfter() {
    fireFormEvent(new FormEvent(this, FormEvent.TYPE_STORE_AFTER));
  }

  /**
   * send request that form was activated by gui
   */
  private void fireFormActivated() {
    try {
      interceptFormActivated();
      fireFormEvent(new FormEvent(this, FormEvent.TYPE_ACTIVATED));
    }
    catch (RuntimeException | PlatformError e) {
      throw BEANS.get(PlatformExceptionTranslator.class).translate(e)
          .withContextInfo("form", getClass().getName());
    }
  }

  /**
   * send request that form was closed by gui
   */
  private void fireFormClosed() {
    try {
      fireFormEvent(new FormEvent(this, FormEvent.TYPE_CLOSED));
    }
    catch (RuntimeException | PlatformError e) {
      throw BEANS.get(PlatformExceptionTranslator.class).translate(e)
          .withContextInfo("form", getClass().getName());
    }
  }

  protected void fireFormEvent(FormEvent e) {
    EventListener[] listeners = m_listenerList.getListeners(FormListener.class);
    if (listeners != null && listeners.length > 0) {
      RuntimeException pe = null;
      for (int i = 0; i < listeners.length; i++) {
        try {
          ((FormListener) listeners[i]).formChanged(e);
        }
        catch (RuntimeException ex) {
          if (pe == null) {
            pe = ex;
          }
        }
      }
      if (pe != null) {
        throw pe;
      }
    }
    IEventHistory<FormEvent> h = getEventHistory();
    if (h != null) {
      h.notifyEvent(e);
    }
  }

  @Override
  public void structureChanged(IFormField causingField) {
    fireFormStructureChanged(causingField);
  }

  @SuppressWarnings("bsiRulesDefinition:htmlInString")
  private void fireFormStructureChanged(IFormField causingField) {
    try {
      fireFormEvent(new FormEvent(this, FormEvent.TYPE_STRUCTURE_CHANGED, causingField));
    }
    catch (RuntimeException | PlatformError e) {
      throw BEANS.get(PlatformExceptionTranslator.class).translate(e)
          .withContextInfo("form", getClass().getName())
          .withContextInfo("field", (causingField == null ? "<null>" : causingField.getClass().getName()));
    }
  }

  private void fireFormToFront() {
    try {
      fireFormEvent(new FormEvent(this, FormEvent.TYPE_TO_FRONT));
    }
    catch (RuntimeException | PlatformError e) {
      throw BEANS.get(PlatformExceptionTranslator.class).translate(e)
          .withContextInfo("form", getClass().getName());
    }
  }

  private void fireFormToBack() {
    try {
      fireFormEvent(new FormEvent(this, FormEvent.TYPE_TO_BACK));
    }
    catch (RuntimeException | PlatformError e) {
      throw BEANS.get(PlatformExceptionTranslator.class).translate(e)
          .withContextInfo("form", getClass().getName());
    }
  }

  @SuppressWarnings("bsiRulesDefinition:htmlInString")
  private void fireRequestFocus(IFormField f) {
    try {
      fireFormEvent(new FormEvent(this, FormEvent.TYPE_REQUEST_FOCUS, f));
    }
    catch (RuntimeException | PlatformError e) {
      throw BEANS.get(PlatformExceptionTranslator.class).translate(e)
          .withContextInfo("form", getClass().getName())
          .withContextInfo("field", (f == null ? "<null>" : f.getClass().getName()));
    }
  }

  @Override
  public String getTitle() {
    return propertySupport.getPropertyString(PROP_TITLE);
  }

  @Override
  public void setTitle(String title) {
    propertySupport.setPropertyString(PROP_TITLE, title);
  }

  @Override
  public String getSubTitle() {
    return propertySupport.getPropertyString(PROP_SUB_TITLE);
  }

  @Override
  public void setSubTitle(String subTitle) {
    propertySupport.setPropertyString(PROP_SUB_TITLE, subTitle);
  }

  @Override
  public boolean isMaximizeEnabled() {
    return propertySupport.getPropertyBool(PROP_MAXIMIZE_ENABLED);
  }

  @Override
  public void setMaximizeEnabled(boolean b) {
    propertySupport.setPropertyBool(PROP_MAXIMIZE_ENABLED, b);
  }

  @Override
  public boolean isMinimizeEnabled() {
    return propertySupport.getPropertyBool(PROP_MINIMIZE_ENABLED);
  }

  @Override
  public void setMinimizeEnabled(boolean b) {
    propertySupport.setPropertyBool(PROP_MINIMIZE_ENABLED, b);
  }

  @Override
  public boolean isMaximized() {
    return propertySupport.getPropertyBool(PROP_MAXIMIZED);
  }

  @Override
  public void setMaximized(boolean b) {
    if (isMaximizeEnabled()) {
      if (b) {
        propertySupport.setPropertyBool(PROP_MINIMIZED, false);
      }
      // maximized state of ui could be out of sync, fire always
      propertySupport.setPropertyAlwaysFire(PROP_MAXIMIZED, b);
    }
  }

  @Override
  public boolean isMinimized() {
    return propertySupport.getPropertyBool(PROP_MINIMIZED);
  }

  @Override
  public void setMinimized(boolean b) {
    if (isMinimizeEnabled()) {
      if (b) {
        propertySupport.setPropertyBool(PROP_MAXIMIZED, false);
      }
      // minimized state of ui could be out of sync, fire always
      propertySupport.setPropertyAlwaysFire(PROP_MINIMIZED, b);

    }
  }

  @Override
  public boolean isShowOnStart() {
    return FLAGS_BIT_HELPER.isBitSet(SHOW_ON_START, m_flags);
  }

  @Override
  public void setShowOnStart(boolean showOnStart) {
    m_flags = FLAGS_BIT_HELPER.changeBit(SHOW_ON_START, showOnStart, m_flags);
  }

  @Override
  public boolean isModal() {
    return m_modal.get();
  }

  @Override
  public void setModal(boolean modal) {
    Assertions.assertFalse(getDesktop().isShowing(this), "Property 'modal' cannot be changed because Form is already showing [form={}]", this);
    m_modal.set(modal, true);
  }

  @Override
  public void setCacheBounds(boolean cacheBounds) {
    m_flags = FLAGS_BIT_HELPER.changeBit(CACHE_BOUNDS, cacheBounds, m_flags);
  }

  @Override
  public boolean isCacheBounds() {
    return FLAGS_BIT_HELPER.isBitSet(CACHE_BOUNDS, m_flags);
  }

  @Override
  public String computeCacheBoundsKey() {
    return getClass().getName();
  }

  @Override
  public int getDisplayHint() {
    return m_displayHint;
  }

  @Override
  public void setDisplayHint(int displayHint) {
    Assertions.assertFalse(getDesktop().isShowing(this), "Property 'displayHint' cannot be changed because Form is already showing [form={}]", this);

    switch (displayHint) {
      case DISPLAY_HINT_DIALOG:
      case DISPLAY_HINT_POPUP_WINDOW:
      case DISPLAY_HINT_VIEW: {
        m_displayHint = displayHint;
        break;
      }
      default: {
        throw new IllegalArgumentException("Unsupported displayHint " + displayHint);
      }
    }

    // Update modality hint if not explicitly set yet.
    boolean modal = (displayHint == IForm.DISPLAY_HINT_DIALOG);
    m_modal.set(modal, false);
    m_displayParent.set(resolveDisplayParent(), false);
  }

  @Override
  public IDisplayParent getDisplayParent() {
    return m_displayParent.get();
  }

  @Override
  public void setDisplayParent(IDisplayParent displayParent) {
    if (displayParent == null) {
      displayParent = resolveDisplayParent();
    }
    displayParent = Assertions.assertNotNull(displayParent, "'displayParent' must not be null");

    if (m_displayParent.get() == displayParent) {
      m_displayParent.markAsPreferredValue();
      return;
    }

    if (!getDesktop().isShowing(this)) {
      m_displayParent.set(displayParent, true); // If not showing yet, the 'displayParent' can be changed without detach/attach.
    }
    else {
      // This Form is already showing and must be attached to the new 'displayParent'.
      getDesktop().hideForm(this);
      m_displayParent.set(displayParent, true);
      getDesktop().showForm(this);
    }
  }

  @Override
  public String getDisplayViewId() {
    return m_displayViewId;
  }

  @Override
  public void setDisplayViewId(String viewId) {
    m_displayViewId = viewId;
  }

  @Override
  public boolean isAskIfNeedSave() {
    return FLAGS_BIT_HELPER.isBitSet(ASK_IF_NEED_SAVE, m_flags);
  }

  @Override
  public void setAskIfNeedSave(boolean b) {
    m_flags = FLAGS_BIT_HELPER.changeBit(ASK_IF_NEED_SAVE, b, m_flags);
  }

  @Override
  public void toFront() {
    fireFormToFront();
  }

  @Override
  public void toBack() {
    fireFormToBack();
  }

  @Override
  public boolean isButtonsArmed() {
    return FLAGS_BIT_HELPER.isBitSet(BUTTONS_ARMED, m_flags);
  }

  @Override
  public void setButtonsArmed(boolean b) {
    m_flags = FLAGS_BIT_HELPER.changeBit(BUTTONS_ARMED, b, m_flags);
  }

  @Override
  public boolean isCloseTimerArmed() {
    return FLAGS_BIT_HELPER.isBitSet(CLOSE_TIMER_ARMED, m_flags);
  }

  @Override
  public void setCloseTimerArmed(boolean closeTimerArmed) {
    m_flags = FLAGS_BIT_HELPER.changeBit(CLOSE_TIMER_ARMED, closeTimerArmed, m_flags);
    if (!closeTimerArmed && m_closeTimerFuture != null) {
      m_closeTimerFuture.cancel(false);
      m_closeTimerFuture = null;
    }
  }

  @Override
  public String toString() {
    return "Form " + getFormId();
  }

  @Override
  public IFormUIFacade getUIFacade() {
    return m_uiFacade;
  }

  protected void handleSystemButtonEventInternal(ButtonEvent e) {
    switch (e.getType()) {
      case ButtonEvent.TYPE_CLICKED: {
        // disable close timer
        setCloseTimerArmed(false);
        if (isButtonsArmed() && checkForVerifyingFields()) {
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
          catch (RuntimeException | PlatformError ex) {
            BEANS.get(ExceptionHandler.class).handle(BEANS.get(PlatformExceptionTranslator.class).translate(ex)
                .withContextInfo("button", e.getButton().getClass().getName()));
          }
        }
        break;
      }
    }
  }

  protected IDisplayParent resolveDisplayParent() {
    return m_initialClientRunContext.call(new Callable<IDisplayParent>() {

      @Override
      public IDisplayParent call() throws Exception {
        return BEANS.get(DisplayParentResolver.class).resolve(AbstractForm.this);
      }
    });
  }

  /**
   * Button controller for ok,cancel, save etc.
   */
  private class P_SystemButtonListener implements ButtonListener {
    @Override
    public void buttonChanged(ButtonEvent e) {
      // auto-detaching
      if (m_systemButtonListener != this) {
        ((IButton) e.getSource()).removeButtonListener(this);
        return;
      }
      handleSystemButtonEventInternal(e);
    }
  }// end private class

  private class P_MainBoxPropertyChangeProxy implements PropertyChangeListener {
    @Override
    public void propertyChange(PropertyChangeEvent e) {
      if (IFormField.PROP_SAVE_NEEDED.equals(e.getPropertyName())) {
        calculateSaveNeeded();
      }
      else if (IFormField.PROP_EMPTY.equals(e.getPropertyName())) {
        propertySupport.firePropertyChange(PROP_EMPTY, e.getOldValue(), e.getNewValue());
      }
    }
  }

  /**
   * Starts the timer that periodically invokes {@link AbstractForm#interceptTimer(String).
   */
  protected IFuture<Void> startTimer(int intervalSeconds, final String timerId) {
    return ModelJobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        try {
          LOG.info("timer {}", timerId);
          interceptTimer(timerId);
        }
        catch (RuntimeException | PlatformError e) {
          throw BEANS.get(PlatformExceptionTranslator.class).translate(e)
              .withContextInfo("form", getClass().getName())
              .withContextInfo("timerId", timerId);
        }
      }
    }, ModelJobs.newInput(ClientRunContexts.copyCurrent())
        .withName("Form timer")
        .withExecutionTrigger(Jobs.newExecutionTrigger()
            .withStartIn(intervalSeconds, TimeUnit.SECONDS)
            .withSchedule(SimpleScheduleBuilder.repeatSecondlyForever(intervalSeconds))));
  }

  /**
   * Installs the timer to close the Form once the given seconds elapse
   */
  private IFuture<?> installFormCloseTimer(final long seconds) {
    final long startMillis = System.currentTimeMillis();
    final long delayMillis = TimeUnit.SECONDS.toMillis(seconds);

    return ModelJobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        final long elapsedMillis = System.currentTimeMillis() - startMillis;
        final long remainingSeconds = TimeUnit.MILLISECONDS.toSeconds(delayMillis - elapsedMillis);

        if (!isCloseTimerArmed()) {
          setSubTitle(null);
        }
        else if (remainingSeconds > 0) {
          setSubTitle("" + remainingSeconds);
        }
        else {
          setCloseTimerArmed(false); // cancel the periodic action

          try {
            interceptCloseTimer();
          }
          catch (RuntimeException | PlatformError e) {
            throw BEANS.get(PlatformExceptionTranslator.class).translate(e)
                .withContextInfo("form", getClass().getName());
          }
        }
      }
    }, ModelJobs.newInput(ClientRunContexts.copyCurrent())
        .withName("Close timer")
        .withExceptionHandling(null, false)
        .withExecutionTrigger(Jobs.newExecutionTrigger()
            .withSchedule(SimpleScheduleBuilder.repeatSecondlyForever())));
  }

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

  protected class P_UIFacade implements IFormUIFacade {
    @Override
    public void fireFormActivatedFromUI() {
      fireFormActivated();
    }

    @Override
    public void fireFormClosingFromUI() {
      // check if some field is verifying input. In this case cancel ui call
      if (!checkForVerifyingFields()) {
        return;
      }
      closeFormInternal(false);
    }

    @Override
    public void fireFormKilledFromUI() {
      closeFormInternal(true);
    }
  }// end private class

  /**
   * The extension delegating to the local methods. This Extension is always at the end of the chain and will not call
   * any further chain elements.
   */
  protected static class LocalFormExtension<FORM extends AbstractForm> extends AbstractExtension<FORM> implements IFormExtension<FORM> {

    /**
     * @param owner
     */
    public LocalFormExtension(FORM owner) {
      super(owner);
    }

    @Override
    public void execCloseTimer(FormCloseTimerChain chain) {
      getOwner().execCloseTimer();
    }

    @Override
    public void execInactivityTimer(FormInactivityTimerChain chain) {
      getOwner().execInactivityTimer();
    }

    @Override
    public void execStored(FormStoredChain chain) {
      getOwner().execStored();
    }

    @Override
    public boolean execIsSaveNeeded(IsSaveNeededFieldsChain chain) {
      return getOwner().execIsSaveNeeded();
    }

    @Override
    public boolean execCheckFields(FormCheckFieldsChain chain) {
      return getOwner().execCheckFields();
    }

    @Override
    public void execResetSearchFilter(FormResetSearchFilterChain chain, SearchFilter searchFilter) {
      getOwner().execResetSearchFilter(searchFilter);
    }

    @Override
    public void execAddSearchTerms(FormAddSearchTermsChain chain, SearchFilter search) {
      getOwner().execAddSearchTerms(search);
    }

    @Override
    public void execOnVetoException(FormOnVetoExceptionChain chain, VetoException e, int code) {
      getOwner().execOnVetoException(e, code);
    }

    @Override
    public void execFormActivated(FormFormActivatedChain chain) {
      getOwner().execFormActivated();
    }

    @Override
    public void execDisposeForm(FormDisposeFormChain chain) {
      getOwner().execDisposeForm();
    }

    @Override
    public void execTimer(FormTimerChain chain, String timerId) {
      getOwner().execTimer(timerId);
    }

    @Override
    public AbstractFormData execCreateFormData(FormCreateFormDataChain chain) {
      return getOwner().execCreateFormData();
    }

    @Override
    public void execInitForm(FormInitFormChain chain) {
      getOwner().execInitForm();
    }

    @Override
    public boolean execValidate(FormValidateChain chain) {
      return getOwner().execValidate();
    }

    @Override
    public void execOnCloseRequest(FormOnCloseRequestChain chain, boolean kill, Set<Integer> enabledButtonSystemTypes) {
      getOwner().execOnCloseRequest(kill, enabledButtonSystemTypes);
    }

    @Override
    public void execDataChanged(FormDataChangedChain chain, Object... dataTypes) {
      getOwner().execDataChanged(dataTypes);
    }
  }

  protected final void interceptCloseTimer() {
    List<? extends IFormExtension<? extends AbstractForm>> extensions = getAllExtensions();
    FormCloseTimerChain chain = new FormCloseTimerChain(extensions);
    chain.execCloseTimer();
  }

  protected final void interceptInactivityTimer() {
    List<? extends IFormExtension<? extends AbstractForm>> extensions = getAllExtensions();
    FormInactivityTimerChain chain = new FormInactivityTimerChain(extensions);
    chain.execInactivityTimer();
  }

  protected final void interceptStored() {
    List<? extends IFormExtension<? extends AbstractForm>> extensions = getAllExtensions();
    FormStoredChain chain = new FormStoredChain(extensions);
    chain.execStored();
  }

  protected final boolean interceptIsSaveNeeded() {
    List<? extends IFormExtension<? extends AbstractForm>> extensions = getAllExtensions();
    IsSaveNeededFieldsChain chain = new IsSaveNeededFieldsChain(extensions);
    return chain.execIsSaveNeeded();
  }

  protected final boolean interceptCheckFields() {
    List<? extends IFormExtension<? extends AbstractForm>> extensions = getAllExtensions();
    FormCheckFieldsChain chain = new FormCheckFieldsChain(extensions);
    return chain.execCheckFields();
  }

  protected final void interceptResetSearchFilter(SearchFilter searchFilter) {
    List<? extends IFormExtension<? extends AbstractForm>> extensions = getAllExtensions();
    FormResetSearchFilterChain chain = new FormResetSearchFilterChain(extensions);
    chain.execResetSearchFilter(searchFilter);
  }

  protected final void interceptAddSearchTerms(SearchFilter search) {
    List<? extends IFormExtension<? extends AbstractForm>> extensions = getAllExtensions();
    FormAddSearchTermsChain chain = new FormAddSearchTermsChain(extensions);
    chain.execAddSearchTerms(search);
  }

  protected final void interceptOnVetoException(VetoException e, int code) {
    List<? extends IFormExtension<? extends AbstractForm>> extensions = getAllExtensions();
    FormOnVetoExceptionChain chain = new FormOnVetoExceptionChain(extensions);
    chain.execOnVetoException(e, code);
  }

  protected final void interceptFormActivated() {
    List<? extends IFormExtension<? extends AbstractForm>> extensions = getAllExtensions();
    FormFormActivatedChain chain = new FormFormActivatedChain(extensions);
    chain.execFormActivated();
  }

  protected final void interceptDisposeForm() {
    List<? extends IFormExtension<? extends AbstractForm>> extensions = getAllExtensions();
    FormDisposeFormChain chain = new FormDisposeFormChain(extensions);
    chain.execDisposeForm();
  }

  protected final void interceptTimer(String timerId) {
    List<? extends IFormExtension<? extends AbstractForm>> extensions = getAllExtensions();
    FormTimerChain chain = new FormTimerChain(extensions);
    chain.execTimer(timerId);
  }

  protected final AbstractFormData interceptCreateFormData() {
    List<? extends IFormExtension<? extends AbstractForm>> extensions = getAllExtensions();
    FormCreateFormDataChain chain = new FormCreateFormDataChain(extensions);
    return chain.execCreateFormData();
  }

  protected final void interceptInitForm() {
    List<? extends IFormExtension<? extends AbstractForm>> extensions = getAllExtensions();
    FormInitFormChain chain = new FormInitFormChain(extensions);
    chain.execInitForm();
  }

  protected final boolean interceptValidate() {
    List<? extends IFormExtension<? extends AbstractForm>> extensions = getAllExtensions();
    FormValidateChain chain = new FormValidateChain(extensions);
    return chain.execValidate();
  }

  protected final void interceptOnCloseRequest(boolean kill, Set<Integer> enabledButtonSystemTypes) {
    List<? extends IFormExtension<? extends AbstractForm>> extensions = getAllExtensions();
    FormOnCloseRequestChain chain = new FormOnCloseRequestChain(extensions);
    chain.execOnCloseRequest(kill, enabledButtonSystemTypes);
  }

  protected final void interceptDataChanged(Object... dataTypes) {
    List<? extends IFormExtension<? extends AbstractForm>> extensions = getAllExtensions();
    FormDataChangedChain chain = new FormDataChangedChain(extensions);
    chain.execDataChanged(dataTypes);
  }
}
