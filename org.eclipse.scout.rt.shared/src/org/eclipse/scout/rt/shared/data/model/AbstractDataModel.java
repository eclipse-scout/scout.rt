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
package org.eclipse.scout.rt.shared.data.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.ConfigurationUtility;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;

public abstract class AbstractDataModel implements IDataModel, Serializable {
  private static final long serialVersionUID = 1L;
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractDataModel.class);

  private boolean m_calledInitializer;
  private List<IDataModelAttribute> m_attributes;
  private List<IDataModelEntity> m_entities;

  public AbstractDataModel() {
    this(true);
  }

  public AbstractDataModel(boolean callInitializer) {
    super();
    if (callInitializer) {
      callInitializer();
    }
  }

  protected void callInitializer() {
    if (!m_calledInitializer) {
      m_calledInitializer = true;
      initConfig();
    }
  }

  protected List<IDataModelAttribute> createAttributes() {
    Class[] all = ConfigurationUtility.getDeclaredPublicClasses(getClass());
    List<Class<IDataModelAttribute>> filtered = ConfigurationUtility.filterClasses(all, IDataModelAttribute.class);
    List<Class<? extends IDataModelAttribute>> sortedAndFiltered = ConfigurationUtility.sortFilteredClassesByOrderAnnotation(filtered, IDataModelAttribute.class);
    List<IDataModelAttribute> attributes = new ArrayList<IDataModelAttribute>(sortedAndFiltered.size());
    for (Class<? extends IDataModelAttribute> attributeClazz : sortedAndFiltered) {
      try {
        attributes.add(ConfigurationUtility.newInnerInstance(this, attributeClazz));
      }
      catch (Exception e) {
        LOG.warn(null, e);
      }
    }
    return attributes;
  }

  protected List<IDataModelEntity> createEntities() {
    Class[] all = ConfigurationUtility.getDeclaredPublicClasses(getClass());
    List<Class<IDataModelEntity>> filtered = ConfigurationUtility.filterClasses(all, IDataModelEntity.class);
    List<Class<? extends IDataModelEntity>> sortedAndFiltered = ConfigurationUtility.sortFilteredClassesByOrderAnnotation(filtered, IDataModelEntity.class);
    List<IDataModelEntity> entities = new ArrayList<IDataModelEntity>(sortedAndFiltered.size());
    for (Class<? extends IDataModelEntity> dataModelEntityClazz : sortedAndFiltered) {
      try {
        entities.add(ConfigurationUtility.newInnerInstance(this, dataModelEntityClazz));
      }
      catch (Exception e) {
        LOG.warn(null, e);
      }
    }
    return entities;
  }

  @SuppressWarnings("deprecation")
  protected void initConfig() {
    // attributes
    m_attributes = createAttributes();
    for (IDataModelAttribute a : m_attributes) {
      a.setParentEntity(null);
    }
    // entities
    m_entities = createEntities();
    HashMap<Class<? extends IDataModelEntity>, IDataModelEntity> instanceMap = new HashMap<Class<? extends IDataModelEntity>, IDataModelEntity>();
    for (IDataModelEntity e : m_entities) {
      e.setParentEntity(null);
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
      catch (Throwable t) {
        LOG.error("entity " + e, t);
      }
    }
    for (IDataModelAttribute a : getAttributes()) {
      try {
        a.initAttribute();
      }
      catch (Throwable t) {
        LOG.error("attribute " + a, t);
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
