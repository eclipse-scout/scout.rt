/*******************************************************************************
 * Copyright (c) 2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.group;

import java.util.List;

import org.eclipse.scout.rt.client.ModelContextProxy;
import org.eclipse.scout.rt.client.ModelContextProxy.ModelContext;
import org.eclipse.scout.rt.client.extension.ui.group.GroupChains.GroupDisposeGroupChain;
import org.eclipse.scout.rt.client.extension.ui.group.GroupChains.GroupInitGroupChain;
import org.eclipse.scout.rt.client.extension.ui.group.IGroupExtension;
import org.eclipse.scout.rt.client.ui.AbstractWidget;
import org.eclipse.scout.rt.client.ui.IWidget;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.IOrdered;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.classid.ITypeWithClassId;
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.reflect.ConfigurationUtility;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.shared.extension.AbstractExtension;
import org.eclipse.scout.rt.shared.extension.ContributionComposite;
import org.eclipse.scout.rt.shared.extension.IExtension;
import org.eclipse.scout.rt.shared.extension.ObjectExtensions;

@ClassId("634c5f32-ca74-40a8-87cf-571e93ae3f64")
public abstract class AbstractGroup extends AbstractWidget implements IGroup {

  private IGroupUIFacade m_uiFacade;
  private final ObjectExtensions<AbstractGroup, IGroupExtension<? extends AbstractGroup>> m_objectExtensions;
  protected ContributionComposite m_contributionHolder;
  private ITypeWithClassId m_container;

  public AbstractGroup() {
    this(true);
  }

  public AbstractGroup(boolean callInitializer) {
    super(false);
    m_objectExtensions = new ObjectExtensions<>(this, false);
    if (callInitializer) {
      callInitializer();
    }
  }

  @Override
  protected void callInitializer() {
    interceptInitConfig();
  }

  protected final void interceptInitConfig() {
    m_objectExtensions.initConfigAndBackupExtensionContext(createLocalExtension(), this::initConfig);
  }

  @Override
  protected void initConfig() {
    super.initConfig();
    m_uiFacade = BEANS.get(ModelContextProxy.class).newProxy(new P_UIFacade(), ModelContext.copyCurrent());
    m_contributionHolder = new ContributionComposite(this);
    setOrder(calculateViewOrder());
    setCollapsed(getConfiguredCollapsed());
    setTitle(getConfiguredTitle());
    setHeaderVisible(getConfiguredHeaderVisible());
    setBody(createBody());
  }

  @Override
  public void postInitConfig() {
    // NOP
    // FIXME CGU How to init Body? -> Add IWidget.init(), IWidget.dispose(), IWidget.postInitConfig()
  }

  @Override
  public final void init() {
    try {
      initInternal();
      interceptInitGroup();
    }
    catch (Exception e) {
      handleInitException(e);
    }
  }

  protected void initInternal() {
    if (getContainer() == null) {
      throw new IllegalStateException("Group is not connected to a container");
    }
  }

  protected void handleInitException(Exception exception) {
    throw new PlatformException("Exception occured while initializing group", exception);
  }

  protected void execInitGroup() {
    // NOP
  }

  @Override
  public final void dispose() {
    disposeInternal();
    interceptDisposeGroup();
  }

  protected void disposeInternal() {
    // NOP
  }

  protected void execDisposeGroup() {
    // NOP
  }

  /**
   * Calculates the groups's view order, e.g. if the @Order annotation is set to 30.0, the method will return 30.0. If
   * no {@link Order} annotation is set, the method checks its super classes for an @Order annotation.
   *
   * @since 3.10.0-M4
   */
  @SuppressWarnings("squid:S1244")
  protected double calculateViewOrder() {
    double viewOrder = getConfiguredViewOrder();
    Class<?> cls = getClass();
    if (viewOrder == IOrdered.DEFAULT_ORDER) {
      while (cls != null && IGroup.class.isAssignableFrom(cls)) {
        if (cls.isAnnotationPresent(Order.class)) {
          Order order = (Order) cls.getAnnotation(Order.class);
          return order.value();
        }
        cls = cls.getSuperclass();
      }
    }
    return viewOrder;
  }

  /**
   * Configures the view order of this group. The view order determines the order in which the groups appear in the
   * group box. The order of groups with no view order configured ({@code < 0}) is initialized based on the
   * {@link Order} annotation of the group class.
   * <p>
   * Subclasses can override this method. The default is {@link IOrdered#DEFAULT_ORDER}.
   *
   * @return View order of this group.
   */
  @ConfigProperty(ConfigProperty.DOUBLE)
  @Order(80)
  protected double getConfiguredViewOrder() {
    return IOrdered.DEFAULT_ORDER;
  }

  @Override
  public double getOrder() {
    return propertySupport.getPropertyDouble(PROP_ORDER);
  }

  @Override
  public void setOrder(double order) {
    propertySupport.setPropertyDouble(PROP_ORDER, order);
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(80)
  protected boolean getConfiguredCollapsed() {
    return false;
  }

  @Override
  public void setCollapsed(boolean collapsed) {
    propertySupport.setPropertyBool(PROP_COLLAPSED, collapsed);
  }

  @Override
  public boolean isCollapsed() {
    return propertySupport.getPropertyBool(PROP_COLLAPSED);
  }

  @Override
  public void toggleCollapse() {
    setCollapsed(!isCollapsed());
  }

  @ConfigProperty(ConfigProperty.STRING)
  @Order(90)
  protected String getConfiguredTitle() {
    return null;
  }

  @Override
  public void setTitle(String title) {
    propertySupport.setPropertyString(PROP_TITLE, title);
  }

  @Override
  public String getTitle() {
    return propertySupport.getPropertyString(PROP_TITLE);
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(100)
  protected boolean getConfiguredHeaderVisible() {
    return true;
  }

  @Override
  public void setHeaderVisible(boolean headerVisible) {
    propertySupport.setPropertyBool(PROP_HEADER_VISIBLE, headerVisible);
  }

  @Override
  public boolean isHeaderVisible() {
    return propertySupport.getPropertyBool(PROP_HEADER_VISIBLE);
  }

  protected IWidget createBody() {
    List<IWidget> contributedFields = m_contributionHolder.getContributionsByClass(IWidget.class);
    IWidget result = CollectionUtility.firstElement(contributedFields);
    if (result != null) {
      return result;
    }

    Class<? extends IWidget> configuredBody = getConfiguredBody();
    if (configuredBody != null) {
      return ConfigurationUtility.newInnerInstance(this, configuredBody);
    }
    return null;
  }

  private Class<? extends IWidget> getConfiguredBody() {
    Class[] dca = ConfigurationUtility.getDeclaredPublicClasses(getClass());
    return ConfigurationUtility.filterClass(dca, IWidget.class);
  }

  @Override
  public void setBody(IWidget body) {
    propertySupport.setProperty(PROP_BODY, body);
  }

  @Override
  public IWidget getBody() {
    return (IWidget) propertySupport.getProperty(PROP_BODY);
  }

  @Override
  public ITypeWithClassId getContainer() {
    return m_container;
  }

  @Override
  public void setContainer(ITypeWithClassId container) {
    m_container = container;
  }

  @Override
  public String classId() {
    String simpleClassId = ConfigurationUtility.getAnnotatedClassIdWithFallback(getClass(), true);
    return getContainer().classId() + ID_CONCAT_SYMBOL + simpleClassId;
  }

  @Override
  public final List<? extends IGroupExtension<? extends AbstractGroup>> getAllExtensions() {
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

  @Override
  public final <T> T optContribution(Class<T> contribution) {
    return m_contributionHolder.optContribution(contribution);
  }

  protected final void interceptDisposeGroup() {
    List<? extends IGroupExtension<? extends AbstractGroup>> extensions = getAllExtensions();
    GroupDisposeGroupChain chain = new GroupDisposeGroupChain(extensions);
    chain.execDisposeGroup();
  }

  protected final void interceptInitGroup() {
    List<? extends IGroupExtension<? extends AbstractGroup>> extensions = getAllExtensions();
    GroupInitGroupChain chain = new GroupInitGroupChain(extensions);
    chain.execInitGroup();
  }

  protected IGroupExtension<? extends AbstractGroup> createLocalExtension() {
    return new LocalGroupExtension<>(this);
  }

  /**
   * The extension delegating to the local methods. This Extension is always at the end of the chain and will not call
   * any further chain elements.
   */
  protected static class LocalGroupExtension<OWNER extends AbstractGroup> extends AbstractExtension<OWNER> implements IGroupExtension<OWNER> {

    public LocalGroupExtension(OWNER owner) {
      super(owner);
    }

    @Override
    public void execDisposeGroup(GroupDisposeGroupChain chain) {
      getOwner().execDisposeGroup();
    }

    @Override
    public void execInitGroup(GroupInitGroupChain chain) {
      getOwner().execInitGroup();
    }

  }

  @Override
  public IGroupUIFacade getUIFacade() {
    return m_uiFacade;
  }

  protected class P_UIFacade implements IGroupUIFacade {
    @Override
    public void setCollapsedFromUI(boolean collapsed) {
      setCollapsed(collapsed);
    }
  }

}
