/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.group;

import java.util.Arrays;
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
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.reflect.ConfigurationUtility;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.shared.data.basic.NamedBitMaskHelper;
import org.eclipse.scout.rt.shared.dimension.IDimensions;
import org.eclipse.scout.rt.shared.extension.AbstractExtension;
import org.eclipse.scout.rt.shared.extension.ContributionComposite;
import org.eclipse.scout.rt.shared.extension.IExtension;
import org.eclipse.scout.rt.shared.extension.ObjectExtensions;

@ClassId("634c5f32-ca74-40a8-87cf-571e93ae3f64")
public abstract class AbstractGroup extends AbstractWidget implements IGroup {

  private static final NamedBitMaskHelper VISIBLE_BIT_HELPER = new NamedBitMaskHelper(IDimensions.VISIBLE, IDimensions.VISIBLE_GRANTED);

  private IGroupUIFacade m_uiFacade;
  private final ObjectExtensions<AbstractGroup, IGroupExtension<? extends AbstractGroup>> m_objectExtensions;
  protected ContributionComposite m_contributionHolder;

  /**
   * Provides 8 dimensions for visibility.<br>
   * Internally used: {@link IDimensions#VISIBLE}, {@link IDimensions#VISIBLE_GRANTED}.<br>
   * 6 dimensions remain for custom use. This FormField is visible, if all dimensions are visible (all bits set).
   */
  private byte m_visible;

  public AbstractGroup() {
    this(true);
  }

  public AbstractGroup(boolean callInitializer) {
    super(false);
    m_visible = NamedBitMaskHelper.ALL_BITS_SET; // default visible
    m_objectExtensions = new ObjectExtensions<>(this, false);
    if (callInitializer) {
      callInitializer();
    }
  }

  @Override
  protected void initConfigInternal() {
    m_objectExtensions.initConfigAndBackupExtensionContext(createLocalExtension(), this::initConfig);
  }

  @Override
  protected void initConfig() {
    super.initConfig();
    m_uiFacade = BEANS.get(ModelContextProxy.class).newProxy(new P_UIFacade(), ModelContext.copyCurrent());
    m_contributionHolder = new ContributionComposite(this);
    setOrder(calculateViewOrder());
    setCollapsed(getConfiguredCollapsed());
    setCollapsible(getConfiguredCollapsible());
    setCollapseStyle(getConfiguredCollapseStyle());
    setTitle(getConfiguredTitle());
    setVisible(getConfiguredVisible());
    setHeader(createHeader());
    setHeaderFocusable(getConfiguredHeaderFocusable());
    setHeaderVisible(getConfiguredHeaderVisible());
    setBody(createBody());
  }

  @Override
  protected final void initInternal() {
    super.initInternal();
    try {
      initGroupInternal();
      interceptInitGroup();
    }
    catch (Exception e) {
      handleInitException(e);
    }
  }

  protected void initGroupInternal() {
    Assertions.assertNotNull(getParent(), "Group is not connected to a container");
  }

  protected void handleInitException(Exception exception) {
    throw new PlatformException("Exception occurred while initializing group", exception);
  }

  protected void execInitGroup() {
    // NOP
  }

  @Override
  protected final void disposeInternal() {
    interceptDisposeGroup();
    super.disposeInternal();
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
          Order order = cls.getAnnotation(Order.class);
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

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(82)
  protected boolean getConfiguredCollapsible() {
    return true;
  }

  @Override
  public void setCollapsible(boolean collapsible) {
    propertySupport.setPropertyBool(PROP_COLLAPSIBLE, collapsible);
  }

  @Override
  public boolean isCollapsible() {
    return propertySupport.getPropertyBool(PROP_COLLAPSIBLE);
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(85)
  protected String getConfiguredCollapseStyle() {
    return COLLAPSE_STYLE_LEFT;
  }

  @Override
  public void setCollapseStyle(String collapseStyle) {
    propertySupport.setPropertyString(PROP_COLLAPSE_STYLE, collapseStyle);
  }

  @Override
  public String getCollapseStyle() {
    return propertySupport.getPropertyString(PROP_COLLAPSE_STYLE);
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

  @Override
  public void setTitleSuffix(String suffix) {
    propertySupport.setPropertyString(PROP_TITLE_SUFFIX, suffix);
  }

  @Override
  public String getTitleSuffix() {
    return propertySupport.getPropertyString(PROP_TITLE_SUFFIX);
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(95)
  protected boolean getConfiguredVisible() {
    return true;
  }

  @Override
  public boolean isVisibleGranted() {
    return isVisible(IDimensions.VISIBLE_GRANTED);
  }

  @Override
  public void setVisibleGranted(boolean visible) {
    setVisible(visible, IDimensions.VISIBLE_GRANTED);
  }

  @Override
  public boolean isVisible() {
    return propertySupport.getPropertyBool(PROP_VISIBLE);
  }

  @Override
  public void setVisible(boolean b) {
    setVisible(b, IDimensions.VISIBLE);
  }

  @Override
  public void setVisible(boolean visible, String dimension) {
    m_visible = VISIBLE_BIT_HELPER.changeBit(dimension, visible, m_visible);
    calculateVisible();
  }

  @Override
  public boolean isVisible(String dimension) {
    return VISIBLE_BIT_HELPER.isBitSet(dimension, m_visible);
  }

  private void calculateVisible() {
    propertySupport.setPropertyBool(PROP_VISIBLE, NamedBitMaskHelper.allBitsSet(m_visible));
  }

  protected IWidget createHeader() {
    Class<? extends IWidget> configuredHeader = getConfiguredHeader();
    if (configuredHeader != null) {
      return ConfigurationUtility.newInnerInstance(this, configuredHeader);
    }
    return null;
  }

  protected Class<? extends IWidget> getConfiguredHeader() {
    return null;
  }

  @Override
  public void setHeader(IWidget header) {
    propertySupport.setProperty(PROP_HEADER, header);
  }

  @Override
  public IWidget getHeader() {
    return (IWidget) propertySupport.getProperty(PROP_HEADER);
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(110)
  protected boolean getConfiguredHeaderFocusable() {
    return false;
  }

  @Override
  public void setHeaderFocusable(boolean headerFocusable) {
    propertySupport.setPropertyBool(PROP_HEADER_FOCUSABLE, headerFocusable);
  }

  @Override
  public boolean isHeaderFocusable() {
    return propertySupport.getPropertyBool(PROP_HEADER_FOCUSABLE);
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

  @Override
  public List<? extends IWidget> getChildren() {
    return CollectionUtility.flatten(super.getChildren(), Arrays.asList(getHeader(), getBody()));
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

  protected Class<? extends IWidget> getConfiguredBody() {
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
  public Object getGroupId() {
    return propertySupport.getProperty(PROP_GROUP_ID);
  }

  @Override
  public void setGroupId(Object groupId) {
    propertySupport.setProperty(PROP_GROUP_ID, groupId);
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
  public String classId() {
    String simpleClassId = ConfigurationUtility.getAnnotatedClassIdWithFallback(getClass(), true);
    return getParent().classId() + ID_CONCAT_SYMBOL + simpleClassId;
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
