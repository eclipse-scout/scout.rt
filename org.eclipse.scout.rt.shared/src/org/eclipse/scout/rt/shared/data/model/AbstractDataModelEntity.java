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

import java.security.Permission;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.scout.commons.ConfigurationUtility;
import org.eclipse.scout.commons.annotations.ConfigOperation;
import org.eclipse.scout.commons.annotations.ConfigProperty;
import org.eclipse.scout.commons.annotations.ConfigPropertyValue;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.beans.AbstractPropertyObserver;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.shared.services.common.security.IAccessControlService;
import org.eclipse.scout.service.SERVICES;

public abstract class AbstractDataModelEntity extends AbstractPropertyObserver implements IDataModelEntity {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractDataModelEntity.class);

  private String m_id;
  private Permission m_visiblePermission;
  private boolean m_visibleGranted;
  private boolean m_visibleProperty;
  private boolean m_visible;
  private String m_text;
  private String m_iconId;
  private ArrayList<IDataModelAttribute> m_attributes;
  private ArrayList<IDataModelEntity> m_entities;
  private IDataModelEntity m_parentEntity;
  private boolean m_initializedChildEntities;

  public AbstractDataModelEntity() {
    m_attributes = new ArrayList<IDataModelAttribute>();
    m_entities = new ArrayList<IDataModelEntity>();
    initConfig();
  }

  /*
   * Configuration
   */

  @ConfigProperty(ConfigProperty.TEXT)
  @Order(20)
  @ConfigPropertyValue("null")
  protected String getConfiguredText() {
    return null;
  }

  @ConfigProperty(ConfigProperty.ICON_ID)
  @Order(10)
  @ConfigPropertyValue("null")
  protected String getConfiguredIconId() {
    return null;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(50)
  @ConfigPropertyValue("true")
  protected boolean getConfiguredVisible() {
    return true;
  }

  /**
   * Initialize this entity.
   */
  @ConfigOperation
  @Order(10)
  protected void execInitEntity() throws ProcessingException {
  }

  private Class<? extends IDataModelAttribute>[] getConfiguredAttributes() {
    Class[] c = ConfigurationUtility.getDeclaredPublicClasses(getClass());
    return ConfigurationUtility.sortFilteredClassesByOrderAnnotation(c, IDataModelAttribute.class);
  }

  private Class<? extends IDataModelEntity>[] getConfiguredEntities() {
    Class[] c = ConfigurationUtility.getDeclaredPublicClasses(getClass());
    return ConfigurationUtility.sortFilteredClassesByOrderAnnotation(c, IDataModelEntity.class);
  }

  protected void initConfig() {
    m_visibleGranted = true;
    setText(getConfiguredText());
    setIconId(getConfiguredIconId());
    setVisible(getConfiguredVisible());
    ArrayList<IDataModelAttribute> attributes = new ArrayList<IDataModelAttribute>();
    for (Class<? extends IDataModelAttribute> c : getConfiguredAttributes()) {
      try {
        IDataModelAttribute a = ConfigurationUtility.newInnerInstance(this, c);
        attributes.add(a);
      }
      catch (Exception e) {
        LOG.warn(null, e);
      }
    }
    injectAttributesInternal(attributes);
    m_attributes = new ArrayList<IDataModelAttribute>();
    m_attributes.addAll(attributes);
    for (IDataModelAttribute a : m_attributes) {
      a.setParentEntity(this);
    }
    //lazy create entities at point when setParentEntity is set, this is necessary to avoid cyclic loops
    m_entities = new ArrayList<IDataModelEntity>();
  }

  /*
   * Runtime
   */

  public final void initEntity() throws ProcessingException {
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
  }

  public Permission getVisiblePermission() {
    return m_visiblePermission;
  }

  public void setVisiblePermission(Permission p) {
    m_visiblePermission = p;
    boolean b;
    if (p != null) {
      b = SERVICES.getService(IAccessControlService.class).checkPermission(p);
    }
    else {
      b = true;
    }
    setVisibleGranted(b);
  }

  public boolean isVisibleGranted() {
    return m_visibleGranted;
  }

  public void setVisibleGranted(boolean b) {
    m_visibleGranted = b;
    calculateVisible();
  }

  public boolean isVisible() {
    return m_visible;
  }

  public void setVisible(boolean b) {
    m_visibleProperty = b;
    calculateVisible();
  }

  /**
   * no access control for system buttons CANCEL and CLOSE
   */
  private void calculateVisible() {
    // access control
    m_visible = m_visibleGranted && m_visibleProperty;
  }

  public String getIconId() {
    return m_iconId;
  }

  public void setIconId(String s) {
    m_iconId = s;
  }

  public String getText() {
    return m_text;
  }

  public void setText(String s) {
    m_text = s;
  }

  public IDataModelAttribute[] getAttributes() {
    return m_attributes.toArray(new IDataModelAttribute[0]);
  }

  public IDataModelEntity[] getEntities() {
    return m_entities.toArray(new IDataModelEntity[0]);
  }

  public IDataModelAttribute getAttribute(Class<? extends IDataModelAttribute> attributeClazz) {
    for (IDataModelAttribute attribute : m_attributes) {
      if (attribute.getClass() == attributeClazz) {
        return attribute;
      }
    }
    return null;
  }

  public IDataModelEntity getEntity(Class<? extends IDataModelEntity> entityClazz) {
    for (IDataModelEntity entity : m_entities) {
      if (entity.getClass() == entityClazz) {
        return entity;
      }
    }
    return null;
  }

  public IDataModelEntity getParentEntity() {
    return m_parentEntity;
  }

  public void setParentEntity(IDataModelEntity parent) {
    m_parentEntity = parent;
  }

  public void initializeChildEntities(Map<Class<? extends IDataModelEntity>, IDataModelEntity> instanceMap) {
    if (!m_initializedChildEntities) {
      m_initializedChildEntities = true;
      ArrayList<IDataModelEntity> newInstances = new ArrayList<IDataModelEntity>();
      ArrayList<IDataModelEntity> entities = new ArrayList<IDataModelEntity>();
      for (Class<? extends IDataModelEntity> c : getConfiguredEntities()) {
        try {
          //check if a parent is of same type, in that case use reference
          IDataModelEntity e = instanceMap.get(c);
          if (e == null) {
            e = ConfigurationUtility.newInnerInstance(this, c);
            newInstances.add(e);
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
      for (IDataModelEntity e : newInstances) {
        e.setParentEntity(this);
      }
      for (IDataModelEntity e : newInstances) {
        e.initializeChildEntities(instanceMap);
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
   * 
   * @param entityList
   *          live and mutable list of configured attributes
   */
  protected void injectEntitiesInternal(List<IDataModelEntity> entityList) {
  }
}
