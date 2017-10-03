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
package org.eclipse.scout.rt.shared.data.model;

import java.io.Serializable;
import java.security.Permission;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.IOrdered;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigOperation;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.reflect.AbstractPropertyObserver;
import org.eclipse.scout.rt.platform.reflect.ConfigurationUtility;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.collection.OrderedCollection;
import org.eclipse.scout.rt.shared.data.basic.NamedBitMaskHelper;
import org.eclipse.scout.rt.shared.dimension.IDimensions;
import org.eclipse.scout.rt.shared.extension.AbstractSerializableExtension;
import org.eclipse.scout.rt.shared.extension.ContributionComposite;
import org.eclipse.scout.rt.shared.extension.ExtensionUtility;
import org.eclipse.scout.rt.shared.extension.IContributionOwner;
import org.eclipse.scout.rt.shared.extension.IExtensibleObject;
import org.eclipse.scout.rt.shared.extension.IExtension;
import org.eclipse.scout.rt.shared.extension.ObjectExtensions;
import org.eclipse.scout.rt.shared.extension.data.model.DataModelEntityChains.DataModelEntityInitEntityChain;
import org.eclipse.scout.rt.shared.extension.data.model.IDataModelEntityExtension;
import org.eclipse.scout.rt.shared.services.common.security.IAccessControlService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ClassId("d64571d4-5521-45c3-84c2-a22294542747")
public abstract class AbstractDataModelEntity extends AbstractPropertyObserver implements IDataModelEntity, Serializable, IContributionOwner, IExtensibleObject {

  private static final long serialVersionUID = 1L;
  private static final String INITIALIZED = "INITIALIZED";
  private static final String ONE_TO_MANY = "ONE_TO_MANY";
  private static final String INITIALIZED_CHILD_ENTITIES = "INITIALIZED_CHILD_ENTITIES";

  private static final Logger LOG = LoggerFactory.getLogger(AbstractDataModelEntity.class);
  private static final NamedBitMaskHelper FLAGS_BIT_HELPER = new NamedBitMaskHelper(INITIALIZED, ONE_TO_MANY, INITIALIZED_CHILD_ENTITIES);
  private static final NamedBitMaskHelper VISIBLE_BIT_HELPER = new NamedBitMaskHelper(IDimensions.VISIBLE, IDimensions.VISIBLE_GRANTED);

  private double m_order;
  private Permission m_visiblePermission;
  private String m_text;
  private String m_iconId;
  private List<IDataModelAttribute> m_attributes;
  private List<IDataModelEntity> m_entities;
  private IDataModelEntity m_parentEntity;
  private IContributionOwner m_contributionHolder;
  private final ObjectExtensions<AbstractDataModelEntity, IDataModelEntityExtension<? extends AbstractDataModelEntity>> m_objectExtensions;

  /**
   * Provides 8 boolean flags.<br>
   * Currently used: {@link #INITIALIZED}, {@link #ONE_TO_MANY}, {@link #INITIALIZED_CHILD_ENTITIES}
   */
  private byte m_flags;

  /**
   * Provides 8 dimensions for visibility.<br>
   * Internally used: {@link IDimensions#VISIBLE}, {@link IDimensions#VISIBLE_GRANTED}.<br>
   * 6 dimensions remain for custom use. This DataModelEntity is visible, if all dimensions are visible (all bits set).
   */
  private byte m_visible;

  public AbstractDataModelEntity() {
    this(true);
  }

  /**
   * @param callInitConfig
   *          true if {@link #callInitializer()} should automatically be invoked, false if the subclass invokes
   *          {@link #callInitializer()} itself
   */
  public AbstractDataModelEntity(boolean callInitConfig) {
    m_attributes = new ArrayList<IDataModelAttribute>();
    m_entities = new ArrayList<IDataModelEntity>();
    m_objectExtensions = new ObjectExtensions<AbstractDataModelEntity, IDataModelEntityExtension<? extends AbstractDataModelEntity>>(this, false);
    if (callInitConfig) {
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

  protected final void callInitializer() {
    interceptInitConfig();
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
      while (cls != null && IDataModelEntity.class.isAssignableFrom(cls)) {
        if (cls.isAnnotationPresent(Order.class)) {
          Order order = (Order) cls.getAnnotation(Order.class);
          return order.value();
        }
        cls = cls.getSuperclass();
      }
    }
    return viewOrder;
  }

  @Override
  public String classId() {
    String simpleClassId = ConfigurationUtility.getAnnotatedClassIdWithFallback(getClass());
    IDataModelEntity parentEntity = getParentEntity();
    if (parentEntity != null) {
      return simpleClassId + ID_CONCAT_SYMBOL + parentEntity.classId();
    }
    return simpleClassId;
  }

  /*
   * Configuration
   */

  @ConfigProperty(ConfigProperty.TEXT)
  @Order(20)
  protected String getConfiguredText() {
    return null;
  }

  @ConfigProperty(ConfigProperty.ICON_ID)
  @Order(10)
  protected String getConfiguredIconId() {
    return null;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(50)
  protected boolean getConfiguredVisible() {
    return true;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(50)
  protected boolean getConfiguredOneToMany() {
    return true;
  }

  /**
   * Configures the view order of this data model entity. The view order determines the order in which the entities
   * appear. The order of entities with no view order configured ({@code < 0}) is initialized based on the {@link Order}
   * annotation of the entity class.
   * <p>
   * Subclasses can override this method. The default is {@link IOrdered#DEFAULT_ORDER}.
   *
   * @return View order of this entity.
   */
  @ConfigProperty(ConfigProperty.DOUBLE)
  @Order(60)
  protected double getConfiguredViewOrder() {
    return IOrdered.DEFAULT_ORDER;
  }

  /**
   * Initialize this entity.
   */
  @ConfigOperation
  @Order(10)
  protected void execInitEntity() {
  }

  private List<Class<IDataModelAttribute>> getConfiguredAttributes() {
    Class[] dca = ConfigurationUtility.getDeclaredPublicClasses(getClass());
    return ConfigurationUtility.filterClasses(dca, IDataModelAttribute.class);
  }

  private List<Class<IDataModelEntity>> getConfiguredEntities() {
    Class[] dca = ConfigurationUtility.getDeclaredPublicClasses(getClass());
    return ConfigurationUtility.filterClasses(dca, IDataModelEntity.class);
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
    m_visible = NamedBitMaskHelper.ALL_BITS_SET; // default visible
    m_contributionHolder = new ContributionComposite(this);
    setText(getConfiguredText());
    setIconId(getConfiguredIconId());
    setVisible(getConfiguredVisible());
    setOneToMany(getConfiguredOneToMany());
    setOrder(calculateViewOrder());

    List<Class<IDataModelAttribute>> configuredAttributes = getConfiguredAttributes();
    List<IDataModelAttribute> contributedAttributes = m_contributionHolder.getContributionsByClass(IDataModelAttribute.class);

    OrderedCollection<IDataModelAttribute> attributes = new OrderedCollection<IDataModelAttribute>();
    for (Class<? extends IDataModelAttribute> c : configuredAttributes) {
      attributes.addOrdered(ConfigurationUtility.newInnerInstance(this, c));
    }

    attributes.addAllOrdered(contributedAttributes);

    injectAttributesInternal(attributes);
    ExtensionUtility.moveModelObjects(attributes);
    m_attributes = attributes.getOrderedList();

    for (IDataModelAttribute a : m_attributes) {
      if (a instanceof AbstractDataModelAttribute) {
        ((AbstractDataModelAttribute) a).setParentEntity(this);
      }
    }
    //lazy create entities at point when setParentEntity is set, this is necessary to avoid cyclic loops
    m_entities = new ArrayList<IDataModelEntity>();
  }

  @Override
  public final List<? extends IDataModelEntityExtension<? extends AbstractDataModelEntity>> getAllExtensions() {
    return m_objectExtensions.getAllExtensions();
  }

  protected IDataModelEntityExtension<? extends AbstractDataModelEntity> createLocalExtension() {
    return new LocalDataModelEntityExtension<AbstractDataModelEntity>(this);
  }

  @Override
  public <T extends IExtension<?>> T getExtension(Class<T> c) {
    return m_objectExtensions.getExtension(c);
  }

  @Override
  public Map<String, String> getMetaDataOfEntity() {
    return null;
  }

  /*
   * Runtime
   */

  private boolean isInitialized() {
    return FLAGS_BIT_HELPER.isBitSet(INITIALIZED, m_flags);
  }

  private void setInitialized() {
    m_flags = FLAGS_BIT_HELPER.setBit(INITIALIZED, m_flags);
  }

  @Override
  public final void initEntity() {
    if (isInitialized()) {
      return;
    }

    try {
      interceptInitEntity();
    }
    catch (RuntimeException ex) {
      LOG.error("entity {}", this, ex);
    }
    for (IDataModelAttribute a : getAttributes()) {
      try {
        a.initAttribute();
      }
      catch (RuntimeException ex) {
        LOG.error("attribute {}/{}", this, a, ex);
      }
    }
    setInitialized();
    for (IDataModelEntity e : getEntities()) {
      try {
        e.initEntity();
      }
      catch (RuntimeException ex) {
        LOG.error("entity {}/{}", this, e, ex);
      }
    }
  }

  @Override
  public Permission getVisiblePermission() {
    return m_visiblePermission;
  }

  @Override
  public void setVisiblePermission(Permission p) {
    setVisiblePermissionInternal(p);
    boolean b;
    if (p != null) {
      b = BEANS.get(IAccessControlService.class).checkPermission(p);
    }
    else {
      b = true;
    }
    setVisibleGranted(b);
  }

  protected void setVisiblePermissionInternal(Permission p) {
    m_visiblePermission = p;
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
  public void setVisible(boolean visible) {
    setVisible(visible, IDimensions.VISIBLE);
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

  @Override
  public boolean isOneToMany() {
    return FLAGS_BIT_HELPER.isBitSet(ONE_TO_MANY, m_flags);
  }

  @Override
  public void setOneToMany(boolean b) {
    m_flags = FLAGS_BIT_HELPER.changeBit(ONE_TO_MANY, b, m_flags);
  }

  private boolean isInitializedChildEntities() {
    return FLAGS_BIT_HELPER.isBitSet(INITIALIZED_CHILD_ENTITIES, m_flags);
  }

  private void setInitializedChildEntities() {
    m_flags = FLAGS_BIT_HELPER.setBit(INITIALIZED_CHILD_ENTITIES, m_flags);
  }

  @Override
  public double getOrder() {
    return m_order;
  }

  @Override
  public void setOrder(double order) {
    m_order = order;
  }

  @Override
  public String getIconId() {
    return m_iconId;
  }

  @Override
  public void setIconId(String s) {
    m_iconId = s;
  }

  @Override
  public String getText() {
    return m_text;
  }

  @Override
  public void setText(String s) {
    m_text = s;
  }

  @Override
  public List<IDataModelAttribute> getAttributes() {
    return CollectionUtility.arrayList(m_attributes);
  }

  @Override
  public List<IDataModelEntity> getEntities() {
    return CollectionUtility.arrayList(m_entities);
  }

  @Override
  public IDataModelAttribute getAttribute(Class<? extends IDataModelAttribute> attributeClazz) {
    for (IDataModelAttribute attribute : m_attributes) {
      if (attribute.getClass() == attributeClazz) {
        return attribute;
      }
    }
    return null;
  }

  @Override
  public IDataModelEntity getEntity(Class<? extends IDataModelEntity> entityClazz) {
    for (IDataModelEntity entity : m_entities) {
      if (entity.getClass() == entityClazz) {
        return entity;
      }
    }
    return null;
  }

  @Override
  public IDataModelEntity getParentEntity() {
    return m_parentEntity;
  }

  public void setParentEntity(IDataModelEntity parent) {
    m_parentEntity = parent;
  }

  @Override
  public void initializeChildEntities(Map<Class<? extends IDataModelEntity>, IDataModelEntity> instanceMap) {
    if (isInitializedChildEntities()) {
      return;
    }
    setInitializedChildEntities();
    List<Class<IDataModelEntity>> configuredEntities = getConfiguredEntities();
    List<IDataModelEntity> contributedEntities = m_contributionHolder.getContributionsByClass(IDataModelEntity.class);
    int numEntities = configuredEntities.size() + contributedEntities.size();

    Set<IDataModelEntity> newConfiguredInstances = new HashSet<IDataModelEntity>(numEntities);
    OrderedCollection<IDataModelEntity> entities = new OrderedCollection<IDataModelEntity>();
    for (Class<? extends IDataModelEntity> c : configuredEntities) {
      //check if a parent is of same type, in that case use reference
      IDataModelEntity e = instanceMap.get(c);
      if (e == null) {
        e = ConfigurationUtility.newInnerInstance(this, c);
        newConfiguredInstances.add(e);
        instanceMap.put(c, e);
      }
      entities.addOrdered(e);
    }
    newConfiguredInstances.addAll(contributedEntities);
    entities.addAllOrdered(contributedEntities);
    injectEntitiesInternal(entities);
    ExtensionUtility.moveModelObjects(entities);

    m_entities.clear();
    m_entities.addAll(entities.getOrderedList());

    for (IDataModelEntity e : m_entities) {
      if (e instanceof AbstractDataModelEntity) {
        AbstractDataModelEntity adme = (AbstractDataModelEntity) e;
        if (adme.getParentEntity() != this) {
          adme.setParentEntity(this);
        }
      }
    }
    for (IDataModelEntity e : m_entities) {
      if (newConfiguredInstances.contains(e) || !instanceMap.containsKey(e.getClass())) {
        e.initializeChildEntities(instanceMap);
      }
    }
  }

  /**
   * Override this internal method only in order to make use of dynamic attributes<br>
   * Used to add and/or remove attributes<br>
   * To change the order or specify the insert position use {@link IDataModelAttribute#setOrder(double)}.
   *
   * @param attributes
   *          live and mutable collection of configured attributes
   */
  protected void injectAttributesInternal(OrderedCollection<IDataModelAttribute> attributes) {
  }

  /**
   * Override this internal method only in order to make use of dynamic entities<br>
   * Used to add and/or remove entities<br>
   * To change the order or specify the insert position use {@link IDataModelEntity#setOrder(double)}.<br>
   * Note that {@link #initializeChildEntities(Map)} is also called on injected entities
   *
   * @param entities
   *          live and mutable collection of configured entities
   */
  protected void injectEntitiesInternal(OrderedCollection<IDataModelEntity> entities) {
  }

  /**
   * The extension delegating to the local methods. This Extension is always at the end of the chain and will not call
   * any further chain elements.
   */
  protected static class LocalDataModelEntityExtension<OWNER extends AbstractDataModelEntity> extends AbstractSerializableExtension<OWNER> implements IDataModelEntityExtension<OWNER> {
    private static final long serialVersionUID = 1L;

    public LocalDataModelEntityExtension(OWNER owner) {
      super(owner);
    }

    @Override
    public void execInitEntity(DataModelEntityInitEntityChain chain) {
      getOwner().execInitEntity();
    }

  }

  protected final void interceptInitEntity() {
    List<? extends IDataModelEntityExtension<? extends AbstractDataModelEntity>> extensions = getAllExtensions();
    DataModelEntityInitEntityChain chain = new DataModelEntityInitEntityChain(extensions);
    chain.execInitEntity();
  }
}
