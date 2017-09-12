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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.scout.rt.platform.reflect.ConfigurationUtility;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.collection.OrderedCollection;
import org.eclipse.scout.rt.shared.extension.ContributionComposite;
import org.eclipse.scout.rt.shared.extension.ExtensionUtility;
import org.eclipse.scout.rt.shared.extension.IContributionOwner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractDataModel implements IDataModel, Serializable, IContributionOwner {
  private static final long serialVersionUID = 1L;
  private static final Logger LOG = LoggerFactory.getLogger(AbstractDataModel.class);

  private boolean m_calledInitializer;
  private List<IDataModelAttribute> m_attributes;
  private List<IDataModelEntity> m_entities;
  private IContributionOwner m_contributionHolder;

  public AbstractDataModel() {
    this(true);
  }

  public AbstractDataModel(boolean callInitializer) {
    super();
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
    if (!m_calledInitializer) {
      m_calledInitializer = true;
      initConfig();
    }
  }

  protected List<IDataModelAttribute> createAttributes(Object holder) {
    Class[] all = ConfigurationUtility.getDeclaredPublicClasses(holder.getClass());
    List<Class<IDataModelAttribute>> filtered = ConfigurationUtility.filterClasses(all, IDataModelAttribute.class);

    List<IDataModelAttribute> contributedAttributes = m_contributionHolder.getContributionsByClass(IDataModelAttribute.class);

    OrderedCollection<IDataModelAttribute> attributes = new OrderedCollection<>();
    for (Class<? extends IDataModelAttribute> attributeClazz : filtered) {
      IDataModelAttribute a = ConfigurationUtility.newInnerInstance(holder, attributeClazz);
      attributes.addOrdered(a);
    }
    attributes.addAllOrdered(contributedAttributes);
    ExtensionUtility.moveModelObjects(attributes);
    return attributes.getOrderedList();
  }

  protected List<IDataModelEntity> createEntities(Object holder) {
    Class[] all = ConfigurationUtility.getDeclaredPublicClasses(holder.getClass());
    List<Class<IDataModelEntity>> filtered = ConfigurationUtility.filterClasses(all, IDataModelEntity.class);
    List<IDataModelEntity> contributedEntities = m_contributionHolder.getContributionsByClass(IDataModelEntity.class);

    OrderedCollection<IDataModelEntity> entities = new OrderedCollection<>();
    for (Class<? extends IDataModelEntity> dataModelEntityClazz : filtered) {
      entities.addOrdered(ConfigurationUtility.newInnerInstance(holder, dataModelEntityClazz));
    }

    entities.addAllOrdered(contributedEntities);

    ExtensionUtility.moveModelObjects(entities);
    return entities.getOrderedList();
  }

  protected List<IDataModelAttribute> createAttributes() {
    return createAttributes(this);
  }

  protected List<IDataModelEntity> createEntities() {
    return createEntities(this);
  }

  protected void initConfig() {
    m_contributionHolder = new ContributionComposite(this);

    // attributes
    m_attributes = createAttributes();
    for (IDataModelAttribute a : m_attributes) {
      if (a instanceof AbstractDataModelAttribute) {
        ((AbstractDataModelAttribute) a).setParentEntity(null);
      }
    }
    // entities
    m_entities = createEntities();
    Map<Class<? extends IDataModelEntity>, IDataModelEntity> instanceMap = new HashMap<>(m_entities.size());
    for (IDataModelEntity e : m_entities) {
      if (e instanceof AbstractDataModelEntity) {
        ((AbstractDataModelEntity) e).setParentEntity(null);
      }
      instanceMap.put(e.getClass(), e);
    }
    for (IDataModelEntity e : m_entities) {
      e.initializeChildEntities(instanceMap);
    }
  }

  @Override
  public void init() {
    //init tree structure
    for (IDataModelEntity e : getEntities()) {
      try {
        e.initEntity();
      }
      catch (RuntimeException ex) {
        LOG.error("entity {}", e, ex);
      }
    }
    for (IDataModelAttribute a : getAttributes()) {
      try {
        a.initAttribute();
      }
      catch (RuntimeException ex) {
        LOG.error("attribute {}", a, ex);
      }
    }
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
}
