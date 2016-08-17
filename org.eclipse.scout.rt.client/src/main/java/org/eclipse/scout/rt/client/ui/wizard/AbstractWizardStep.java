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

import java.util.List;

import org.eclipse.scout.rt.client.extension.ui.wizard.IWizardStepExtension;
import org.eclipse.scout.rt.client.extension.ui.wizard.WizardStepChains.WizardStepActionChain;
import org.eclipse.scout.rt.client.extension.ui.wizard.WizardStepChains.WizardStepActivateChain;
import org.eclipse.scout.rt.client.extension.ui.wizard.WizardStepChains.WizardStepDeactivateChain;
import org.eclipse.scout.rt.client.extension.ui.wizard.WizardStepChains.WizardStepDisposeChain;
import org.eclipse.scout.rt.client.extension.ui.wizard.WizardStepChains.WizardStepFormClosedChain;
import org.eclipse.scout.rt.client.extension.ui.wizard.WizardStepChains.WizardStepFormDiscardedChain;
import org.eclipse.scout.rt.client.extension.ui.wizard.WizardStepChains.WizardStepFormStoredChain;
import org.eclipse.scout.rt.client.ui.form.FormEvent;
import org.eclipse.scout.rt.client.ui.form.FormListener;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.button.IButton;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.IOrdered;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigOperation;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.exception.ExceptionHandler;
import org.eclipse.scout.rt.platform.reflect.AbstractPropertyObserver;
import org.eclipse.scout.rt.platform.reflect.ConfigurationUtility;
import org.eclipse.scout.rt.platform.reflect.IPropertyObserver;
import org.eclipse.scout.rt.shared.extension.AbstractExtension;
import org.eclipse.scout.rt.shared.extension.IExtensibleObject;
import org.eclipse.scout.rt.shared.extension.IExtension;
import org.eclipse.scout.rt.shared.extension.ObjectExtensions;

@ClassId("39d99aa9-002c-4367-9558-20225928fbd1")
public abstract class AbstractWizardStep<FORM extends IForm> extends AbstractPropertyObserver implements IWizardStep<FORM>, IPropertyObserver, IExtensibleObject {

  private IWizard m_wizard;
  private FORM m_form;
  private FormListener m_formListener;
  private boolean m_performingWizardStepAction;
  private int m_activationCounter;
  private boolean m_initialized;
  private final ObjectExtensions<AbstractWizardStep<FORM>, IWizardStepExtension<FORM, ? extends AbstractWizardStep<FORM>>> m_objectExtensions;

  public AbstractWizardStep() {
    this(true);
  }

  public AbstractWizardStep(boolean callInitializer) {
    m_objectExtensions = new ObjectExtensions<AbstractWizardStep<FORM>, IWizardStepExtension<FORM, ? extends AbstractWizardStep<FORM>>>(this, false);
    if (callInitializer) {
      callInitializer();
    }
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

  @ConfigProperty(ConfigProperty.TEXT)
  @Order(30)
  protected String getConfiguredTooltipText() {
    return null;
  }

  /**
   * @deprecated use getConfiguredTitle()
   */
  @Deprecated
  @ConfigProperty(ConfigProperty.TEXT)
  @Order(35)
  protected String getConfiguredTitleHtml() {
    return null;
  }

  /**
   * @deprecated use getConfiguredSubTitle(), will be removed in Scout 6.1
   */
  @Deprecated
  @ConfigProperty(ConfigProperty.TEXT)
  @Order(38)
  protected String getConfiguredDescriptionHtml() {
    return null;
  }

  @ConfigProperty(ConfigProperty.ICON_ID)
  @Order(40)
  protected String getConfiguredIconId() {
    return null;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(50)
  protected boolean getConfiguredEnabled() {
    return true;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(60)
  protected boolean getConfiguredVisible() {
    return true;
  }

  /**
   * Configures the css class(es) of this field.
   * <p>
   * Subclasses can override this method. Default is {@code null}.
   *
   * @return a string containing one or more classes separated by space, or null if no class should be set.
   */
  @ConfigProperty(ConfigProperty.STRING)
  @Order(70)
  protected String getConfiguredCssClass() {
    return null;
  }

  /**
   * Configures the view order of this wizard step. The view order determines the order in which the steps appear. The
   * order of steps with no view order configured ({@code < 0}) is initialized based on the {@link Order} annotation of
   * the wizard step class.
   * <p>
   * Subclasses can override this method. The default is {@link IOrdered#DEFAULT_ORDER}.
   *
   * @return View order of this wizard step.
   */
  @ConfigProperty(ConfigProperty.DOUBLE)
  @Order(80)
  protected double getConfiguredViewOrder() {
    return IOrdered.DEFAULT_ORDER;
  }

  /**
   * Configures whether the wizard action is initially enabled.
   * <p>
   * Subclasses can override this method. The default is <code>false</code>.
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(90)
  protected boolean getConfiguredActionEnabled() {
    return false;
  }

  /**
   * @param stepKind
   *          any of the STEP_* constants activate this step normally creates a form, calls
   *          {@link IForm#startWizardStep(IWizardStep, Class)} on the form and places the form inside the wizard
   *          {@link IWizard#setWizardForm(org.eclipse.scout.rt.client.ui.form.IForm)}
   */
  @Order(10)
  @ConfigOperation
  protected void execActivate(int stepKind) {
  }

  /**
   * @param stepKind
   *          any of the STEP_* constants deactivate this step
   */
  @Order(20)
  @ConfigOperation
  protected void execDeactivate(int stepKind) {
  }

  /**
   * dispose this step The default implementation closes the form at {@link #getForm()}
   */
  @Order(30)
  @ConfigOperation
  protected void execDispose() {
    FORM f = getForm();
    if (f != null) {
      f.doClose();
    }
  }

  /**
   * When the cached form is stored (it may still be open) this method is called.
   *
   * @param activation
   *          true if this method is called by the wizard itself by {@link IWizardStep#activate(int)},
   *          {@link IWizardStep#deactivate(int)} or {@link IWizardStep#dispose()} The default implementation does
   *          nothing.
   */
  @Order(40)
  @ConfigOperation
  protected void execFormStored(boolean activation) {
  }

  /**
   * When the cached form is discarded (save was either not requested or it was forcedly closed) this method is called.
   *
   * @param activation
   *          true if this method is called by the wizard itself by {@link IWizardStep#activate(int)},
   *          {@link IWizardStep#deactivate(int)} or {@link IWizardStep#dispose()} The default implementation does
   *          nothing.
   */
  @Order(50)
  @ConfigOperation
  protected void execFormDiscarded(boolean activation) {
  }

  /**
   * When the cached form is closed (after some store and/or a discard operation) this method is called.
   *
   * @param activation
   *          true if this method is called by the wizard itself by {@link IWizardStep#activate(int)},
   *          {@link IWizardStep#deactivate(int)} or {@link IWizardStep#dispose()} The default implementation calls
   *          {@link IWizard#doNextStep()} iff activation=false and form was saved (formDataChanged=true)
   */
  @Order(60)
  @ConfigOperation
  protected void execFormClosed(boolean activation) {
    if (!activation) {
      if (getForm().isFormStored() && getForm().getCloseSystemType() != IButton.SYSTEM_TYPE_CANCEL) {
        getWizard().doNextStep();
      }
    }
  }

  /**
   * Called when an "action" is performed on the wizard step (i.e. it is clicked in the UI).
   * <p>
   * The default delegates to the wizard.
   */
  @Order(70)
  @ConfigOperation
  protected void execAction() {
    if (getWizard() != null) {
      getWizard().doStepAction(this);
    }
  }

  /**
   * Calculates the column's view order, e.g. if the @Order annotation is set to 30.0, the method will return 30.0. If
   * no {@link Order} annotation is set, the method checks its super classes for an @Order annotation.
   *
   * @since 3.10.0-M4
   */
  protected double calculateViewOrder() {
    double viewOrder = getConfiguredViewOrder();
    Class<?> cls = getClass();
    if (viewOrder == IOrdered.DEFAULT_ORDER) {
      while (cls != null && IWizardStep.class.isAssignableFrom(cls)) {
        if (cls.isAnnotationPresent(Order.class)) {
          Order order = (Order) cls.getAnnotation(Order.class);
          return order.value();
        }
        cls = cls.getSuperclass();
      }
    }
    return viewOrder;
  }

  protected final void interceptInitConfig() {
    m_objectExtensions.initConfig(createLocalExtension(), new Runnable() {
      @Override
      public void run() {
        initConfig();
      }
    });
  }

  protected void initConfig() {
    setTitle(getConfiguredTitle());
    setSubTitle(getConfiguredSubTitle());
    setTooltipText(getConfiguredTooltipText());
    setTitleHtml(getConfiguredTitleHtml());
    setDescriptionHtml(getConfiguredDescriptionHtml());
    setIconId(getConfiguredIconId());
    setEnabled(getConfiguredEnabled());
    setVisible(getConfiguredVisible());
    setCssClass((getConfiguredCssClass()));
    setOrder(calculateViewOrder());
    setActionEnabled(getConfiguredActionEnabled());
  }

  protected IWizardStepExtension<FORM, ? extends AbstractWizardStep<FORM>> createLocalExtension() {
    return new LocalWizardStepExtension<FORM, AbstractWizardStep<FORM>>(this);
  }

  @Override
  public final List<? extends IWizardStepExtension<FORM, ? extends AbstractWizardStep<FORM>>> getAllExtensions() {
    return m_objectExtensions.getAllExtensions();
  }

  @Override
  public <T extends IExtension<?>> T getExtension(Class<T> c) {
    return m_objectExtensions.getExtension(c);
  }

  /*
   * Runtime
   */

  @Override
  public FORM getForm() {
    return m_form;
  }

  @Override
  public void setForm(FORM f) {
    // remove old
    if (m_form != null) {
      if (m_formListener != null) {
        m_form.removeFormListener(m_formListener);
      }
    }
    m_form = f;
    // add old
    if (m_form != null) {
      if (m_formListener == null) {
        m_formListener = new FormListener() {
          @Override
          public void formChanged(FormEvent e) {
            try {
              switch (e.getType()) {
                case FormEvent.TYPE_STORE_AFTER: {
                  interceptFormStored(m_activationCounter > 0);
                  break;
                }
                case FormEvent.TYPE_DISCARDED: {
                  interceptFormDiscarded(m_activationCounter > 0);
                  break;
                }
                case FormEvent.TYPE_CLOSED: {
                  interceptFormClosed(m_activationCounter > 0);
                  break;
                }
              }
            }
            catch (Exception pe) {
              BEANS.get(ExceptionHandler.class).handle(pe);
            }
            switch (e.getType()) {
              case FormEvent.TYPE_CLOSED: {
                setForm(null);
                break;
              }
            }
          }
        };
      }
      m_form.addFormListener(m_formListener);
    }
  }

  @Override
  public IWizard getWizard() {
    return m_wizard;
  }

  @Override
  public void setWizardInternal(IWizard w) {
    m_wizard = w;
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
  public String getTooltipText() {
    return propertySupport.getPropertyString(PROP_TOOLTIP_TEXT);
  }

  @Override
  public void setTooltipText(String tooltipText) {
    propertySupport.setPropertyString(PROP_TOOLTIP_TEXT, tooltipText);
  }

  @SuppressWarnings("deprecation")
  @Deprecated
  @Override
  public String getTitleHtml() {
    return propertySupport.getPropertyString(PROP_TITLE_HTML);
  }

  @SuppressWarnings("deprecation")
  @Deprecated
  @Override
  public void setTitleHtml(String s) {
    propertySupport.setPropertyString(PROP_TITLE_HTML, s);
  }

  @SuppressWarnings("deprecation")
  @Deprecated
  @Override
  public String getDescriptionHtml() {
    return propertySupport.getPropertyString(PROP_DESCRIPTION_HTML);
  }

  @SuppressWarnings("deprecation")
  @Deprecated
  @Override
  public void setDescriptionHtml(String s) {
    propertySupport.setPropertyString(PROP_DESCRIPTION_HTML, s);
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
  public boolean isEnabled() {
    return propertySupport.getPropertyBool(PROP_ENABLED);
  }

  @Override
  public void setEnabled(boolean enabled) {
    propertySupport.setPropertyBool(PROP_ENABLED, enabled);
  }

  @Override
  public boolean isVisible() {
    return propertySupport.getPropertyBool(PROP_VISIBLE);
  }

  @Override
  public void setVisible(boolean visible) {
    propertySupport.setPropertyBool(PROP_VISIBLE, visible);
  }

  @Override
  public boolean isActionEnabled() {
    return propertySupport.getPropertyBool(PROP_ACTION_ENABLED);
  }

  @Override
  public void setActionEnabled(boolean actionEnabled) {
    propertySupport.setPropertyBool(PROP_ACTION_ENABLED, actionEnabled);
  }

  @Override
  public String getCssClass() {
    return (String) propertySupport.getProperty(PROP_CSS_CLASS);
  }

  @Override
  public void setCssClass(String cssClass) {
    propertySupport.setProperty(PROP_CSS_CLASS, cssClass);
  }

  @Override
  public double getOrder() {
    return propertySupport.getPropertyDouble(PROP_ORDER);
  }

  @Override
  public void setOrder(double order) {
    propertySupport.setPropertyDouble(PROP_ORDER, order);
  }

  @Override
  public void activate(int stepKind) {
    try {
      m_activationCounter++;
      interceptActivate(stepKind);
    }
    finally {
      m_activationCounter--;
    }
  }

  @Override
  public void deactivate(int stepKind) {
    try {
      m_activationCounter++;
      interceptDeactivate(stepKind);
    }
    finally {
      m_activationCounter--;
    }
  }

  @Override
  public void dispose() {
    try {
      m_activationCounter++;
      interceptDispose();
    }
    finally {
      m_activationCounter--;
    }
  }

  protected boolean isPerformingWizardStepAction() {
    return m_performingWizardStepAction;
  }

  protected void setPerformingWizardStepAction(boolean performingWizardStepAction) {
    m_performingWizardStepAction = performingWizardStepAction;
  }

  @Override
  public void doAction() {
    if (isActionEnabled() && !isPerformingWizardStepAction()) {
      try {
        setPerformingWizardStepAction(true);
        interceptAction();
      }
      finally {
        setPerformingWizardStepAction(false);
      }
    }
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "[" + getTitle() + "]";
  }

  /**
   * Needs to be overridden for dynamically added steps.
   */
  @Override
  public String classId() {
    return ConfigurationUtility.getAnnotatedClassIdWithFallback(getClass());
  }

  /**
   * The extension delegating to the local methods. This Extension is always at the end of the chain and will not call
   * any further chain elements.
   */
  protected static class LocalWizardStepExtension<FORM extends IForm, OWNER extends AbstractWizardStep<FORM>> extends AbstractExtension<OWNER> implements IWizardStepExtension<FORM, OWNER> {

    public LocalWizardStepExtension(OWNER owner) {
      super(owner);
    }

    @Override
    public void execDeactivate(WizardStepDeactivateChain<? extends IForm> chain, int stepKind) {
      getOwner().execDeactivate(stepKind);
    }

    @Override
    public void execDispose(WizardStepDisposeChain<? extends IForm> chain) {
      getOwner().execDispose();
    }

    @Override
    public void execFormClosed(WizardStepFormClosedChain<? extends IForm> chain, boolean activation) {
      getOwner().execFormClosed(activation);
    }

    @Override
    public void execActivate(WizardStepActivateChain<? extends IForm> chain, int stepKind) {
      getOwner().execActivate(stepKind);
    }

    @Override
    public void execFormDiscarded(WizardStepFormDiscardedChain<? extends IForm> chain, boolean activation) {
      getOwner().execFormDiscarded(activation);
    }

    @Override
    public void execFormStored(WizardStepFormStoredChain<? extends IForm> chain, boolean activation) {
      getOwner().execFormStored(activation);
    }

    @Override
    public void execAction(WizardStepActionChain<? extends IForm> chain) {
      getOwner().execAction();
    }
  }

  protected final void interceptDeactivate(int stepKind) {
    List<? extends IWizardStepExtension<FORM, ? extends AbstractWizardStep<? extends IForm>>> extensions = getAllExtensions();
    WizardStepDeactivateChain<FORM> chain = new WizardStepDeactivateChain<FORM>(extensions);
    chain.execDeactivate(stepKind);
  }

  protected final void interceptDispose() {
    List<? extends IWizardStepExtension<FORM, ? extends AbstractWizardStep<? extends IForm>>> extensions = getAllExtensions();
    WizardStepDisposeChain<FORM> chain = new WizardStepDisposeChain<FORM>(extensions);
    chain.execDispose();
  }

  protected final void interceptFormClosed(boolean activation) {
    List<? extends IWizardStepExtension<FORM, ? extends AbstractWizardStep<? extends IForm>>> extensions = getAllExtensions();
    WizardStepFormClosedChain<FORM> chain = new WizardStepFormClosedChain<FORM>(extensions);
    chain.execFormClosed(activation);
  }

  protected final void interceptActivate(int stepKind) {
    List<? extends IWizardStepExtension<FORM, ? extends AbstractWizardStep<? extends IForm>>> extensions = getAllExtensions();
    WizardStepActivateChain<FORM> chain = new WizardStepActivateChain<FORM>(extensions);
    chain.execActivate(stepKind);
  }

  protected final void interceptFormDiscarded(boolean activation) {
    List<? extends IWizardStepExtension<FORM, ? extends AbstractWizardStep<? extends IForm>>> extensions = getAllExtensions();
    WizardStepFormDiscardedChain<FORM> chain = new WizardStepFormDiscardedChain<FORM>(extensions);
    chain.execFormDiscarded(activation);
  }

  protected final void interceptFormStored(boolean activation) {
    List<? extends IWizardStepExtension<FORM, ? extends AbstractWizardStep<? extends IForm>>> extensions = getAllExtensions();
    WizardStepFormStoredChain<FORM> chain = new WizardStepFormStoredChain<FORM>(extensions);
    chain.execFormStored(activation);
  }

  protected final void interceptAction() {
    List<? extends IWizardStepExtension<FORM, ? extends AbstractWizardStep<? extends IForm>>> extensions = getAllExtensions();
    WizardStepActionChain<FORM> chain = new WizardStepActionChain<FORM>(extensions);
    chain.execAction();
  }
}
