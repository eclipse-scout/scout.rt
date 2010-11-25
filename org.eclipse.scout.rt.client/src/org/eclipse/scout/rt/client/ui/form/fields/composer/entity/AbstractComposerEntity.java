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
package org.eclipse.scout.rt.client.ui.form.fields.composer.entity;

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
import org.eclipse.scout.rt.client.ui.form.fields.composer.attribute.IComposerAttribute;
import org.eclipse.scout.rt.shared.services.common.security.IAccessControlService;
import org.eclipse.scout.service.SERVICES;

@SuppressWarnings("deprecation")
public abstract class AbstractComposerEntity extends AbstractPropertyObserver implements IComposerEntity {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractComposerEntity.class);

  private String m_id;
  private Permission m_visiblePermission;
  private boolean m_visibleGranted;
  private boolean m_visibleProperty;
  private boolean m_visible;
  private String m_text;
  private String m_iconId;
  private ArrayList<IComposerAttribute> m_attributes;
  private ArrayList<IComposerEntity> m_entities;
  private IComposerEntity m_parentEntity;
  private boolean m_initializedChildEntities;

  public AbstractComposerEntity() {
    m_attributes = new ArrayList<IComposerAttribute>();
    m_entities = new ArrayList<IComposerEntity>();
    initConfig();
  }

  /*
   * Configuration
   */

  /**
   * @deprecated the id must always by the class simple name
   */
  @Deprecated
  @ConfigProperty(ConfigProperty.STRING)
  @Order(30)
  @ConfigPropertyValue("null")
  protected String getConfiguredId() {
    return null;
  }

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

  private Class<? extends IComposerAttribute>[] getConfiguredComposerAttributes() {
    Class[] c = ConfigurationUtility.getDeclaredPublicClasses(getClass());
    return ConfigurationUtility.sortFilteredClassesByOrderAnnotation(c, IComposerAttribute.class);
  }

  private Class<? extends IComposerEntity>[] getConfiguredComposerEntities() {
    Class[] c = ConfigurationUtility.getDeclaredPublicClasses(getClass());
    return ConfigurationUtility.sortFilteredClassesByOrderAnnotation(c, IComposerEntity.class);
  }

  protected void initConfig() {
    m_visibleGranted = true;
    setId(getConfiguredId());
    setText(getConfiguredText());
    setIconId(getConfiguredIconId());
    setVisible(getConfiguredVisible());
    ArrayList<IComposerAttribute> attributes = new ArrayList<IComposerAttribute>();
    for (Class<? extends IComposerAttribute> c : getConfiguredComposerAttributes()) {
      try {
        IComposerAttribute a = ConfigurationUtility.newInnerInstance(this, c);
        attributes.add(a);
      }
      catch (Exception e) {
        LOG.warn(null, e);
      }
    }
    injectAttributesInternal(attributes);
    m_attributes = new ArrayList<IComposerAttribute>();
    m_attributes.addAll(attributes);
    for (IComposerAttribute a : m_attributes) {
      a.setParentEntity(this);
    }
    //lazy create entities at point when setParentEntity is set, this is necessary to avoid cyclic loops
    m_entities = new ArrayList<IComposerEntity>();
  }

  /*
   * Runtime
   */

  public final void initEntity() throws ProcessingException {
    execInitEntity();
    for (IComposerAttribute a : getComposerAttributes()) {
      a.initAttribute();
    }
  }

  public String getId() {
    if (m_id != null) return m_id;
    else return getClass().getSimpleName();
  }

  public void setId(String s) {
    m_id = s;
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

  public IComposerAttribute[] getComposerAttributes() {
    return m_attributes.toArray(new IComposerAttribute[0]);
  }

  public IComposerEntity[] getComposerEntities() {
    return m_entities.toArray(new IComposerEntity[0]);
  }

  public IComposerEntity getParentEntity() {
    return m_parentEntity;
  }

  public void setParentEntity(IComposerEntity parent) {
    m_parentEntity = parent;
  }

  public void initializeChildEntities(Map<Class<? extends IComposerEntity>, IComposerEntity> instanceMap) {
    if (!m_initializedChildEntities) {
      m_initializedChildEntities = true;
      ArrayList<IComposerEntity> newInstances = new ArrayList<IComposerEntity>();
      ArrayList<IComposerEntity> entities = new ArrayList<IComposerEntity>();
      for (Class<? extends IComposerEntity> c : getConfiguredComposerEntities()) {
        try {
          //check if a parent is of same type, in that case use reference
          IComposerEntity e = instanceMap.get(c);
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
      for (IComposerEntity e : newInstances) {
        e.setParentEntity(this);
      }
      for (IComposerEntity e : newInstances) {
        e.initializeChildEntities(instanceMap);
      }
    }
  }

  /**
   * @deprecated processing logic belongs to server
   */
  @Deprecated
  protected String getConfiguredStatement() {
    return null;
  }

  /**
   * @deprecated processing logic belongs to server
   */
  @Deprecated
  public String getLegacyStatement() {
    return getConfiguredStatement();
  }

  /**
   * do not use this internal method<br>
   * Used add/remove attributes
   * 
   * @param attributeList
   *          live and mutable list of configured attributes
   */
  protected void injectAttributesInternal(List<IComposerAttribute> attributeList) {
  }

  /**
   * do not use this internal method<br>
   * Used add/remove entities
   * 
   * @param entityList
   *          live and mutable list of configured attributes
   */
  protected void injectEntitiesInternal(List<IComposerEntity> entityList) {
  }
}
