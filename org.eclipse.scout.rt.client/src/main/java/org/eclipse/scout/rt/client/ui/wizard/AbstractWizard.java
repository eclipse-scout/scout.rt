/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.wizard;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.client.extension.ui.wizard.IWizardExtension;
import org.eclipse.scout.rt.client.extension.ui.wizard.WizardChains.WizardActiveStepChangedChain;
import org.eclipse.scout.rt.client.extension.ui.wizard.WizardChains.WizardAnyFieldChangedChain;
import org.eclipse.scout.rt.client.extension.ui.wizard.WizardChains.WizardAppLinkActionChain;
import org.eclipse.scout.rt.client.extension.ui.wizard.WizardChains.WizardCancelChain;
import org.eclipse.scout.rt.client.extension.ui.wizard.WizardChains.WizardContainerFormClosedChain;
import org.eclipse.scout.rt.client.extension.ui.wizard.WizardChains.WizardCreateContainerFormChain;
import org.eclipse.scout.rt.client.extension.ui.wizard.WizardChains.WizardDecorateContainerFormChain;
import org.eclipse.scout.rt.client.extension.ui.wizard.WizardChains.WizardFinishChain;
import org.eclipse.scout.rt.client.extension.ui.wizard.WizardChains.WizardNextStepChain;
import org.eclipse.scout.rt.client.extension.ui.wizard.WizardChains.WizardPostStartChain;
import org.eclipse.scout.rt.client.extension.ui.wizard.WizardChains.WizardPreviousStepChain;
import org.eclipse.scout.rt.client.extension.ui.wizard.WizardChains.WizardRefreshButtonPolicyChain;
import org.eclipse.scout.rt.client.extension.ui.wizard.WizardChains.WizardResetChain;
import org.eclipse.scout.rt.client.extension.ui.wizard.WizardChains.WizardStartChain;
import org.eclipse.scout.rt.client.extension.ui.wizard.WizardChains.WizardStepActionChain;
import org.eclipse.scout.rt.client.extension.ui.wizard.WizardChains.WizardSuspendChain;
import org.eclipse.scout.rt.client.job.ModelJobs;
import org.eclipse.scout.rt.client.session.ClientSessionProvider;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.form.FormEvent;
import org.eclipse.scout.rt.client.ui.form.FormListener;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.IValueField;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigOperation;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.exception.ExceptionHandler;
import org.eclipse.scout.rt.platform.exception.PlatformError;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.job.IBlockingCondition;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.reflect.AbstractPropertyObserver;
import org.eclipse.scout.rt.platform.reflect.ConfigurationUtility;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.collection.OrderedCollection;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.platform.util.concurrent.OptimisticLock;
import org.eclipse.scout.rt.platform.util.event.FastListenerList;
import org.eclipse.scout.rt.platform.util.event.IFastListenerList;
import org.eclipse.scout.rt.shared.extension.AbstractExtension;
import org.eclipse.scout.rt.shared.extension.ContributionComposite;
import org.eclipse.scout.rt.shared.extension.ExtensionUtility;
import org.eclipse.scout.rt.shared.extension.IContributionOwner;
import org.eclipse.scout.rt.shared.extension.IExtensibleObject;
import org.eclipse.scout.rt.shared.extension.IExtension;
import org.eclipse.scout.rt.shared.extension.ObjectExtensions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ClassId("d9f41936-b7c1-4635-b769-f7999dd5eb0d")
public abstract class AbstractWizard extends AbstractPropertyObserver implements IWizard, IContributionOwner, IExtensibleObject {
  private static final Logger LOG = LoggerFactory.getLogger(AbstractWizard.class);

  private boolean m_initialized;
  private final FastListenerList<WizardListener> m_listenerList;
  private List<IWizardStep<? extends IForm>> m_availableStepList;
  private List<IWizardStep<? extends IForm>> m_stepList;
  private IWizardStep<? extends IForm> m_activeStep;
  private final OptimisticLock m_changingLock;
  private List<WizardEvent> m_accumulatedEvents; // event accumulation (coalescation)
  private IContributionOwner m_contributionHolder;
  private final ObjectExtensions<AbstractWizard, IWizardExtension<? extends AbstractWizard>> m_objectExtensions;
  private final IBlockingCondition m_blockingCondition;

  private IWizardContainerForm m_containerForm;
  private PropertyChangeListener m_anyFieldChangeListener;
  private FormListener m_containerFormListener;

  public AbstractWizard() {
    this(true);
  }

  public AbstractWizard(boolean callInitializer) {
    m_listenerList = new FastListenerList<>();
    m_changingLock = new OptimisticLock();
    m_accumulatedEvents = new ArrayList<>(3);
    m_availableStepList = new ArrayList<>(0);
    m_stepList = new ArrayList<>(0);
    m_blockingCondition = Jobs.newBlockingCondition(false);
    m_objectExtensions = new ObjectExtensions<>(this, true);
    if (callInitializer) {
      callInitializer();
    }
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

  @Override
  public final <T> T optContribution(Class<T> contribution) {
    return m_contributionHolder.optContribution(contribution);
  }

  protected void callInitializer() {
    if (!m_initialized) {
      interceptInitConfig();
      m_initialized = true;
    }
  }

  /*
   * Configuration
   */

  @ConfigProperty(ConfigProperty.TEXT)
  @Order(10)
  protected String getConfiguredTitle() {
    return null;
  }

  @ConfigProperty(ConfigProperty.TEXT)
  @Order(20)
  protected String getConfiguredSubTitle() {
    return null;
  }

  /**
   * Defines if the wizard should be closed, whenever the container form is disposed. If set to {@code false} the owner
   * of the wizard is responsible to always close the wizard. Otherwise listeners could be left registered, causing
   * memory leaks.
   * <p>
   * Default: {@code true}
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(30)
  protected boolean getConfiguredManagedByContainerForm() {
    return true;
  }

  @SuppressWarnings("unchecked")
  private List<Class<? extends IWizardStep<? extends IForm>>> getConfiguredAvailableSteps() {
    Class<?>[] dca = ConfigurationUtility.getDeclaredPublicClasses(getClass());
    List<Class<IWizardStep>> filtered = ConfigurationUtility.filterClasses(dca, IWizardStep.class);

    List<Class<? extends IWizardStep<? extends IForm>>> result = new ArrayList<>(filtered.size());
    for (Class<? extends IWizardStep> wizardStep : filtered) {
      result.add((Class<? extends IWizardStep<? extends IForm>>) wizardStep);
    }
    return result;
  }

  /**
   * this method may be overwritten to provide an own wizard container form.
   */
  @ConfigOperation
  @Order(5)
  protected IWizardContainerForm execCreateContainerForm() {
    return new DefaultWizardContainerForm(this);
  }

  /**
   * Called directly after the wizard container form is created. Useful to to set display hints, change button
   * visibilities etc.
   */
  protected void execDecorateContainerForm() {
  }

  /**
   * Whenever the active step changes (due to {@link #activateStep(IWizardStep)} then this method is called. It can be
   * used for example to decorate the step labels and description depending on the current state or to decorate the
   * current wizard form in {@link #getWizardForm()}
   */
  @ConfigOperation
  @Order(6)
  protected void execActiveStepChanged() {
  }

  /**
   * startup wizard The container form is available at {@link #getContainerForm()}. After this operation the container
   * form is started if not already started in this method. Normally custom code in this method activates a step using
   * {@link IWizardStep#activate(int)} The default implementation simply copies the available step list to the step list
   * and activates the first step
   */
  @Order(10)
  @ConfigOperation
  protected void execStart() {
    List<IWizardStep<? extends IForm>> steps = getAvailableSteps();
    setSteps(steps);
    if (!steps.isEmpty()) {
      activateStep(steps.get(0));
    }
  }

  /**
   * Invoked after {@link #execStart()} was called and the wizard container form was started (
   * {@link IWizardContainerForm#startWizard()}).
   */
  @Order(15)
  @ConfigOperation
  protected void execPostStart() {
  }

  /**
   * next The default implementation activates the step after the current one
   */
  @Order(20)
  @ConfigOperation
  protected void execNextStep() {
    IWizardStep<? extends IForm> step = getNextStep();
    activateStep(step);
  }

  /**
   * previous The default implementation activates the step before the current one
   */
  @Order(30)
  @ConfigOperation
  protected void execPreviousStep() {
    IWizardStep<? extends IForm> step = getPreviousStep();
    activateStep(step);
  }

  /**
   * finish button clicked The default implementation just activates a null step and closes the wizard
   */
  @Order(40)
  @ConfigOperation
  protected void execFinish() {
    activateStep(null);
    close();
  }

  /**
   * cancel button clicked The default implementation just closes the wizard
   */
  @Order(50)
  @ConfigOperation
  protected void execCancel() {
    close();
  }

  /**
   * suspend button clicked The default implementation just closes the wizard
   */
  @Order(60)
  @ConfigOperation
  protected void execSuspend() {
    close();
  }

  /**
   * reset button clicked The default implementation does nothing
   */
  @Order(65)
  @ConfigOperation
  protected void execReset() {
  }

  /**
   * This method is called whenever a field value has changed. It can be used to refresh the button policy by calling
   * {@link #refreshButtonPolicy()} The default implementation does nothing
   */
  @ConfigOperation
  @Order(70)
  protected void execAnyFieldChanged(IFormField source) {
  }

  /**
   * This method is called once the container form is closed. The default implementation closes the wizard.
   */
  @ConfigOperation
  @Order(75)
  protected void execContainerFormClosed() {
    close();
  }

  /**
   * This method is used to make then wizard buttons visible/invisible/enabled/disabled based on the current form and
   * wizard state. The default implementation just sets the previous, next and finish buttons correctly based on the
   * {@link #getSteps()}.
   */
  @ConfigOperation
  @Order(80)
  protected void execRefreshButtonPolicy() {
    IWizardStep<? extends IForm> prev = getPreviousStep();
    IWizardStep<? extends IForm> next = getNextStep();
    //
    IWizardAction action;
    action = getContainerForm().getWizardPreviousStepButton();
    if (action != null) {
      action.setView(true, prev != null);
    }
    action = getContainerForm().getWizardNextStepButton();
    if (action != null) {
      action.setView(next != null, true);
    }
    action = getContainerForm().getWizardFinishButton();
    if (action != null) {
      action.setView(next == null, true);
    }
  }

  /**
   * Called when an app link has been clicked.
   * <p>
   * Subclasses can override this method. The default does nothing.
   */
  @ConfigOperation
  @Order(10)
  protected void execAppLinkAction(String ref) {
  }

  /**
   * Called when the "action" for a step has been executed (i.e. the step has been clicked in the step list).
   * <p>
   * Subclasses can override this method. The default activates the wizard step that triggered the action.
   */
  @ConfigOperation
  @Order(10)
  protected void execStepAction(IWizardStep<? extends IForm> step) {
    int stepKind = getStepKind(getActiveStep(), step);
    activateStep(step, (stepKind == IWizardStep.STEP_NEXT), (stepKind == IWizardStep.STEP_PREVIOUS));
  }

  protected final void interceptInitConfig() {
    m_objectExtensions.initConfig(createLocalExtension(), this::initConfig);
  }

  @SuppressWarnings("unchecked")
  protected void initConfig() {
    setTitle(getConfiguredTitle());
    setSubTitle(getConfiguredSubTitle());
    setManagedByContainerForm(getConfiguredManagedByContainerForm());
    // initially the wizard is in state "closed"
    setClosedInternal(true);
    setCloseTypeInternal(CloseType.Unknown);

    m_contributionHolder = new ContributionComposite(AbstractWizard.this);

    m_containerForm = createContainerForm();
    Assertions.assertNotNull(m_containerForm, "Missing container form");
    interceptDecorateContainerForm();

    if (isManagedByContainerForm()) {
      m_containerFormListener = this::handleContainerFormEvent;
      m_containerForm.addFormListener(m_containerFormListener);
    }

    // Run the initialization on behalf of the container form.
    runWithinContainerForm(() -> {
      // steps
      List<Class<? extends IWizardStep<? extends IForm>>> configuredAvailableSteps = getConfiguredAvailableSteps();
      List<IWizardStep> contributedSteps = m_contributionHolder.getContributionsByClass(IWizardStep.class);
      OrderedCollection<IWizardStep<? extends IForm>> steps = new OrderedCollection<>();
      for (Class<? extends IWizardStep<? extends IForm>> element : configuredAvailableSteps) {
        IWizardStep<? extends IForm> step = ConfigurationUtility.newInnerInstance(AbstractWizard.this, element);
        steps.addOrdered(step);
      }
      for (IWizardStep step : contributedSteps) {
        steps.addOrdered(step);
      }
      injectStepsInternal(steps);
      ExtensionUtility.moveModelObjects(steps);
      setAvailableSteps(steps.getOrderedList());

      // add listener to listen on any field in active form
      m_anyFieldChangeListener = e -> {
        try {
          interceptAnyFieldChanged((IFormField) e.getSource());
        }
        catch (RuntimeException | PlatformError t) {
          LOG.error("{} {}={}", e.getSource(), e.getPropertyName(), e.getNewValue(), t);
        }
      };
      propertySupport.addPropertyChangeListener(PROP_WIZARD_FORM, e -> {
        IForm oldForm = (IForm) e.getOldValue();
        IForm newForm = (IForm) e.getNewValue();
        if (oldForm != null) {
          oldForm.getRootGroupBox().removeSubtreePropertyChangeListener(IValueField.PROP_VALUE, m_anyFieldChangeListener);
        }
        if (newForm != null) {
          newForm.getRootGroupBox().addSubtreePropertyChangeListener(IValueField.PROP_VALUE, m_anyFieldChangeListener);
        }
      });
    });
  }

  protected void handleContainerFormEvent(FormEvent e) {
    if (e.getType() == FormEvent.TYPE_CLOSED) {
      m_containerForm.removeFormListener(m_containerFormListener);
      m_containerFormListener = null;
      interceptContainerFormClosed();
    }
  }

  protected IWizardExtension<? extends AbstractWizard> createLocalExtension() {
    return new LocalWizardExtension<>(this);
  }

  @Override
  public final List<? extends IWizardExtension<? extends AbstractWizard>> getAllExtensions() {
    return m_objectExtensions.getAllExtensions();
  }

  @Override
  public <T extends IExtension<?>> T getExtension(Class<T> c) {
    return m_objectExtensions.getExtension(c);
  }

  /**
   * Override this internal method only in order to make use of dynamic wizard steps<br>
   * Used to add and/or remove steps<br>
   * To change the order or specify the insert position use {@link IWizardStep#setOrder(double)}.
   *
   * @param steps
   *          live and mutable collection of configured steps, yet not initialized
   */
  protected void injectStepsInternal(OrderedCollection<IWizardStep<? extends IForm>> steps) {
  }

  @Override
  public IFastListenerList<WizardListener> wizardListeners() {
    return m_listenerList;
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
      m_accumulatedEvents.removeIf(existingEvent -> existingEvent.getType() == e.getType());
      m_accumulatedEvents.add(e);
    }
    else {
      wizardListeners().list().forEach(listener -> listener.wizardChanged(e));
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
  public boolean isManagedByContainerForm() {
    return propertySupport.getPropertyBool(PROP_MANAGED_BY_CONTAINER_FORM);
  }

  private void setManagedByContainerForm(boolean managedByContainerForm) {
    propertySupport.setPropertyBool(PROP_MANAGED_BY_CONTAINER_FORM, managedByContainerForm);
  }

  @Override
  public void setChanging(boolean changing) {
    if (changing) {
      m_changingLock.acquire();
    }
    else {
      m_changingLock.release();
      if (m_changingLock.isReleased()) {
        // now send all accumulated events
        List<WizardEvent> list = m_accumulatedEvents;
        m_accumulatedEvents = new ArrayList<>(3);
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
  public IDesktop getDesktop() {
    if (ClientSessionProvider.currentSession() != null) {
      return ClientSessionProvider.currentSession().getDesktop();
    }
    return null;
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
      if (m_stepList.get(i).equals(step)) {
        return i;
      }
    }
    return -1;
  }

  @Override
  public List<IWizardStep<? extends IForm>> getAvailableSteps() {
    return CollectionUtility.arrayList(m_availableStepList);
  }

  @Override
  public void setAvailableSteps(List<IWizardStep<? extends IForm>> steps) {
    m_availableStepList = new ArrayList<>();
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
    return CollectionUtility.arrayList(m_stepList);
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
      m_stepList = new ArrayList<>();
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
  public void activateStep(IWizardStep<? extends IForm> step) {
    activateStep(step, false, false);
  }

  @Override
  public void activateStep(IWizardStep<? extends IForm> targetStep, boolean jumpForward, boolean jumpBackward) {
    if (m_activeStep == targetStep) {
      return;
    }

    List<IWizardStep<? extends IForm>> steps;
    int kind = getStepKind(m_activeStep, targetStep);
    boolean skipIntermediateSteps = (kind == IWizardStep.STEP_NEXT && jumpForward) || (kind == IWizardStep.STEP_PREVIOUS && jumpBackward);
    if (!skipIntermediateSteps) {
      // gather intermediate steps; returned list contains at least target step that may be null
      steps = getStepSpan(m_activeStep, false, targetStep, true);
    }
    else {
      steps = Collections.singletonList(targetStep);
    }

    try {
      for (IWizardStep<? extends IForm> step : steps) {
        activateStepInternal(step, kind);
      }
    }
    finally {
      refreshButtonPolicy();
      fireStateChanged();
    }
  }

  protected void activateStepInternal(IWizardStep<? extends IForm> step, int kind) {
    // deactivate
    if (m_activeStep != null) {
      // if this call veto, m_activeStep is still set with an active valid step
      m_activeStep.deactivate(kind);
    }
    // activate
    IWizardStep<? extends IForm> lastStep = m_activeStep;
    m_activeStep = step;
    if (m_activeStep != null) {
      // if this call veto, m_activeStep is invalid and we have to go to the last step and reactivate it
      try {
        m_activeStep.activate(kind);
      }
      catch (RuntimeException e) {
        m_activeStep = lastStep;
        if (lastStep != null) {
          try {
            // if this activate throws again an exception, then m_activeStep is not active but has least a step that was active during activation process
            int stepKindInverted = kind == IWizardStep.STEP_NEXT ? IWizardStep.STEP_PREVIOUS : IWizardStep.STEP_NEXT;
            lastStep.activate(stepKindInverted);
          }
          catch (RuntimeException innerException) {
            BEANS.get(ExceptionHandler.class).handle(innerException);
          }
        }
        throw e;
      }
    }
    // notify callback for each step changed
    try {
      interceptActiveStepChanged();
    }
    catch (RuntimeException | PlatformError e) {
      BEANS.get(ExceptionHandler.class).handle(e);
    }
  }

  @Override
  public void refreshButtonPolicy() {
    try {
      interceptRefreshButtonPolicy();
    }
    catch (RuntimeException | PlatformError e) {
      BEANS.get(ExceptionHandler.class).handle(e);
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
    return IWizardStep.STEP_PREVIOUS;
  }

  @Override
  public List<IWizardStep<? extends IForm>> getStepSpan(IWizardStep<? extends IForm> from, boolean includeFrom, IWizardStep<? extends IForm> to, boolean includeTo) {
    List<IWizardStep<? extends IForm>> list = new ArrayList<>();
    if (includeFrom) {
      list.add(from);
    }
    if (from != null && to != null) {
      int fromIndex = getStepIndex(from);
      int toIndex = getStepIndex(to);
      if (fromIndex < toIndex - 1) {
        int n = toIndex - fromIndex - 1;
        for (int i = 0; i < n; i++) {
          list.add(m_stepList.get(fromIndex + 1 + i));
        }
      }
      if (fromIndex > toIndex + 1) {
        int n = fromIndex - toIndex - 1;
        for (int i = 0; i < n; i++) {
          list.add(m_stepList.get(fromIndex - 1 - i));
        }
      }
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
    if (!m_stepList.isEmpty() && index >= 0) {
      return CollectionUtility.arrayList(m_stepList.subList(0, Math.min(index, m_stepList.size())));
    }
    else {
      return CollectionUtility.emptyArrayList();
    }
  }

  public List<IWizardStep<? extends IForm>> getExpectedFuture() {
    int index = getStepIndex(getActiveStep());
    if (!m_stepList.isEmpty() && index < m_stepList.size()) {
      return CollectionUtility.arrayList(m_stepList.subList(Math.max(index + 1, 0), m_stepList.size()));
    }
    else {
      return CollectionUtility.emptyArrayList();
    }
  }

  @Override
  public void start() {
    assertClosed();
    if (m_blockingCondition.isBlocking()) {
      throw new ProcessingException("The wizard " + getClass().getSimpleName() + " has already been started");
    }
    m_blockingCondition.setBlocking(true);
    setClosedInternal(false);
    setCloseTypeInternal(CloseType.Unknown);

    // Run the initialization on behalf of this Form.
    runWithinContainerForm(() -> {
      interceptStart();
      if (m_containerForm.isFormStartable()) {
        m_containerForm.startWizard();
      }
      interceptPostStart();
    });
  }

  @Override
  public void close() {
    if (!isClosed()) {
      // close container form
      try {
        if (m_containerForm != null) {
          if (m_containerFormListener != null) {
            m_containerForm.removeFormListener(m_containerFormListener);
            m_containerFormListener = null;
          }
          if (!m_containerForm.isFormClosed()) {
            m_containerForm.doClose();
          }
        }
      }
      catch (RuntimeException e) {
        LOG.error("Unexpected error while closing container form: {}", m_containerForm, e);
      }
      // dispose all steps
      Collection<IWizardStep<? extends IForm>> set = new HashSet<>();
      set.addAll(getAvailableSteps());
      set.addAll(getSteps());
      for (IWizardStep<? extends IForm> step : set) {
        try {
          step.dispose();
        }
        catch (RuntimeException t) {
          LOG.error("Unexpected error while disposing step: {}", step, t);
        }
      }
      if (getCloseType() == CloseType.Unknown) {
        setCloseTypeInternal(CloseType.Closed);
      }
      setClosedInternal(true);
      fireClosed();
      // unlock
      m_blockingCondition.setBlocking(false);
    }
  }

  @Override
  public void waitFor() {
    // check if the desktop is observing this process
    IDesktop desktop = getDesktop();
    if (desktop == null || !desktop.isOpened()) {
      throw new ProcessingException("Cannot wait for {}. No desktop found, or the desktop is not opened in the UI yet.", new Object[]{getClass().getName()})
          .withCode(WAIT_FOR_ERROR_CODE);
    }
    // Do not exit upon ui cancel request, as the file chooser would be closed immediately otherwise.
    m_blockingCondition.waitFor(ModelJobs.EXECUTION_HINT_UI_INTERACTION_REQUIRED);
  }

  @Override
  public boolean isOpen() {
    return !isClosed();
  }

  @Override
  public boolean isClosed() {
    return propertySupport.getPropertyBool(PROP_CLOSED);
  }

  protected void setClosedInternal(boolean closed) {
    propertySupport.setProperty(PROP_CLOSED, closed);
  }

  @Override
  public CloseType getCloseType() {
    return (CloseType) propertySupport.getProperty(PROP_CLOSE_TYPE);
  }

  protected void setCloseTypeInternal(CloseType t) {
    propertySupport.setProperty(PROP_CLOSE_TYPE, t);
  }

  /**
   * next step The default implementation calls {@link #interceptNextStep()}
   */
  @Override
  public void doNextStep() {
    if (isOpen()) {
      runWithinContainerForm(this::interceptNextStep);
    }
  }

  /**
   * previous step The default implementation calls {@link #interceptPreviousStep()}
   */
  @Override
  public void doPreviousStep() {
    if (isOpen()) {
      runWithinContainerForm(this::interceptPreviousStep);
    }
  }

  /**
   * finish The default implementation calls {@link #interceptFinish()}
   */
  @Override
  public void doFinish() {
    if (isOpen()) {
      runWithinContainerForm(() -> {
        interceptFinish();
        setCloseTypeInternal(CloseType.Finished);
      });
    }
  }

  /**
   * cancel The default implementation calls {@link #interceptCancel()}
   */
  @Override
  public void doCancel() {
    if (isOpen()) {
      runWithinContainerForm(() -> {
        interceptCancel();
        setCloseTypeInternal(CloseType.Cancelled);
      });
    }
  }

  /**
   * suspend The default implementation calls {@link #interceptSuspend()}
   */
  @Override
  public void doSuspend() {
    if (isOpen()) {
      runWithinContainerForm(() -> {
        interceptSuspend();
        setCloseTypeInternal(CloseType.Suspended);
      });
    }
  }

  /**
   * reset The default implementation calls {@link #interceptReset()}
   */
  @Override
  public void doReset() {
    runWithinContainerForm(this::interceptReset);
  }

  @Override
  public void doAppLinkAction(final String ref) {
    if (isOpen()) {
      runWithinContainerForm(() -> interceptAppLinkAction(ref));
    }
  }

  @Override
  public void doStepAction(final IWizardStep<? extends IForm> step) {
    if (isOpen()) {
      runWithinContainerForm(() -> interceptStepAction(step));
    }
  }

  private void assertClosed() {
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
  public IWizardContainerForm createContainerForm() {
    return interceptCreateContainerForm();
  }

  @Override
  public IWizardContainerForm getContainerForm() {
    return m_containerForm;
  }

  @Override
  public String classId() {
    return ConfigurationUtility.getAnnotatedClassIdWithFallback(getClass());
  }

  /**
   * The extension delegating to the local methods. This Extension is always at the end of the chain and will not call
   * any further chain elements.
   */
  protected static class LocalWizardExtension<OWNER extends AbstractWizard> extends AbstractExtension<OWNER> implements IWizardExtension<OWNER> {

    public LocalWizardExtension(OWNER owner) {
      super(owner);
    }

    @Override
    public void execActiveStepChanged(WizardActiveStepChangedChain chain) {
      getOwner().execActiveStepChanged();
    }

    @Override
    public void execSuspend(WizardSuspendChain chain) {
      getOwner().execSuspend();
    }

    @Override
    public void execRefreshButtonPolicy(WizardRefreshButtonPolicyChain chain) {
      getOwner().execRefreshButtonPolicy();
    }

    @Override
    public void execCancel(WizardCancelChain chain) {
      getOwner().execCancel();
    }

    @Override
    public void execStart(WizardStartChain chain) {
      getOwner().execStart();
    }

    @Override
    public void execPostStart(WizardPostStartChain chain) {
      getOwner().execPostStart();
    }

    @Override
    public IWizardContainerForm execCreateContainerForm(WizardCreateContainerFormChain chain) {
      return getOwner().execCreateContainerForm();
    }

    @Override
    public void execDecorateContainerForm(WizardDecorateContainerFormChain chain) {
      getOwner().execDecorateContainerForm();
    }

    @Override
    public void execAnyFieldChanged(WizardAnyFieldChangedChain chain, IFormField source) {
      getOwner().execAnyFieldChanged(source);
    }

    @Override
    public void execContainerFormClosed(WizardContainerFormClosedChain chain) {
      getOwner().execContainerFormClosed();
    }

    @Override
    public void execReset(WizardResetChain chain) {
      getOwner().execReset();
    }

    @Override
    public void execAppLinkAction(WizardAppLinkActionChain chain, String ref) {
      getOwner().execAppLinkAction(ref);
    }

    @Override
    public void execStepAction(WizardStepActionChain chain, IWizardStep<? extends IForm> step) {
      getOwner().execStepAction(step);
    }

    @Override
    public void execPreviousStep(WizardPreviousStepChain chain) {
      getOwner().execPreviousStep();
    }

    @Override
    public void execNextStep(WizardNextStepChain chain) {
      getOwner().execNextStep();
    }

    @Override
    public void execFinish(WizardFinishChain chain) {
      getOwner().execFinish();
    }

  }

  protected final void interceptActiveStepChanged() {
    List<? extends IWizardExtension<? extends AbstractWizard>> extensions = getAllExtensions();
    WizardActiveStepChangedChain chain = new WizardActiveStepChangedChain(extensions);
    chain.execActiveStepChanged();
  }

  protected final void interceptSuspend() {
    List<? extends IWizardExtension<? extends AbstractWizard>> extensions = getAllExtensions();
    WizardSuspendChain chain = new WizardSuspendChain(extensions);
    chain.execSuspend();
  }

  protected final void interceptRefreshButtonPolicy() {
    List<? extends IWizardExtension<? extends AbstractWizard>> extensions = getAllExtensions();
    WizardRefreshButtonPolicyChain chain = new WizardRefreshButtonPolicyChain(extensions);
    chain.execRefreshButtonPolicy();
  }

  protected final void interceptCancel() {
    List<? extends IWizardExtension<? extends AbstractWizard>> extensions = getAllExtensions();
    WizardCancelChain chain = new WizardCancelChain(extensions);
    chain.execCancel();
  }

  protected final void interceptStart() {
    List<? extends IWizardExtension<? extends AbstractWizard>> extensions = getAllExtensions();
    WizardStartChain chain = new WizardStartChain(extensions);
    chain.execStart();
  }

  protected final void interceptPostStart() {
    List<? extends IWizardExtension<? extends AbstractWizard>> extensions = getAllExtensions();
    WizardPostStartChain chain = new WizardPostStartChain(extensions);
    chain.execPostStart();
  }

  protected final IWizardContainerForm interceptCreateContainerForm() {
    List<? extends IWizardExtension<? extends AbstractWizard>> extensions = getAllExtensions();
    WizardCreateContainerFormChain chain = new WizardCreateContainerFormChain(extensions);
    return chain.execCreateContainerForm();
  }

  protected void interceptDecorateContainerForm() {
    List<? extends IWizardExtension<? extends AbstractWizard>> extensions = getAllExtensions();
    WizardDecorateContainerFormChain chain = new WizardDecorateContainerFormChain(extensions);
    chain.execDecorateContainerForm();
  }

  protected final void interceptAnyFieldChanged(IFormField source) {
    List<? extends IWizardExtension<? extends AbstractWizard>> extensions = getAllExtensions();
    WizardAnyFieldChangedChain chain = new WizardAnyFieldChangedChain(extensions);
    chain.execAnyFieldChanged(source);
  }

  protected final void interceptContainerFormClosed() {
    List<? extends IWizardExtension<? extends AbstractWizard>> extensions = getAllExtensions();
    WizardContainerFormClosedChain chain = new WizardContainerFormClosedChain(extensions);
    chain.execContainerFormClosed();
  }

  protected final void interceptReset() {
    List<? extends IWizardExtension<? extends AbstractWizard>> extensions = getAllExtensions();
    WizardResetChain chain = new WizardResetChain(extensions);
    chain.execReset();
  }

  protected final void interceptAppLinkAction(String ref) {
    List<? extends IWizardExtension<? extends AbstractWizard>> extensions = getAllExtensions();
    WizardAppLinkActionChain chain = new WizardAppLinkActionChain(extensions);
    chain.execAppLinkAction(ref);
  }

  protected final void interceptStepAction(IWizardStep<? extends IForm> wizardStep) {
    List<? extends IWizardExtension<? extends AbstractWizard>> extensions = getAllExtensions();
    WizardStepActionChain chain = new WizardStepActionChain(extensions);
    chain.execStepAction(wizardStep);
  }

  protected final void interceptPreviousStep() {
    List<? extends IWizardExtension<? extends AbstractWizard>> extensions = getAllExtensions();
    WizardPreviousStepChain chain = new WizardPreviousStepChain(extensions);
    chain.execPreviousStep();
  }

  protected final void interceptNextStep() {
    List<? extends IWizardExtension<? extends AbstractWizard>> extensions = getAllExtensions();
    WizardNextStepChain chain = new WizardNextStepChain(extensions);
    chain.execNextStep();
  }

  protected final void interceptFinish() {
    List<? extends IWizardExtension<? extends AbstractWizard>> extensions = getAllExtensions();
    WizardFinishChain chain = new WizardFinishChain(extensions);
    chain.execFinish();
  }

  protected void runWithinContainerForm(final IRunnable runnable) {
    ClientRunContexts
        .copyCurrent()
        .withForm(getContainerForm())
        .run(runnable);
  }
}
