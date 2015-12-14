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
package org.eclipse.scout.rt.client.ui.wizard;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.client.extension.ui.wizard.IWizardExtension;
import org.eclipse.scout.rt.client.extension.ui.wizard.WizardChains.WizardActiveStepChangedChain;
import org.eclipse.scout.rt.client.extension.ui.wizard.WizardChains.WizardAnyFieldChangedChain;
import org.eclipse.scout.rt.client.extension.ui.wizard.WizardChains.WizardAppLinkActionChain;
import org.eclipse.scout.rt.client.extension.ui.wizard.WizardChains.WizardCancelChain;
import org.eclipse.scout.rt.client.extension.ui.wizard.WizardChains.WizardCreateContainerFormChain;
import org.eclipse.scout.rt.client.extension.ui.wizard.WizardChains.WizardDecorateContainerFormChain;
import org.eclipse.scout.rt.client.extension.ui.wizard.WizardChains.WizardFinishChain;
import org.eclipse.scout.rt.client.extension.ui.wizard.WizardChains.WizardNextStepChain;
import org.eclipse.scout.rt.client.extension.ui.wizard.WizardChains.WizardPreviousStepChain;
import org.eclipse.scout.rt.client.extension.ui.wizard.WizardChains.WizardRefreshButtonPolicyChain;
import org.eclipse.scout.rt.client.extension.ui.wizard.WizardChains.WizardResetChain;
import org.eclipse.scout.rt.client.extension.ui.wizard.WizardChains.WizardStartChain;
import org.eclipse.scout.rt.client.extension.ui.wizard.WizardChains.WizardStepActionChain;
import org.eclipse.scout.rt.client.extension.ui.wizard.WizardChains.WizardSuspendChain;
import org.eclipse.scout.rt.client.job.ModelJobs;
import org.eclipse.scout.rt.client.session.ClientSessionProvider;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.IValueField;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigOperation;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.platform.exception.ExceptionHandler;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.job.IBlockingCondition;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.reflect.AbstractPropertyObserver;
import org.eclipse.scout.rt.platform.reflect.ConfigurationUtility;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.EventListenerList;
import org.eclipse.scout.rt.platform.util.collection.OrderedCollection;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.platform.util.concurrent.OptimisticLock;
import org.eclipse.scout.rt.shared.extension.AbstractExtension;
import org.eclipse.scout.rt.shared.extension.ContributionComposite;
import org.eclipse.scout.rt.shared.extension.ExtensionUtility;
import org.eclipse.scout.rt.shared.extension.IContributionOwner;
import org.eclipse.scout.rt.shared.extension.IExtensibleObject;
import org.eclipse.scout.rt.shared.extension.IExtension;
import org.eclipse.scout.rt.shared.extension.ObjectExtensions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractWizard extends AbstractPropertyObserver implements IWizard, IContributionOwner, IExtensibleObject {
  private static final Logger LOG = LoggerFactory.getLogger(AbstractWizard.class);

  private boolean m_initialized;
  private final EventListenerList m_listenerList;
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

  public AbstractWizard() {
    this(true);
  }

  public AbstractWizard(boolean callInitializer) {
    m_listenerList = new EventListenerList();
    m_changingLock = new OptimisticLock();
    m_accumulatedEvents = new ArrayList<WizardEvent>(3);
    m_availableStepList = new ArrayList<IWizardStep<? extends IForm>>(0);
    m_stepList = new ArrayList<IWizardStep<? extends IForm>>(0);
    m_blockingCondition = Jobs.newBlockingCondition(false);
    m_objectExtensions = new ObjectExtensions<AbstractWizard, IWizardExtension<? extends AbstractWizard>>(this);
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

  @SuppressWarnings("unchecked")
  private List<Class<? extends IWizardStep<? extends IForm>>> getConfiguredAvailableSteps() {
    Class<?>[] dca = ConfigurationUtility.getDeclaredPublicClasses(getClass());
    List<Class<IWizardStep>> filtered = ConfigurationUtility.filterClasses(dca, IWizardStep.class);

    List<Class<? extends IWizardStep<? extends IForm>>> result = new ArrayList<Class<? extends IWizardStep<? extends IForm>>>(filtered.size());
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
    if (steps.size() > 0) {
      activateStep(steps.get(0));
    }
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
   * This is a delegate methode that is normally called by the wizard status field (html field) in the
   * {@link IWizardContainerForm} whenever a link is clicked.
   *
   * @param url
   * @param path
   *          {@link URL#getPath()}
   * @param local
   *          true if the url is not a valid external url but a local model url (http://local/...)
   * @deprecated use {@link #execAppLinkAction(String)} instead
   */
  @ConfigOperation
  @Order(230)
  @Deprecated
  protected void execHyperlinkAction(URL url, String path, boolean local) {
    LOG.info("execHyperlinkAction {} (in {})", url, getClass().getName());
  }

  /**
   * Called when an app link has been clicked.
   * <p>
   * Subclasses can override this method. The default does nothing.
   */
  @ConfigOperation
  @Order(10)
  protected void execAppLinkAction(String ref) {
    //FIXME cgu: remove this code when execpHyperlinkAction has been removed
    URL url = null;
    boolean local = false;
    if (ref != null) {
      try {
        url = new URL(ref);
        local = "local".equals(url.getHost());
      }
      catch (MalformedURLException e) {
        LOG.error("Malformed URL '{}'", ref, e);
      }
    }
    execHyperlinkAction(url, ref, local);
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
    m_objectExtensions.initConfig(createLocalExtension(), new Runnable() {
      @Override
      public void run() {
        initConfig();
      }
    });
  }

  @SuppressWarnings({"boxing", "unchecked"})
  protected void initConfig() {
    setTitle(getConfiguredTitle());
    setSubTitle(getConfiguredSubTitle());
    // initially the wizard is in state "closed"
    setClosedInternal(true);
    setCloseTypeInternal(CloseType.Unknown);

    m_contributionHolder = new ContributionComposite(AbstractWizard.this);

    m_containerForm = createContainerForm();
    Assertions.assertNotNull(m_containerForm, "Missing container form");
    interceptDecorateContainerForm();

    // Run the initialization on behalf of the container form.
    ClientRunContexts.copyCurrent().withForm(m_containerForm).run(new IRunnable() {
      @Override
      public void run() throws Exception {
        // steps
        List<Class<? extends IWizardStep<? extends IForm>>> configuredAvailableSteps = getConfiguredAvailableSteps();
        List<IWizardStep> contributedSteps = m_contributionHolder.getContributionsByClass(IWizardStep.class);
        OrderedCollection<IWizardStep<? extends IForm>> steps = new OrderedCollection<IWizardStep<? extends IForm>>();
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
        m_anyFieldChangeListener = new PropertyChangeListener() {
          @Override
          public void propertyChange(PropertyChangeEvent e) {
            try {
              interceptAnyFieldChanged((IFormField) e.getSource());
            }
            catch (RuntimeException t) {
              LOG.error("{} {}={}", e.getSource(), e.getPropertyName(), e.getNewValue(), t);
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
    });
  }

  protected IWizardExtension<? extends AbstractWizard> createLocalExtension() {
    return new LocalWizardExtension<AbstractWizard>(this);
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
  public void activateStep(IWizardStep<? extends IForm> step) {
    activateStep(step, false, false);
  }

  @Override
  public void activateStep(IWizardStep<? extends IForm> step, boolean jumpForward, boolean jumpBackward) {
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
          interceptActiveStepChanged();
        }
        catch (RuntimeException e) {
          BEANS.get(ExceptionHandler.class).handle(e);
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
      interceptRefreshButtonPolicy();
    }
    catch (RuntimeException e) {
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
  public void start() {
    assertClosed();
    if (m_blockingCondition.isBlocking()) {
      throw new ProcessingException("The wizard " + getClass().getSimpleName() + " has already been started");
    }
    m_blockingCondition.setBlocking(true);
    setClosedInternal(false);
    setCloseTypeInternal(CloseType.Unknown);

    // Run the initialization on behalf of this Form.
    ClientRunContexts.copyCurrent().withForm(m_containerForm).run(new IRunnable() {

      @Override
      public void run() throws Exception {
        interceptStart();
        if (!m_containerForm.isFormStarted()) {
          m_containerForm.startWizard();
        }
      }
    });
  }

  @Override
  public void close() {
    if (!isClosed()) {
      // close container form
      try {
        if (m_containerForm != null) {
          m_containerForm.doClose();
        }
      }
      catch (RuntimeException e) {
        // TODO [5.2] abr: Check if logging is the correct here
        LOG.error("Unexpected error while closing form: {}", m_containerForm, e);
      }
      // dispose all steps
      HashSet<IWizardStep<? extends IForm>> set = new HashSet<IWizardStep<? extends IForm>>();
      set.addAll(getAvailableSteps());
      set.addAll(getSteps());
      for (IWizardStep<? extends IForm> step : set) {
        try {
          step.dispose();
        }
        catch (RuntimeException t) {
          // TODO [5.2] abr: Check if logging is the correct here
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
      interceptNextStep();
    }
  }

  /**
   * previous step The default implementation calls {@link #interceptPreviousStep()}
   */
  @Override
  public void doPreviousStep() {
    if (isOpen()) {
      interceptPreviousStep();
    }
  }

  /**
   * finish The default implementation calls {@link #interceptFinish()}
   */
  @Override
  public void doFinish() {
    if (isOpen()) {
      interceptFinish();
      setCloseTypeInternal(CloseType.Finished);
    }
  }

  /**
   * cancel The default implementation calls {@link #interceptCancel()}
   */
  @Override
  public void doCancel() {
    if (isOpen()) {
      interceptCancel();
      setCloseTypeInternal(CloseType.Cancelled);
    }
  }

  /**
   * suspend The default implementation calls {@link #interceptSuspend()}
   */
  @Override
  public void doSuspend() {
    if (isOpen()) {
      interceptSuspend();
      setCloseTypeInternal(CloseType.Suspended);
    }
  }

  /**
   * reset The default implementation calls {@link #interceptReset()}
   */
  @Override
  public void doReset() {
    interceptReset();
  }

  @SuppressWarnings("deprecation")
  @Override
  public void doHyperlinkAction(URL url, String path, boolean local) {
    if (isOpen()) {
      execHyperlinkAction(url, path, local);
    }
  }

  @Override
  public void doAppLinkAction(String ref) {
    if (isOpen()) {
      interceptAppLinkAction(ref);
    }
  }

  @Override
  public void doStepAction(IWizardStep<? extends IForm> step) {
    if (isOpen()) {
      interceptStepAction(step);
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
}
