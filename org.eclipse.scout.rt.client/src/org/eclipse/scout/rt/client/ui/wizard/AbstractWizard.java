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
package org.eclipse.scout.rt.client.ui.wizard;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.ConfigurationUtility;
import org.eclipse.scout.commons.EventListenerList;
import org.eclipse.scout.commons.OptimisticLock;
import org.eclipse.scout.commons.annotations.ClassId;
import org.eclipse.scout.commons.annotations.ConfigOperation;
import org.eclipse.scout.commons.annotations.ConfigProperty;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.beans.AbstractPropertyObserver;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.BlockingCondition;
import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.IValueField;
import org.eclipse.scout.rt.client.ui.form.fields.button.IButton;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.shared.services.common.exceptionhandler.IExceptionHandlerService;
import org.eclipse.scout.service.SERVICES;

public abstract class AbstractWizard extends AbstractPropertyObserver implements IWizard {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractWizard.class);

  private boolean m_initialized;
  private final EventListenerList m_listenerList;
  private List<IWizardStep<? extends IForm>> m_availableStepList;
  private List<IWizardStep<? extends IForm>> m_stepList;
  private IWizardStep<? extends IForm> m_activeStep;
  // event accumulation (coalescation)
  private final OptimisticLock m_changingLock;
  private List<WizardEvent> m_accumulatedEvents;
  //
  private boolean m_displayHintLocked;
  private boolean m_modal;
  private String m_displayViewId;
  private int m_displayHint;
  private final BlockingCondition m_blockingCondition;

  private IWizardContainerForm m_containerForm;
  private PropertyChangeListener m_anyFieldChangeListener;

  public AbstractWizard() {
    this(true);
  }

  public AbstractWizard(boolean callInitializer) {
    m_listenerList = new EventListenerList();
    m_changingLock = new OptimisticLock();
    m_accumulatedEvents = new ArrayList<WizardEvent>(3);
    m_availableStepList = new ArrayList<IWizardStep<? extends IForm>>(0);
    m_stepList = new ArrayList<IWizardStep<? extends IForm>>(0);
    m_blockingCondition = new BlockingCondition(false);
    if (callInitializer) {
      callInitializer();
    }
  }

  protected void callInitializer() {
    if (!m_initialized) {
      initConfig();
      m_initialized = true;
    }
  }

  /*
   * Configuration
   */

  @ConfigProperty(ConfigProperty.FORM_DISPLAY_HINT)
  @Order(100)
  protected int getConfiguredDisplayHint() {
    return DISPLAY_HINT_DIALOG;
  }

  @ConfigProperty(ConfigProperty.FORM_VIEW_ID)
  @Order(105)
  protected String getConfiguredDisplayViewId() {
    return null;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(106)
  protected boolean getConfiguredModal() {
    return false;
  }

  @ConfigProperty(ConfigProperty.TEXT)
  @Order(10)
  protected String getConfiguredTitle() {
    return null;
  }

  @ConfigProperty(ConfigProperty.TEXT)
  @Order(20)
  protected String getConfiguredTitleHtml() {
    return null;
  }

  @ConfigProperty(ConfigProperty.TEXT)
  @Order(30)
  protected String getConfiguredTooltipText() {
    return null;
  }

  /**
   * @deprecated: Use a {@link ClassId} annotation as key for Doc-Text. Will be removed in the 5.0 Release.
   */
  @Deprecated
  @Order(40)
  protected String getConfiguredDoc() {
    return null;
  }

  @ConfigProperty(ConfigProperty.ICON_ID)
  @Order(20)
  protected String getConfiguredIconId() {
    return null;
  }

  @ConfigProperty(ConfigProperty.STRING)
  @Order(50)
  protected String getConfiguredWizardNo() {
    return null;
  }

  @SuppressWarnings("unchecked")
  private List<Class<? extends IWizardStep<? extends IForm>>> getConfiguredAvailableSteps() {
    Class<?>[] dca = ConfigurationUtility.getDeclaredPublicClasses(getClass());
    List<Class<IWizardStep>> filtered = ConfigurationUtility.filterClasses(dca, IWizardStep.class);
    List<Class<? extends IWizardStep>> wizardSteps = ConfigurationUtility.sortFilteredClassesByOrderAnnotation(filtered, IWizardStep.class);
    List<Class<? extends IWizardStep<? extends IForm>>> result = new ArrayList<Class<? extends IWizardStep<? extends IForm>>>();
    for (Class<? extends IWizardStep> wizardStep : wizardSteps) {
      result.add((Class<? extends IWizardStep<? extends IForm>>) wizardStep);
    }
    return result;
  }

  /**
   * create and eventually open a form containing the wizard.<br>
   * this method may be overwritten to provide an own wizard representation
   * form.
   * 
   * @return
   * @throws ProcessingException
   */
  @ConfigOperation
  @Order(5)
  protected IWizardContainerForm execCreateContainerForm() throws ProcessingException {
    DefaultWizardContainerForm containerForm = new DefaultWizardContainerForm(this);
    containerForm.setDisplayHint(getDisplayHint());
    containerForm.setDisplayViewId(getDisplayViewId());
    containerForm.setModal(isModal());
    return containerForm;
  }

  /**
   * Whenever the active step changes (due to {@link #activateStep(IWizardStep)} then this method is called. It can be
   * used for example to decorate the step
   * labels and description depending on the current state or to decorate the
   * current wizard form in {@link #getWizardForm()}
   * 
   * @throws ProcessingException
   */
  @ConfigOperation
  @Order(6)
  protected void execActiveStepChanged() throws ProcessingException {
  }

  /**
   * startup wizard The container form is available at {@link #getContainerForm()}. After this operation the container
   * form is
   * started if not already started in this method. Normally custom code in this
   * method activates a step using {@link IWizardStep#activate(int)} The default
   * implementation simply copies the available step list to the step list and
   * activates the first step
   */
  @Order(10)
  @ConfigOperation
  protected void execStart() throws ProcessingException {
    List<IWizardStep<? extends IForm>> steps = getAvailableSteps();
    setSteps(steps);
    if (steps.size() > 0) {
      activateStep(steps.get(0));
    }
  }

  /**
   * next The default implementation activates the step after the current one
   */
  @Order(20)
  @ConfigOperation
  protected void execNextStep() throws ProcessingException {
    IWizardStep<? extends IForm> step = getNextStep();
    activateStep(step);
  }

  /**
   * previous The default implementation activates the step before the current
   * one
   */
  @Order(30)
  @ConfigOperation
  protected void execPreviousStep() throws ProcessingException {
    IWizardStep<? extends IForm> step = getPreviousStep();
    activateStep(step);
  }

  /**
   * finish button clicked The default implementation just activates a null step
   * and closes the wizard
   */
  @Order(40)
  @ConfigOperation
  protected void execFinish() throws ProcessingException {
    activateStep(null);
    close();
  }

  /**
   * cancel button clicked The default implementation just closes the wizard
   */
  @Order(50)
  @ConfigOperation
  protected void execCancel() throws ProcessingException {
    close();
  }

  /**
   * suspend button clicked The default implementation just closes the wizard
   */
  @Order(60)
  @ConfigOperation
  protected void execSuspend() throws ProcessingException {
    close();
  }

  /**
   * reset button clicked The default implementation does nothing
   */
  @Order(65)
  @ConfigOperation
  protected void execReset() throws ProcessingException {
  }

  /**
   * This method is called whenever a field value has changed. It can be used to
   * refresh the button policy by calling {@link #refreshButtonPolicy()} The
   * default implementation does nothing
   */
  @ConfigOperation
  @Order(70)
  protected void execAnyFieldChanged(IFormField source) throws ProcessingException {
  }

  /**
   * This method is used to make then wizard buttons
   * visible/invisible/enabled/disabled based on the current form and wizard
   * state. The default implementation just sets the previous, next and finish
   * buttons correctly based on the {@link #getSteps()}.
   */
  @ConfigOperation
  @Order(80)
  protected void execRefreshButtonPolicy() throws ProcessingException {
    IWizardStep<? extends IForm> prev = getPreviousStep();
    IWizardStep<? extends IForm> next = getNextStep();
    //
    IButton b;
    b = getContainerForm().getWizardPreviousStepButton();
    if (b != null) {
      b.setView(true, prev != null, false);
    }
    b = getContainerForm().getWizardNextStepButton();
    if (b != null) {
      b.setView(next != null, true, false);
    }
    b = getContainerForm().getWizardFinishButton();
    if (b != null) {
      b.setView(next == null, true, false);
    }
  }

  /**
   * This is a delegate methode that is normally called by the wizard status
   * field (html field) in the {@link IWizardContainerForm} whenever a link is
   * clicked.
   * 
   * @param url
   * @param path
   *          {@link URL#getPath()}
   * @param local
   *          true if the url is not a valid external url but a local model url
   *          (http://local/...)
   */
  @ConfigOperation
  @Order(230)
  protected void execHyperlinkAction(URL url, String path, boolean local) throws ProcessingException {
    LOG.info("execHyperlinkAction " + url + " (in " + getClass().getName() + ")");
  }

  @SuppressWarnings("boxing")
  protected void initConfig() {
    setDisplayHint(getConfiguredDisplayHint());
    setDisplayViewId(getConfiguredDisplayViewId());
    setModal(getConfiguredModal());
    setTitle(getConfiguredTitle());
    setTitleHtml(getConfiguredTitleHtml());
    setTooltipText(getConfiguredTooltipText());
    setIconId(getConfiguredIconId());
    setWizardNo(getConfiguredWizardNo());
    // initially the wizard is in state "closed"
    propertySupport.setPropertyBool(PROP_CLOSED, true);
    setCloseTypeInternal(CloseType.Unknown);
    // steps
    ArrayList<IWizardStep<? extends IForm>> list = new ArrayList<IWizardStep<? extends IForm>>();
    for (Class<? extends IWizardStep<? extends IForm>> element : getConfiguredAvailableSteps()) {
      try {
        IWizardStep<? extends IForm> step = ConfigurationUtility.newInnerInstance(this, element);
        list.add(step);
      }
      catch (Exception e) {
        LOG.error("failed creating " + element, e);
      }
    }
    injectStepsInternal(list);

    setAvailableSteps(list);
    // add listener to listen on any field in active form
    m_anyFieldChangeListener = new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent e) {
        try {
          execAnyFieldChanged((IFormField) e.getSource());
        }
        catch (Throwable t) {
          LOG.error("" + e.getSource() + " " + e.getPropertyName() + "=" + e.getNewValue(), t);
        }
      }
    };
    propertySupport.addPropertyChangeListener(PROP_WIZARD_FORM, new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent e) {
        IForm oldForm = (IForm) e.getOldValue();
        IForm newForm = (IForm) e.getNewValue();
        if (oldForm != null) {
          oldForm.getRootGroupBox().removeSubtreePropertyChangeListener(IValueField.PROP_VALUE, m_anyFieldChangeListener);
        }
        if (newForm != null) {
          newForm.getRootGroupBox().addSubtreePropertyChangeListener(IValueField.PROP_VALUE, m_anyFieldChangeListener);
        }
      }
    });
  }

  /**
   * Used to manage wizard steps i.e. to add/remove wizard steps
   * 
   * @param steps
   *          live and mutable list of configured steps, not yet initialized
   *          and added to the step list
   */
  protected void injectStepsInternal(List<IWizardStep<? extends IForm>> steps) {
  }

  /*
   * Runtime
   */

  @Override
  public void addWizardListener(WizardListener listener) {
    m_listenerList.add(WizardListener.class, listener);
  }

  @Override
  public void removeWizardListener(WizardListener listener) {
    m_listenerList.remove(WizardListener.class, listener);
  }

  private void fireStateChanged() {
    fireWizardEvent(new WizardEvent(this, WizardEvent.TYPE_STATE_CHANGED));
  }

  private void fireClosed() {
    fireWizardEvent(new WizardEvent(this, WizardEvent.TYPE_CLOSED));
  }

  private void fireWizardEvent(WizardEvent e) {
    if (m_changingLock.isAcquired()) {
      // coalesce event with existing event list
      for (Iterator<WizardEvent> it = m_accumulatedEvents.iterator(); it.hasNext();) {
        WizardEvent existingEvent = it.next();
        if (existingEvent.getType() == e.getType()) {
          it.remove();
        }
      }
      m_accumulatedEvents.add(e);
    }
    else {
      WizardListener[] a = m_listenerList.getListeners(WizardListener.class);
      if (a != null && a.length > 0) {
        for (WizardListener element : a) {
          element.wizardChanged(e);
        }
      }
    }
  }

  @Override
  public void setChanging(boolean b) {
    if (b) {
      m_changingLock.acquire();
    }
    else {
      m_changingLock.release();
      if (m_changingLock.isReleased()) {
        // now send all accumulated events
        List<WizardEvent> list = m_accumulatedEvents;
        m_accumulatedEvents = new ArrayList<WizardEvent>(3);
        for (WizardEvent e : list) {
          fireWizardEvent(e);
        }
      }
    }
  }

  @Override
  public boolean isChanging() {
    return m_changingLock.isAcquired();
  }

  @Override
  public int getDisplayHint() {
    return m_displayHint;
  }

  @Override
  public void setDisplayHint(int i) {
    if (m_displayHintLocked) {
      throw new IllegalArgumentException("displayHint cannot be changed once the form handling has started");
    }
    switch (i) {
      case DISPLAY_HINT_DIALOG: {
        m_displayHint = i;
        break;
      }
      case DISPLAY_HINT_VIEW: {
        m_displayHint = i;
        break;
      }
      default: {
        throw new IllegalArgumentException("invalid displayHint " + i);
      }
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
  public boolean isModal() {
    return m_modal;
  }

  @Override
  public void setModal(boolean b) {
    m_modal = b;
  }

  @Override
  public String getTitle() {
    return propertySupport.getPropertyString(PROP_TITLE);
  }

  @Override
  public void setTitle(String s) {
    propertySupport.setPropertyString(PROP_TITLE, s);
  }

  @Override
  public String getTitleHtml() {
    return propertySupport.getPropertyString(PROP_TITLE_HTML);
  }

  @Override
  public void setTitleHtml(String s) {
    propertySupport.setPropertyString(PROP_TITLE_HTML, s);
  }

  @Override
  public String getTooltipText() {
    return propertySupport.getPropertyString(PROP_TOOLTIP_TEXT);
  }

  @Override
  public void setTooltipText(String s) {
    propertySupport.setPropertyString(PROP_TOOLTIP_TEXT, s);
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
  public String getSubTitle() {
    return propertySupport.getPropertyString(PROP_SUB_TITLE);
  }

  @Override
  public void setSubTitle(String s) {
    propertySupport.setPropertyString(PROP_SUB_TITLE, s);
  }

  @Override
  public String getWizardNo() {
    return propertySupport.getPropertyString(PROP_SUB_TITLE);
  }

  @Override
  public void setWizardNo(String s) {
    propertySupport.setPropertyString(PROP_SUB_TITLE, s);
  }

  public IDesktop getDesktop() {
    return ClientSyncJob.getCurrentSession().getDesktop();
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T extends IWizardStep<? extends IForm>> T getAvailableStep(Class<T> type) {
    for (IWizardStep<? extends IForm> step : m_availableStepList) {
      if (type.isInstance(step)) {
        return (T) step;
      }
    }
    return null;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T extends IWizardStep<? extends IForm>> T getStep(Class<T> type) {
    for (IWizardStep<? extends IForm> step : m_stepList) {
      if (type.isInstance(step)) {
        return (T) step;
      }
    }
    return null;
  }

  @Override
  public IWizardStep<? extends IForm> getStep(int index) {
    if (index >= 0 && index < m_stepList.size()) {
      return m_stepList.get(index);
    }
    else {
      return null;
    }
  }

  @Override
  public IWizardStep<? extends IForm> getStepBySimpleClassName(String simpleClassName) {
    for (IWizardStep<? extends IForm> step : m_stepList) {
      if (step.getClass().getSimpleName().equals(simpleClassName)) {
        return step;
      }
    }
    return null;
  }

  @Override
  public IWizardStep<? extends IForm> getStepByClassName(String className) {
    for (IWizardStep<? extends IForm> step : m_stepList) {
      if (step.getClass().getName().equals(className)) {
        return step;
      }
    }
    return null;
  }

  @Override
  public int getStepIndex(IWizardStep<? extends IForm> step) {
    if (step == null) {
      return -1;
    }
    for (int i = 0; i < m_stepList.size(); i++) {
      if (m_stepList.get(i) == step) {
        return i;
      }
    }
    return -1;
  }

  @Override
  public List<IWizardStep<? extends IForm>> getAvailableSteps() {
    return CollectionUtility.<IWizardStep<? extends IForm>> arrayList(m_availableStepList);
  }

  @Override
  public void setAvailableSteps(List<IWizardStep<? extends IForm>> steps) {
    m_availableStepList = new ArrayList<IWizardStep<? extends IForm>>();
    if (steps != null) {
      for (IWizardStep<? extends IForm> step : steps) {
        if (step != null) {
          m_availableStepList.add(step);
        }
      }
    }
  }

  @Override
  public List<IWizardStep<? extends IForm>> getSteps() {
    return CollectionUtility.<IWizardStep<? extends IForm>> arrayList(m_stepList);
  }

  @Override
  public void setSteps(IWizardStep<?>... steps) {
    if (steps == null) {
      steps = new IWizardStep<?>[0];
    }
    setSteps(Arrays.asList(steps));
  }

  @Override
  public void setSteps(List<IWizardStep<? extends IForm>> steps) {
    try {
      setChanging(true);
      //
      // remove old
      if (m_stepList != null) {
        for (IWizardStep<? extends IForm> step : m_stepList) {
          step.setWizardInternal(null);
        }
      }
      // add new
      m_stepList = new ArrayList<IWizardStep<? extends IForm>>();
      if (steps != null) {
        for (IWizardStep<? extends IForm> step : steps) {
          if (step != null) {
            m_stepList.add(step);
            step.setWizardInternal(this);
          }
        }
      }
      fireStateChanged();
    }
    finally {
      setChanging(false);
    }
  }

  @Override
  public IWizardStep<? extends IForm> getActiveStep() {
    return m_activeStep;
  }

  protected void setActiveStepInternal(IWizardStep<? extends IForm> activeStep) {
    m_activeStep = activeStep;
  }

  @Override
  public void activateStep(IWizardStep<? extends IForm> step) throws ProcessingException {
    activateStep(step, false, false);
  }

  @Override
  public void activateStep(IWizardStep<? extends IForm> step, boolean jumpForward, boolean jumpBackward) throws ProcessingException {
    if (m_activeStep != step) {
      try {
        int kind = getStepKind(m_activeStep, step);
        // old target
        if (m_activeStep != null) {
          // this call may veto
          m_activeStep.deactivate(kind);
        }
        // in-between targets
        List<IWizardStep<? extends IForm>> intermediateSteps = getStepSpan(m_activeStep, false, step, false);
        if (intermediateSteps.size() > 0) {
          if ((kind == IWizardStep.STEP_NEXT && !jumpForward) || (kind == IWizardStep.STEP_PREVIOUS && !jumpBackward)) {
            for (IWizardStep<? extends IForm> intermediateStep : intermediateSteps) {
              // these calls may veto
              intermediateStep.activate(kind);
              intermediateStep.deactivate(kind);
            }
          }
        }
        // new target
        m_activeStep = step;
        if (m_activeStep != null) {
          // this call may veto
          m_activeStep.activate(kind);
        }
        // notify callback
        try {
          execActiveStepChanged();
        }
        catch (ProcessingException e) {
          SERVICES.getService(IExceptionHandlerService.class).handleException(e);
        }
        catch (Throwable t) {
          SERVICES.getService(IExceptionHandlerService.class).handleException(new ProcessingException("Unexpected", t));
        }
      }
      finally {
        refreshButtonPolicy();
        fireStateChanged();
      }
    }
  }

  @Override
  public void refreshButtonPolicy() {
    try {
      execRefreshButtonPolicy();
    }
    catch (ProcessingException e) {
      SERVICES.getService(IExceptionHandlerService.class).handleException(e);
    }
    catch (Throwable t) {
      SERVICES.getService(IExceptionHandlerService.class).handleException(new ProcessingException("Unexpected", t));
    }
  }

  @Override
  public int getStepKind(IWizardStep<? extends IForm> from, IWizardStep<? extends IForm> to) {
    if (from == null && to == null) {
      return IWizardStep.STEP_NEXT;
    }
    if (from == null) {
      return IWizardStep.STEP_NEXT;
    }
    if (to == null) {
      return IWizardStep.STEP_NEXT;
    }
    int fromIndex = getStepIndex(from);
    int toIndex = getStepIndex(to);
    if (fromIndex == toIndex) {
      return IWizardStep.STEP_NEXT;
    }
    if (fromIndex < toIndex) {
      return IWizardStep.STEP_NEXT;
    }
    if (fromIndex > toIndex) {
      return IWizardStep.STEP_PREVIOUS;
    }
    return IWizardStep.STEP_NEXT;
  }

  @Override
  public List<IWizardStep<? extends IForm>> getStepSpan(IWizardStep<? extends IForm> from, boolean includeFrom, IWizardStep<? extends IForm> to, boolean includeTo) {
    ArrayList<IWizardStep<? extends IForm>> list = new ArrayList<IWizardStep<? extends IForm>>();
    //
    if (from == null && to == null) {
      return list;
    }
    if (from == null) {
      if (to != null && includeTo) {
        list.add(to);
      }
      return list;
    }
    if (to == null) {
      if (from != null && includeFrom) {
        list.add(from);
      }
      return list;
    }
    int fromIndex = getStepIndex(from);
    int toIndex = getStepIndex(to);
    if (fromIndex == toIndex) {
      if (includeFrom) {
        list.add(from);
      }
      else if (includeTo) {
        list.add(to);
      }
      return list;
    }
    if (fromIndex == toIndex - 1) {
      if (includeFrom) {
        list.add(from);
      }
      if (includeTo) {
        list.add(to);
      }
      return list;
    }
    if (fromIndex < toIndex - 1) {
      if (includeFrom) {
        list.add(from);
      }
      int n = toIndex - fromIndex - 1;
      for (int i = 0; i < n; i++) {
        list.add(m_stepList.get(fromIndex + 1 + i));
      }
      if (includeTo) {
        list.add(to);
      }
      return list;
    }
    if (fromIndex == toIndex + 1) {
      if (includeFrom) {
        list.add(from);
      }
      if (includeTo) {
        list.add(to);
      }
      return list;
    }
    if (fromIndex > toIndex + 1) {
      if (includeFrom) {
        list.add(from);
      }
      int n = fromIndex - toIndex - 1;
      for (int i = 0; i < n; i++) {
        list.add(m_stepList.get(fromIndex - 1 - i));
      }
      if (includeTo) {
        list.add(to);
      }
      return list;
    }
    // default
    if (includeFrom) {
      list.add(from);
    }
    if (includeTo) {
      list.add(to);
    }
    return list;
  }

  @Override
  public IWizardStep<? extends IForm> getPreviousStep() {
    int index = getStepIndex(getActiveStep()) - 1;
    if (index >= 0 && index < m_stepList.size()) {
      return m_stepList.get(index);
    }
    else {
      return null;
    }
  }

  @Override
  public IWizardStep<? extends IForm> getNextStep() {
    int index = getStepIndex(getActiveStep()) + 1;
    if (index >= 0 && index < m_stepList.size()) {
      return m_stepList.get(index);
    }
    else {
      return null;
    }
  }

  @Override
  public IWizardStep<? extends IForm> getPreviousEnabledStep() {
    int index = getStepIndex(getActiveStep()) - 1;
    while (index >= 0 && index < m_stepList.size() && !m_stepList.get(index).isEnabled()) {
      index--;
    }
    if (index >= 0 && index < m_stepList.size()) {
      return m_stepList.get(index);
    }
    else {
      return null;
    }
  }

  @Override
  public IWizardStep<? extends IForm> getNextEnabledStep() {
    int index = getStepIndex(getActiveStep()) + 1;
    while (index >= 0 && index < m_stepList.size() && !m_stepList.get(index).isEnabled()) {
      index++;
    }
    if (index >= 0 && index < m_stepList.size()) {
      return m_stepList.get(index);
    }
    else {
      return null;
    }
  }

  public List<IWizardStep<? extends IForm>> getHistory() {
    int index = getStepIndex(getActiveStep());
    if (m_stepList.size() > 0 && index >= 0) {
      return CollectionUtility.arrayList(m_stepList.subList(0, Math.min(index, m_stepList.size())));
    }
    else {
      return CollectionUtility.emptyArrayList();
    }
  }

  public List<IWizardStep<? extends IForm>> getExpectedFuture() {
    int index = getStepIndex(getActiveStep());
    if (m_stepList.size() > 0 && index < m_stepList.size()) {
      return CollectionUtility.arrayList(m_stepList.subList(Math.max(index + 1, 0), m_stepList.size()));
    }
    else {
      return CollectionUtility.emptyArrayList();
    }
  }

  @Override
  public void start() throws ProcessingException {
    assertClosed();
    if (m_blockingCondition.isBlocking()) {
      throw new ProcessingException("The wizard " + getClass().getSimpleName() + " has already been started");
    }
    m_blockingCondition.setBlocking(true);
    propertySupport.setPropertyBool(PROP_CLOSED, false);
    setCloseTypeInternal(CloseType.Unknown);
    createContainerForm();
    execStart();
    if (m_containerForm != null && !m_containerForm.isFormOpen()) {
      m_containerForm.startWizard();
    }
  }

  @Override
  public void close() throws ProcessingException {
    if (!isClosed()) {
      // close container form
      try {
        if (m_containerForm != null) {
          m_containerForm.doClose();
          m_containerForm = null;
        }
      }
      catch (Throwable t) {
        LOG.error("closing " + getTitle(), t);
      }
      // dispose all steps
      HashSet<IWizardStep<? extends IForm>> set = new HashSet<IWizardStep<? extends IForm>>();
      set.addAll(getAvailableSteps());
      set.addAll(getSteps());
      for (IWizardStep<? extends IForm> step : set) {
        try {
          step.dispose();
        }
        catch (Throwable t) {
          LOG.error("closing " + getTitle(), t);
        }
      }
      if (getCloseType() == CloseType.Unknown) {
        setCloseTypeInternal(CloseType.Closed);
      }
      propertySupport.setPropertyBool(PROP_CLOSED, true);
      fireClosed();
      // unlock
      m_blockingCondition.release();
    }
  }

  @Override
  public void waitFor() throws ProcessingException {
    // check if the desktop is observing this process
    IDesktop desktop = ClientSyncJob.getCurrentSession().getDesktop();
    if (desktop == null || !desktop.isOpened()) {
      throw new ProcessingException("Cannot wait for " + getClass().getName() + ". There is no desktop or the desktop has not yet been opened in the ui", null, WAIT_FOR_ERROR_CODE);
    }
    try {
      m_blockingCondition.waitFor();
    }
    catch (InterruptedException e) {
      throw new ProcessingException(ScoutTexts.get("UserInterrupted"), e);
    }
  }

  @Override
  public boolean isOpen() {
    return !isClosed();
  }

  @Override
  public boolean isClosed() {
    return propertySupport.getPropertyBool(PROP_CLOSED);
  }

  @Override
  public CloseType getCloseType() {
    return (CloseType) propertySupport.getProperty(PROP_CLOSE_TYPE);
  }

  private void setCloseTypeInternal(CloseType t) {
    propertySupport.setProperty(PROP_CLOSE_TYPE, t);
  }

  /**
   * next step The default implementation calls {@link #execNextStep()}
   */
  @Override
  public void doNextStep() throws ProcessingException {
    if (isOpen()) {
      try {
        execNextStep();
      }
      catch (ProcessingException pe) {
        throw pe;
      }
      catch (Throwable t) {
        throw new ProcessingException("Unexpected", t);
      }
    }
  }

  /**
   * previous step The default implementation calls {@link #execPreviousStep()}
   */
  @Override
  public void doPreviousStep() throws ProcessingException {
    if (isOpen()) {
      try {
        execPreviousStep();
      }
      catch (ProcessingException pe) {
        throw pe;
      }
      catch (Throwable t) {
        throw new ProcessingException("Unexpected", t);
      }
    }
  }

  /**
   * finish The default implementation calls {@link #execFinish()}
   */
  @Override
  public void doFinish() throws ProcessingException {
    if (isOpen()) {
      CloseType oldType = getCloseType();
      try {
        setCloseTypeInternal(CloseType.Finished);
        execFinish();
      }
      catch (ProcessingException pe) {
        setCloseTypeInternal(oldType);
        throw pe;
      }
      catch (Throwable t) {
        setCloseTypeInternal(oldType);
        throw new ProcessingException("Unexpected", t);
      }
    }
  }

  /**
   * cancel The default implementation calls {@link #execCancel()}
   */
  @Override
  public void doCancel() throws ProcessingException {
    if (isOpen()) {
      CloseType oldType = getCloseType();
      try {
        setCloseTypeInternal(CloseType.Cancelled);
        execCancel();
      }
      catch (ProcessingException pe) {
        setCloseTypeInternal(oldType);
        throw pe;
      }
      catch (Throwable t) {
        setCloseTypeInternal(oldType);
        throw new ProcessingException("Unexpected", t);
      }
    }
  }

  /**
   * suspend The default implementation calls {@link #execSuspend()}
   */
  @Override
  public void doSuspend() throws ProcessingException {
    if (isOpen()) {
      CloseType oldType = getCloseType();
      try {
        setCloseTypeInternal(CloseType.Suspended);
        execSuspend();
      }
      catch (ProcessingException pe) {
        setCloseTypeInternal(oldType);
        throw pe;
      }
      catch (Throwable t) {
        setCloseTypeInternal(oldType);
        throw new ProcessingException("Unexpected", t);
      }
    }
  }

  /**
   * reset The default implementation calls {@link #execReset()}
   */
  @Override
  public void doReset() throws ProcessingException {
    try {
      execReset();
    }
    catch (ProcessingException pe) {
      throw pe;
    }
    catch (Throwable t) {
      throw new ProcessingException("Unexpected", t);
    }
  }

  /**
   * This is a delegate methode that is normally called by the wizard status
   * field (html field) in the {@link IWizardContainerForm} whenever a link is
   * clicked.
   * 
   * @param url
   * @param path
   *          {@link URL#getPath()}
   * @param local
   *          true if the url is not a valid external url but a local model url
   *          (http://local/...) The default implementation calls {@link #execHyperlinkAction(URL, String, boolean)}
   */
  @Override
  public void doHyperlinkAction(URL url, String path, boolean local) throws ProcessingException {
    if (isOpen()) {
      execHyperlinkAction(url, path, local);
    }
  }

  private void assertOpen() throws ProcessingException {
    if (isClosed()) {
      throw new ProcessingException("wizard is closed");
    }
  }

  private void assertClosed() throws ProcessingException {
    if (!isClosed()) {
      throw new ProcessingException("wizard is already started");
    }
  }

  @Override
  public IForm getWizardForm() {
    return (IForm) propertySupport.getProperty(PROP_WIZARD_FORM);
  }

  @Override
  public void setWizardForm(IForm form) {
    propertySupport.setProperty(PROP_WIZARD_FORM, form);
  }

  @Override
  public IWizardContainerForm createContainerForm() throws ProcessingException {
    if (m_containerForm == null) {
      m_containerForm = execCreateContainerForm();
    }
    return m_containerForm;
  }

  @Override
  public IWizardContainerForm getContainerForm() {
    return m_containerForm;
  }

  @Override
  public String classId() {
    return ConfigurationUtility.getAnnotatedClassIdWithFallback(getClass());
  }

}
