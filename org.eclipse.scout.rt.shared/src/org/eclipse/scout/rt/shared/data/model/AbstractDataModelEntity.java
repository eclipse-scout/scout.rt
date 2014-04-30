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
import java.security.Permission;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.ConfigurationUtility;
import org.eclipse.scout.commons.annotations.ConfigOperation;
import org.eclipse.scout.commons.annotations.ConfigProperty;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.beans.AbstractPropertyObserver;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.shared.services.common.security.IAccessControlService;
import org.eclipse.scout.service.SERVICES;

@SuppressWarnings("deprecation")
public abstract class AbstractDataModelEntity extends AbstractPropertyObserver implements IDataModelEntity, Serializable {
  private static final long serialVersionUID = 1L;
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractDataModelEntity.class);

  private String m_id;
  private Permission m_visiblePermission;
  private boolean m_visibleGranted;
  private boolean m_visibleProperty;
  private boolean m_visible;
  private boolean m_oneToMany;
  private String m_text;
  private String m_iconId;
  private ArrayList<IDataModelAttribute> m_attributes;
  private ArrayList<IDataModelEntity> m_entities;
  private IDataModelEntity m_parentEntity;
  private boolean m_initializedChildEntities;
  private boolean m_initialized;

  public AbstractDataModelEntity() {
    this(true);
  }

  /**
   * @param callInitConfig
   *          true if {@link #callInitConfig()} should automcatically be invoked, false if the subclass invokes
   *          {@link #callInitConfig()} itself
   */
  public AbstractDataModelEntity(boolean callInitConfig) {
    m_attributes = new ArrayList<IDataModelAttribute>();
    m_entities = new ArrayList<IDataModelEntity>();
    if (callInitConfig) {
      callInitConfig();
    }
  }

  protected void callInitConfig() {
    initConfig();
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
   * Initialize this entity.
   */
  @ConfigOperation
  @Order(10)
  protected void execInitEntity() throws ProcessingException {
  }

  private List<Class<? extends IDataModelAttribute>> getConfiguredAttributes() {
    Class[] dca = ConfigurationUtility.getDeclaredPublicClasses(getClass());
    List<Class<IDataModelAttribute>> filtered = ConfigurationUtility.filterClasses(dca, IDataModelAttribute.class);
    return ConfigurationUtility.sortFilteredClassesByOrderAnnotation(filtered, IDataModelAttribute.class);
  }

  private List<Class<? extends IDataModelEntity>> getConfiguredEntities() {
    Class[] dca = ConfigurationUtility.getDeclaredPublicClasses(getClass());
    List<Class<IDataModelEntity>> filtered = ConfigurationUtility.filterClasses(dca, IDataModelEntity.class);
    return ConfigurationUtility.sortFilteredClassesByOrderAnnotation(filtered, IDataModelEntity.class);
  }

  protected void initConfig() {
    m_visibleGranted = true;
    setText(getConfiguredText());
    setIconId(getConfiguredIconId());
    setVisible(getConfiguredVisible());
    setOneToMany(getConfiguredOneToMany());
    ArrayList<IDataModelAttribute> attributes = new ArrayList<IDataModelAttribute>();
    for (Class<? extends IDataModelAttribute> c : getConfiguredAttributes()) {
      try {
        attributes.add(ConfigurationUtility.newInnerInstance(this, c));
      }
      catch (Exception e) {
        LOG.warn(null, e);
      }
    }
    injectAttributesInternal(attributes);
    m_attributes = new ArrayList<IDataModelAttribute>(attributes);
    for (IDataModelAttribute a : m_attributes) {
      a.setParentEntity(this);
    }
    //lazy create entities at point when setParentEntity is set, this is necessary to avoid cyclic loops
    m_entities = new ArrayList<IDataModelEntity>();
  }

  @Override
  public Map<String, String> getMetaDataOfEntity() {
    return null;
  }

  /*
   * Runtime
   */

  @Override
  public final void initEntity() throws ProcessingException {
    if (m_initialized) {
      return;
    }

    try {
      execInitEntity();
    }
    catch (Throwable t) {
      LOG.error("entity " + this, t);
    }
    for (IDataModelAttribute a : getAttributes()) {
      try {
        a.initAttribute();
      }
      catch (Throwable t) {
        LOG.error("attribute " + this + "/" + a, t);
      }
    }
    m_initialized = true;
    for (IDataModelEntity e : getEntities()) {
      try {
        e.initEntity();
      }
      catch (Throwable t) {
        LOG.error("entity " + this + "/" + e, t);
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
      b = SERVICES.getService(IAccessControlService.class).checkPermission(p);
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
    return m_visibleGranted;
  }

  @Override
  public void setVisibleGranted(boolean b) {
    m_visibleGranted = b;
    calculateVisible();
  }

  @Override
  public boolean isVisible() {
    return m_visible;
  }

  @Override
  public void setVisible(boolean b) {
    setVisibleProperty(b);
    calculateVisible();
  }

  protected void setVisibleProperty(boolean b) {
    m_visibleProperty = b;
  }

  protected boolean isVisibleProperty() {
    return m_visibleProperty;
  }

  @Override
  public boolean isOneToMany() {
    return m_oneToMany;
  }

  @Override
  public void setOneToMany(boolean b) {
    m_oneToMany = b;
  }

  /**
   * no access control for system buttons CANCEL and CLOSE
   */
  private void calculateVisible() {
    // access control
    m_visible = m_visibleGranted && m_visibleProperty;
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

  @Override
  public void setParentEntity(IDataModelEntity parent) {
    m_parentEntity = parent;
  }

  @Override
  public void initializeChildEntities(Map<Class<? extends IDataModelEntity>, IDataModelEntity> instanceMap) {
    if (!m_initializedChildEntities) {
      m_initializedChildEntities = true;
      ArrayList<IDataModelEntity> newConfiguredInstances = new ArrayList<IDataModelEntity>();
      ArrayList<IDataModelEntity> entities = new ArrayList<IDataModelEntity>();
      for (Class<? extends IDataModelEntity> c : getConfiguredEntities()) {
        try {
          //check if a parent is of same type, in that case use reference
          IDataModelEntity e = instanceMap.get(c);
          if (e == null) {
            e = ConfigurationUtility.newInnerInstance(this, c);
            newConfiguredInstances.add(e);
            instanceMap.put(c, e);
          }
          entities.add(e);
        }
        catch (Exception ex) {
          LOG.warn(null, ex);
        }
      }
      injectEntitiesInternal(entities);
      m_entities.clear();
      m_entities.addAll(entities);
      for (IDataModelEntity e : m_entities) {
        if (e.getParentEntity() != this) {
          e.setParentEntity(this);
        }
      }
      for (IDataModelEntity e : m_entities) {
        if (newConfiguredInstances.contains(e) || !instanceMap.containsKey(e.getClass())) {
          e.initializeChildEntities(instanceMap);
        }
      }
    }
  }

  /**
   * do not use this internal method<br>
   * Used add/remove attributes
   * 
   * @param attributeList
   *          live and mutable list of configured attributes
   */
  protected void injectAttributesInternal(List<IDataModelAttribute> attributeList) {
  }

  /**
   * do not use this internal method<br>
   * Used add/remove entities
   * <p>
   * Note that {@link #initializeChildEntities(Map)} is also called on injected entities
   * 
   * @param entityList
   *          live and mutable list of configured attributes
   */
  protected void injectEntitiesInternal(List<IDataModelEntity> entityList) {
  }
}
