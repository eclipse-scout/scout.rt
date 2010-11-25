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
package org.eclipse.scout.rt.shared.data.form.fields.composer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.scout.commons.ConfigurationUtility;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.shared.data.form.fields.treefield.AbstractTreeFieldData;

/**
 * Data representation for a composer field
 */
public abstract class AbstractComposerData extends AbstractTreeFieldData {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractComposerData.class);
  private static final long serialVersionUID = 1L;

  private Map<String, AbstractComposerAttributeData> m_aMap;
  private Map<String, AbstractComposerEntityData> m_eMap;

  public AbstractComposerData() {
  }

  private Class<? extends AbstractComposerAttributeData>[] getConfiguredComposerAttributeDatas() {
    Class[] dca = ConfigurationUtility.getDeclaredPublicClasses(getClass());
    return ConfigurationUtility.filterClasses(dca, AbstractComposerAttributeData.class);
  }

  private Class<? extends AbstractComposerEntityData>[] getConfiguredComposerEntityDatas() {
    Class[] dca = ConfigurationUtility.getDeclaredPublicClasses(getClass());
    return ConfigurationUtility.filterClasses(dca, AbstractComposerEntityData.class);
  }

  @Override
  protected void initConfig() {
    super.initConfig();
    // add attributes
    m_aMap = new HashMap<String, AbstractComposerAttributeData>();
    ArrayList<AbstractComposerAttributeData> aList = new ArrayList<AbstractComposerAttributeData>();
    Class<? extends AbstractComposerAttributeData>[] aArray = getConfiguredComposerAttributeDatas();
    for (int i = 0; i < aArray.length; i++) {
      AbstractComposerAttributeData a;
      try {
        a = ConfigurationUtility.newInnerInstance(this, aArray[i]);
        aList.add(a);
      }// end try
      catch (Exception e) {
        LOG.warn(null, e);
      }
    }// end for
    setAttributes(aList.toArray(new AbstractComposerAttributeData[aList.size()]));
    for (AbstractComposerAttributeData a : getAttributes()) {
      a.setParentEntity(null);
    }
    // add entities
    m_eMap = new HashMap<String, AbstractComposerEntityData>();
    ArrayList<AbstractComposerEntityData> eList = new ArrayList<AbstractComposerEntityData>();
    Class<? extends AbstractComposerEntityData>[] eArray = getConfiguredComposerEntityDatas();
    for (int i = 0; i < eArray.length; i++) {
      AbstractComposerEntityData e;
      try {
        e = ConfigurationUtility.newInnerInstance(this, eArray[i]);
        eList.add(e);
      }// end try
      catch (Exception ex) {
        ex.printStackTrace();
      }
    }// end for
    setEntities(eList.toArray(new AbstractComposerEntityData[eList.size()]));
    HashMap<Class<? extends AbstractComposerEntityData>, AbstractComposerEntityData> instanceMap = new HashMap<Class<? extends AbstractComposerEntityData>, AbstractComposerEntityData>();
    for (AbstractComposerEntityData e : getEntities()) {
      e.setParentEntity(null);
      instanceMap.put(e.getClass(), e);
    }
    for (AbstractComposerEntityData e : getEntities()) {
      e.initializeChildEntities(instanceMap);
    }
  }

  @SuppressWarnings("unchecked")
  public <T extends AbstractComposerAttributeData> T getAttributeByClass(Class<T> c) {
    AbstractComposerAttributeData a = getAttributeBySimpleName(c != null ? c.getSimpleName() : null);
    if (a != null && c != null && c.isAssignableFrom(a.getClass())) {
      return (T) a;
    }
    else {
      return null;
    }
  }

  public AbstractComposerAttributeData getAttributeBySimpleName(String simpleName) {
    return m_aMap.get(simpleName);
  }

  public void setAttributes(AbstractComposerAttributeData[] array) {
    m_aMap.clear();
    if (array != null) {
      for (AbstractComposerAttributeData data : array) {
        if (data != null) {
          m_aMap.put(array.getClass().getSimpleName(), data);
        }
      }
    }
  }

  public AbstractComposerAttributeData[] getAttributes() {
    if (m_aMap == null) return new AbstractComposerAttributeData[0];
    return m_aMap.values().toArray(new AbstractComposerAttributeData[m_aMap.size()]);
  }

  @SuppressWarnings("unchecked")
  public <T extends AbstractComposerEntityData> T getEntityByClass(Class<T> c) {
    AbstractComposerEntityData a = getEntityBySimpleName(c != null ? c.getSimpleName() : null);
    if (a != null && c != null && c.isAssignableFrom(a.getClass())) {
      return (T) a;
    }
    else {
      return null;
    }
  }

  public AbstractComposerEntityData getEntityBySimpleName(String simpleName) {
    return m_eMap.get(simpleName);
  }

  public void setEntities(AbstractComposerEntityData[] array) {
    m_eMap.clear();
    if (array != null) {
      for (AbstractComposerEntityData data : array) {
        if (data != null) {
          m_eMap.put(data.getClass().getSimpleName(), data);
        }
      }
    }
  }

  public AbstractComposerEntityData[] getEntities() {
    if (m_eMap == null) return new AbstractComposerEntityData[0];
    return m_eMap.values().toArray(new AbstractComposerEntityData[m_eMap.size()]);
  }

}
